package net.serverpeon.discord.internal.ws

data class PayloadOut<E : Any>(val op: Int, val d: E)