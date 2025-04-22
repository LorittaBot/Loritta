package net.perfectdreams.loritta.serializable.responses

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.common.utils.daily.DailyRewardQuestion

@Serializable
sealed interface LorittaRPCResponse

// While these could be "object", kotlinx.serialization has an issue where objects can't inherit from multiple interfaces. https://github.com/Kotlin/kotlinx.serialization/issues/1937
sealed interface DailyPayoutError {
    @Serializable
    class AlreadyGotTheDailyRewardSameAccount : DailyPayoutError, GetDailyRewardStatusResponse, GetDailyRewardResponse

    @Serializable
    class AlreadyGotTheDailyRewardSameIp : DailyPayoutError, GetDailyRewardStatusResponse, GetDailyRewardResponse
}

sealed interface DiscordAccountError {
    @Serializable
    class InvalidDiscordAuthorization :
        DiscordAccountError,
        GetDailyRewardStatusResponse,
        GetDailyRewardResponse

    @Serializable
    class UserIsLorittaBanned :
        DiscordAccountError,
        GetDailyRewardStatusResponse,
        GetDailyRewardResponse
}

sealed interface UserVerificationError {
    @Serializable
    class BlockedEmail : UserVerificationError, GetDailyRewardStatusResponse, GetDailyRewardResponse

    @Serializable
    class BlockedIp : UserVerificationError, GetDailyRewardStatusResponse, GetDailyRewardResponse

    @Serializable
    class DiscordAccountNotVerified : UserVerificationError, GetDailyRewardStatusResponse, GetDailyRewardResponse
}

@Serializable
sealed interface GetDailyRewardStatusResponse : LorittaRPCResponse {
    @Serializable
    data class Success(
        val captchaSiteKey: String,
        val receivedDailyWithSameIp: Boolean,
        val question: DailyRewardQuestion
    ) : GetDailyRewardStatusResponse
}

@Serializable
sealed interface GetDailyRewardResponse : LorittaRPCResponse {
    @Serializable
    data class Success(
        val receivedDailyAt: Long,
        val dailyPayoutWithoutAnyBonus: Int,
        val dailyPayoutBonuses: List<DailyPayoutBonus>,
        val question: DailyRewardQuestion?,
        val currentBalance: Long,
        val sponsoredBy: SonhosSponsor?,
        val failedGuilds: List<FailedGuild>,
        val twitchChannelToAdvertise: TwitchChannel?,
        val loriCoolCardsEventReward: LoriCoolCardsEventReward?
    ) : GetDailyRewardResponse {
        @Serializable
        data class FailedGuild(
            val guild: GuildInfo,
            val type: DailyGuildMissingRequirement,
            val data: Long,
            val multiplier: Double
        )

        @Serializable
        data class SonhosSponsor(
            val sonhosMultipliedBy: Double,
            val sponsoredByGuild: GuildInfo,
            val sponsoredByUser: UserInfo?,
            val originalPayout: Int
        )

        @Serializable
        sealed class DailyPayoutBonus {
            @Serializable
            data class DailyQuestionBonus(
                val quantity: Int
            ) : DailyPayoutBonus()
        }

        @Serializable
        data class TwitchChannel(val channelId: String)

        @Serializable
        data class LoriCoolCardsEventReward(
            val eventName: String,
            val endsAt: Instant,
            val receivedBoosterPacks: Int,
            val stickerPackImageUrl: String,
            val sonhosReward: Long
        )
    }

    @Serializable
    class InvalidCaptchaToken : GetDailyRewardResponse

    @Serializable
    data class GuildInfo(
        val name: String,
        val iconId: String?,
        val id: Long
    )

    @Serializable
    data class UserInfo(
        val id: Long,
        val name: String,
        val discriminator: String,
        val effectiveAvatarUrl: String
    )
}