package com.github.renuevo.common

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/**
 * <pre>
 * @className : VoMapperUtils
 * @author : Deokhwa.Kim
 * @since : 2018-08-03
 * @version : 1.2
 * @update : 2019-04-10
 * @sammary : methodType에 따라 길이 변경
 * @description : 상속 메소드 기능 추가
</pre> *
 */
class VoMapperUtils {
    fun <T> getFieldMehtods(classType: Class<T>, methodType: String): Map<String, Method> {
        val methodsMap = ConcurrentHashMap<String, Method>()
        val fields = classType.declaredFields

        for (field in fields) {
            when (methodType) {
                "set" -> methodsMap[field.name] = classType.getDeclaredMethod(methodType + field.name.substring(0, 1).toUpperCase() + field.name.substring(1), String::class.java)
                "get" -> methodsMap[field.name] = classType.getDeclaredMethod(methodType + field.name.substring(0, 1).toUpperCase() + field.name.substring(1))
            }
        }
        return methodsMap
    }

    /**
     * <pre>
     * @methodName : getSetMethods
     * @author : Deokhwa.Kim
     * @since : 2018-01-30 오후 4:02
     * @param classType, type
     * @description : data와 매핑될 vo의 set메소드 매핑
     * @return java.util.Map<java.lang.String , java.lang.reflect.Method>
    </java.lang.String></pre> *
     */
    fun <T> getMethods(classType: Class<T>, methodType: String, type: String): Map<String, Method>? {

        val methodsMap = ConcurrentHashMap<String, Method>()
        val methods = classType.declaredMethods
        var key: String
        var index: Int

        for (method in methods) {
            if (method.name.startsWith(methodType)) {
                when (type) {
                    "lower" -> key = method.name.substring(3).toLowerCase()
                    "upper" -> key = method.name.substring(3).toUpperCase()
                    "underUpper" -> {
                        //setCompany_name_str -> companyNameStr
                        key = method.name.substring(3, 4).toLowerCase() + method.name.substring(4)

                        if (key.substring(key.length - 1) == "_")
                            key = key.substring(0, key.length - 1)

                        do {
                            index = key.indexOf("_")
                            if (index == -1) break
                            key = key.substring(0, index) + key.substring(index + 1, index + 2).toUpperCase() + key.substring(index + 2)
                        } while (true)
                    }
                    "upperUnder" -> {
                        //setCompanyNameStr -> company_name_str
                        key = method.name.substring(3, 4).toLowerCase() + method.name.substring(4)
                        do {
                            index = regIndexOf(key)
                            if (index == -1) break
                            key = key.substring(0, index) + "_" + key.substring(index, index + 1).toLowerCase() + key.substring(index + 1)
                        } while (true)
                    }
                    else ->
                        //setStringIdx -> stringIdx
                        key = method.name.substring(3, 4).toLowerCase() + method.name.substring(4)
                }

                if (!methodsMap.containsKey(key))
                    methodsMap[key] = method
                else
                    throw Exception("$key this key duplication!")
            }
        }
        return methodsMap
    }

    /**
     * <pre>
     * @methodName : getMethods
     * @author : Deokhwa.Kim
     * @update : 2018-11-28 오후 1:33
     * @since : 2018-08-03 오후 2:07
     * @param classType, methodType
     * @return java.util.Map<java.lang.String , java.lang.reflect.Method>
    </java.lang.String></pre> *
     */
    fun <T> getMethods(classType: Class<T>, methodType: String): Map<String, Method>? {
        return createMethodMap(classType.declaredMethods, methodType)
    }

    /**
     *<pre>
     * @methodName : getMembers
     * @author : Deokhwa.Kim
     * @since : 2019-06-03 오전 11:17
     * @summary : 프로퍼티 접근자 생성
     * @param : [classType]
     *</pre>
     **/
    companion object {
        fun getMembers(classType: KClass<*>): Map<String, KMutableProperty<*>> {
            val membersMap = ConcurrentHashMap<String, KMutableProperty<*>>()
            for (kProp in classType.memberProperties.filterIsInstance<KMutableProperty<*>>()) {
                val stringBuilder = StringBuilder()
                for (char in kProp.name) {
                    if (char in 'A'..'Z')
                        stringBuilder.append("_")
                    stringBuilder.append(char.toLowerCase())
                }
                membersMap[kProp.name] = kProp  //원형
                membersMap[stringBuilder.toString()] = kProp    //companyNameStr -> company_name_str
            }

            //함수로 임의의 프로퍼티 접근자 지정
            for (kProp in classType.memberFunctions) {
                if (kProp.name.startsWith("voMapper")) {
                    val sampleClass = classType.createInstance()
                    var memberName = kProp.name.replace("voMapper".toRegex(), "")
                    memberName = memberName[0].toLowerCase() + memberName.substring(1, memberName.length)   //첫 대문자 변형
                    if (membersMap.containsKey(kProp.call(sampleClass) as String)) {
                        val stringBuilder = StringBuilder()
                        for (char in memberName) {
                            if (char in 'A'..'Z')
                                stringBuilder.append("_")
                            stringBuilder.append(char.toLowerCase())
                        }
                        membersMap[memberName] = membersMap[kProp.call(sampleClass) as String] as KMutableProperty<*> //원형
                        membersMap[stringBuilder.toString()] = membersMap[kProp.call(sampleClass) as String] as KMutableProperty<*>  //companyNameStr -> company_name_str
                    }
                }
            }


            return membersMap
        }
    }

