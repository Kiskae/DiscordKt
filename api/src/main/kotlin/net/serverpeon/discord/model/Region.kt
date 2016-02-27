package net.serverpeon.discord.model

/**
 * Represents one of the regions in which a [Guild] may be hosted.
 */
interface Region {
    /**
     * Alphanumeric (with dashes) id representing this region.
     */
    val id: String

    /**
     * Approximate continent on which this region exists.
     */
    val continent: Continent

    enum class Continent {
        NORTH_AMERICA,
        SOUTH_AMERICA, // Currently unused
        EUROPE,
        ASIA,
        AUSTRALIA,
        AFRICA, // Currently unused
        UNKNOWN
    }
}