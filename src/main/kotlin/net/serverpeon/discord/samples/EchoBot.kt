package net.serverpeon.discord.samples

import com.google.common.eventbus.Subscribe
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.event.message.MessageCreateEvent
import net.serverpeon.discord.message.Message

fun main(args: Array<String>) {
    val client = DiscordClient.newBuilder().build()

    client.eventBus().register(object {
        @Subscribe
        fun echoMessage(ev: MessageCreateEvent) {
            if (ev.message.rawContent.startsWith("!echo ")) {
                ev.channel.sendMessage(Message.Builder()
                        .append(ev.message.author)
                        .append(" ")
                        .append(ev.message.rawContent.substring(6))
                        .build())
            }
        }
    })

    client.startEmittingEvents()

    client.closeFuture().await()
}