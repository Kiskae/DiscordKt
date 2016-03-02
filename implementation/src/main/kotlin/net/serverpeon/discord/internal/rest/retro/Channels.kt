package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.jsonmodels.ChannelModel
import net.serverpeon.discord.internal.jsonmodels.MessageModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.model.*
import retrofit2.Call
import retrofit2.http.*

interface Channels {
    @PATCH("channels/{channel_id}")
    fun editChannel(@Path("channel_id") id: WrappedId<Channel>,
                    @Body data: EditChannelRequest): Call<ChannelModel>

    data class EditChannelRequest(val name: String, val position: Int, val topic: String)

    @DELETE("channels/{channel_id}")
    fun deleteChannel(@Path("channel_id") id: WrappedId<Channel>): Call<Void>

    @POST("channels/{channel_id}/typing")
    fun postActivity(@Path("channel_id") id: WrappedId<Channel>): Call<Void>

    @GET("channels/{channel_id}/messages")
    fun getMessages(@Path("channel_id") id: WrappedId<Channel>,
                    @Query("before") before: WrappedId<PostedMessage>? = null,
                    @Query("after") after: WrappedId<PostedMessage>? = null,
                    @Query("limit") limit: Int? = null): Call<List<MessageModel>>

    @POST("channels/{channel_id}/messages")
    fun sendMessage(@Path("channel_id") id: WrappedId<Channel>,
                    @Body data: SendMessageRequest): Call<MessageModel>

    data class SendMessageRequest(val content: String,
                                  val mentions: List<DiscordId<User>>? = null,
                                  val nonce: String? = null,
                                  val tts: Boolean? = null)

    @PATCH("channels/{channel_id}/messages/{id}")
    fun editMessage(@Path("channel_id") id: WrappedId<Channel>,
                    @Path("id") messageId: WrappedId<PostedMessage>,
                    @Body data: EditMessageRequest): Call<MessageModel>

    data class EditMessageRequest(val content: String, val mentions: List<DiscordId<User>>? = null)

    @DELETE("channels/{channel_id}/messages/{id}")
    fun deleteMessage(@Path("channel_id") id: WrappedId<Channel>,
                      @Path("id") messageId: WrappedId<PostedMessage>): Call<Void>

    @POST("channels/{channel_id}/messages/{id}/ack")
    fun acknowledgeMessage(@Path("channel_id") id: WrappedId<Channel>,
                           @Path("id") messageId: WrappedId<PostedMessage>): Call<Void>

    @PUT("channels/{channel_id}/permissions/{target_id}")
    fun changePermissionsUser(@Path("channel_id") channelId: WrappedId<Channel>,
                              @Path("target_id") targetId: WrappedId<User>,
                              @Body data: EditPermissionRequest<User>): Call<Void>

    @PUT("channels/{channel_id}/permissions/{target_id}")
    fun changePermissionsRole(@Path("channel_id") channelId: WrappedId<Channel>,
                              @Path("target_id") targetId: WrappedId<Role>,
                              @Body data: EditPermissionRequest<Role>): Call<Void>

    data class EditPermissionRequest<T : DiscordId.Identifiable<T>>(val allow: PermissionSet,
                                                                    val deny: PermissionSet,
                                                                    val id: DiscordId<T>,
                                                                    val type: String)

    @DELETE("channels/{channel_id}/permissions/{target_id}")
    fun deletePermissionsUser(@Path("channel_id") channelId: WrappedId<Channel>,
                              @Path("target_id") targetId: WrappedId<User>): Call<Void>

    @DELETE("channels/{channel_id}/permissions/{target_id}")
    fun deletePermissionsRole(@Path("channel_id") channelId: WrappedId<Channel>,
                              @Path("target_id") targetId: WrappedId<Role>): Call<Void>

    @GET("channels/{channel_id}/invites")
    @Deprecated("Unfinished")
    fun getChannelInvites(@Path("channel_id") id: WrappedId<Channel>): Call<Any>
}