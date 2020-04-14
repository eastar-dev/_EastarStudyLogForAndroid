@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package android.util

import android.annotation.SuppressLint
import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DT {
    const val DAY1 = 86400000L
    const val HOUR1 = 3600000L
    const val MINUTE1 = 60000L
    const val DAY30 = 86400000L * 30L
    //달력표시용 날짜계산
    const val MAX_DISPLAY_DAY = 42

    @JvmStatic
    fun stripTime(milliseconds: Long): Long {
        //        return ((milliseconds + TimeZone.getDefault().getRawOffset()) / DAY1 * DAY1) - TimeZone.getDefault().getRawOffset();
        val c = Calendar.getInstance()
        c.timeInMillis = milliseconds
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun stripDay(milliseconds: Long): Long {
        val millisecondsStrip = stripTime(milliseconds)
        val c = Calendar.getInstance()
        c.timeInMillis = millisecondsStrip
        c.set(Calendar.DATE, c.getActualMinimum(Calendar.MONTH))
        return c.timeInMillis
    }

    fun move(milliseconds: Long, field: Int, distance: Int): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = milliseconds
        c.add(field, distance)
        return c.timeInMillis
    }

    /** elapsed 시간을 일용한 주기(period) + seed 시간 */
    fun next(period: Long, seed: Long): Long {
        val current = System.currentTimeMillis()
        val elapsed = SystemClock.elapsedRealtime()
        val offset = TimeZone.getDefault().rawOffset.toLong()
        return elapsed + (period - (current + offset) % period + seed) % period
    }

    fun optParse(date: String?, from: SimpleDateFormat): Long = kotlin.runCatching { date?.let { from.parse(it) }?.time ?: 0L }.getOrDefault(0L)

    @JvmStatic
    fun format(milliseconds: Long, to: SimpleDateFormat): String = if (milliseconds == 0L) "" else to.format(Date(milliseconds))

    @JvmStatic
    fun optFormat(date: String?, from: SimpleDateFormat, to: SimpleDateFormat): String = format(optParse(date, from), to)

    @JvmStatic
    fun optFormat(date: String?, from: SDF, to: SDF): String = optFormat(date, from.sdf, to.sdf)

    fun getStartEndDaysForCalendar(millis: Long, start_end: LongArray) {
        val dayCurrent = stripTime(millis)
        val cal = Calendar.getInstance()
        cal.timeInMillis = dayCurrent
        cal.set(Calendar.DATE, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekGap = Calendar.SUNDAY - dayOfWeek
        cal.add(Calendar.DATE, dayOfWeekGap)
        val dayStart = cal.timeInMillis
        cal.add(Calendar.DATE, MAX_DISPLAY_DAY)
        val dayEnd = cal.timeInMillis

        start_end[0] = dayStart
        start_end[1] = dayEnd
    }

    fun getDayOfWeek(milliseconds: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = milliseconds
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * <pre>
     * public static final long DAY1 = 86400000L;
     * public static long stripTime(long milliseconds) {
     * return ((milliseconds + TimeZone.getDefault().getRawOffset()) / DAY1 * DAY1) - TimeZone.getDefault().getRawOffset();
     * }
    </pre> *
     */
    @JvmStatic
    fun distance(milliseconds_first: Long, milliseconds_end: Long, field: Int): Int {
        val sd = stripTime(milliseconds_first)
        val ed = stripTime(milliseconds_end)

        require(ed >= sd) { "must milliseconds_first < milliseconds_end $sd,$ed" }

        require(field == Calendar.YEAR || field == Calendar.MONTH || field == Calendar.DATE || field == Calendar.DAY_OF_WEEK) { "filed must in Calendar.YEAR, Calendar.MONTH or Calendar.DATE" }

        val firstDayOfWeek = Calendar.getInstance().firstDayOfWeek
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        start.timeInMillis = sd
        end.timeInMillis = ed
        val sy = start.get(Calendar.YEAR)
        val ey = end.get(Calendar.YEAR)
        val sm = start.get(Calendar.MONTH)
        val em = end.get(Calendar.MONTH)

        start.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        val sw = start.timeInMillis
        end.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        val ew = end.timeInMillis

        return when (field) {
            Calendar.YEAR -> ey - sy
            Calendar.MONTH -> (ey - sy) * 12 + (em - sm)
            Calendar.DAY_OF_WEEK -> ((ew - sw) / (DAY1 * Calendar.DAY_OF_WEEK)).toInt()
            Calendar.DATE -> ((ed - sd) / DAY1).toInt()
            else -> -1
        }
    }
}
