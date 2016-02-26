package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.MessageModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage

interface Messages {
    data class Create(val msg: MessageModel) : Messages
    //TODO: Update data is partial, should be separate w/ full null fields
    data class Update(val msg: MessageModel) : Messages

    data class Delete(val id: DiscordId<PostedMessage>, val channel_id: DiscordId<Channel>) : Messages
    data class Acknowledge(val message_id: DiscordId<PostedMessage>, val channel_id: DiscordId<Channel>) : Messages
}