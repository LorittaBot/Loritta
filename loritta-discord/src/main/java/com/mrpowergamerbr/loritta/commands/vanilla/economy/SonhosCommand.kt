package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.servers.moduleconfigs.EconomyConfig
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class SonhosCommand : AbstractCommand("sonhos", listOf("atm", "bal", "balance"), category = CommandCategory.ECONOMY) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.economy.sonhos.description"]
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
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
			economyConfig = transaction(Databases.loritta) {
				loritta.getOrCreateServerConfig(context.guild.idLong).economyConfig
			}

			localEconomyEnabled = economyConfig?.enabled == true
		}

		if (context.userHandle == retrieveDreamsFromUser) {
			val userSonhos = lorittaProfile?.money ?: 0L

			val youHaveReply = LorittaReply(
                    context.locale[
                            "commands.economy.sonhos.youHaveSonhos",
                            userSonhos,
                            context.locale[
                                    "commands.economy.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"
                            ],
                            if (userSonhos > 0) {
                                val globalEconomyPosition = loritta.newSuspendedTransaction {
                                    Profiles.select { Profiles.money greaterEq userSonhos }.count()
                                }

                                context.locale[
                                        "commands.economy.sonhos.currentRankPosition",
                                        globalEconomyPosition,
                                        context.locale[
                                                "commands.economy.sonhos.sonhosRankingCommand",
                                                context.config.commandPrefix
                                        ]
                                ]
                            } else {
                                ""
                            }
                    ],
                    Emotes.LORI_RICH
            )

			if (localEconomyEnabled && economyConfig != null) { // Sistema de ecnomia local está ativado!
				val localProfile = context.config.getUserData(retrieveDreamsFromUser.idLong)
				context.reply(
						false,
						youHaveReply,
                        LorittaReply(
                                locale["SONHOS_YouHave", localProfile.money, if (localProfile.money == BigDecimal.ONE) {
                                    economyConfig.economyName
                                } else {
                                    economyConfig.economyNamePlural
                                }],
                                "\uD83D\uDCB5",
                                mentionUser = false
                        )
				)
			} else {
				context.reply(
						youHaveReply
				)
				logger.info("Usuário ${retrieveDreamsFromUser.idLong} possui $userSonhos sonhos!")
			}
		} else {
			val userSonhos = lorittaProfile?.money ?: 0L

			val someoneHasReply = LorittaReply(
                    context.locale[
                            "commands.economy.sonhos.userHasSonhos",
                            retrieveDreamsFromUser.asMention,
                            userSonhos,
                            context.locale[
                                    "commands.economy.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"
                            ],
                            if (userSonhos > 0) {
                                val globalEconomyPosition = loritta.newSuspendedTransaction {
                                    Profiles.select { Profiles.money greaterEq userSonhos }.count()
                                }
                                context.locale[
                                        "commands.economy.sonhos.userCurrentRankPosition",
                                        retrieveDreamsFromUser.asMention,
                                        globalEconomyPosition,
                                        context.locale[
                                                "commands.economy.sonhos.sonhosRankingCommand",
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
				val localProfile = context.config.getUserData(retrieveDreamsFromUser.idLong)
				context.reply(
						false,
						someoneHasReply,
                        LorittaReply(
                                locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, localProfile.money, if (localProfile.money == BigDecimal.ONE) {
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