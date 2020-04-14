package android.util

import android.icu.util.ChineseCalendar
import android.os.Build
import java.util.*

object LunHelper {

    // 음력 데이터
    // 평달   작은달              : 1
    // 평달   큰달                : 2
    // 평달이 작고 윤달도 작으면  : 3
    // 평달이 작고 윤달이 크면    : 4
    // 평달이 크고 윤달이 작으면  : 5
    // 평달과 윤달이 모두 크면    : 6
    private val mLunarTable = arrayOf(intArrayOf(2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 5, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1),
            /* 1901 */ intArrayOf(2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1), intArrayOf(2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 2, 4, 1, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2), intArrayOf(1, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 5, 1, 2, 2, 1, 2, 2),
            /* 1911 */ intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2), intArrayOf(2, 2, 1, 2, 5, 1, 2, 1, 2, 1, 1, 2), intArrayOf(2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1), intArrayOf(2, 3, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 5, 2, 2, 1, 2, 2), intArrayOf(1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2),
            /* 1921 */ intArrayOf(2, 1, 2, 2, 3, 2, 1, 1, 2, 1, 2, 2), intArrayOf(1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2), intArrayOf(2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1), intArrayOf(2, 1, 2, 5, 2, 1, 2, 2, 1, 2, 1, 2), intArrayOf(1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(1, 5, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 2, 1, 1, 5, 1, 2, 1, 2, 2, 1), intArrayOf(2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1),
            /* 1931 */ intArrayOf(2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 6, 1, 2, 1, 2, 1, 1, 2), intArrayOf(1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2), intArrayOf(1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 4, 1, 2, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 2, 1, 1, 2, 1, 4, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1), intArrayOf(2, 2, 1, 2, 2, 4, 1, 1, 2, 1, 2, 1),
            /* 1941 */ intArrayOf(2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2), intArrayOf(1, 1, 2, 4, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2), intArrayOf(2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2), intArrayOf(2, 5, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 2, 3, 2, 1, 2, 1, 2), intArrayOf(2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1), intArrayOf(2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2),
            /* 1951 */ intArrayOf(1, 2, 1, 2, 4, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2), intArrayOf(1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(2, 1, 4, 1, 1, 2, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 2, 1, 1, 5, 2, 1, 2, 2), intArrayOf(1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2),
            /* 1961 */ intArrayOf(1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2), intArrayOf(1, 2, 5, 2, 1, 1, 2, 1, 1, 2, 2, 1), intArrayOf(2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 2, 1, 5, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1, 2),
            /* 1971 */ intArrayOf(1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 1), intArrayOf(2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 2, 1, 5, 2, 1, 1, 2), intArrayOf(2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1), intArrayOf(2, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 1, 6, 1, 2, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2),
            /* 1981 */ intArrayOf(2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2), intArrayOf(2, 1, 2, 2, 1, 1, 2, 1, 1, 5, 2, 2), intArrayOf(1, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1), intArrayOf(2, 1, 2, 2, 1, 5, 2, 2, 1, 2, 1, 2), intArrayOf(1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(1, 2, 1, 1, 5, 1, 2, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2),
            /* 1991 */ intArrayOf(1, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2), intArrayOf(1, 2, 5, 2, 1, 2, 1, 1, 2, 1, 2, 1), intArrayOf(2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 2, 2, 1, 5, 2, 1, 1, 2), intArrayOf(1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2), intArrayOf(1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 1, 2, 3, 2, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1), intArrayOf(2, 2, 2, 3, 2, 1, 1, 2, 1, 2, 1, 2),
            /* 2001 */ intArrayOf(2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1), intArrayOf(2, 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2), intArrayOf(1, 5, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 1), intArrayOf(2, 1, 2, 1, 2, 1, 5, 2, 2, 1, 2, 2), intArrayOf(1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2), intArrayOf(2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 1, 5, 1, 2, 1, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1),
            /* 2011 */ intArrayOf(2, 1, 6, 2, 1, 2, 1, 1, 2, 1, 2, 1), intArrayOf(2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 2, 1, 2, 1, 2, 5, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 1), intArrayOf(2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2), intArrayOf(2, 1, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(2, 1, 2, 5, 2, 1, 1, 2, 1, 2, 1, 2), intArrayOf(1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1),
            /* 2021 */ intArrayOf(2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2), intArrayOf(1, 5, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2), intArrayOf(1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2), intArrayOf(1, 2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1), intArrayOf(2, 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2), intArrayOf(1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 5, 2, 1, 2, 2, 1, 2, 1, 2, 1),
            /* 2031 */ intArrayOf(2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 5, 2), intArrayOf(1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1), intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 4, 1, 1, 2, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2), intArrayOf(2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1), intArrayOf(2, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1, 1), intArrayOf(2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1), intArrayOf(2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2),
            /* 2041 */ intArrayOf(1, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2), intArrayOf(1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2), intArrayOf(2, 1, 2, 1, 1, 2, 3, 2, 1, 2, 2, 2), intArrayOf(2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2))//@formatter:on

    //leapmonth : 0 - 평달 1 - 윤달
    data class LunDate(var year: Int, var month: Int, var day: Int, var leapMonth: Int) {
        override fun toString() = "음 %d.%d%s".format(month, day, if (leapMonth == 1) "(윤)" else "")
    }

    fun sol2lun(milliseconds: Long): LunDate {
        val c = Calendar.getInstance()
        c.timeInMillis = milliseconds
        return sol2lun(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE))
    }

    /*
     * 양력 <. 음력 변환 함수 type : 1 - 양력 . 음력 2 - 음력 . 양력 leapmonth : 0 - 평달 1 - 윤달
     * (type = 2 일때만 유효)
     */
    fun sol2lun(year: Int, month: Int, day: Int): LunDate {
        var solYear: Int
        var solMonth: Int
        var solDay: Int
        var lunYear: Int
        var lunMonth: Int
        var lunDay: Int
        var lunLeapMonth: Int
        var lunMonthDay: Int
        var lunIndex: Int

        val solMonthDay = intArrayOf(31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        /* range check */
        if (year < 1900 || year > 2040) {
            return LunDate(0, 0, 0, 0)
        }

        /* 속도 개선을 위해 기준 일자를 여러개로 한다 */
        if (year >= 2000) {
            /* 기준일자 양력 2000년 1월 1일 (음력 1999년 11월 25일) */
            solYear = 2000
            solMonth = 1
            solDay = 1
            lunYear = 1999
            lunMonth = 11
            lunDay = 25
            lunLeapMonth = 0

            solMonthDay[1] = 29 /* 2000 년 2월 28일 */
            lunMonthDay = 30 /* 1999년 11월 */
        } else if (year >= 1970) {
            /* 기준일자 양력 1970년 1월 1일 (음력 1969년 11월 24일) */
            solYear = 1970
            solMonth = 1
            solDay = 1
            lunYear = 1969
            lunMonth = 11
            lunDay = 24
            lunLeapMonth = 0

            solMonthDay[1] = 28 /* 1970 년 2월 28일 */
            lunMonthDay = 30 /* 1969년 11월 */
        } else if (year >= 1940) {
            /* 기준일자 양력 1940년 1월 1일 (음력 1939년 11월 22일) */
            solYear = 1940
            solMonth = 1
            solDay = 1
            lunYear = 1939
            lunMonth = 11
            lunDay = 22
            lunLeapMonth = 0

            solMonthDay[1] = 29 /* 1940 년 2월 28일 */
            lunMonthDay = 29 /* 1939년 11월 */
        } else {
            /* 기준일자 양력 1900년 1월 1일 (음력 1899년 12월 1일) */
            solYear = 1900
            solMonth = 1
            solDay = 1
            lunYear = 1899
            lunMonth = 12
            lunDay = 1
            lunLeapMonth = 0

            solMonthDay[1] = 28 /* 1900 년 2월 28일 */
            lunMonthDay = 30 /* 1899년 12월 */
        }

        lunIndex = lunYear - 1899

        while (true) {
            if (year == solYear && month == solMonth && day == solDay) {
                return LunDate(lunYear, lunMonth, lunDay, lunLeapMonth)
            }

            /* add a day of solar calendar */
            if (solMonth == 12 && solDay == 31) {
                solYear++
                solMonth = 1
                solDay = 1

                /* set monthDay of Feb */
                if (solYear % 400 == 0)
                    solMonthDay[1] = 29
                else if (solYear % 100 == 0)
                    solMonthDay[1] = 28
                else if (solYear % 4 == 0)
                    solMonthDay[1] = 29
                else
                    solMonthDay[1] = 28

            } else if (solMonthDay[solMonth - 1] == solDay) {
                solMonth++
                solDay = 1
            } else
                solDay++

            /* add a day of lunar calendar */
            if (lunMonth == 12 && (mLunarTable[lunIndex][lunMonth - 1] == 1 && lunDay == 29 || mLunarTable[lunIndex][lunMonth - 1] == 2 && lunDay == 30)) {
                lunYear++
                lunMonth = 1
                lunDay = 1

                if (lunYear > 2043) {
                    return LunDate(0, 0, 0, 0)
                }

                lunIndex = lunYear - 1899

                if (mLunarTable[lunIndex][lunMonth - 1] == 1)
                    lunMonthDay = 29
                else if (mLunarTable[lunIndex][lunMonth - 1] == 2)
                    lunMonthDay = 30
            } else if (lunDay == lunMonthDay) {
                if (mLunarTable[lunIndex][lunMonth - 1] >= 3 && lunLeapMonth == 0) {
                    lunDay = 1
                    lunLeapMonth = 1
                } else {
                    lunMonth++
                    lunDay = 1
                    lunLeapMonth = 0
                }

                if (mLunarTable[lunIndex][lunMonth - 1] == 1)
                    lunMonthDay = 29
                else if (mLunarTable[lunIndex][lunMonth - 1] == 2)
                    lunMonthDay = 30
                else if (mLunarTable[lunIndex][lunMonth - 1] == 3)
                    lunMonthDay = 29
                else if (mLunarTable[lunIndex][lunMonth - 1] == 4 && lunLeapMonth == 0)
                    lunMonthDay = 29
                else if (mLunarTable[lunIndex][lunMonth - 1] == 4 && lunLeapMonth == 1)
                    lunMonthDay = 30
                else if (mLunarTable[lunIndex][lunMonth - 1] == 5 && lunLeapMonth == 0)
                    lunMonthDay = 30
                else if (mLunarTable[lunIndex][lunMonth - 1] == 5 && lunLeapMonth == 1)
                    lunMonthDay = 29
                else if (mLunarTable[lunIndex][lunMonth - 1] == 6)
                    lunMonthDay = 30
            } else {
                lunDay++
            }
        }
    }
}

//(0 : 평달, 1 : 윤달)
fun LunHelper.LunDate.toSolar(): Long {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        return -1

    val cc = ChineseCalendar()
    cc.set(ChineseCalendar.EXTENDED_YEAR, year + 2637)
    cc.set(ChineseCalendar.MONTH, month)
    cc.set(ChineseCalendar.DAY_OF_MONTH, day)
    cc.set(ChineseCalendar.IS_LEAP_MONTH, leapMonth);
    return cc.timeInMillis
}

fun Long.toLunar(): LunHelper.LunDate {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        return LunHelper.LunDate(0, 0, 0, 0);

    val cc = ChineseCalendar()
    cc.timeInMillis = this
    return LunHelper.LunDate(cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637,
            cc.get(ChineseCalendar.MONTH),
            cc.get(ChineseCalendar.DAY_OF_MONTH),
            cc.get(ChineseCalendar.IS_LEAP_MONTH)
    )
}