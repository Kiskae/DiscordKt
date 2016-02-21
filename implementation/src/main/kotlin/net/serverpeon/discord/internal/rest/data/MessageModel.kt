package net.serverpeon.discord.internal.rest.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Message
import java.net.URI
import java.time.ZonedDateTime

data class MessageModel(val nonce: String?,
                        val attachments: List<Attachment>,
                        val tts: Boolean,
                        val embeds: List<OEmbed>,
                        val timestamp: ZonedDateTime,
                        val mention_everyone: Boolean,
                        val id: DiscordId<Message>,
                        val edited_timestamp: ZonedDateTime?,
                        val author: UserModel,
                        val content: String,
                        val channel_id: DiscordId<Channel>,
                        val mentions: List<JsonElement>) {
    data class Attachment(val width: Int?,
                          val url: URI,
                          val size: Int,
                          val proxy_url: URI,
                          override val id: DiscordId<Attachment>,
                          val height: Int?,
                          val filename: String) : DiscordId.Identifiable<Attachment>

    /**
     * @property video Provided if type == VIDEO
     * @property author Provided if type == ARTICLE
     */
    data class OEmbed(val type: Type,
                      val url: URI,
                      val title: String?,
                      val thumbnail: Thumbnail?,
                      val provider: Provider?,
                      val description: String?,
                      val video: Video?,
                      val author: Article?) {
        enum class Type {
            @SerializedName("video")
            VIDEO,
            @SerializedName("link")
            LINK,
            @SerializedName("article")
            ARTICLE,
        }

        data class Thumbnail(val width: Int, val url: URI, val proxy_url: URI, val height: Int)
        data class Provider(val name: String, val url: URI?)
        data class Video(val width: Int, val url: URI, val height: Int)
        data class Article(val name: String, val url: URI?)
    }
}