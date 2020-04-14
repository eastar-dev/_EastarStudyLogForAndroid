package smart.net

import android.log.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

@Suppress("LocalVariableName")
class OkHttp3Logger : Interceptor {
    @Suppress("unused", "ObjectPropertyName", "ObjectPropertyName", "MayBeConstant")
    companion object {
        var LOG = true
        var _OUT_1 = true
        var _OUT_2 = false
        var _OUT_H = false
        var _OUT_C = false
        var _IN_1 = true
        var _IN_2 = false
        var _IN_H = false
        var _IN_C = false

        val COOKIE = "Cookie"
        val SET_COOKIE = "Set-Cookie"

        private val UTF8 = Charset.forName("UTF-8")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!LOG) return chain.proceed(request)

        val _out_1 = request.method + ":" + request.url

        val body = request.body
        val _out_2 = StringBuilder()
        if (body != null && !bodyHasUnknownEncoding(request.headers)) {
            val buffer = Buffer()
            body.writeTo(buffer)
            val contentType = body.contentType()
            var charset: Charset? = UTF8
            if (contentType != null)
                charset = contentType.charset(UTF8)


            if (isPlaintext(buffer)) {
                _out_2.divide.append("BODY:[${body.contentLength()}]")
                _out_2.divide.append(buffer.clone().readString(charset!!))
            } else {
                _out_2.divide.append("BODY_BINARY:[${body.contentLength()}]")
            }
        }

        val _out_c = StringBuilder()
        if (_OUT_C) {
            val headers = request.headers
            for (i in 0 until headers.size)
                if (headers.name(i) == COOKIE)
                    _out_c.divide.append(headers.name(i) + ": " + headers.value(i))
        }

        val _out_h = StringBuilder()
        if (_OUT_H) {
            val headers = request.headers
            for (i in 0 until headers.size)
                if (headers.name(i) != COOKIE)
                    _out_h.divide.append(headers.name(i) + ": " + headers.value(i))
        }

        if (_OUT_1 && !_OUT_C && !_OUT_H && !_OUT_2) {
            if (_out_2.length > 1000) _out_2.setLength(1000)
            Log.e(_out_1, _out_2.toString().replace('\n', '↙'))
        } else {
            if (_OUT_C) Log.e(_out_c)
            if (_OUT_H) Log.e(_out_h)
            if (_OUT_1) Log.e(_out_1)
            if (_OUT_2) Log.e(_out_2)
        }

        val startNs = System.nanoTime()
        return kotlin.runCatching {
            chain.proceed(request)
        }.onFailure {
            Log.w("HTTP FAILED: ${it.javaClass.simpleName} ${it.message}")
            Log.printStackTrace(it)
            //val _in_1 = StringBuilder().append("${response.code()} ${response.message()} ${response.request().url()} (${tookMs}ms)")
            //Log.w(_out_1 + _out_2.toString().replace('\n', '↙'))

            //val responseBody = response.body()
            //val contentLength = responseBody!!.contentLength()

        }.onSuccess { response ->
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val responseBody = response.body
            val contentLength = responseBody!!.contentLength()

            val _in_c = StringBuilder()
            if (_IN_C) {
                val headers = request.headers
                for (i in 0 until headers.size)
                    if (headers.name(i) == SET_COOKIE)
                        _in_c.divide.append(headers.name(i) + ": " + headers.value(i))
            }

            val _in_h = StringBuilder()
            if (_IN_H) {
                val headers = response.headers
                for (i in 0 until headers.size)
                    if (headers.name(i) != SET_COOKIE)
                        _in_h.divide.append(headers.name(i) + ": " + headers.value(i))
            }

            val _in_1 = StringBuilder().append(
                "${response.code} ${response.message} ${response.request.url} (${tookMs}ms)"
            )

            val _in_2 = StringBuilder()
            if (response.body != null && !bodyHasUnknownEncoding(response.headers)) {
                val source = responseBody.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer


                val headers = response.headers
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    var gzippedLength = buffer.size
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        gzippedResponseBody = GzipSource(buffer.clone())
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    } finally {
                        gzippedResponseBody?.close()
                    }
                }

                var charset: Charset? = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null)
                    charset = contentType.charset(UTF8)
                if (contentLength != 0L)
                    _in_2.divide.append(buffer.clone().readString(charset!!))
            }

            val priority = if (response.isSuccessful) Log.INFO else Log.WARN
            if (_IN_1 && !_IN_C && !_IN_H && !_IN_2) {
                if (_in_2.length > 1000) _in_2.setLength(1000)
                Log.p(priority, _in_1.toString(), _in_2.toString().replace('\n', '↙').trim())
            } else {
                if (_IN_C) Log.i(_in_c)
                if (_IN_H) Log.i(_in_h)
                if (_IN_1) Log.i(_in_1)
                if (_IN_2) Log.i(_in_2.toString().trim())
            }
        }.getOrThrow()
    }

    private val StringBuilder.divide: StringBuilder
        get() = if (isNotEmpty())
            append("\n")
        else
            this

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return (contentEncoding != null
            && !contentEncoding.equals("identity", ignoreCase = true)
            && !contentEncoding.equals("gzip", ignoreCase = true))
    }


    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }
}
