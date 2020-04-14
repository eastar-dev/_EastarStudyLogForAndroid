@file:Suppress("unused")

package android.util

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author r
 *
 *
 * <pre>
 * 기능 : ssp관련 파일IO Util
 *
 * file     => xxxxxxxxxx.xxx              filename and ext
 * filename => xxxxxxxxxxx                 has no ext
 * pathfile => /xxxx/xxxxx/xxxxxxxxx.xxx   absolute path + filename + ext
 * ext      => .xxx                        note : has dot(".")
 *
 * &lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
</pre> *
 */
object FU {

    private fun getFreeSpace(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return blockSize * availableBlocks
    }

    fun delete(pathFile: Any): Boolean {
        val dir = toFile(pathFile)
        if (!dir.exists())
            return true

        val temp = getTempDir(dir.parentFile ?: dir, dir.name)
        dir.renameTo(temp)
        return deleteR(temp)
    }

    private fun getTempDir(directory: File, prefix: String = ""): File {
        var result: File
        do {
            result = File(directory, prefix + Random().nextInt())
        } while (result.exists())
        return result
    }

    private fun deleteR(pathFile: Any): Boolean {
        val dir = toFile(pathFile)
        return when {
            !dir.exists() -> true
            dir.isFile -> dir.delete()
            dir.isDirectory -> {
                var result = true
                dir.listFiles()?.forEach {
                    result = result && deleteR(it)
                }
                result
            }
            else -> false
        }
    }

    fun toFile(pathFile: Any): File = when (pathFile) {
        is Uri -> File(pathFile.schemeSpecificPart)
        is String -> File(pathFile)
        is File -> pathFile
        else -> throw UnsupportedOperationException("Must the file is Uri or String(pathfile) or File class")
    }

    fun toString(pathFile: Any): String = when (pathFile) {
        is Uri -> pathFile.schemeSpecificPart
        is String -> File(pathFile).name
        is File -> pathFile.name
        else -> throw UnsupportedOperationException("Must the file is Uri or String(pathfile) or File class")
    }

    fun toUri(pathFile: Any): Uri = Uri.fromFile(toFile(pathFile))

    fun createTimebaseTempFile(prefix: String = "", suffix: String = "", path: File? = null): File =
            File.createTempFile(prefix + SimpleDateFormat("_yyyyMMdd_HHmmss_SSS_", Locale.getDefault()).format(Date()), suffix, path)

    @JvmStatic
    fun copy(source: File, target: File) = source.copyTo(target)

    fun copy(source: String, target: String) = File(source).copyTo(File(target))
    fun copyDB(source: SQLiteDatabase, target: File) = File(source.path).copyTo(target)
    fun copy(inputStream: InputStream, outputStream: OutputStream) = inputStream.copyTo(outputStream)

    @Suppress("UNCHECKED_CAST")
    fun <T> load(file: File): T = ObjectInputStream(FileInputStream(file)).use { it.readObject() as T }

    fun save(file: File, obj: Any) = ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(obj) }
    fun saveByte(file: File, buffer: ByteArray) = file.writeBytes(buffer)
    fun loadByte(file: File) = file.readBytes()
    fun saveString(file: File, data: String) = file.writeText(data)
    fun loadString(file: File): String = file.readText()

    fun saveUTF(file: File, str: String) = ObjectOutputStream(FileOutputStream(file)).use { it.writeUTF(str) }
    fun loadUTF(file: File): String = ObjectInputStream(FileInputStream(file)).use { it.readUTF() }

    fun loadLine(file: File, lineNumber: Int): String {
        val buf = LineNumberReader(FileReader(file))
        buf.lineNumber = lineNumber
        val str = buf.readLine()
        buf.close()
        return str
    }

    fun saveLineAppend(file: File, str: String) {
        val buf = PrintWriter(FileWriter(file, true))
        buf.println(str)
        buf.close()
    }

}
