package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CaraCoroaCommand
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.utils.*
import net.perfectdreams.loritta.utils.extensions.refreshInDeferredTransaction
import net.perfectdreams.loritta.utils.extensions.toJDA

class CoinFlipBetCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
		plugin.loritta,
		listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
				.flatMap { listOf("$it bet", "$it apostar") },
		CommandCategory.ECONOMY
) {
	companion object {
		// Used to avoid dupes
		private val mutex = Mutex()
	}

	override fun command() = create {
		localizedDescription("commands.command.flipcoinbet.description")
		localizedExamples("commands.command.flipcoinbet.examples")

		usage {
			arguments {
				argument(ArgumentType.USER) {}
				argument(ArgumentType.NUMBER) {}
			}
		}

		this.canUseInPrivateChannel = false

		executesDiscord {
			if (2 > args.size)
				this.explainAndExit()

			val _user = validate(user(0))
			val invitedUser = _user.toJDA()

			if (invitedUser == user)
				fail(locale["commands.command.flipcoinbet.cantBetSelf"], Constants.ERROR)

			val selfActiveDonations = loritta.getActiveMoneyFromDonationsAsync(discordMessage.author.idLong)
			val otherActiveDonations = loritta.getActiveMoneyFromDonationsAsync(invitedUser.idLong)

			val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
			val otherPlan = UserPremiumPlans.getPlanFromValue(otherActiveDonations)

			val hasNoTax: Boolean
			val whoHasTheNoTaxReward: User?
			val plan: UserPremiumPlans?

			if (selfPlan.totalCoinFlipReward == 1.0) {
				whoHasTheNoTaxReward = discordMessage.author
				hasNoTax = true
				plan = selfPlan
			} else if (otherPlan.totalCoinFlipReward == 1.0) {
				whoHasTheNoTaxReward = invitedUser
				hasNoTax = true
				plan = otherPlan
			} else {
				whoHasTheNoTaxReward = null
				hasNoTax = false
				plan = UserPremiumPlans.Essential
			}

			val number = NumberUtils.convertShortenedNumberToLong(args[1])
					?: GenericReplies.invalidNumber(this, args[1].stripCodeMarks())

			val tax = (number * (1.0 - plan.totalCoinFlipReward)).toLong()
			val money = number - tax

			if (!hasNoTax && tax == 0L)
				fail(locale["commands.command.flipcoinbet.youNeedToBetMore"], Constants.ERROR)

			if (0 >= number)
				fail(locale["commands.command.flipcoinbet.zeroMoney"], Constants.ERROR)

			val selfUserProfile = lorittaUser.profile

			if (number > selfUserProfile.money)
				fail(locale["commands.command.flipcoinbet.notEnoughMoneySelf"], Constants.ERROR)

			val invitedUserProfile = loritta.getOrCreateLorittaProfile(invitedUser.id)
			val bannedState = invitedUserProfile.getBannedState()

			if (number > invitedUserProfile.money || bannedState != null)
				fail(locale["commands.command.flipcoinbet.notEnoughMoneyInvited", invitedUser.asMention], Constants.ERROR)

			// Only allow users to participate in a coin flip bet if the user got their daily reward today
			AccountUtils.getUserTodayDailyReward(lorittaUser.profile)
					?: fail(locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", serverConfig.commandPrefix], Constants.ERROR)

			val message = reply(
					LorittaReply(
						if (hasNoTax)
							locale[
									"commands.command.flipcoinbet.startBetNoTax",
									invitedUser.asMention,
									user.asMention,
									locale["commands.command.flipcoin.heads"],
									money,
									locale["commands.command.flipcoin.tails"],
									whoHasTheNoTaxReward?.asMention ?: "???"
							]
						else
							locale[
									"commands.command.flipcoinbet.startBet",
									invitedUser.asMention,
									user.asMention,
									locale["commands.command.flipcoin.heads"],
									money,
									locale["commands.command.flipcoin.tails"],
									number,
									tax
							],
						Emotes.LORI_RICH,
						mentionUser = false
					),
					LorittaReply(
							locale[
									"commands.command.flipcoinbet.clickToAcceptTheBet",
									invitedUser.asMention,
									"✅"
							],
							"🤝",
							mentionUser = false
					)
			).toJDA()

			message.onReactionAdd(this) {
				if (it.reactionEmote.name == "✅") {
					mutex.withLock {
						if (LorittaLauncher.loritta.messageInteractionCache.containsKey(it.messageIdLong)) {
							val usersThatReactedToTheMessage = it.reaction.retrieveUsers().await()

							if (invitedUser in usersThatReactedToTheMessage && user in usersThatReactedToTheMessage) {
								message.removeAllFunctions()
								plugin.launch {
									mutex.withLock {
										listOf(
												selfUserProfile.refreshInDeferredTransaction(),
												invitedUserProfile.refreshInDeferredTransaction()
										).awaitAll()

										if (number > selfUserProfile.money)
											return@withLock

										if (number > invitedUserProfile.money)
											return@withLock

										val isTails = Loritta.RANDOM.nextBoolean()
										val prefix: String
										val message: String

										if (isTails) {
											prefix = "<:coroa:412586257114464259>"
											message = locale["${CaraCoroaCommand.LOCALE_PREFIX}.tails"]
										} else {
											prefix = "<:cara:412586256409559041>"
											message = locale["${CaraCoroaCommand.LOCALE_PREFIX}.heads"]
										}

										val winner: User
										val loser: User

										if (isTails) {
											winner = user
											loser = invitedUser
											loritta.newSuspendedTransaction {
												selfUserProfile.addSonhosNested(money)
												invitedUserProfile.takeSonhosNested(number)

												PaymentUtils.addToTransactionLogNested(
														number,
														SonhosPaymentReason.COIN_FLIP_BET,
														givenBy = invitedUserProfile.id.value,
														receivedBy = selfUserProfile.id.value
												)
											}
										} else {
											winner = invitedUser
											loser = user
											loritta.newSuspendedTransaction {
												invitedUserProfile.addSonhosNested(money)
												selfUserProfile.takeSonhosNested(number)

												PaymentUtils.addToTransactionLogNested(
														number,
														SonhosPaymentReason.COIN_FLIP_BET,
														givenBy = selfUserProfile.id.value,
														receivedBy = invitedUserProfile.id.value
												)
											}
										}

										reply(
												LorittaReply(
														"**$message!**",
														prefix,
														mentionUser = false
												),
												LorittaReply(
														locale["commands.command.flipcoinbet.congratulations", winner.asMention, money, loser.asMention],
														Emotes.LORI_RICH,
														mentionUser = false
												)
										)
									}
								}
							}
						}
					}
				}
			}

			message.addReaction("✅").queue()
		}
	}
}