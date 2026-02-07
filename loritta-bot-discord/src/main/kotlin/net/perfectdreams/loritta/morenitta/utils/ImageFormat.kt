package net.perfectdreams.loritta.morenitta.utils

enum class ImageFormat(val extension: String, val supportsAnimation: Boolean) {
    PNG("png", false),
    JPG("jpg", false),
    GIF("gif", true),
    WEBP("webp", true)
}