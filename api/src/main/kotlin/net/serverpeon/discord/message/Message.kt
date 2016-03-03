package net.serverpeon.discord.message

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Emoji
import net.serverpeon.discord.model.User
import rx.Observable

interface Message {
    val mentions: Observable<User>

    val encodedContent: String

    val content: String

    class Builder {
        private val parts: MutableList<MessageParts.Part> = mutableListOf()
        private val mentions: MutableList<User> = mutableListOf()

        /**
         * Appends the given string to the message without modification.
         */
        fun append(str: String): Builder {
            parts.add(MessageParts.Text(str))
            return this
        }

        /**
         * Inserts the given custom emoji into the message.
         *
         * These emoji can be obtained through the Guild that owns them.
         */
        fun append(emoji: Emoji): Builder {
            parts.add(MessageParts.GuildEmoji(emoji))
            return this
        }

        /**
         * Appends a mention of the given user into the message and instructs Discord to alert them.
         *
         * Use [appendNoAlert] if the mentioned user should not be alerted.
         */
        fun append(user: User): Builder {
            parts.add(MessageParts.UserMention(user))
            mentions.add(user)
            return this
        }

        /**
         * Appends a mention of the given channel into the message. If the channel is not a part of the same guild
         * as the channel that the message is sent to then this will fail.
         */
        fun append(channel: Channel.Public): Builder {
            parts.add(MessageParts.ChannelMention(channel))
            return this
        }

        /**
         * Appends a code block of the given language.
         *
         * A limited number of languages will have specialized syntax highlighting through this method.
         */
        fun appendCodeBlock(code: String, language: String): Builder {
            parts.add(MessageParts.CodeBlock(code, language))
            return this
        }

        /**
         * Appends a mention to everyone in the channel through the `@everyone` mention.
         *
         * Depending on permissions this might do nothing besides add the text to the message.
         */
        fun appendMentionEveryone(): Builder {
            parts.add(MessageParts.MentionEveryone)
            return this
        }

        /**
         * Appends a mention to a user but does not alert them to the mention.
         */
        fun appendNoAlert(user: User): Builder {
            parts.add(MessageParts.UserMention(user))
            return this
        }

        /**
         * Create a message that is ready for sending.
         */
        fun build(): Message {
            return Impl(ImmutableList.copyOf(parts), ImmutableList.copyOf(mentions))
        }
    }

    private class Impl(val parts: List<MessageParts.Part>, val innerMentions: List<User>) : Message {
        override val mentions: Observable<User> = Observable.from(innerMentions)
        override val encodedContent: String
            get() = parts.fold(StringBuilder()) { sb, part ->
                sb.append(part.encodedRepr())
            }.toString()
        override val content: String
            get() = parts.fold(StringBuilder()) { sb, part ->
                sb.append(part.prettyRepr())
            }.toString()

        override fun toString(): String = content
    }
}