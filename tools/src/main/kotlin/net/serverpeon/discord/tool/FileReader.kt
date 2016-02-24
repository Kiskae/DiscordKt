package net.serverpeon.discord.tool

import com.google.common.io.LineProcessor

class FileReader(val mapper: (String) -> TaggedField?) : LineProcessor<String> {
    private val analyzer = JsonAnalyzer()

    override fun processLine(line: String): Boolean {
        mapper(line)?.let { tagged ->
            analyzer.feed(tagged.tag, tagged.json)
        }
        return true
    }

    override fun getResult(): String {
        return analyzer.gson.toJson(analyzer.result)
    }

    data class TaggedField(val tag: String, val json: String)
}