package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.jsonmodels.PrivateChannelModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User
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

    @POST("users/{id}/channels")
    fun createPrivateChannel(@Path("id") myId: WrappedId<User>,
                             @Body data: PrivateChannelCreate): Call<PrivateChannelModel>

    data class PrivateChannelCreate(val recipient_id: DiscordId<User>)

    @GET("users/{user_id}/avatars/{avatar_id}.jpg")
    @Streaming
    @Deprecated("Unfinished")
    fun getAvatar()
}