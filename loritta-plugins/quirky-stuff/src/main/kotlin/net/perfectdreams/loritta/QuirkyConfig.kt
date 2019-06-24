package net.perfectdreams.loritta

import com.fasterxml.jackson.annotation.JsonCreator

class QuirkyConfig @JsonCreator constructor(
        val randomReactions: RandomReactionsConfig,
        val thankYouLori: ThankYouLoriConfig,
        val tioDoPave: TioDoPaveConfig,
        val addFanArts: AddFanArtsConfig,
        val changeBanner: ChangeBannerConfig,
        val topDonatorsRank: TopDonatorsRankConfig
) {
    class RandomReactionsConfig @JsonCreator constructor(
            val enabled: Boolean,
            val maxBound: Int,
            val reactions: Map<Int, String>,
            val contextAwareReactions: List<ContextAwareReaction>
    ) {
        class ContextAwareReaction@JsonCreator constructor(
                val match: String,
                val chanceOf: Double,
                val reactions: List<String>
        )
    }

    class TioDoPaveConfig @JsonCreator constructor(
            val enabled: Boolean,
            val chance: Double
    )

    class ThankYouLoriConfig @JsonCreator constructor(
            val enabled: Boolean,
            val channelId: Long,
            val reactions: List<String>
    )

    class AddFanArtsConfig @JsonCreator constructor(
            val enabled: Boolean,
            val emoteId: Long,
            val channels: List<Long>,
            val fanArtFiles: String,
            val firstFanArtChannelId: Long,
            val firstFanArtRoleId: Long,
            val placesToPlaceStuff: List<String>
    )

    class ChangeBannerConfig @JsonCreator constructor(
            val enabled: Boolean,
            val timeMod: Long,
            val guilds: List<Long>,
            val channels: List<Long>
    )

    class TopDonatorsRankConfig @JsonCreator constructor(
            val enabled: Boolean,
            val channels: List<Long>
    )
}