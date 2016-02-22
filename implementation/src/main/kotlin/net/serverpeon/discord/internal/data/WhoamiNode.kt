package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.SelfModel
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.User

data class WhoamiNode(val root: DiscordNode,
                      override val id: DiscordId<User>,
                      override var username: String,
                      override var discriminator: String,
                      override var avatar: DiscordId<User.Avatar>?,
                      var email: String) : UserNode {
    override fun acceptEvent(event: Any) {
        when (event) {
            is Misc.UserUpdate -> {
                event.self.let {
                    this.username = it.username
                    this.discriminator = it.discriminator
                    this.avatar = it.avatar
                    this.email = it.email
                }
            }
        }
    }

    companion object {
        fun from(self: SelfModel, root: DiscordNode): WhoamiNode {
            // Note: we ignore the 'verified' node since we're logged in, so we're always verified
            return WhoamiNode(
                    root,
                    self.id,
                    self.username,
                    self.discriminator,
                    self.avatar,
                    self.email
            )
        }
    }
}