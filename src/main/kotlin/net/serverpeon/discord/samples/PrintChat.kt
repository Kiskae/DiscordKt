package net.serverpeon.discord.samples

import com.google.common.eventbus.Subscribe
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.event.message.MessageCreateEvent
import net.serverpeon.discord.model.Channel

fun main(args: Array<String>) {
    val client = DiscordClient.newBuilder().build()

    client.eventBus().register(object {
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