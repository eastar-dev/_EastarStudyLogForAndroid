package android.util

import kotlin.math.min
import kotlin.math.sign

class Version(private val versionText: String) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        val thisVer = versionText.split('.')
        val anotherVer = other.versionText.split('.')

        for (i in 0 until min(thisVer.size, anotherVer.size)) {
            val l = thisVer[i]
            val r = anotherVer[i]
            val lNumber = "^(\\d+|)(.*)".toRegex().matchEntire(l)?.groupValues?.get(1)?.takeUnless { it.isEmpty() }?.toInt()
            val rNumber = "^(\\d+|)(.*)".toRegex().matchEntire(r)?.groupValues?.get(1)?.takeUnless { it.isEmpty() }?.toInt()
            val lString = "^(\\d+|)(.*)".toRegex().matchEntire(l)?.groupValues?.get(2)
            val rString = "^(\\d+|)(.*)".toRegex().matchEntire(r)?.groupValues?.get(2)

            val result = when {
                lNumber != null && rNumber != null && lNumber != rNumber -> (lNumber - rNumber).sign
                lNumber != null && rNumber == null -> +1
                lNumber == null && rNumber != null -> -1
                else -> when {
                    lString == null && rString == null -> 0
                    lString != null && rString != null -> lString.compareTo(rString)
                    lString == null && rString != null -> +1
                    lString != null && rString == null -> -1
                    else -> 0
                }
            }
            if (result != 0)
                return result
        }
        return (thisVer.size - anotherVer.size).sign
    }

    override fun equals(other: Any?): Boolean {
        if (other is Version)
            return compareTo(other) == 0
        return false
    }

    override fun hashCode(): Int {
        return versionText.hashCode()
    }
}

val String.version get() = Version(this)