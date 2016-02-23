package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.rest.data.ChannelModel

interface Channels : Event {
    interface Create : Channels {
        data class Public(val channel: ChannelModel) : Create {
            override fun accept(visitor: Event.Visitor) = visitor.channelCreate(this)
        }

        data class Private(val channel: PrivateChannelModel) : Create {
            override fun accept(visitor: Event.Visitor) = visitor.channelCreate(this)
        }
    }

    data class Update(val channel: ChannelModel) : Channels {
        override fun accept(visitor: Event.Visitor) = visitor.channelUpdate(this)
    }

    interface Delete : Channels {
        data class Public(val channel: ChannelModel) : Delete {
            override fun accept(visitor: Event.Visitor) = visitor.channelDelete(this)
        }
        data class Private(val channel: PrivateChannelModel) : Delete {
            override fun accept(visitor: Event.Visitor) = visitor.channelDelete(this)
        }
    }
}