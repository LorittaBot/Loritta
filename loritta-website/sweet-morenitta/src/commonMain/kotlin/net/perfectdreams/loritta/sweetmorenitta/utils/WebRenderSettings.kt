package net.perfectdreams.loritta.sweetmorenitta.utils

import net.perfectdreams.loritta.sweetmorenitta.AssetHashProvider

class WebRenderSettings(
        val websiteUrl: String,
        val path: String,
        val hashProvider: AssetHashProvider,
        val addBotUrl: String
)