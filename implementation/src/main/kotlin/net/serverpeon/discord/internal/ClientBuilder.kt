package net.serverpeon.discord.internal

import net.serverpeon.discord.DiscordClient

class ClientBuilder : DiscordClient.Builder {
    override fun build(): DiscordClient {
        throw UnsupportedOperationException()
    }
}