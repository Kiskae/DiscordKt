package net.serverpeon.discord.internal.ws.data.event

import net.serverpeon.discord.internal.rest.data.MessageModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Message

interface Messages {
    data class Create(val msg: MessageModel) : Messages
    data class Update(val msg: MessageModel) : Messages
    data class Delete(val id: DiscordId<Message>, val channel_id: DiscordId<Channel>) : Messages
    data class Acknowledge(val message_id: DiscordId<Message>, val channel_id: DiscordId<Channel>) : Messages
}