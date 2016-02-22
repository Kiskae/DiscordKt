package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.UserModel
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

interface UserNode : User, EventProcessor {
    override var username: String
    override var discriminator: String
    override var avatar: DiscordId<User.Avatar>?

    companion object {
        fun from(data: UserModel): UserNode {
            return Profile(
                    data.id,
                    data.username,
                    data.discriminator,
                    data.avatar
            )
        }
    }

    data class Profile(override val id: DiscordId<User>,
                       override var username: String,
                       override var discriminator: String,
                       override var avatar: DiscordId<User.Avatar>?) : UserNode {
        override fun acceptEvent(event: Any) {
            when (event) {
                is Misc.PresenceUpdate -> {
                    event.user.let {
                        it.username?.let { this.username = it }
                        it.discriminator?.let { this.discriminator = it }
                        it.avatar?.let { this.avatar = it }
                    }
                }
            }
        }
    }
}