package net.serverpeon.discord.event

import net.serverpeon.discord.model.ClientModel

/**
 * Fired when the model has synchronized with Discord and access is available.
 *
 * Please be aware that guild membership relationships might still be populating for larger guilds; listen for
 * [MembersLoadedEvent] if that data needs to be loaded.
 *
 * Can be fired more than once if a reconnect happens in the background.
 */
interface ModelReadyEvent : Event {
    val model: ClientModel
}