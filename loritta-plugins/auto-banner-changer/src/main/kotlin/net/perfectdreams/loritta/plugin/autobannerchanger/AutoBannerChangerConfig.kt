package net.perfectdreams.loritta.plugin.autobannerchanger

import kotlinx.serialization.Serializable

@Serializable
class AutoBannerChangerConfig(
        val enabled: Boolean,
        val timeMod: Long,
        val banners: List<String>,
        val guilds: List<Long>,
        val channels: List<Long>
)