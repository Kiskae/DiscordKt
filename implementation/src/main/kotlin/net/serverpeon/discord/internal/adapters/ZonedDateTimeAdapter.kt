package net.serverpeon.discord.internal.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ZonedDateTimeAdapter : TypeAdapter<ZonedDateTime>() {
    //2015-11-01T19:23:29.137000+00:00
    private val DISCORD_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun write(writer: JsonWriter, value: ZonedDateTime) {
        writer.value(value.format(DISCORD_FORMAT))
    }

    override fun read(reader: JsonReader): ZonedDateTime {
        return ZonedDateTime.parse(reader.nextString(), DISCORD_FORMAT)
    }
}