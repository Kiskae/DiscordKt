package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.UserModel

open class UserNode {
    companion object {
        fun from(data: UserModel): UserNode {
            throw UnsupportedOperationException()
        }
    }
}