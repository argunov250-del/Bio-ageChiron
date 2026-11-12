package com.bioage.alpha

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipInputStream
import kotlin.math.*

/** Imported raw measurements are private to the app, not uploaded anywhere. */
class ChironStore(context: Context): SQLiteOpenHelper(context, "chiron_health.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE samples (id TEXT PRIMARY KEY, day TEXT NOT NULL, type TEXT NOT NULL, source TEXT NOT NULL, start_ms INTEGER NOT NULL, end_ms INTEGER NOT NULL, value REAL NOT NULL)")
        db.execSQL("CREATE INDEX idx_samples_day_type ON samples(day,type)")
    }
    override fun onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) {}
    fun daily(day:String, type:String):List<Sample> {
        val out=mutableListOf<Sample>()
        readableDatabase.rawQuery("SELECT source,start_ms,end_ms,value FROM samples WHERE day=? AND type=?",arrayOf(day,type)).use { c ->
            while(c.moveToNext()) out.add(Sample(c.getString(0),c.getLong(1),c.getLong(2),c.getDouble(3)))
        }
        return out
    }
    fun latestDay():String? = readableDatabase.rawQuery("SELECT MAX(day) FROM samples",null).use { if(it.moveToFirst()) it.getString(0) else null }
    fun totalRows():Long = readableDatabase.rawQuery("SELECT COUNT(*) FROM samples",null).use { it.moveToFirst();it.getLong(0) }
}
data class Sample(val source:String,val start:Long,val end:Long,val value:Double)
data class ImportResult(val parsed:Long,val inserted:Long,val ignored:Long)

/** ZIP is streamed, XML is pull-parsed; never inflate the 1.49 GB export into RAM. */
object AppleHealthImporter {
    private val types=mapOf(
        "HKQuantityTypeIdentifierHeartRate" to "heart_rate",
        "HKQuantityTypeIdentifierRestingHeartRate" to "resting_hr",
        "HKQuantityTypeIdentifierHeartRateVariabilitySDNN" to "hrv_sdnn",
        "HKQuantityTypeIdentifierStepCount" to "steps",
        "HKQuantityTypeIdentifierVO2Max" to "vo2max",
        "HKQuantityTypeIdentifierOxygenSaturation" to "spo2",
        "HKQuantityTypeIdentifierRespiratoryRate" to "respiration",
        "HKQuantityTypeIdentifierActiveEnergyBurned" to "active_kcal",
        "HKCategoryTypeIdentifierSleepAnalysis" to "sleep"
    )
    private fun timestamp(s:String):Long? = runCatching {
        val f=SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z",Locale.US)
        f.isLenient=false
        f.parse(s)?.time
    }.getOrNull()
    private fun hash(s:String):String = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).take(16).joinToString("") { "%02x".format(it) }
    fun import(context:Context, uri:Uri, onProgress:(Long)->Unit = {}):ImportResult {
        val store=ChironStore(context)
        val db=store.writableDatabase
        var parsed=0L; var inserted=0L; var ignored=0L
        var found=false
        val input=context.contentResolver.openInputStream(uri) ?: error("Cannot open archive")
        input.use { raw -> ZipInputStream(BufferedInputStream(raw, 128*1024)).use { zip ->
            while(true) {
                val entry=zip.nextEntry ?: break
                if(entry.name == "apple_health_export/export.xml" || entry.name == "export.xml") {
                    if(found) error("Duplicate export.xml")
                    found=true
                    val parser=Xml.newPullParser()
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
                    parser.setInput(zip,"UTF-8")
                    val insert=db.compileStatement("INSERT OR IGNORE INTO samples(id,day,type,source,start_ms,end_ms,value) VALUES(?,?,?,?,?,?,?)")
                    db.beginTransaction()
                    try {
                        while(parser.eventType!=XmlPullParser.END_DOCUMENT) {
                            if(parser.eventType==XmlPullParser.START_TAG && parser.name=="Record") {
                                parsed++
                                val type=types[parser.getAttributeValue(null,"type")]
                                if(type!=null) {
                                    val start=parser.getAttributeValue(null,"startDate") ?: ""
                                    val end=parser.getAttributeValue(null,"endDate") ?: ""
                                    val source=parser.getAttributeValue(null,"sourceName")?.take(100) ?: "unknown"
                                    val original=parser.getAttributeValue(null,"value") ?: ""
                                    val unit=parser.getAttributeValue(null,"unit") ?: ""
                                    val a=timestamp(start); val b=timestamp(end)
                                    var value=original.toDoubleOrNull()
                                    if(type=="sleep") {
                                        value=if(original.contains("Asleep",true)) 1.0 else null
                                    }
                                    if(type=="spo2" && unit=="%") value=value?.times(100.0)
                                    if(a!=null && b!=null && b>=a && value!=null && value.isFinite() && value>=0 && value<1e9) {
                                        val day=start.take(10)
                                        val id=hash("$type|$source|$start|$end|$original|$unit")
                                        insert.clearBindings()
                                        insert.bindString(1,id);insert.bindString(2,day);insert.bindString(3,type)
                                        insert.bindString(4,source);insert.bindLong(5,a);insert.bindLong(6,b)
                                        insert.bindDouble(7,value)
                                        if(insert.executeInsert()!=-1L) inserted++ else ignored++
                                    } else ignored++
                                }
                                if(parsed%10000L==0L) onProgress(parsed)
                                if(parsed%50000L==0L) { db.setTransactionSuccessful();db.endTransaction();db.beginTransaction() }
                            }
                            parser.next()
                        }
                        db.setTransactionSuccessful()
                    } finally { db.endTransaction(); insert.close() }
                }
                zip.closeEntry()
            }
        } }
        store.close()
        check(found) { "Apple Health export.xml not found in ZIP" }
        return ImportResult(parsed,inserted,ignored)
    }
}

