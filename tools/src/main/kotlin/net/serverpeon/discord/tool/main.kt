package net.serverpeon.discord.tool

import com.google.common.io.Files
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.regex.Pattern

fun main(args: Array<String>) {
    val pattern = Pattern.compile(".+?\\[([^\\]]+?)\\] (\\{.+)$")
    val result = Files.readLines(File(args[0]), Charset.defaultCharset(), FileReader {
        val matcher = pattern.matcher(it)
        if (matcher.matches()) {
            FileReader.TaggedField(matcher.group(1), matcher.group(2))
        } else {
            null
        }
    })
    Files.asCharSink(Paths.get(args[0]).let {
        it.resolveSibling("${it.fileName}.model.json")
    }.toFile(), Charset.defaultCharset())
            .openBufferedStream()
            .use {
                it.write(result)
            }
}