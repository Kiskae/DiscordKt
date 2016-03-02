package net.serverpeon.discord.internal.rest

import com.google.common.io.Resources
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.TypeAdapters
import net.serverpeon.discord.internal.adapters.*
import net.serverpeon.discord.internal.jsonmodels.ReadyEventModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import org.junit.Test
import java.awt.Color
import java.time.Duration
import java.time.ZonedDateTime

class ParseTest {
    val gson = GsonBuilder().apply {
        registerTypeAdapter(Color::class.java, ColorAdapter.nullSafe())
        registerTypeAdapterFactory(TypeAdapters.newFactory(DiscordId::class.java, DiscordIdAdapter.nullSafe()))
        registerTypeAdapter(Duration::class.java, DurationAdapter.nullSafe())
        registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter.nullSafe())
        registerTypeAdapter(PermissionSet::class.java, PermissionSetAdapter.nullSafe())
        setPrettyPrinting()
    }.create()

    @Test
    fun testReadyPacket() {
        val source = Resources.asCharSource(Resources.getResource("ws/ready.json"), Charsets.UTF_8)
        source.openBufferedStream().use {
            val data = gson.fromJson(it, ReadyEventModel::class.java)
            println(data.guilds[2].channels[0])
        }
    }
}