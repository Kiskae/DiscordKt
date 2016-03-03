package net.serverpeon.discord.event

import net.serverpeon.discord.model.ClientModel

/**
 * Fired when the model has synchronized with Discord and access is available.
 *
 * Can be fired more than once when a reconnect or server move happens in the background.
 */
interface ModelReadyEvent : Event {
    val model: ClientModel
}