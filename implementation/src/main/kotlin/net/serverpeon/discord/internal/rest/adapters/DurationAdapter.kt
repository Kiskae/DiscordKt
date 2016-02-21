package net.serverpeon.discord.internal.rest.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Duration

object DurationAdapter : TypeAdapter<Duration>() {
    override fun write(writer: JsonWriter, value: Duration) {
        writer.value(value.seconds)
    }

    override fun read(reader: JsonReader): Duration {
        return Duration.ofSeconds(reader.nextLong())
    }
}