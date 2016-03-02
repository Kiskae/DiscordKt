package net.serverpeon.discord.internal.rest.retro

import net.serverpeon.discord.internal.jsonmodels.RegionModel
import retrofit2.Call
import retrofit2.http.GET

interface Voice {
    @GET("voice/regions")
    fun serverRegions(): Call<List<RegionModel>>
}