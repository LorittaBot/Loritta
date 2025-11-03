package net.perfectdreams.loritta.morenitta.website.rpc.processors.economy

import io.ktor.server.application.*
import net.perfectdreams.loritta.common.utils.daily.DailyRewardQuestions
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.LorittaRpcProcessor
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.serializable.requests.GetDailyRewardStatusRequest
import net.perfectdreams.loritta.serializable.responses.*

class GetDailyRewardStatusProcessor(val m: LorittaWebsite) : LorittaRpcProcessor {
    suspend fun process(call: ApplicationCall, request: GetDailyRewardStatusRequest): LorittaRPCResponse {
        val ip = call.request.trueIp

        when (val result = getDiscordAccountInformation(m.loritta, call)) {
            LorittaRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> return DiscordAccountError.InvalidDiscordAuthorization()
            LorittaRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> return DiscordAccountError.UserIsLorittaBanned()
            is LorittaRpcProcessor.DiscordAccountInformationResult.Success -> {
                val userIdentification = result.session.getUserIdentification(m.loritta)

                return when (DailyAccountSafetyUtils.verifyIfAccountAndIpAreSafe(m.loritta, userIdentification, ip)) {
                    DailyAccountSafetyUtils.AccountCheckResult.BlockedEmail -> UserVerificationError.BlockedEmail()
                    DailyAccountSafetyUtils.AccountCheckResult.BlockedIp -> UserVerificationError.BlockedIp()
                    DailyAccountSafetyUtils.AccountCheckResult.NotVerified -> UserVerificationError.DiscordAccountNotVerified()
                    DailyAccountSafetyUtils.AccountCheckResult.Safe -> {
                        // If the account is "safe", we will check if the user can get the reward
                        return when (val payoutResult = DailyAccountSafetyUtils.checkIfUserCanPayout(m.loritta, userIdentification, ip)) {
                            is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameAccount -> DailyPayoutError.AlreadyGotTheDailyRewardSameAccount()
                            is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp -> DailyPayoutError.AlreadyGotTheDailyRewardSameIp()
                            is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.Success -> {
                                // TODO: The question should be based on today, not random
                                val todaysRandomQuestion = DailyRewardQuestions.all.random()

                                GetDailyRewardStatusResponse.Success(
                                    m.loritta.config.loritta.turnstileCaptchas.dailyReward.siteKey,
                                    payoutResult.sameIpDailyAt != 0,
                                    todaysRandomQuestion
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}