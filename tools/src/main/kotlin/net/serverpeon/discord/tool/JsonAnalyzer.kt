package net.serverpeon.discord.tool

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement

class JsonAnalyzer(val gson: Gson = GsonBuilder()
        .registerTypeHierarchyAdapter(JsonModel::class.java, JsonModel.Serializer)
        .setPrettyPrinting()
        .create()) {
    val result: Any
        get() = prototypes
    private val prototypes: MutableMap<String, JsonModel> = mutableMapOf()

    fun feed(label: String, json: String) {
        val parsedJson = gson.fromJson(json, JsonElement::class.java)
        val model = prototypes.getOrPut(label) { JsonModel.createFrom(parsedJson) }
        model.acceptData(parsedJson)
    }
}