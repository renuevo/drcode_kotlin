package com.github.renuevo.csv


import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import com.github.renuevo.common.VoMapperUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

/**
 * <pre>
 * @className : CsvUtils
 * @author : Deokhwa.Kim
 * @description : csv 유틸
 * @version : 1.7 -> kotlin으로 변경
 * @update : 2019-06-11
 * @since : 2018-08-03
</pre>
 */

class CsvUtils {

    fun writeCsv(list: List<Array<String>>, path: String, charsetName: String) {
        var pathStr = path

        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val outputStream = FileOutputStream(pathStr)

        //UTF-8 BOM으로 인코딩 설정
        if (charsetName.equals("utf-8", ignoreCase = true)) {
            outputStream.write(0xEF)
            outputStream.write(0xBB)
            outputStream.write(0xBF)
        }
        CSVWriter(OutputStreamWriter(outputStream)).use {
            it.writeAll(list)
        }
    }

    /**
     * <pre>
     * @methodName : readCsv
     * @author : Deokhwa.Kim
     * @since : 2018-01-24 오후 7:41
     * @param path, charsetName, line
     * @description : line 추가로 몇 라인부터 읽어올지 정할수 있게 되었다
     * @return java.util.List<java.lang.String []></java.lang.String>
     * </pre>
     */
    fun readCsv(path: String, charsetName: String, line: Int): List<Array<String>> {
        var pathStr = path


        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val resultList = ArrayList<Array<String>>()

        var csvLine: Array<String>
        var firstLine = true

        val csvReader = CSVReader(InputStreamReader(FileInputStream(pathStr), charsetName.toUpperCase()), ',', '\"', line)
        csvReader.use {
            while (true) {
                csvLine = it.readNext() ?: break
                if (firstLine) {
                    csvLine[0] = removeUTF8BOM(csvLine[0])
                    firstLine = false
                }
                resultList.add(csvLine)
            }
        }

        return resultList
    }

    fun <T> readModelCsv(path: String, charsetName: String, classType: Class<T>): List<T> {
        var pathStr = path
        val memberMap = VoMapperUtils.getMembers(classType::class)

        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val resultList = ArrayList<T>()


        var csvLine: Array<String>
        var csvTitle: Array<String>

        var classTemplate: T
        val csvReader = CSVReader(InputStreamReader(FileInputStream(pathStr), charsetName.toUpperCase()), ',', '\"', 0)

        csvReader.use {
            csvTitle = it.readNext()
            csvTitle[0] = removeUTF8BOM(csvTitle[0])

            while (true) {
                csvLine = csvReader.readNext() ?: break
                classTemplate = classType.newInstance()
                csvTitle.indices.forEach { idx ->
                    if (memberMap[csvTitle[idx]]?.returnType?.isSupertypeOf(Int::class.createType())!!)
                        memberMap[csvTitle[idx]]?.setter?.call(classTemplate, csvLine[idx].toInt())
                    else
                        memberMap[csvTitle[idx]]?.setter?.call(classTemplate, csvLine[idx])
                }

                resultList.add(classTemplate)
            }
        }
        return resultList
    }

    fun <T> writeCsv(list: List<T>, path: String, charsetName: String, classType: Class<T>) {

        var pathStr = path
        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val outputStream = FileOutputStream(pathStr)

        //UTF-8 BOM으로 인코딩 설정
        if (charsetName.equals("utf-8", ignoreCase = true)) {
            outputStream.write(0xEF)
            outputStream.write(0xBB)
            outputStream.write(0xBF)
        }

        CSVWriter(OutputStreamWriter(outputStream)).use {

            //제목 라인 input
            val fields = classType.declaredFields
            val outStrings = arrayOfNulls<String>(fields.size)
            val methodNames = arrayOfNulls<String>(fields.size)
            for (i in fields.indices) {
                outStrings[i] = fields[i].name
                methodNames[i] = "get" + fields[i].name.substring(0, 1).toUpperCase() + fields[i].name.substring(1)
            }
            it.writeNext(outStrings)

            for (objectData in list) {
                for (i in fields.indices) {
                    outStrings[i] = classType.getDeclaredMethod(methodNames[i]).invoke(objectData).toString()
                }
                it.writeNext(outStrings)
            }
        }
    }

    fun <T> writeCsv(writeMap: Map<String, List<T>>, path: String, charsetName: String, classType: Class<T>) {
        var pathStr = path

        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val outputStream = FileOutputStream(pathStr)

        //UTF-8 BOM으로 인코딩 설정
        if (charsetName.equals("utf-8", ignoreCase = true)) {
            outputStream.write(0xEF)
            outputStream.write(0xBB)
            outputStream.write(0xBF)
        }

        CSVWriter(OutputStreamWriter(outputStream)).use {

            //제목 라인 input
            val fields = classType.declaredFields
            val outStrings = arrayOfNulls<String>(fields.size)
            val methodNames = arrayOfNulls<String>(fields.size)
            for (i in fields.indices) {
                outStrings[i] = fields[i].name
                methodNames[i] = "get" + fields[i].name.substring(0, 1).toUpperCase() + fields[i].name.substring(1)
            }
            it.writeNext(outStrings)

            for (key in writeMap.keys) {
                for (objectData in writeMap.getValue(key)) {
                    for (i in fields.indices) {
                        outStrings[i] = classType.getDeclaredMethod(methodNames[i]).invoke(objectData) as String
                    }
                    it.writeNext(outStrings)
                }
            }
        }
    }


    fun writeCsv(map: Map<String, String>, path: String, charsetName: String, keyTitle: String, valueTitle: String) {
        var pathStr = path

        if (!path.substring(path.length - 4).equals(".csv", ignoreCase = true))
            pathStr += ".csv"

        val outputStream = FileOutputStream(pathStr)

        //UTF-8 BOM으로 인코딩 설정
        if (charsetName.equals("utf-8", ignoreCase = true)) {
            outputStream.write(0xEF)
            outputStream.write(0xBB)
            outputStream.write(0xBF)
        }

        CSVWriter(OutputStreamWriter(outputStream)).use {

            //제목 라인 input
            val outStrings = arrayOfNulls<String>(2)
            outStrings[0] = keyTitle
            outStrings[1] = valueTitle
            it.writeNext(outStrings)

            for (key in map.keys) {
                outStrings[0] = key
                outStrings[1] = map[key]
                it.writeNext(outStrings)
            }
        }

    }

    companion object {
        private const val UTF8_BOM = "\uFEFF"
        private fun removeUTF8BOM(str: String): String {
            return if (str.startsWith(UTF8_BOM)) str.substring(1) else str
        }
    }

}
