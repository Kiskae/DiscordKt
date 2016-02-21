package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.model.Guild
import retrofit2.Call
import retrofit2.http.*

interface Users {
    interface Me {
        @DELETE("users/@me/guilds/{id}")
        fun leaveGuild(@Path("id") id: WrappedId<Guild>): Call<Void>

        @GET("users/@me/guilds")
        @Deprecated("Unfinished")
        fun getGuilds()

        @POST("users/@me/channels")
        @Deprecated("Unfinished")
        fun createPrivateChannel()

        @PATCH("users/@me")
        @Deprecated("Unfinished")
        fun editProfile()
    }

    @GET("users/{user_id}/avatars/{avatar_id}.jpg")
    @Streaming
    @Deprecated("Unfinished")
    fun getAvatar()
}