package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.serialization.Serializable

@Serializable
data class StoredGenericInteractionData(
    /**
     * This is used to avoid kotlinx.serialization issues when trying to decode a object that has similar structure to this object
     *
     * So we try creating a very "unique" deserialization structure to avoid issues.
     */
    val dummy: Dummy,
    val interactionDataId: Long
) {
    @Serializable
    data class Dummy(
        val a: String,
        val b: String,
        val c: String
    )
}