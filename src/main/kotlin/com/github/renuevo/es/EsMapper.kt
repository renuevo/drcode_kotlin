package kr.co.saramin.lab.common


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class EsMapper() {

    private val objectMapper: ObjectMapper = ObjectMapper()

    fun getSearchCount(response: String): Int {
        var jsonNode = objectMapper.readTree(response)
        jsonNode = jsonNode.get("hits")
        return jsonNode.get("total").asInt()
    }

    /**
     * <pre>
     * @mathodName : getCount
     * @author : Deokhwa.Kim
     * @since : 2018-03-21 오후 1:49
     * @description : _count query 대응 매핑
     * @param response
     * @return long
     * </pre>
     */
    fun getCount(response: String): Long {
        val jsonNode = objectMapper.readTree(response)
        return java.lang.Long.parseLong(jsonNode.get("count").toString())
    }

    /**
     * <pre>
     * @mathodName : getSearch
     * @author : Deokhwa.Kim
     * @since : 2017-12-29 오전 11:17
     * @param response
     * @description : 검색 결과를 전체를 반환 _index, _version등을 다 포함
     * @return java.util.List<T>
     *</pre>
     */
    fun <T> getSearch(response: String): List<T> {
        var jsonNode = objectMapper.readTree(response)
        jsonNode = jsonNode.get("hits")
        return objectMapper.readValue(jsonNode.get("hits").toString(), object : TypeReference<List<T>>() {

        })
    }

    /**
     * <pre>
     * @mathodName : getSearchSource
     * @author : Deokhwa.Kim
     * @since : 2017-12-29 오전 11:18
     * @param response
     * @description : 검색 결과의 source만 추출하여 반환
     * @return java.util.List<T>
    </T></pre> *
     */
    fun <T> getSearchSource(response: String, classType: Class<T>): List<T> {
        var jsonNode = objectMapper.readTree(response)
        jsonNode = jsonNode.get("hits")

        val resultList = ArrayList<T>()
        var voObject: T

        if (jsonNode.get("hits").isArray()) {
            for (jsonHits in jsonNode.get("hits")) {
                voObject = objectMapper.readValue(jsonHits.get("_source").toString(), classType)
                resultList.add(voObject)
            }
        }

        return resultList
    }

    fun <T> getAggsBuckets(response: String, aggsName: String): List<T> {
        var jsonNode = objectMapper.readTree(response)
        jsonNode = jsonNode.get("aggregations")
        jsonNode = jsonNode.get(aggsName)
        return objectMapper.readValue(jsonNode.get("buckets").toString(), object : TypeReference<List<T>>() {})
    }

    fun <T> getAggsBuckets(response: String, aggsName: String, classType: Class<T>): List<T> {
        var jsonNode = objectMapper.readTree(response)
        jsonNode = jsonNode.get("aggregations")
        jsonNode = jsonNode.get(aggsName)

        val resultList = ArrayList<T>()
        var voObject: T

        if (jsonNode.get("buckets").isArray()) {
            for (jsonHits in jsonNode.get("buckets")) {
                voObject = objectMapper.readValue(jsonHits.toString(), classType)
                resultList.add(voObject)
            }
        }
        return resultList
    }

    fun <T> getAggsBuckets(response: String): List<T> {
        val jsonNode = objectMapper.readTree(response)
        return getAggsBuckets(
            response,
            getFieldName(jsonNode.get("aggregations"), 0)
        )   //처음 field 값으로 aggsregations_name 고정
    }

    /**
     * <pre>
     * @mathodName : getFieldName
     * @author : Deokhwa.Kim
     * @since : 2017-12-19 오전 10:28
     * @param jsonNode, index
     * @description : index로 jsonNode의 field값을 가져옴
     * @return java.lang.String
    </pre> *
     */
    private fun getFieldName(jsonNode: JsonNode, index: Int): String {
        var idx = 0
        var fieldName = ""
        val nameIter = jsonNode.fieldNames()

        while (nameIter.hasNext()) {
            fieldName = nameIter.next()
            if (index == idx)
                break
            idx++
        }

        return fieldName
    }


}
