package net.perfectdreams.loritta

import kotlinx.serialization.Serializable

@Serializable
class QuirkyConfig(
        val randomReactions: RandomReactionsConfig,
        val thankYouLori: ThankYouLoriConfig,
        val tioDoPave: TioDoPaveConfig,
        val addFanArts: AddFanArtsConfig,
        val topDonatorsRank: TopDonatorsRankConfig,
        val topVotersRank: TopVotersRankConfig,
        val sponsorsAdvertisement: SponsorsAdvertisementConfig,
        val canecaUsers: List<Long>
) {
    @Serializable
    class RandomReactionsConfig(
            val enabled: Boolean,
            val maxBound: Int,
            val reactions: List<String>,
            val contextAwareReactions: List<ContextAwareReaction>
    ) {
        @Serializable
        class ContextAwareReaction(
                val match: String,
                val chanceOf: Double,
                val reactions: List<String>
        )
    }
    @Serializable
    class TioDoPaveConfig(
            val enabled: Boolean,
            val chance: Double
    )
    @Serializable
    class ThankYouLoriConfig(
            val enabled: Boolean,
            val channelId: Long,
            val giveDonationKeyIfSentBeforeTime: Long,
            val donationKeyValue: Double,
            val expiresAt: Long,
            val reactions: List<String>
    )
    @Serializable
    class AddFanArtsConfig(
            val enabled: Boolean,
            val emoteId: Long,
            val channels: List<Long>,
            val fanArtFiles: String,
            val firstFanArtChannelId: Long,
            val firstFanArtRoleId: Long,
            val placesToPlaceStuff: List<String>
    )
    @Serializable
    class TopDonatorsRankConfig(
            val enabled: Boolean,
            val topRole1: Long,
            val topRole2: Long,
            val topRole3: Long,
            val channels: List<Long>
    )
    @Serializable
    class TopVotersRankConfig(
            val enabled: Boolean,
            val topRole1: Long,
            val topRole2: Long,
            val topRole3: Long,
            val channels: List<Long>
    )
    @Serializable
    class SponsorsAdvertisementConfig(
            val enabled: Boolean,
            val channelId: Long
    )
}