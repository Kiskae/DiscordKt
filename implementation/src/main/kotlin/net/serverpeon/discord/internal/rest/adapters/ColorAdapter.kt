package net.serverpeon.discord.internal.rest.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.awt.Color

object ColorAdapter : TypeAdapter<Color>() {
    override fun read(reader: JsonReader): Color {
        return Color(reader.nextInt())
    }

    override fun write(writer: JsonWriter, value: Color) {
        writer.value(value.rgb)
    }
}