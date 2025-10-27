package net.perfectdreams.loritta.placeholders

import kotlinx.serialization.Serializable

@Serializable
data class LorittaPlaceholder(
    /**
     * The name of the placeholder, without the brackets
     */
    val name: String,

    /**
     * If the placeholder is hidden or not. Used for deprecated placeholders.
     */
    val hidden: Boolean
) {
    /**
     * Creates a placeholder key for the [input] by wrapping it between {...}
     *
     * Example: If [input] is "@user", the returned value will be "{@user}"
     *
     * @param input the key
     * @return      the created placeholder key
     */
    val asKey: String
        get() = "{$name}"
}