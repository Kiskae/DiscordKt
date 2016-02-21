package net.serverpeon.discord.internal.ws.data.message

import com.google.gson.annotations.SerializedName
import net.serverpeon.discord.internal.ws.PayloadOut

/**
 * @property token Login token for the user account
 * @property v API version
 * @property properties System properties (probably for analytics)
 * @property large_threshold Threshold for when to consider a guild 'large'
 * @property compress Presumably whether to compress the messages sent
 */
data class ConnectMsg(val token: String,
                      val v: Int,
                      val properties: Properties,
                      val large_threshold: Int,
                      val compress: Boolean? = null) {

    data class Properties(@SerializedName("\$os") val operatingSystem: String,
                          @SerializedName("\$browser") val browser: String,
                          @SerializedName("\$device") val device: String,
                          @SerializedName("\$referrer") val referrer: String,
                          @SerializedName("\$referrer_domain") val referrerDomain: String)

    fun toPayload(): PayloadOut<ConnectMsg> = PayloadOut(2, this)
}