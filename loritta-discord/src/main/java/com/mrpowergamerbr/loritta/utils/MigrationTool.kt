package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ProfileDesign
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.UserSettings
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.tables.ProfileDesignsPayments
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	companion object {
	}

	fun migrateProfiles() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		/*
		                            "NostalgiaProfileCreator" -> "Padrão"
                                    "DefaultProfileCreator" -> "Moderno"
                                    "MSNProfileCreator" -> "MSN"
                                    "OrkutProfileCreator" -> "Orkut"
                                    "MonicaAtaProfileCreator" -> "Mônica \"ata\""
                                    "CowboyProfileCreator" -> "Cowboy"
                                    "LoriAtaProfileCreator" -> "Loritta \"ata\""
                                    "UndertaleProfileCreator" -> "Undertale Battle"
                                    "PlainWhiteProfileCreator" -> "Simplesmente Branco"
                                    "PlainOrangeProfileCreator" -> "Simplesmente Laranja"
                                    "PlainPurpleProfileCreator" -> "Simplesmente Roxo"
                                    "PlainAquaProfileCreator" -> "Simplesmente Azul"
                                    "PlainGreenProfileCreator" -> "Simplesmente Verde"
                                    "PlainGreenHeartsProfileCreator" -> "Simplesmente Verde com Flores"
                                    "NextGenProfileCreator" -> "Próxima Geração"
                                    "Halloween2019ProfileCreator" -> "Evento de Halloween 2019"
                                    "Christmas2019ProfileCreator" -> "Evento de Natal 2019"
                                    "LorittaChristmas2019ProfileCreator" -> "Evento de Natal 2019"
		 */
		loritta.initPostgreSql()
		transaction(Databases.loritta) {
			UserSettings.selectAll().forEach {
				val boughtProfiles = it[UserSettings.boughtProfiles]

				if (boughtProfiles.isNotEmpty()) {
					val profile = Profiles.select { Profiles.settings eq it[UserSettings.id] }
							.first()

					for (boughtProfile in boughtProfiles) {
						val internalName = convertFromOldClassNameToInternalName(boughtProfile) ?: return@forEach

						val profileDesign = ProfileDesign.findById(internalName)!!

						ProfileDesignsPayments.insert {
							it[ProfileDesignsPayments.userId] = profile[Profiles.id].value
							it[ProfileDesignsPayments.boughtAt] = -1
							it[ProfileDesignsPayments.cost] = profileDesign.rarity.getProfilePrice().toLong()
							it[ProfileDesignsPayments.profile] = profileDesign.id
						}
					}

					val activeProfile = convertFromOldClassNameToInternalName(it[UserSettings.activeProfile] ?: return@forEach) ?: return@forEach
					val profileDesign = ProfileDesign.findById(activeProfile)!!

					UserSettings.update({ UserSettings.id eq it[UserSettings.id] }) {
						it[UserSettings.activeProfileDesign] = profileDesign.id
					}
				}
			}
		}

		println("Finished!")
	}

	fun convertFromOldClassNameToInternalName(className: String): String? {
		return when (className) {
			"NostalgiaProfileCreator" -> "defaultDark"
			"DefaultProfileCreator" -> "modernBlurple"
			"MSNProfileCreator" -> "msn"
			"OrkutProfileCreator" -> "orkut"
			"MonicaAtaProfileCreator" -> "monicaAta"
			"CowboyProfileCreator" -> "cowboy"
			"LoriAtaProfileCreator" -> "loriAta"
			"UndertaleProfileCreator" -> "undertaleBattle"
			"PlainWhiteProfileCreator" -> "plainWhite"
			"PlainOrangeProfileCreator" -> "plainOrange"
			"PlainPurpleProfileCreator" -> "plainPurple"
			"PlainAquaProfileCreator" -> "plainAqua"
			"PlainGreenProfileCreator" -> "plainGreen"
			"PlainGreenHeartsProfileCreator" -> "plainGreenHearts"
			"NextGenProfileCreator" -> "nextGenDark"
			"Halloween2019ProfileCreator" -> "halloween2019"
			"Christmas2019ProfileCreator" -> "christmas2019"
			"LorittaChristmas2019ProfileCreator" -> "lorittaChristmas2019"
			else -> {
				println("Unknown profile creator $className! Bug?")
				null
			}
		}
	}
}