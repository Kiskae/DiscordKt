package net.serverpeon.discord.internal.ws.client

import javax.websocket.Session

data class EventWrapper(val session: Session, val event: Any)