    /**
     * <pre>
     * @methodName : createMethodMap
     * @author : Deokhwa.Kim
     * @since : 2018-11-28 오후 1:33
     * @summary : field에대한 method Map 생성
     * @param : methods, methodType
     * @return : java.util.Map<java.lang.String></java.lang.String>,java.lang.reflect.Method>
     * </pre>
     */
    private fun createMethodMap(methods: Array<Method>, methodType: String): Map<String, Method>? {

        val methodsMap = ConcurrentHashMap<String, Method>()
        val methodsSet = ConcurrentHashMap.newKeySet<String>()

        var key: String
        var index: Int

        try {
            for (method in methods) {
                methodsSet.clear()
                if (method.name.startsWith(methodType)) {
                    key = method.name.substring(methodType.length).toLowerCase()
                    if (inputChk(methodsMap.keys, methodsSet, key))
                        throw Exception()
                    methodsMap[key] = method
                    methodsSet.add(key)

                    key = method.name.substring(methodType.length).toUpperCase()
                    if (inputChk(methodsMap.keys, methodsSet, key))
                        throw Exception()
                    methodsMap[key] = method
                    methodsSet.add(key)

                    key = method.name.substring(methodType.length, methodType.length + 1).toLowerCase() + method.name.substring(methodType.length + 1)
                    if (key.substring(key.length - 1) == "_")
                        key = key.substring(0, key.length - 1)

                    //setCompany_name_str -> companyNameStr
                    do {
                        index = key.indexOf("_")
                        if (index == -1) break
                        key = key.substring(0, index) + key.substring(index + 1, index + 2).toUpperCase() + key.substring(index + 2)
                    } while (true)

                    if (inputChk(methodsMap.keys, methodsSet, key))
                        throw Exception()
                    methodsMap[key] = method
                    methodsSet.add(key)

                    //setCompanyNameStr -> company_name_str
                    key = method.name.substring(methodType.length, methodType.length + 1).toLowerCase() + method.name.substring(methodType.length + 1)
                    do {
                        index = regIndexOf(key)
                        if (index == -1) break
                        key = key.substring(0, index) + "_" + key.substring(index, index + 1).toLowerCase() + key.substring(index + 1)
                    } while (true)
                    if (inputChk(methodsMap.keys, methodsSet, key))
                        throw Exception()
                    methodsMap[key] = method
                    methodsSet.add(key)

                    //setStringIdx -> stringIdx
                    key = method.name.substring(methodType.length, methodType.length + 1).toLowerCase() + method.name.substring(methodType.length + 1)
                    if (inputChk(methodsMap.keys, methodsSet, key))
                        throw Exception()
                    methodsMap[key] = method

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return methodsMap
    }

    /**
     * <pre>
     * @methodName : getMethodsSuper
     * @author : Deokhwa.Kim
     * @since : 2018-11-28 오후 1:30
     * @param classType, methodType
     * @return java.util.Map<java.lang.String , java.lang.reflect.Method>
    </java.lang.String></pre> *
     */
    fun <T> getMethodsSuper(classType: Class<T>, methodType: String): Map<String, Method>? {
        val methodList = ArrayList(classType.declaredMethods.toList())
        methodList.addAll(classType.superclass.declaredMethods.toList())
        return createMethodMap(methodList.toTypedArray(), methodType)
    }

    private fun inputChk(methodsKeySet: Set<String>, keySet: Set<String>, key: String): Boolean {
        if (methodsKeySet.contains(key) && !keySet.contains(key)) {
            throw Exception("$key this key is ambiguous")
        }
        return false
    }

    private fun regIndexOf(key: String): Int {
        val pattern = Pattern.compile("[A-Z]")
        val matcher = pattern.matcher(key)
        return if (matcher.find()) matcher.start() else -1
    }

}
