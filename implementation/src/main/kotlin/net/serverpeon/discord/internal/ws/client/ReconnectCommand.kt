package net.serverpeon.discord.internal.ws.client

import java.net.URI

internal data class ReconnectCommand(val url: URI, var sequence: Int? = null)