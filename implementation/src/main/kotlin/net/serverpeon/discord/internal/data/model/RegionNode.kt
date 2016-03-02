package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.internal.jsonmodels.RegionModel
import net.serverpeon.discord.model.Region

class RegionNode(override val id: String) : Region {
    override val continent: Region.Continent by lazy { guessContinent(id) }

    override fun toString(): String {
        return "Region(id='$id')"
    }

    companion object {
        private fun guessContinent(regionId: String): Region.Continent {
            return when (regionId) {
                "amsterdam" -> Region.Continent.EUROPE
                "frankfurt" -> Region.Continent.EUROPE
                "london" -> Region.Continent.EUROPE
                "singapore" -> Region.Continent.ASIA
                "sydney" -> Region.Continent.AUSTRALIA
                else -> {
                    if (regionId.startsWith("us-")) {
                        Region.Continent.NORTH_AMERICA
                    } else {
                        Region.Continent.UNKNOWN
                    }
                }
            }
        }

        fun create(data: RegionModel): RegionNode {
            return RegionNode(data.id)
        }
    }
}