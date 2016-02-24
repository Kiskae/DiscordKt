package net.serverpeon.discord.tool

import com.google.gson.annotations.SerializedName

class GetOrCreate<T> {
    @SerializedName("_")
    private var internalType: T? = null

    fun getOrCreate(init: () -> T): T {
        return if (internalType == null) {
            internalType = init()
            internalType as T
        } else {
            internalType!!
        }
    }
}