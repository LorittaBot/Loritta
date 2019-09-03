package net.perfectdreams.loritta

import com.fasterxml.jackson.annotation.JsonCreator

class QuirkyConfig @JsonCreator constructor(
        val randomReactions: RandomReactionsConfig,
        val thankYouLori: ThankYouLoriConfig,
        val tioDoPave: TioDoPaveConfig,
        val addFanArts: AddFanArtsConfig,
        val changeBanner: ChangeBannerConfig,
        val topDonatorsRank: TopDonatorsRankConfig,
        val topVotersRank: TopVotersRankConfig,
        val sponsorsAdvertisement: SponsorsAdvertisementConfig
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
            val giveDonationKeyIfSentBeforeTime: Long,
            val donationKeyValue: Double,
            val expiresAt: Long,
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
            val banners: List<String>,
            val guilds: List<Long>,
            val channels: List<Long>
    )

    class TopDonatorsRankConfig @JsonCreator constructor(
            val enabled: Boolean,
            val topRole1: Long,
            val topRole2: Long,
            val topRole3: Long,
            val channels: List<Long>
    )

    class TopVotersRankConfig @JsonCreator constructor(
            val enabled: Boolean,
            val topRole1: Long,
            val topRole2: Long,
            val topRole3: Long,
            val channels: List<Long>
    )

    class SponsorsAdvertisementConfig @JsonCreator constructor(
            val enabled: Boolean,
            val channelId: Long
    )
}