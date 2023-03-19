package net.perfectdreams.loritta.cinnamon.showtime.backend.utils.commands

import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.EtherealGambiImages

data class AdditionalCommandInfoConfig(
        val name: String,
        val imageInfo: EtherealGambiImages.PreloadedImageInfo? = null,
        // For images that aren't supported by EtherealGambi
        val imageUrl: String? = null,
        val videoUrl: String? = null
)