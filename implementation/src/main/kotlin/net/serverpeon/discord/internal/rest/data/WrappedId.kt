package net.serverpeon.discord.internal.rest.data

import net.serverpeon.discord.model.DiscordId

class WrappedId<T : DiscordId.Identifiable<T>>(val wrappedId: DiscordId<T>) {
    override fun toString(): String {
        return wrappedId.id
    }
}