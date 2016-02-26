package net.serverpeon.discord.message

import net.serverpeon.discord.model.User
import rx.Observable

interface Message {
    val mentions: Observable<User>

    //How to present content... (possibly sanitize?)
    val content: String

    interface Builder {

    }
}