/** Interpretable estimates, not WHOOP proprietary metrics or clinical diagnostics. */
object ChironMetrics {
    private fun median(xs:List<Double>):Double? { if(xs.isEmpty())return null;val s=xs.sorted();return (s[(s.size-1)/2]+s[s.size/2])/2 }
    data class Recovery(val score:Int?,val details:String)
    fun recovery(hrvToday:Double?, restingToday:Double?, sleepHours:Double?,
                 hrvBaseline:List<Double>,restingBaseline:List<Double>):Recovery {
        val h=median(hrvBaseline.filter{it>0});val r=median(restingBaseline.filter{it>0})
        if(hrvToday==null || restingToday==null || sleepHours==null || h==null || r==null || hrvBaseline.size<7 || restingBaseline.size<7)
            return Recovery(null,"Недостаточно свежих данных HRV, пульса покоя, сна или 7 дней базовой линии")
        val hrvFactor=(hrvToday/h).coerceIn(.5,1.5)
        val hrFactor=(r/restingToday.coerceAtLeast(1.0)).coerceIn(.7,1.3)
        val sleepFactor=(sleepHours/8.0).coerceIn(0.0,1.15)
        val score=((.45*hrvFactor+.30*hrFactor+.25*sleepFactor)/1.15*100).roundToInt().coerceIn(0,100)
        return Recovery(score,"Экспериментальный индекс: HRV 45%, пульс покоя 30%, сон 25%; не WHOOP Recovery")
    }
    /** TRIMP-like load from measured HR intervals, if a personal max/rest HR is supplied. */
    fun strain(heartRateSamples:List<Sample>,restingHr:Double?,maxHr:Double?):Double? {
        if(restingHr==null || maxHr==null || maxHr<=restingHr+20 || heartRateSamples.size<3)return null
        val ordered=heartRateSamples.sortedBy{it.start}
        var load=0.0
        for(i in 0 until ordered.size-1) {
            val minutes=((ordered[i+1].start-ordered[i].start)/60000.0).coerceIn(0.0,5.0)
            val intensity=((ordered[i].value-restingHr)/(maxHr-restingHr)).coerceIn(0.0,1.0)
            load+=minutes*intensity*exp(1.92*intensity)
        }
        return load.takeIf{it>0}
    }
}
