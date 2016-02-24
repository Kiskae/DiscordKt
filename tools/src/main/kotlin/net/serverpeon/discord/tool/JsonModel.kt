package net.serverpeon.discord.tool

import com.google.common.collect.ImmutableSet
import com.google.gson.*
import java.lang.reflect.Type

abstract class JsonModel(val type: String) {
    abstract fun acceptData(el: JsonElement)

    class Object : JsonModel("object") {
        internal val fields: MutableMap<String, JsonModel> = mutableMapOf()
        internal val requiredFields: GetOrCreate<MutableSet<String>> = GetOrCreate()
        internal val nullableFields: MutableSet<String> = mutableSetOf()

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
        internal val prototype: GetOrCreate<JsonModel> = GetOrCreate()

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
        internal val fieldType: MutableSet<String> = mutableSetOf()

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

    object Serializer : JsonSerializer<JsonModel> {
        override fun serialize(src: JsonModel, type: Type, context: JsonSerializationContext): JsonElement {
            return when (src) {
                is Object -> {
                    JsonArray().apply {
                        val req = src.requiredFields.getOrCreate { ImmutableSet.of() }
                        for ((k, v) in src.fields) {
                            add(JsonObject().apply {
                                addProperty("name", k)
                                addProperty("required", k in req)
                                addProperty("nullable", k in src.nullableFields)
                                add("content", context.serialize(v))
                            })
                        }
                    }
                }
                is Array -> {
                    JsonObject().apply {
                        addProperty("__", "array")
                        add("content", context.serialize(src.prototype.getOrCreate { Null }))
                    }
                }
                is Field -> {
                    JsonObject().apply {
                        add("type", src.fieldType.let {
                            if (it.isEmpty()) {
                                JsonNull.INSTANCE
                            } else if (it.size == 1) {
                                JsonPrimitive(it.first())
                            } else {
                                JsonArray().apply {
                                    it.forEach { add(it) }
                                }
                            }
                        })
                    }
                }
                Null -> {
                    JsonNull.INSTANCE
                }
                else -> error("Unknown type")
            }
        }
    }

    private object Null : JsonModel("null") {
        override fun acceptData(el: JsonElement) {
            error("Should never happen")
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