package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.*
import rx.Observable

class GuildNode(val root: DiscordNode, override val id: DiscordId<Guild>) : Guild, Event.Visitor {
    var channels = createEmptyMap<Channel, ChannelNode>()
    var roles = createEmptyMap<Role, RoleNode>()
    var members = createEmptyMap<User, MemberNode>()
    var emojis = createEmptyMap<Emoji, EmojiNode>()

    override fun channelCreate(e: Channels.Create.Public) {
        check(e.channel.id !in channels) { "Double channel creation: $e" }

        val channel = ChannelNode.from(e.channel, root)
        channels = channels.immutableAdd(channel.id, channel)
    }

    override fun guildUpdate(e: Guilds.General.Update) {
        //TODO: what needs to be updated
    }

    override fun guildMemberAdd(e: Guilds.Members.Add) {
        check(e.member.user.id !in members) { "Redundant member adds: $e" }

        val newMember = MemberNode.from(e.member, this, root)
        members = members.immutableAdd(newMember.id, newMember)
    }

    override fun guildMemberUpdate(e: Guilds.Members.Update) = wireToUser(e.member.user.id, e)

    override fun guildMemberRemove(e: Guilds.Members.Remove) {
        check(e.member.user.id in members) { "Trying to remove non-existent member: $e" }
        members = members.immutableRemove(e.member.user.id)
    }

    override fun guildBanAdd(e: Guilds.Bans.Add) {
        // Ignored for now
    }

    override fun guildBanRemove(e: Guilds.Bans.Remove) {
        // Ignored for now
    }

    override fun guildRoleCreate(e: Guilds.Roles.Create) {
        check(e.role.id !in roles) { "Duplicate role create $e" }

        val role = RoleNode.from(e.role, root)
        roles = roles.immutableAdd(role.id, role)
    }

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        roles[e.role.id]!!.visit(e)
    }

    override fun guildRoleDelete(e: Guilds.Roles.Delete) {
        check(e.role_id in roles) { "Attempt to remove non-existent role: $e" }
        members.values.forEach { it.visit(e) }
        roles = roles.immutableRemove(e.role_id)
    }

    override fun guildEmojiUpdate(e: Guilds.EmojiUpdate) {
        emojis = e.emojis.map { parseEmoji(it, this) }.toImmutableIdMap()
    }

    override fun guildIntegrationsUpdate(e: Guilds.IntegrationsUpdate) {
        // Only a notification, ignored for now
    }

    override fun presenceUpdate(e: Misc.PresenceUpdate) = wireToUser(e.user.id, e)

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        //FIXME: How the hell are we going to do this?
        super.voiceStateUpdate(e)
    }

    override fun wireToUser(id: DiscordId<User>, e: Event) {
        members[id]?.visit(e)
    }

    override fun channelDelete(e: Channels.Delete.Public) {
        check(e.channel.id in channels) { "Removing non-existent channel: $e" }
        channels.immutableRemove(e.channel.id)
    }

    override fun toString(): String {
        return "Guild(id=$id, channels=${channels.values}, roles=${roles.values}, membersNo=${members.size})"
    }

    class EmojiNode(
            val roles: List<Role>,
            override val name: String,
            override val imported: Boolean,
            override val id: DiscordId<Emoji>) : Emoji {
        override val restrictedTo: Observable<Role>
            get() = Observable.defer { Observable.from(roles) }

        override fun toString(): String {
            return "Emoji(id=$id, name='$name')"
        }
    }

    companion object {
        private fun parseEmoji(model: GuildModel.DataEmoji, guildNode: GuildNode): EmojiNode {
            return EmojiNode(
                    ImmutableList.copyOf(model.roles.map { guildNode.roles[it]!! }),
                    model.name,
                    model.managed,
                    model.id
            )
        }

        fun from(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
            val guildNode = GuildNode(root, data.id)

            guildNode.channels = data.channels.map { ChannelNode.from(it, root) }.toImmutableIdMap()
            guildNode.roles = data.roles.map { RoleNode.from(it, root) }.toImmutableIdMap()
            guildNode.members = data.members.map { MemberNode.from(it, guildNode, root) }.toImmutableIdMap()
            guildNode.emojis = data.emojis.map { parseEmoji(it, guildNode) }.toImmutableIdMap()

            return guildNode
        }
    }
}