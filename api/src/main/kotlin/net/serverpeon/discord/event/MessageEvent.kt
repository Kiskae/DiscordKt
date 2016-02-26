package net.serverpeon.discord.event

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage

interface MessageEvent {
    val channel: Channel
    val messageId: DiscordId<PostedMessage>

    interface New : MessageEvent {
        val message: PostedMessage
    }

    interface Edit : MessageEvent {
        val message: PostedMessage
    }

    interface Deleted : MessageEvent
}