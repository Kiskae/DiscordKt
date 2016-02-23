package net.serverpeon.discord.model

interface Channel : DiscordId.Identifiable<Channel> {
    val topic: String
    val isPrivate: Boolean

    //TODO: potentially make sub interfaces for Public/Private split
}