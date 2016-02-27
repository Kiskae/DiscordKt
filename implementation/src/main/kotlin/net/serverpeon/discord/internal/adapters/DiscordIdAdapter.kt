package net.serverpeon.discord.internal.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.serverpeon.discord.model.DiscordId

object DiscordIdAdapter : TypeAdapter<DiscordId<*>>() {
    override fun write(writer: JsonWriter, value: DiscordId<*>) {
        writer.value(value.repr)
    }

    override fun read(reader: JsonReader): DiscordId<*> {
        return DiscordId<Dummy>(reader.nextString())
    }

    private interface Dummy : DiscordId.Identifiable<Dummy>
}