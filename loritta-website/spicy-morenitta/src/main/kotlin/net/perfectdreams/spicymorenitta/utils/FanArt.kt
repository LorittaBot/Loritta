package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.Serializable
import kotlin.js.Date

@Serializable
data class FanArt(
        val fileName: String,
        @Serializable(with = TestSerializer::class) val createdAt: Date,
        val tags: Set<String>
)