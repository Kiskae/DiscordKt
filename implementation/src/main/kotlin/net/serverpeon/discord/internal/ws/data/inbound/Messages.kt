package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.jsonmodels.MessageModel
import net.serverpeon.discord.internal.jsonmodels.UserModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import java.time.ZonedDateTime

interface Messages {
    data class Create(val msg: MessageModel) : Messages

    data class Update(val msg: PartialMessageModel) : Messages

    data class PartialMessageModel(
            val id: DiscordId<PostedMessage>,
            val nonce: String?,
            val attachments: List<MessageModel.Attachment>?,
            val tts: Boolean?,
            val embeds: List<MessageModel.OEmbed>,
            val timestamp: ZonedDateTime?,
            val mention_everyone: Boolean?,
            val edited_timestamp: ZonedDateTime?,
            val author: UserModel?,
            val content: String?,
            val channel_id: DiscordId<Channel>,
            val mentions: List<UserModel>?)

    data class Delete(val id: DiscordId<PostedMessage>, val channel_id: DiscordId<Channel>) : Messages
    data class Acknowledge(val message_id: DiscordId<PostedMessage>, val channel_id: DiscordId<Channel>) : Messages
}