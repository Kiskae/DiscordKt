package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.PermissionSet

/**
 * Fired when the user (or a moderator with the [PermissionSet.Permission.MANAGE_MESSAGES]) deletes a message.
 */
interface MessageDeleteEvent : Event {
    val channel: Channel.Text

    val deletedMessageId: DiscordId<PostedMessage>
}