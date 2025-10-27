package net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker

data class Color(val rgb: Int) {
    constructor(red: Int, green: Int, blue: Int) : this((red and 0xff shl 16) or (green and 0xff shl 8) or (blue and 0xff))

    val red: Int get() = (rgb shr 16) and 0xFF
    val green: Int get() = (rgb shr 8) and 0xFF
    val blue: Int get() = (rgb shr 0) and 0xFF

    fun toHex(): String {
        // Convert each component to its hexadecimal representation
        val redHex = red.toString(16).padStart(2, '0')
        val greenHex = green.toString(16).padStart(2, '0')
        val blueHex = blue.toString(16).padStart(2, '0')

        // Combine the components into a single hexadecimal string
        val hex = "#$redHex$greenHex$blueHex"

        return hex
    }

    companion object {
        // https://stackoverflow.com/a/41654372/7271796
        fun fromHex(input: String) = Color(input.removePrefix("#").toInt(16))
    }
}