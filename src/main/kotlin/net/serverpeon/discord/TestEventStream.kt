package net.serverpeon.discord

import com.google.common.eventbus.Subscribe
import net.serverpeon.discord.event.Event

fun main(args: Array<String>) {
    val token = System.getenv("DISCORD_TOKEN")
    val email = System.getenv("DISCORD_EMAIL")
    val password = System.getenv("DISCORD_PASSWORD")

    val client = DiscordClient.newBuilder().apply {
        if (token != null) {
            token(token)
        } else {
            login(email, password)
        }
    }.retries(0).build()

    client.eventBus().register(object {
        @Subscribe
        fun event(ev: Event) {
            println(ev)
        }
    })

    client.startEmittingEvents()

    client.closeFuture().await()
}