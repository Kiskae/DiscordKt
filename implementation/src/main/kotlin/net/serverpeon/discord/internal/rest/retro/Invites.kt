package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.jsonmodels.InviteModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.model.Invite
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Invites {
    @POST("invite/{invite_id_or_xkcd}")
    fun acceptInvite(@Path("invite_id_or_xkcd") id: WrappedId<Invite>): Call<InviteModel.Basic>

    @DELETE("invite/{invite_id}")
    fun deleteInvite(@Path("invite_id") id: WrappedId<Invite>): Call<InviteModel.Basic>

    @GET("invite/{invite_id_or_xkcd}")
    fun getInvite(@Path("invite_id_or_xkcd") id: WrappedId<Invite>): Call<InviteModel.Basic>
}