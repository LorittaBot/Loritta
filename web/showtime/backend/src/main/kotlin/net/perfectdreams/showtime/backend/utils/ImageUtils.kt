package net.perfectdreams.showtime.backend.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.showtime.backend.ShowtimeBackend

object ImageUtils {
    val optimizedImagesInfoWithVariants = readFromResourcesAndDeserialize("/images-info.json")

    val bundledImagesAttributes = readFromResourcesAndDeserialize("/bundled-images-attributes.json")

    val optimizedImagesAttributes = readFromResourcesAndDeserialize("/optimized-images-attributes.json")

    val imagesAttributes = bundledImagesAttributes + optimizedImagesAttributes

    fun getImageAttributes(path: String) = imagesAttributes.firstOrNull { it.path.removePrefix("static") == path } ?: error("Missing ImageInfo for \"$path\"!")

    private fun readFromResourcesAndDeserialize(path: String) = Json.decodeFromString<List<ImageInfo>>(
        (ShowtimeBackend::class.java.getResourceAsStream(path) ?: error("Missing $path in the application resources!"))
            .readAllBytes()
            .toString(Charsets.UTF_8)
    )
}