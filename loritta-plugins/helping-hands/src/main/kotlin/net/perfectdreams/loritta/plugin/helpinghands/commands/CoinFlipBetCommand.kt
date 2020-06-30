package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CaraCoroaCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MessageInteractionFunctions
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.utils.extensions.refreshInDeferredTransaction
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object CoinFlipBetCommand : DSLCommandBase {
	val mutex = Mutex()

	override fun command(plugin: HelpingHandsPlugin, loritta: LorittaBot) = create(
			loritta,
			listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
					.flatMap { listOf("$it bet", "$it apostar") }
	) {
		description { it["commands.economy.flipcoinbet.description"] }

		examples {
			listOf(
					"@MrPowerGamerBR 100",
					"@Loritta 1000"
			)
		}

		usage {
			arguments {
				argument(ArgumentType.USER) {}
				argument(ArgumentType.NUMBER) {}
			}
		}

		this.canUseInPrivateChannel = false

		executes {
			loritta as Loritta

			if (2 > args.size) {
				this.explain()
				return@executes
			}

			val context = checkType<DiscordCommandContext>(this)

			val _user = validate(context.user(0))
			val invitedUser = _user.toJDA()

			if (invitedUser == context.user)
				fail(locale["commands.economy.flipcoinbet.cantBetSelf"], Constants.ERROR)

			val selfActiveDonations = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonations(context.discordMessage.author.idLong)
			val otherActiveDonations = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonations(invitedUser.idLong)

			val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
			val otherPlan = UserPremiumPlans.getPlanFromValue(otherActiveDonations)

			val hasNoTax: Boolean
			val whoHasTheNoTaxReward: User?
			val plan: UserPremiumPlans?

			if (selfPlan.totalCoinFlipReward == 1.0) {
				whoHasTheNoTaxReward = context.discordMessage.author
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

			val number = context.args[1].toLongOrNull()
					?: fail(locale["commands.invalidNumber", context.args[1].stripCodeMarks()], Emotes.LORI_CRYING.toString())
			val tax = (number * (1.0 - plan.totalCoinFlipReward)).toLong()
			val money = number - tax

			if (!hasNoTax && tax == 0L)
				fail(locale["commands.economy.flipcoinbet.youNeedToBetMore"], Constants.ERROR)

			if (0 >= number)
				fail(locale["commands.economy.flipcoinbet.zeroMoney"], Constants.ERROR)

			val selfUserProfile = context.lorittaUser.profile

			if (number > selfUserProfile.money)
				fail(locale["commands.economy.flipcoinbet.notEnoughMoneySelf"], Constants.ERROR)

			if ((System.currentTimeMillis()/1000) - context.user.timeCreated.toEpochSecond() < 604800) {
				fail(locale["commands.economy.flipcoin.bet.newAccountSelf"])
			} else if ((System.currentTimeMillis()/1000) - invitedUser.timeCreated.toEpochSecond() < 604800) {
				fail(locale["commands.economy.flipcoin.bet.newAccountInvited"])
			}

			val invitedUserProfile = loritta.getOrCreateLorittaProfile(invitedUser.id)
			val bannedState = invitedUserProfile.getBannedState()

			if (number > invitedUserProfile.money || bannedState != null)
				fail(locale["commands.economy.flipcoinbet.notEnoughMoneyInvited", invitedUser.asMention], Constants.ERROR)

			val message = context.reply(
					LorittaReply(
							(
									if (hasNoTax)
										locale[
												"commands.economy.flipcoinbet.startBetNoTax",
												invitedUser.asMention,
												context.user.asMention,
												locale["commands.fun.flipcoin.heads"],
												money,
												locale["commands.fun.flipcoin.tails"],
												whoHasTheNoTaxReward?.asMention ?: "???"
										]
									else
										locale[
												"commands.economy.flipcoinbet.startBet",
												invitedUser.asMention,
												context.user.asMention,
												locale["commands.fun.flipcoin.heads"],
												money,
												locale["commands.fun.flipcoin.tails"],
												number,
												tax
										]
									),
							Emotes.LORI_RICH,
							mentionUser = false
					)
			).toJDA()

			val functions = com.mrpowergamerbr.loritta.utils.loritta.messageInteractionCache.getOrPut(message.idLong) { MessageInteractionFunctions(message.guild.idLong, message.channel.idLong, context.user.idLong) }
			functions.onReactionAdd = {
				if (it.userIdLong == invitedUser.idLong && it.reactionEmote.name == "✅") {
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
								message = context.locale["${CaraCoroaCommand.LOCALE_PREFIX}.tails"]
							} else {
								prefix = "<:cara:412586256409559041>"
								message = context.locale["${CaraCoroaCommand.LOCALE_PREFIX}.heads"]
							}

							val winner: net.dv8tion.jda.api.entities.User
							val loser: net.dv8tion.jda.api.entities.User

							if (isTails) {
								winner = context.user
								loser = invitedUser
								transaction(Databases.loritta) {
									selfUserProfile.money += money
									invitedUserProfile.money -= number

									SonhosTransaction.insert {
										it[givenBy] = invitedUserProfile.id.value
										it[receivedBy] = selfUserProfile.id.value
										it[givenAt] = System.currentTimeMillis()
										it[quantity] = number.toBigDecimal()
										it[reason] = SonhosPaymentReason.COIN_FLIP_BET
									}
								}
							} else {
								winner = invitedUser
								loser = context.user
								transaction(Databases.loritta) {
									invitedUserProfile.money += money
									selfUserProfile.money -= number

									SonhosTransaction.insert {
										it[givenBy] = selfUserProfile.id.value
										it[receivedBy] = invitedUserProfile.id.value
										it[givenAt] = System.currentTimeMillis()
										it[quantity] = number.toBigDecimal()
										it[reason] = SonhosPaymentReason.COIN_FLIP_BET
									}
								}
							}

							context.reply(
									LorittaReply(
											"**$message!**",
											prefix,
											mentionUser = false
									),
									LorittaReply(
											locale["commands.economy.flipcoinbet.congratulations", winner.asMention, money, loser.asMention],
											Emotes.LORI_RICH,
											mentionUser = false
									)
							)
						}
					}
				}
			}

			message.addReaction("✅").queue()
		}
	}
}
