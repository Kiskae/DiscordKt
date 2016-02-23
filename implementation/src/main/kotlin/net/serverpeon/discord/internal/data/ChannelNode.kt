package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.inbound.PrivateChannelModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId

abstract class ChannelNode private constructor(val root: DiscordNode,
                                               override val id: DiscordId<Channel>) : Channel, Event.Visitor {
    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        // Ignored for now, how do we represent this
    }

    override fun typingStart(e: Misc.TypingStart) {
        // Ignored, not something we need to represent
    }

    override fun toString(): String {
        return "Channel(id=$id)"
    }

    class Public internal constructor(root: DiscordNode,
                                      id: DiscordId<Channel>,
                                      override val guild: GuildNode,
                                      override var topic: String,
                                      override val type: Channel.Type,
                                      override var name: String) : ChannelNode(root, id), Channel.Public {
        override val isPrivate: Boolean
            get() = false

        override fun channelUpdate(e: Channels.Update) {
            e.channel.let {
                this.name = it.name
                this.topic = it.topic ?: ""
            }
            //TODO: permission overrides
        }
    }

    class Private internal constructor(root: DiscordNode,
                                       id: DiscordId<Channel>,
                                       override val recipient: UserNode) : ChannelNode(root, id), Channel.Private {
        override val type: Channel.Type
            get() = Channel.Type.PRIVATE
        override val isPrivate: Boolean
            get() = true
    }

    companion object {
        fun from(channel: ChannelModel, guild: GuildNode, root: DiscordNode): ChannelNode.Public {
            return ChannelNode.Public(
                    root,
                    channel.id,
                    guild,
                    channel.topic ?: "",
                    if (channel.type == ChannelModel.Type.TEXT) Channel.Type.TEXT else Channel.Type.VOICE,
                    channel.name
            )
        }

        fun from(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode.Private {
            return ChannelNode.Private(
                    root,
                    privateChannel.id,
                    root.userCache.retrieve(privateChannel.recipient)
            )
        }
    }
}