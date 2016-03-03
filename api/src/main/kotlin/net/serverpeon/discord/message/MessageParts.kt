package net.serverpeon.discord.message

import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Emoji
import net.serverpeon.discord.model.User

internal object MessageParts {
    class CodeBlock(val code: String, val language: String) : Part {
        override fun prettyRepr(): String {
            return "[CodeBlock|$language]"
        }

        override fun encodedRepr(): String {
            return """```${language.trim()}
            $code
            """
        }
    }

    class ChannelMention(val channel: Channel.Public) : Part {
        override fun prettyRepr(): String {
            return "[#${channel.name}]"
        }

        override fun encodedRepr(): String {
            return "<#${channel.id.repr}>"
        }
    }

    object MentionEveryone : Part {
        override fun encodedRepr(): String {
            return "@everyone"
        }

        override fun prettyRepr(): String {
            return "[@everyone]"
        }
    }

    class GuildEmoji(val emoji: Emoji) : Part {
        override fun prettyRepr(): String {
            return "[Emoji|${emoji.name}]"
        }

        override fun encodedRepr(): String {
            return "<:${emoji.name}:${emoji.id.repr}>"
        }
    }

    class UserMention(val user: User) : Part {
        override fun prettyRepr(): String {
            return "[@${user.username}/${user.discriminator}]"
        }

        override fun encodedRepr(): String {
            return "<@${user.id.repr}>"
        }
    }

    class TextEmoji(val codePoint: Int) : Part {
        override fun prettyRepr(): String {
            return "[Emoji|$codePoint]"
        }

        override fun encodedRepr(): String {
            return String(IntArray(2).apply {
                this[0] = 55357
                this[1] = codePoint
            }, 0, 2)
        }
    }

    class Text(val str: String) : Part {
        override fun prettyRepr(): String = str

        override fun encodedRepr(): String = str
    }

    interface Part {
        fun prettyRepr(): String

        fun encodedRepr(): String
    }
}