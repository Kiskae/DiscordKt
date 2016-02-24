package net.serverpeon.discord.tool

import com.google.gson.JsonElement

abstract class JsonModel(val type: String) {
    abstract fun acceptData(el: JsonElement)

    class Object : JsonModel("object") {
        private val fields: MutableMap<String, JsonModel> = mutableMapOf()
        private val requiredFields: GetOrCreate<MutableSet<String>> = GetOrCreate()
        private val nullableFields: MutableSet<String> = mutableSetOf()

        override fun acceptData(el: JsonElement) {
            val obj = el.asJsonObject
            val objFields = mutableSetOf<String>()
            for ((k, v) in obj.entrySet()) {
                objFields.add(k)
                if (v.isJsonNull) {
                    nullableFields.add(k)
                } else {
                    fields.getOrPut(k, { createFrom(v) }).acceptData(v)
                }
            }
            requiredFields.getOrCreate { objFields }.retainAll(objFields)
        }
    }

    class Array : JsonModel("array") {
        private val prototype: GetOrCreate<JsonModel> = GetOrCreate()

        override fun acceptData(el: JsonElement) {
            val arr = el.asJsonArray
            arr.firstOrNull { !it.isJsonNull }?.let { firstEl ->
                val prototype = prototype.getOrCreate {
                    createFrom(firstEl)
                }
                arr.forEach { prototype.acceptData(it) }
            }
        }
    }

    class Field : JsonModel("field") {
        private val fieldType: MutableSet<String> = mutableSetOf()

        override fun acceptData(el: JsonElement) {
            val value = el.asJsonPrimitive
            fieldType.add(when {
                value.isBoolean -> "boolean"
                value.isNumber -> "number"
                value.isString -> "string"
                else -> "undefined"
            })
        }
    }

    companion object {
        fun createFrom(el: JsonElement): JsonModel {
            return when {
                el.isJsonArray -> Array()
                el.isJsonObject -> Object()
                el.isJsonPrimitive -> Field()
                else -> error("Weird Json")
            }
        }
    }
}