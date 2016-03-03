package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage

/**
 *
 */
interface MessageDeleteEvent : Event {
    val channel: Channel.Text

    val deletedMessageId: DiscordId<PostedMessage>
}