package net.serverpeon.discord.event

import net.serverpeon.discord.model.ClientModel

/**
 * Fired when all members have been loaded into the model.
 *
 * This will always be emitted after [ModelReadyEvent] and will only be delayed if the client is a members of a large
 * server.
 */
interface MembersLoadedEvent : Event {
    val model: ClientModel
}