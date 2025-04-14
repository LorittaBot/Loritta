package net.perfectdreams.loritta.website.backend.utils

import kotlinx.serialization.Serializable

@Serializable
data class ImageInfo(
    val path: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val variations: List<ImageInfo>?
) : java.io.Serializable // Needs to be serializable because if not Gradle complains that it can't serialize