package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.inbound.PrivateChannelModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId

class ChannelNode(val root: DiscordNode,
                  override val id: DiscordId<Channel>,
                  override var topic: String,
                  private val recipient: UserNode?,
                  private val guild: GuildNode?) : Channel, Event.Visitor {
    override val isPrivate: Boolean
        get() = recipient != null

    override fun channelUpdate(e: Channels.Update) {
        //TODO: update metadata, probably overrides
    }

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        // Ignored for now, how do we represent this
    }

    override fun typingStart(e: Misc.TypingStart) {
        // Ignored, not something we need to represent
    }

    override fun toString(): String {
        return "Channel(id=$id)"
    }

    companion object {
        fun from(channel: ChannelModel, guild: GuildNode, root: DiscordNode): ChannelNode {
            return ChannelNode(
                    root,
                    channel.id,
                    channel.topic ?: "",
                    null, // Public channels don't have a direct recipient
                    guild
            )
        }

        fun from(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode {
            return ChannelNode(
                    root,
                    privateChannel.id,
                    "", // Private channels don't have topics
                    root.userCache.retrieve(privateChannel.recipient),
                    null // Private channels are not associated with a guild
            )
        }
    }
}