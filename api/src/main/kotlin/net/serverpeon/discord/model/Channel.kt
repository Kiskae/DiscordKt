package net.serverpeon.discord.model

interface Channel : DiscordId.Identifiable<Channel> {
    val isPrivate: Boolean

    interface Public : Channel {
        val guild: Guild
        val topic: String

    }

    interface Private : Channel {
        val recipient: User
    }
}