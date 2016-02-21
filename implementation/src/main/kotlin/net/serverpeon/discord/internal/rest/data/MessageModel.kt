package net.serverpeon.discord.internal.rest.data

import com.google.gson.JsonElement
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Message
import java.time.ZonedDateTime

data class MessageModel(val nonce: String?,
                        val attachments: List<JsonElement>,
                        val tts: Boolean,
                        val embeds: List<JsonElement>,
                        val timestamp: ZonedDateTime,
                        val mention_everyone: Boolean,
                        val id: DiscordId<Message>,
                        val edited_timestamp: ZonedDateTime?,
                        val author: UserModel,
                        val content: String,
                        val channel_id: DiscordId<Channel>,
                        val mentions: List<JsonElement>) {
}