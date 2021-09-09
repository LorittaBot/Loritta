package net.perfectdreams.loritta.cinnamon.common.entities

/**
 * A unique identifier, using unsigned longs, for entities.
 *
 * Keep in mind that this is not a 1:1 relation with another platform (like Discord's) Snowflake, but it was inspired by how Kord handles Discord snowflakes.
 */
class ULongUniqueId(val value: ULong) : Comparable<ULongUniqueId> {
    override fun compareTo(other: ULongUniqueId) = value.compareTo(other.value)
}