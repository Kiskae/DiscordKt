package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.data.model.ChannelNode
import net.serverpeon.discord.internal.jsonmodels.ChannelModel
import net.serverpeon.discord.internal.jsonmodels.PrivateChannelModel

interface Channels : Event {
    interface Create : Channels {
        data class Public(val channel: ChannelModel) : Create {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.channelCreate(visitor, this)
        }

        data class Private(val channel: PrivateChannelModel) : Create {
            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.channelCreate(visitor, this)
        }
    }

    data class Update(val channel: ChannelModel) : Channels {
        override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                = handler.channelUpdate(visitor, this)
    }

    interface Delete : Channels {
        data class Public(val channel: ChannelModel) : Delete, Event.RefHolder<ChannelNode.Public> {
            override var value: ChannelNode.Public? = null

            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.channelDelete(visitor, this)
        }

        data class Private(val channel: PrivateChannelModel) : Delete, Event.RefHolder<ChannelNode.Private> {
            override var value: ChannelNode.Private? = null

            override fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
                    = handler.channelDelete(visitor, this)
        }
    }
}