package net.serverpeon.discord.event

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Message

interface MessageEvent {
    val channel: Channel
    val messageId: DiscordId<Message>

    interface New : MessageEvent {
        val message: Message
    }

    interface Edit : MessageEvent {
        val message: Message
    }

    interface Deleted : MessageEvent
}