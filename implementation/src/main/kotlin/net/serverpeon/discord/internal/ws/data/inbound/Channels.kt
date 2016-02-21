package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.ChannelModel

interface Channels {
    interface Create : Channels {
        data class Public(val channel: ChannelModel) : Create
        data class Private(val channel: PrivateChannelModel) : Create
    }

    data class Update(val channel: ChannelModel) : Channels

    interface Delete : Channels {
        data class Public(val channel: ChannelModel) : Delete
        data class Private(val channel: PrivateChannelModel) : Delete
    }
}