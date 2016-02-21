package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.rest.data.*
import net.serverpeon.discord.model.*
import retrofit2.Call
import retrofit2.http.*
import java.awt.Color
import java.time.Duration

interface Guilds {
    @POST("guilds")
    fun createGuild(@Body data: CreateGuildRequest): Call<GuildModel>

    data class CreateGuildRequest(val name: String, val region: String, val icon: String? = null)

    @PATCH("guilds/{guild_id}")
    fun editGuild(@Path("guild_id") id: WrappedId<Guild>, @Body data: EditGuildRequest): Call<GuildModel>

    data class EditGuildRequest(val name: String,
                                val region: String? = null,
                                val icon: String? = null,
                                val afk_channel_id: DiscordId<Channel>? = null,
                                val afk_timeout: Duration? = null)

    @DELETE("guilds/{guild_id}")
    fun deleteGuild(@Path("guild_id") id: WrappedId<Guild>): Call<GuildModel>

    @GET("guilds/{guild_id}/channels")
    fun guildChannels(@Path("guild_id") id: WrappedId<Guild>): Call<List<ChannelModel>>

    @PATCH("guilds/{guild_id}/members/{user_id}")
    fun editMember(@Path("guild_id") id: WrappedId<Guild>,
                   @Path("user_id") userId: WrappedId<User>,
                   @Body data: EditMemberRequest): Call<Void>

    data class EditMemberRequest(val roles: List<DiscordId<Role>>)

    @DELETE("guilds/{guild_id}/members/{user_id}")
    fun kickMember(@Path("guild_id") id: WrappedId<Guild>,
                   @Path("user_id") userId: WrappedId<User>): Call<Void>

    @GET("guilds/{guild_id}/bans")
    fun getBansForGuild(@Path("guild_id") id: WrappedId<Guild>): Call<List<UserBan>>

    data class UserBan(val user: UserModel)

    @PUT("guilds/{guild_id}/bans/{user_id}")
    fun addBan(@Path("guild_id") id: WrappedId<Guild>,
               @Path("user_id") userId: WrappedId<User>,
               @Query("delete-message-days") deleteLastXDays: Int? = null): Call<Void>

    @DELETE("guilds/{guild_id}/bans/{user_id}")
    fun removeBan(@Path("guild_id") id: WrappedId<Guild>,
                  @Path("user_id") userId: WrappedId<User>): Call<Void>

    @POST("guilds/{guild_id}/roles")
    fun createRole(@Path("guild_id") id: WrappedId<Guild>): Call<RoleModel>

    @PATCH("guilds/{guild_id}/roles/{role_id}")
    fun editRole(@Path("guild_id") id: WrappedId<Guild>,
                 @Path("role_id") roleId: WrappedId<Role>,
                 @Body data: EditRoleRequest): Call<RoleModel>

    data class EditRoleRequest(val color: Color,
                               val hoist: Boolean,
                               val name: String,
                               val permissions: PermissionSet)

    @PATCH("guilds/{guild_id}/roles")
    fun reorderRoles(@Path("guild_id") id: WrappedId<Guild>,
                     @Body data: List<RoleOrder>): Call<List<RoleModel>>

    data class RoleOrder(val id: DiscordId<Role>, val position: Int) {
        init {
            check(position >= 1)
        }
    }

    @DELETE("guilds/{guild_id}/roles/{role_id}")
    fun deleteRole(@Path("guild_id") id: WrappedId<Guild>,
                   @Path("role_id") roleId: WrappedId<Role>): Call<Void>

    @GET("guilds/{guild_id}/invites")
    @Deprecated("Unfinished")
    fun getGuildInvites(@Path("guild_id") id: WrappedId<Guild>): Call<List<Any>> //TODO: replace with RichInvite

    @POST("guilds/{guild_id}/channels")
    fun createChannel(@Path("guild_id") id: WrappedId<Guild>,
                      @Body data: ChannelCreationRequest): Call<ChannelModel>

    data class ChannelCreationRequest(val name: String, val type: ChannelModel.Type)
}