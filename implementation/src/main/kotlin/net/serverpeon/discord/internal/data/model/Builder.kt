package net.serverpeon.discord.internal.data.model

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.internal.data.combineMaps
import net.serverpeon.discord.internal.data.toImmutableIdMap
import net.serverpeon.discord.internal.jsonmodels.*
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild

internal object Builder {
    fun root(data: ReadyEventModel, api: ApiWrapper): DiscordNode {
        val primaryNode = DiscordNode(api)

        //SELF
        val whoami = primaryNode.userCache.retrieve(
                data.user.id,
                self(data.user, primaryNode)
        ) as SelfNode
        primaryNode.self = whoami

        //GUILDS
        val guilds = data.guilds.map { guild(it, primaryNode) }

        //PRIVATE_CHANNELS
        val privateChannels = data.private_channels.map { channel(it, primaryNode) }

        primaryNode.guildMap = guilds.toImmutableIdMap()
        primaryNode.channelMap = combineMaps(
                guilds.flatMap { it.channelMap.values }.toImmutableIdMap(), // Create a single addressable map of channels
                privateChannels.toImmutableIdMap()
        )

        //TODO: SETTINGS?

        return primaryNode
    }

    fun guild(data: GuildModel, root: DiscordNode): GuildNode {
        return GuildNode(root, data.id, data.name, data.owner_id, RegionNode(data.region))
    }

    fun guild(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
        val guildNode = GuildNode(root, data.id, data.name, data.owner_id, RegionNode(data.region))

        val presences = data.presences.map { it.user.id to it }.toMap()
        val voiceStates = data.voice_states.map { it.user_id to it }.toMap()

        guildNode.channelMap = data.channels.map { channel(it, guildNode) }.toImmutableIdMap()
        guildNode.roleMap = data.roles.map { role(it, guildNode) }.toImmutableIdMap()
        guildNode.memberMap = data.members.map {
            member(it, presences[it.user.id], voiceStates[it.user.id], guildNode)
        }.toImmutableIdMap()
        guildNode.emojiMap = data.emojis.map { emoji(it, guildNode) }.toImmutableIdMap()

        return guildNode
    }

    fun emoji(model: GuildModel.DataEmoji, guildNode: GuildNode): GuildNode.EmojiNode {
        return GuildNode.EmojiNode(
                ImmutableList.copyOf(model.roles.map { guildNode.roleMap[it]!! }),
                model.name,
                model.managed,
                model.id,
                model.require_colons
        )
    }

    fun channel(channel: ChannelModel, guild: GuildNode): ChannelNode.Public {
        return ChannelNode.Public(
                guild.root,
                channel.id,
                guild,
                channel.topic ?: "",
                if (channel.type == ChannelModel.Type.TEXT) Channel.Type.TEXT else Channel.Type.VOICE,
                channel.name,
                ChannelNode.translateOverrides(channel.permission_overwrites)
        )
    }

    fun channel(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode.Private {
        return ChannelNode.Private(
                root,
                privateChannel.id,
                root.userCache.retrieve(privateChannel.recipient)
        )
    }

    fun member(model: MemberModel,
               presence: ReadyEventModel.ExtendedGuild.Presence?,
               voiceState: VoiceStateModel?,
               guildNode: GuildNode): MemberNode {
        return MemberNode(
                guildNode,
                guildNode.root.userCache.retrieve(model.user.id, model.user),
                MemberNode.Companion.generateRoleList(model.roles, guildNode.roleMap),
                model.joined_at,
                presence?.status?.let { MemberNode.Companion.mapStatus(it) } ?: Guild.Member.Status.OFFLINE,
                presence?.game?.name,
                voiceState?.deaf ?: false,
                voiceState?.mute ?: false,
                voiceState?.self_deaf ?: false,
                voiceState?.self_mute ?: false
        )
    }

    fun message(model: MessageModel, root: DiscordNode): MessageNode {
        return MessageNode(
                root,
                model.timestamp,
                model.tts,
                model.edited_timestamp,
                model.content,
                model.id,
                root.userCache.retrieve(model.author),
                root.channelMap[model.channel_id]!!
        )
    }

    fun role(model: RoleModel, guild: GuildNode): RoleNode {
        return RoleNode(guild.root,
                guild,
                model.id,
                model.name,
                model.managed,
                model.hoist,
                model.permissions,
                model.color,
                model.position);
    }

    fun user(data: UserModel, node: DiscordNode): UserNode {
        return UserNode.Profile(
                node,
                data.id,
                data.username,
                data.discriminator,
                data.avatar
        )
    }

    fun self(self: SelfModel, root: DiscordNode): SelfNode {
        // Note: we ignore the 'verified' node since we're logged in, so we're always verified
        return SelfNode(
                root,
                self.id,
                self.username,
                self.discriminator,
                self.avatar,
                self.email
        )
    }
}