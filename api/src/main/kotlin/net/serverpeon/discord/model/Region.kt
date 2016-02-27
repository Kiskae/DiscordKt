package net.serverpeon.discord.model

/**
 *
 */
interface Region {
    /**
     *
     */
    val id: String

    /**
     *
     */
    val continent: Continent

    enum class Continent {
        NORTH_AMERICA,
        SOUTH_AMERICA,
        EUROPE,
        ASIA,
        AUSTRALIA,
        AFRICA,
        UNKNOWN
    }
}