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

    /**
     * Known regions, might change at some point in the future.
     *
     * Use [ClientModel.getAvailableServerRegions] to get an up-to-date list.
     */
    enum class KnownRegions(override val id: String, override val continent: Continent) : Region {
        AMSTERDAM("amsterdam", Continent.EUROPE),
        LONDON("london", Continent.EUROPE),
        SINGAPORE("singapore", Continent.ASIA),
        FRANKFURT("frankfurt", Continent.EUROPE),
        US_EAST("us-east", Continent.NORTH_AMERICA),
        US_CENTRAL("us-central", Continent.NORTH_AMERICA),
        US_SOUTH("us-south", Continent.NORTH_AMERICA),
        US_WEST("us-west", Continent.NORTH_AMERICA),
        SYDNEY("sydney", Continent.AUSTRALIA)
    }
}