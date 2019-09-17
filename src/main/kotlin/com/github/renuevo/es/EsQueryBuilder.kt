package com.github.renuevo.es

import java.io.File

object EsQueryBuilder {

    fun getQuery(query: String, vararg variables: Any): String {
        val queryList = queryTokenizer(query)
        return queryCombination(queryList, variables)
    }

    fun getQueryArray(query: String, variables: Array<*>): String {
        val queryList = queryTokenizer(query)
        return queryCombination(queryList, variables)
    }

    fun getQueryArrayList(query: String, variables: List<Any>): String {

        val queryList = queryTokenizer(query)
        val queryBuilder = StringBuilder()

        var index = 0

        for (str in queryList) {
            queryBuilder.append(str)
            if (index < variables.size) {
                queryBuilder.append(variables[index])
                index++
            }
        }

        return queryBuilder.toString()
    }

    private fun queryTokenizer(query: String): Array<String> {
        var queryToken = query.replace("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)".toRegex(), "")
        return queryToken.split("§[^§]+§".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun queryCombination(queryList: Array<String>, variables: Array<*>): String {

        val queryBuilder = StringBuilder()
        var index = 0

        for (str in queryList) {
            queryBuilder.append(str)
            if (index < variables.size) {
                queryBuilder.append(variables[index])
                index++
            }
        }
        return queryBuilder.toString()
    }

    fun getQueryTemplate(qyFile: File): String {
        var str: String
        val qyTemplate = StringBuilder()
        qyFile.bufferedReader().use{
            while(true) {
                str = it.readLine() ?: break
                qyTemplate.append(str)
            }
        }

        return qyTemplate.toString()
    }

}
