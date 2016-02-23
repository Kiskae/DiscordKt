package net.serverpeon.discord.model

/**
 * Discord identifies resources through a unique string id.
 * This class acts as a type-safe wrapper for the various id's present in the model.
 */
data class DiscordId<T : DiscordId.Identifiable<T>>(val repr: String) {
    interface Identifiable<T : Identifiable<T>> {
        /**
         * Discord uniquely identifies this resource with this ID.
         */
        val id: DiscordId<T>
    }
}