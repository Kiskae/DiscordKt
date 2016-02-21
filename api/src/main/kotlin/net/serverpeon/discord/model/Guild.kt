package net.serverpeon.discord.model

interface Guild : DiscordId.Identifiable<Guild> {

    interface Member : User {
    }
}