package net.serverpeon.discord.internal.rest

import net.serverpeon.discord.model.DiscordId

class WrappedId<T : DiscordId.Identifiable<T>>(val wrappedId: DiscordId<T>) {
    override fun toString(): String {
        return wrappedId.repr
    }
}