package net.serverpeon.discord

import com.google.common.eventbus.Subscribe
import net.serverpeon.discord.event.Event
import net.serverpeon.discord.event.message.MessageCreateEvent
import net.serverpeon.discord.model.Channel
import java.util.concurrent.atomic.AtomicInteger

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

    val counter = AtomicInteger(0)
    client.eventBus().register(object {
        @Subscribe
        fun event(ev: Event) {
            if (ev is MessageCreateEvent) return
            println("Event[${counter.andIncrement}] - $ev")
        }

        @Subscribe
        fun printMessage(ev: MessageCreateEvent) {
            println("[${formatChannel(ev.channel)}] ${ev.message.author.username}: ${ev.message.content}")
        }
    })

    client.startEmittingEvents()

    client.closeFuture().await()
}

fun formatChannel(channel: Channel): String {
    return if (channel is Channel.Public) {
        "${channel.guild.name}#${channel.name}"
    } else {
        (channel as Channel.Private).let {
            "#${channel.recipient.username}"
        }
    }
}