package net.serverpeon.discord.internal

import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.TypeAdapters
import net.serverpeon.discord.internal.rest.adapters.*
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.rxObservable
import net.serverpeon.discord.internal.ws.client.DiscordWebsocket
import net.serverpeon.discord.internal.ws.data.outbound.ConnectMsg
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import okhttp3.OkHttpClient
import java.awt.Color
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch

fun main(args: Array<String>) {
    val token = args[0]
    val gson = GsonBuilder().apply {
        registerTypeAdapter(Color::class.java, ColorAdapter.nullSafe())
        registerTypeAdapterFactory(TypeAdapters.newFactory(DiscordId::class.java, DiscordIdAdapter.nullSafe()))
        registerTypeAdapter(Duration::class.java, DurationAdapter.nullSafe())
        registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter.nullSafe())
        registerTypeAdapter(PermissionSet::class.java, PermissionSetAdapter.nullSafe())
    }.create()

    val api = ApiWrapper(OkHttpClient(), gson, token)
    val latch = CountDownLatch(1)
    val logger = api.createLogger()

    api.Gateway.wsEndpoint().rxObservable().flatMap { endpoint ->
        DiscordWebsocket.create(ConnectMsg(
                token = token,
                v = 3,
                properties = ConnectMsg.Properties(
                        operatingSystem = System.getProperty("os.name"),
                        device = System.getProperty("os.arch"),
                        browser = "DiscordKt",
                        referrer = "",
                        referrerDomain = ""
                ),
                large_threshold = 100,
                compress = true
        ), endpoint.url, gson).doAfterTerminate {
            latch.countDown()
        }
    }.subscribe({
        logger.kDebug { "[${latch.count}] ${it.event}" }
    }, {
        logger.kDebug(it) { "Error in event stream" }
    }, {
        logger.kDebug { "Event stream closed" }
    })

    latch.await()
}