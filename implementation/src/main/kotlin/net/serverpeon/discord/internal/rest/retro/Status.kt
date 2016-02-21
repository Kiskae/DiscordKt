package net.serverpeon.discord.internal.rest.retro

import retrofit2.http.GET

interface Status {
    @GET("scheduled-maintenances/active.json")
    @Deprecated("Unfinished")
    fun activeMaintenance()

    @GET("scheduled-maintenances/upcoming.json")
    @Deprecated("Unfinished")
    fun scheduledMaintenance()
}