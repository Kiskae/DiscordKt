package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.internal.jsonmodels.MessageModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Channels
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.message.Message
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

class MessageNode(val root: DiscordNode,
                  override val postedAt: ZonedDateTime,
                  override val textToSpeech: Boolean,
                  override val lastEdited: ZonedDateTime?,
                  override val rawContent: String,
                  override val id: DiscordId<PostedMessage>,
                  override val author: User,
                  override val channel: ChannelNode<*>) : PostedMessage {
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

    override fun toString(): String {
        return "Message(postedAt=$postedAt, tts=$textToSpeech, lastEdited=${lastEdited ?: postedAt}, rawContent='$rawContent', id=$id, author=$author, channel=$channel)"
    }

    inner class Transaction(override var content: Message) : PostedMessage.Edit {
        private var completed = AtomicBoolean(false)

        override fun commit(): CompletableFuture<PostedMessage> {
            if (completed.getAndSet(true)) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                return root.api.Channels.editMessage(WrappedId(channel.id), WrappedId(id), Channels.EditMessageRequest(
                        content = content.encodedContent,
                        mentions = content.mentions.map { it.id }.toList().toBlocking().first()
                )).toFuture().thenApply { Builder.message(it, root) }
            }
        }
    }

    companion object {
        fun create(model: MessageModel, root: DiscordNode) = Builder.message(model, root)

        private fun parse(content: String, root: DiscordNode): Message {
            val len = content.length
            var i = 0
            val builder = Message.Builder()
            while (i < len) {
                val specialEsc = content.indexOf('<', i)
                val codeBlockEsc = content.indexOf("```", i)
                if (isBefore(specialEsc, listOf(codeBlockEsc))) {
                    // Opening <, check next character
                    if (specialEsc + 1 < len) {
                        i = when (content[specialEsc + 1]) {
                            '#' -> {
                                parseTag(i, specialEsc, content, builder) { str, b ->
                                    //                                      cut off @
                                    val channel = root.channelMap[DiscordId(str.substring(1))]
                                    if (channel is ChannelNode.Public) {
                                        b.append(channel)
                                    } else {
                                        b.append(str)
                                    }
                                }
                            }
                            '@' -> {
                                parseTag(i, specialEsc, content, builder) { str, b ->
                                    val user = root.userCache.retrieve(DiscordId(str.substring(1)))
                                    if (user != null) {
                                        b.append(user)
                                    } else {
                                        b.append(str)
                                    }
                                }
                            }
                            else -> {
                                builder.append(content.substring(i..specialEsc))
                                specialEsc + 1
                            }
                        }
                    } else {
                        // Append final part of the string
                        builder.append(content.substring(i))
                        i = len
                    }
                } else if (isBefore(codeBlockEsc, listOf(specialEsc))) {
                    if (codeBlockEsc + 3 < len) {
                        val closingChars = content.indexOf("```", codeBlockEsc + 4)
                        if (closingChars != -1) {
                            val code = content.substring((codeBlockEsc + 3)..(closingChars - 1))
                            val parts = code.split(' ', limit = 2)
                            if (parts.size == 2) {
                                builder.appendCodeBlock(parts[1], parts[0])
                            } else {
                                builder.appendCodeBlock(parts[0], "unknown")
                            }
                            i = closingChars + 3
                        } else {
                            //Just append all the text until after the ```
                            builder.append(content.substring(i..(codeBlockEsc + 2)))
                            i = codeBlockEsc + 3
                        }
                    } else {
                        //Just append all the text until after the ```
                        builder.append(content.substring(i..(codeBlockEsc + 2)))
                        i = codeBlockEsc + 3
                    }
                } else {
                    // No tags left, just append the test of the string
                    builder.append(content.substring(i))
                    i = len
                }
            }

            return builder.build()
        }

        private fun parseTag(previousIndex: Int, locationOfBracket: Int, source: String, builder: Message.Builder,
                             handler: (String, Message.Builder) -> Unit): Int {
            //First append everything before the location bracket
            builder.append(source.substring(previousIndex..(locationOfBracket - 1)))
            val closingBracket = source.indexOf('>', locationOfBracket + 1)
            return if (closingBracket != -1) {
                handler(source.substring((locationOfBracket + 1)..(closingBracket - 1)), builder)
                closingBracket + 1
            } else {
                // Abort, just retroactively append the <
                builder.append("<")
                locationOfBracket + 1
            }
        }

        private fun isBefore(index: Int, otherIndexes: List<Int>): Boolean {
            return if (index != -1) {
                otherIndexes.all { it == -1 || index < it }
            } else {
                false
            }
        }
    }
}