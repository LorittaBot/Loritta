package net.perfectdreams.loritta.plugin.autobannerchanger

import com.fasterxml.jackson.annotation.JsonCreator

class AutoBannerChangerConfig @JsonCreator constructor(
        val enabled: Boolean,
        val timeMod: Long,
        val banners: List<String>,
        val guilds: List<Long>,
        val channels: List<Long>
)