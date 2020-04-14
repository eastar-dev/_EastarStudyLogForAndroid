@file:Suppress("ReplaceWithOperatorAssignment", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")

package android.util

object HangulEx {

    //https://www.unicode.org/charts/PDF/UAC00.pdf //글자
    //https://www.unicode.org/charts/PDF/U3130.pdf //초성
    //https://www.unicode.org/charts/PDF/U1100.pdf //자모
    //https://www.unicode.org/charts/PDF/UA960.pdf //고어초성
    //https://www.unicode.org/charts/PDF/UD7B0.pdf //고어

    //J 자음
    //M 모음
    //JH 초성
    //M 중성
    //JT 종성

    //자음
    private const val r: Char = 0x3131.toChar()
    private const val s: Char = 0x3134.toChar()
    private const val e: Char = 0x3137.toChar()
    private const val f: Char = 0x3139.toChar()
    private const val a: Char = 0x3141.toChar()
    private const val q: Char = 0x3142.toChar()
    private const val t: Char = 0x3145.toChar()
    private const val d: Char = 0x3147.toChar()
    private const val w: Char = 0x3148.toChar()
    private const val c: Char = 0x314a.toChar()
    private const val z: Char = 0x314b.toChar()
    private const val x: Char = 0x314c.toChar()
    private const val v: Char = 0x314d.toChar()
    private const val g: Char = 0x314e.toChar()
    //쌍자음초성
    private const val R: Char = 0x3132.toChar()
    private const val E: Char = 0x3138.toChar()
    private const val Q: Char = 0x3143.toChar()
    private const val T: Char = 0x3146.toChar()
    private const val W: Char = 0x3149.toChar()
    //모음
    //  final static char o = 0x3a8d;
    private const val o: Char = 0x119e.toChar()
    private const val oo: Char = 0x11a2.toChar()
    private const val m: Char = 0x3161.toChar()
    private const val l: Char = 0x3163.toChar()

    //초성 'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    private val JH = listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    private val JH_E = listOf("r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T", "d", "w", "W", "c", "z", "x", "v", "g")
    private val JH_FS = listOf(listOf(r), listOf(r, r), listOf(s), listOf(e), listOf(e, e), listOf(f), listOf(a), listOf(q), listOf(q, q), listOf(t), listOf(t, t), listOf(d), listOf(w), listOf(w, w), listOf(c), listOf(z), listOf(x), listOf(v), listOf(g))

    //중성 ㅏ,ㅐ,ㅑ,ㅒ,ㅓ,ㅔ,ㅕ,ㅖ,ㅗ,ㅘ,ㅙ,ㅚ,ㅛ,ㅜ,ㅝ,ㅞ,ㅟ,ㅠ,ㅡ,ㅢ,ㅣ
    private const val M_LEN = 21
    private val M = listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    private val M_E = listOf("k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl", "y", "n", "nj", "np", "nl", "b", "m", "ml", "l")
    private val M_FS = listOf(listOf(l, o), listOf(l, o, l), listOf(l, o, o), listOf(l, o, o, l), listOf(o, l), listOf(o, l, l), listOf(o, o, l), listOf(o, o, l, l), listOf(o, m), listOf(o, m, l, o), listOf(o, m, l, o, l), listOf(o, m, l), listOf(o, o, m), listOf(m, o), listOf(m, o, o, l), listOf(m, o, o, l, l), listOf(m, o, l), listOf(m, o, o), listOf(m), listOf(m, l), listOf(l))

    //종성 0,ㄱ,ㄲ,ㄳ,ㄴ,ㄵ,ㄶ,ㄷ,ㄹ,ㄺ,ㄻ,ㄼ,ㄽ,ㄾ,ㄿ,ㅀ,ㅁ,ㅂ,ㅄ,ㅅ,ㅆ,ㅇ,ㅈ,ㅊ,ㅋ,ㅌ,ㅍ,ㅎ
    private const val JT_LEN = 28
    private val JT = listOf(0.toChar(), 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    private val JT_E = listOf("", "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g")
    private val JT_FS = listOf(listOf(), listOf(r), listOf(r, r), listOf(r, t), listOf(s), listOf(s, w), listOf(s, g), listOf(e), listOf(f), listOf(f, r), listOf(f, a), listOf(f, q), listOf(f, t), listOf(f, x), listOf(f, v), listOf(f, g), listOf(a), listOf(q), listOf(q, t), listOf(t), listOf(t, t), listOf(d), listOf(w), listOf(c), listOf(z), listOf(x), listOf(v), listOf(g))

    //자소 'ㄱ'(0x3131) ~ 'ㅣ'(0x3163)
    private val JM_FS = listOf(listOf(r), listOf(r, r), listOf(r, t), listOf(s), listOf(s, w), listOf(s, g), listOf(e), listOf(e, e), listOf(f), listOf(f, r), listOf(f, a), listOf(f, q), listOf(f, t), listOf(f, x), listOf(f, v), listOf(f, g), listOf(a), listOf(q), listOf(q, q), listOf(q, t), listOf(t), listOf(t, t), listOf(d), listOf(w), listOf(w, w), listOf(c), listOf(z), listOf(x), listOf(v), listOf(g), listOf(l, o), listOf(l, o, l), listOf(l, o, o), listOf(l, o, o, l), listOf(o, l), listOf(o, l, l), listOf(o, o, l), listOf(o, o, l, l), listOf(o, m), listOf(o, m, l, o), listOf(o, m, l, o, l), listOf(o, m, l), listOf(o, o, m), listOf(m, o), listOf(m, o, o, l), listOf(m, o, o, l, l), listOf(m, o, l), listOf(m, o, o), listOf(m), listOf(m, l), listOf(l))

    //초성가능한 자소 'ㄱ'(0x3131) ~ 'ㅎ'(0x314E) 'ㅏ'~'ㅣ'
    private val JM_FJ = listOf(r, R, r, s, s, s, e, E, f, f, f, f, f, f, f, f, a, q, Q, q, t, T, d, w, W, c, z, x, v, g)

    /**
     * @return 초성만으로분리 아래아 아래아2개짜리는 없어짐
     * ㄺ -> ㄺ
     */
    fun hangul2JH(s: String?): String {
        if (s.isNullOrEmpty())
            return ""

        return s.map {
            when (it) {
                in '가'..'힣' -> {
                    val index = (it - '가') / (M_LEN * JT_LEN)
                    JH[index]
                }
                o, oo -> 0.toChar()
                else -> it
            }
        }.joinToString("")
    }

    /**
     * @return 첫번째 글자(한글인경우 초성만)
     */
    fun hangul2FJH(s: String?): Char {
        if (s.isNullOrEmpty())
            return '#'

        val ch = s[0]
        return if (ch in '가'..'힣') {
            JH[(ch - '가') / (M_LEN * JT_LEN)]
        } else {
            ch
        }
    }

    /**
     * @return 첫번째 글자(한글인경우 초성만 나머지는 '#')
     */
    fun hangul2FJH2(s: String?): Char {
        if (s.isNullOrEmpty())
            return '#'

        val ch = s[0]
        return if (ch in '가'..'힣') {
            JH[(ch - '가') / (M_LEN * JT_LEN)]
        } else {
            '#'
        }
    }

    @JvmStatic
    fun hangul2FJH3(s: String?): Char {
        if (s.isNullOrEmpty())
            return '#'

        return when (val ch = s[0]) {
            in '가'..'힣' -> JH[(ch - '가') / (M_LEN * JT_LEN)]
            in 'ㄱ'..'ㅎ' -> JM_FJ[ch - 'ㄱ']
            in 'ㅏ'..'ㅣ', o, oo -> JM_FJ['ㅇ' - 'ㄱ']
            in '0'..'9', in 'A'..'Z' -> ch
            in 'a'..'z' -> 'A' + (ch - 'a')
            else -> '#'
        }
    }

    /**
     * 최소음소분해 '가'(0xAC00) ~ '힣'(0xD7A3) , 'ㄱ'(0x3131) ~ 'ㅣ'(0x3163)
     * @return 쀍 -> ㅂㅂㅡᆞᆞㅣㅣㄹㄱ
     */
    fun hangul2jaso2(s: String?): String {
        if (s.isNullOrEmpty())
            return ""

        return s.flatMap {
            when (it) {
                in '가'..'힣' -> {
                    var c = it - '가'
                    val a = c / (M_LEN * JT_LEN)
                    c = c % (M_LEN * JT_LEN)
                    val b = c / JT_LEN
                    c = c % JT_LEN

                    if (c == 0)
                        JH_FS[a] + M_FS[b]
                    else
                        JH_FS[a] + M_FS[b] + JT_FS[c]
                }
                in 'ㄱ'..'ㅣ' -> JM_FS[it - 'ㄱ']
                oo -> listOf(o, o)
                else -> listOf(it)
            }
        }.joinToString("")
    }

    // 음소분해 초성중성종성분해 "AC00:가" ~ "D7A3:힣"
    fun hangul2jaso1(s: String?): String {
        if (s.isNullOrEmpty())
            return ""

        return s.map {
            when (it) {
                in '가'..'힣' -> {
                    var c = it - '가'
                    val a = c / (M_LEN * JT_LEN)
                    c = c % (M_LEN * JT_LEN)
                    val b = c / JT_LEN
                    c = c % JT_LEN

                    if (c == 0)
                        "" + JH[a] + M[b]
                    else
                        "" + JH[a] + M[b] + JT[c]

                }
                o, oo -> Unit
                else -> it
            }
        }.joinToString("")
    }

    fun hangul2english(s: String?): String {
        if (s.isNullOrEmpty())
            return ""

        return s.map {
            when (it) {
                in '가'..'힣' -> {
                    var c = it - '가'
                    val a = c / (M_LEN * JT_LEN)
                    c = c % (M_LEN * JT_LEN)
                    val b = c / JT_LEN
                    c = c % JT_LEN

                    if (c == 0)
                        "" + JH_E[a] + M_E[b]
                    else
                        "" + JH_E[a] + M_E[b] + JT_E[c]
                }
                o, oo -> Unit
                else -> it
            }
        }.joinToString("")
    }

    fun containFS(key: String?, value: String?): Boolean {
        if (key.isNullOrEmpty())
            return true
        if (value == null || value.isEmpty() || value.length < key.length)
            return false

        val keyJaso = hangul2jaso2(key)
        val valueJaso = hangul2jaso2(value)
        return valueJaso.contains(keyJaso)
    }

    fun containFS(key: String?, key_jaso: String, value: String?): Boolean {
        if (key.isNullOrEmpty())
            return true
        if (value == null || value.isEmpty() || value.length < key.length)
            return false

        val value_jaso = hangul2jaso2(value)
        return value_jaso.contains(key_jaso)
    }

    fun containFS(key: String?, key_jaso: String, value: String?, value_jaso: String): Boolean {
        if (key.isNullOrEmpty())
            return true
        return if (value == null || value.isEmpty() || value.length < key.length) false else value_jaso.contains(key_jaso)

    }

    /**
     * 초성분해비교
     * ㄱㅅㅎ 고성호 OK
     * ㄳㅎ   고성호 OK
     */
    fun containJH(key: String?, value: String?): Boolean {
        if (key.isNullOrEmpty())
            return true
        if (value.isNullOrEmpty() || value.length < key.length)
            return false

        val keyJaso = hangul2jaso2(hangul2JH(key))
        val valueJaso = hangul2jaso2(hangul2JH(value))
        return valueJaso.contains(keyJaso)
    }

    fun containJH(key: String?, key_jaso: String, value: String?): Boolean {
        if (key.isNullOrEmpty())
            return true
        if (value.isNullOrEmpty() || value.length < key.length)
            return false

        val value_jaso = hangul2JH(value)
        return value_jaso.contains(key_jaso)
    }

    fun containJH(key: String?, key_jaso: String, value: String?, value_jaso: String): Boolean {
        if (key.isNullOrEmpty())
            return true
        if (value.isNullOrEmpty() || value.length < key.length)
            return false

        return value_jaso.contains(key_jaso)
    }

    fun containSmart(key: String?, value: String): Boolean {
        if (key.isNullOrEmpty())
            return true

        val ch1 = key[0]

        if (key.length > 1) {
            val ch2 = key[1]
            if (ch1 in 'ㄱ'..'ㅎ' && ch2 in 'ㄱ'..'ㅎ') {
                return containJH(key, value)
            }
        }

        return if (key.length == 1 && ch1 in 'ㄱ'..'ㅎ')
            containJH(key, value)
        else
            containFS(key, value)

    }

    fun containSmart(key: String?, key_jaso: String, value: String): Boolean {
        if (key.isNullOrEmpty())
            return true

        val ch1 = key[0]

        if (key.length > 1) {
            val ch2 = key[1]
            if (ch1 in 'ㄱ'..'ㅎ' && ch2 in 'ㄱ'..'ㅎ') {
                return containJH(key, value)
            }
        }

        return if (key.length == 1 && ch1 in 'ㄱ'..'ㅎ') {
            containJH(key, value)
        } else containFS(key, key_jaso, value)
    }

    fun containSmart(key: String?, key_jaso: String, value: String, value_jaso: String): Boolean {
        if (key.isNullOrEmpty())
            return true

        val ch1 = key[0]

        if (key.length > 1) {
            val ch2 = key[1]
            if (ch1 in 'ㄱ'..'ㅎ' && ch2 in 'ㄱ'..'ㅎ') {
                return containJH(key, value)
            }
        }

        return if (key.length == 1 && ch1 in 'ㄱ'..'ㅎ')
            containJH(key, value)
        else
            containFS(key, key_jaso, value, value_jaso)
    }

}
