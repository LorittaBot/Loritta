package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.CaraCoroaCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MessageInteractionFunctions
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.dbRefresh
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.toJDA
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
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

			val context = checkType<DiscordCommandContext>(this)

			val _user = validate(context.user(0))
			val invitedUser = _user.toJDA()

			if (invitedUser == context.user) {
				context.reply(
						LorittaReply(
								locale["commands.economy.flipcoinbet.cantBetSelf"],
								Constants.ERROR
						)
				)
				return@executes
			}

			val number = context.args[1].toLong()

			if (0 >= number) {
				context.reply(
						LorittaReply(
								locale["commands.economy.flipcoinbet.zeroMoney"],
								Constants.ERROR
						)
				)
				return@executes
			}

			val selfUserProfile = context.lorittaUser.profile

			if (number > selfUserProfile.money) {
				context.reply(
						LorittaReply(
								locale["commands.economy.flipcoinbet.notEnoughMoneySelf"],
								Constants.ERROR
						)
				)
				return@executes
			}

			val invitedUserProfile = loritta.getOrCreateLorittaProfile(invitedUser.id)

			if (number > invitedUserProfile.money || invitedUserProfile.isBanned) {
				context.reply(
						LorittaReply(
								locale["commands.economy.flipcoinbet.notEnoughMoneyInvited", invitedUser.asMention],
								Constants.ERROR
						)
				)
				return@executes
			}

			val message = context.reply(
					LorittaReply(
							locale[
									"commands.economy.flipcoinbet.startBet",
									invitedUser.asMention,
									context.user.asMention,
									locale["commands.fun.flipcoin.heads"],
									number,
									locale["commands.fun.flipcoin.tails"]
							],
							Emotes.LORI_RICH,
							mentionUser = false
					)
			).toJDA()

			val functions = com.mrpowergamerbr.loritta.utils.loritta.messageInteractionCache.getOrPut(message.idLong) { MessageInteractionFunctions(message.guild.idLong, message.channel.idLong, context.user.id) }
			functions.onReactionAdd = {
				if (it.userIdLong == invitedUser.idLong && it.reactionEmote.name == "✅") {
					message.removeAllFunctions()
					plugin.launch {
						mutex.withLock {
							selfUserProfile.dbRefresh()
							invitedUserProfile.dbRefresh()

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
									selfUserProfile.money += number
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
									invitedUserProfile.money += number
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
											locale["commands.economy.flipcoinbet.congratulations", winner.asMention, number, loser.asMention],
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