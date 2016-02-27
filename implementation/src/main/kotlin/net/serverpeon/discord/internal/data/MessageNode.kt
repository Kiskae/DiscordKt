package net.serverpeon.discord.internal.data

import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.internal.rest.data.MessageModel
import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.internal.rest.retro.Channels
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

class MessageNode(val root: DiscordNode,
                  override val postedAt: ZonedDateTime,
                  override val textToSpeech: Boolean,
                  override val lastEdited: ZonedDateTime?,
                  override val rawContent: String,
                  override val id: DiscordId<PostedMessage>,
                  override val author: User,
                  val channel: ChannelNode) : PostedMessage {
    override val content: Message by lazy { parse(rawContent, root) }

    override fun edit(): PostedMessage.Edit {
        if (author.id != root.self.id) {
            channel.checkPermission(PermissionSet.Permission.MANAGE_MESSAGES)
        }
        return Transaction(content)
    }

    override fun delete(): CompletableFuture<Void> {
        return root.api.Channels.deleteMessage(
                WrappedId(channel.id), WrappedId(id)
        ).toFuture()
    }

    inner class Transaction(override var content: Message) : PostedMessage.Edit {
        private var aborted: TransactionTristate = TransactionTristate.AWAIT

        override fun commit(): CompletableFuture<PostedMessage> {
            if (aborted == TransactionTristate.ABORTED) {
                throw Editable.AbortedTransactionException()
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                aborted = TransactionTristate.COMPLETED
                return root.api.Channels.editMessage(WrappedId(channel.id), WrappedId(id), Channels.EditMessageRequest(
                        content = content.encodedContent,
                        mentions = content.mentions.map { it.id }.toList().toBlocking().first()
                )).toFuture().thenApply { MessageNode.from(it, root) }
            }
        }

        override fun abort() {
            if (aborted == TransactionTristate.AWAIT) {
                aborted = TransactionTristate.ABORTED
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalArgumentException("äbort() after complete()")
            }
        }

    }

    companion object {
        fun from(model: MessageModel, root: DiscordNode): MessageNode {
            return MessageNode(
                    root,
                    model.timestamp,
                    model.tts,
                    model.edited_timestamp,
                    model.content,
                    model.id,
                    root.userCache.retrieve(model.author),
                    root.channelMap[model.channel_id]!!
            )
        }

        fun parse(content: String, root: DiscordNode): Message {
            //TODO: implement parsing of channel/user/emoji
            return Message.Builder().append(content).build()
        }
    }
}