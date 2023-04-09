package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.tables.Profiles
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EconomyConfig
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import org.jetbrains.exposed.sql.select
import java.math.BigDecimal
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import java.time.Instant

class SonhosCommand(loritta: LorittaBot) : AbstractCommand(loritta, "sonhos", listOf("atm", "bal", "balance"), category = net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.sonhos.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.sonhos.examples")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "sonhos atm")

		val retrieveDreamsFromUser = context.getUserAt(0) ?: context.userHandle

		val lorittaProfile = if (retrieveDreamsFromUser == context.userHandle) {
			context.lorittaUser.profile
		} else {
			loritta.getLorittaProfile(retrieveDreamsFromUser.id)
		}

		var localEconomyEnabled = false
		var economyConfig: EconomyConfig? = null

		if (!context.isPrivateChannel) { // Se não estamos em um canal privado
			// Vamos ver se a guild atual utiliza o sistema de economia local!
			economyConfig = loritta.pudding.transaction {
				loritta.getOrCreateServerConfig(context.guild.idLong).economyConfig
			}

			localEconomyEnabled = economyConfig?.enabled == true
		}

		if (context.userHandle == retrieveDreamsFromUser) {
			val userSonhos = lorittaProfile?.money ?: 0L

			val replies = mutableListOf(
				LorittaReply(
					context.locale[
							"commands.command.sonhos.youHaveSonhos",
							userSonhos,
							context.locale[
									"commands.command.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"
							],
							if (userSonhos > 0) {
								val globalEconomyPosition = loritta.newSuspendedTransaction {
									Profiles.select { Profiles.money greaterEq userSonhos }.count()
								}

								context.locale[
										"commands.command.sonhos.currentRankPosition",
										globalEconomyPosition,
										context.locale[
												"commands.command.sonhos.sonhosRankingCommand",
												context.config.commandPrefix
										]
								]
							} else {
								""
							}
					],
					Emotes.LORI_RICH
				)
			)

			if (Instant.now().isBefore(LorittaChristmas2022Event.endOfEvent.toInstant())) {
				replies += LorittaReply(
					"Quer sonhos? Então participe do Evento de Natal da Loritta! ${loritta.commandMentions.eventJoin}",
					mentionUser = false
				)
			}

			context.reply(*replies.toTypedArray())
			logger.info("Usuário ${retrieveDreamsFromUser.idLong} possui $userSonhos sonhos!")
		} else {
			val userSonhos = lorittaProfile?.money ?: 0L

			val someoneHasReply = LorittaReply(
				context.locale[
						"commands.command.sonhos.userHasSonhos",
						retrieveDreamsFromUser.asMention,
						userSonhos,
						context.locale[
								"commands.command.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"
						],
						if (userSonhos > 0) {
							val globalEconomyPosition = loritta.newSuspendedTransaction {
								Profiles.select { Profiles.money greaterEq userSonhos }.count()
							}
							context.locale[
									"commands.command.sonhos.userCurrentRankPosition",
									retrieveDreamsFromUser.asMention,
									globalEconomyPosition,
									context.locale[
											"commands.command.sonhos.sonhosRankingCommand",
											context.config.commandPrefix
									]
							]
						} else {
							""
						}
				],
				Emotes.LORI_RICH
			)

			if (localEconomyEnabled && economyConfig != null) {
				val localProfile = context.config.getUserData(loritta, retrieveDreamsFromUser.idLong)
				context.reply(
					false,
					someoneHasReply,
					LorittaReply(
						locale["commands.command.sonhos.userHasSonhos", retrieveDreamsFromUser.asMention, localProfile.money, if (localProfile.money == BigDecimal.ONE) {
							economyConfig.economyName
						} else {
							economyConfig.economyNamePlural
						}],
						"\uD83D\uDCB5"
					)
				)
			} else {
				context.reply(someoneHasReply)
			}
			logger.info("Usuário ${retrieveDreamsFromUser.id} possui ${userSonhos} sonhos!")
		}
	}
}