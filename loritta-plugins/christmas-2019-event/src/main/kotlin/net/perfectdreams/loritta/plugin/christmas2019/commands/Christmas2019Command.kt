package net.perfectdreams.loritta.plugin.christmas2019.commands

import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019
import net.perfectdreams.loritta.tables.Christmas2019Players
import net.perfectdreams.loritta.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class Christmas2019Command : LorittaDiscordCommand(arrayOf("natal"), CommandCategory.MAGIC) {
	private val logger = KotlinLogging.logger {}

	override fun getDescription(locale: BaseLocale) = "Natal 2019"

	@Subcommand
	suspend fun root(context: DiscordCommandContext) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "participar") {
			if (!Christmas2019.isEventActive()) {
				context.sendMessage("Natal já acabou, sorry ;w;")
				return
			}

			transaction(Databases.loritta) {
				Christmas2019Players.insert {
					it[user] = context.lorittaUser.profile.id
				}
			}

			context.sendMessage("""Obrigada por querer me ajudar a fazer uma festa de Natal para a Loritta! Sem você, eu não iria conseguir enfeitar tudo a tempo para o Natal... ${Emotes.LORI_CRYING}
				|
				|Você pode ver o progresso utilizando `natal`, para conseguir os enfeites de natal, fique de olho em chats ativos! Ao encontrar algo, reaja para coletá-ló! 
				|
				|Coisas de Natal apenas caem de mensagens de usuários que também estão participando do evento, então convide seus amigxs para participar!
				|
				|Feliz Natal! ${Emotes.LORI_TEMMIE} https://loritta.website/assets/img/fanarts/lori_natal_3.png
			""".trimMargin())
			return
		}

		val isAlreadyParticipating = transaction(Databases.loritta) {
			Christmas2019Players.select {
				Christmas2019Players.user eq context.lorittaUser.profile.id
			}.count() != 0
		}

		if (isAlreadyParticipating) {
			if (arg0 == "loriplz") {
				if (!context.isPrivateChannel) {
					context.reply(
							LoriReply(
									"grrr use este comando via mensagem direta!!",
									Constants.ERROR
							)
					)
					return
				}

				val replies = mutableListOf<LoriReply>()

				logger.info { "Initializing recovery for ${context.userHandle.idLong}" }

				transaction(Databases.loritta) {
					val candies = transaction(Databases.loritta) {
						CollectedChristmas2019Points.select {
							CollectedChristmas2019Points.user eq context.userHandle.idLong
						}.count()
					}

					if (context.userHandle.idLong !in Christmas2019.recoveredDreams) {
						val candiesInBrokenZone = transaction(Databases.loritta) {
							CollectedChristmas2019Points.select {
								CollectedChristmas2019Points.user eq context.userHandle.idLong and
										(CollectedChristmas2019Points.guildId eq 297732013006389252L) and
										(CollectedChristmas2019Points.messageId lessEq 661717939174637618L) and
										(CollectedChristmas2019Points.messageId greaterEq 661702816980795392L)
							}.count()
						}

						val howMuchDreams = (candiesInBrokenZone / 125) * 10_000

						if (howMuchDreams >= 0) {
							context.lorittaUser.profile.money += howMuchDreams

							replies.add(
									LoriReply(
											"$howMuchDreams sonhos"
									)
							)
							logger.info { "Recovered $howMuchDreams dreams for ${context.userHandle.idLong}" }
						}

						Christmas2019.recoveredDreams.add(context.userHandle.idLong)
					}


					if (candies >= 3000) {
						val dKey = DonationKey.find {
							DonationKeys.userId eq context.userHandle.idLong and (DonationKeys.value eq 199.99)
						}.firstOrNull()


						if (dKey == null) {
							DonationKey.new {
								this.userId = context.userHandle.idLong
								this.expiresAt = System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLISECONDS
								this.value = 199.99
							}
							replies.add(
									LoriReply(
											"Key de Doador R$ 199,99"
									)
							)
							logger.info { "Recovered Donation Key for ${context.userHandle.idLong}" }
						}
					}

					if (candies >= 2500 && !context.lorittaUser.profile.settings.boughtProfiles.contains("LorittaChristmas2019ProfileCreator")) {
						context.lorittaUser.profile.settings.boughtProfiles += "LorittaChristmas2019ProfileCreator"
						replies.add(
								LoriReply(
										"Perfil Animado de Natal"
								)
						)
						logger.info { "Recovered Animated Profile for ${context.userHandle.idLong}" }
					}

					if (candies >= 777 && !context.lorittaUser.profile.settings.boughtProfiles.contains("Christmas2019ProfileCreator")) {
						context.lorittaUser.profile.settings.boughtProfiles += "Christmas2019ProfileCreator"
						replies.add(
								LoriReply(
										"Perfil de Natal"
								)
						)
						logger.info { "Recovered Profile for ${context.userHandle.idLong}" }
					}
				}

				if (replies.isEmpty()) {
					context.reply(
							LoriReply(
									"Eu não preciso recuperar nada para você, bye bye!!",
									Constants.ERROR
							)
					)
				} else {
					replies.add(
							0,
							LoriReply(
									"Itens Recuperados:"
							)
					)

					context.reply(
							*replies.toTypedArray()
					)
				}
				return
			}

			val candies = transaction(Databases.loritta) {
				CollectedChristmas2019Points.select {
					CollectedChristmas2019Points.user eq context.userHandle.idLong
				}.count()
			}

			context.reply(
					LoriReply(
							"Você já pegou **$candies presentes**!",
							"<:lori_gift:653402818199158805>"
					)
			)
		} else {
			if (!Christmas2019.isEventActive()) {
				context.sendMessage("Natal já acabou, sorry ;w;")
				return
			}
			
			context.sendMessage("""Olá ${context.userHandle.asMention}! ${Emotes.LORI_OWO}
			|
			|Na verdade quem está falando aqui não é a Lori, e sim a Pantufa, a melhor amiga dela. ${Emotes.LORI_SMILE}
			|
			|Eu queria fazer uma festa de Natal para a Loritta, como ela é muito ocupada ela nem teve tempo para enfeitar a casa dela... e por isso eu queria enfeitar a casa dela e fazer uma festa de Natal para ela! ${Emotes.LORI_HEART}
			|
			|Por isso eu estou aqui pedindo para que **você** me ajude a conseguir presentes para conseguir fazer uma festa de Natal que ela jamais irá esquecer! ${Emotes.DEFAULT_DANCE}
			|
			|Você percebeu como as pessoas deixam cair coisas natalinas no chão? Então, pegue os presentes (<:lori_gift:653402818199158805>) das mensagens para mim reagindo neles!
			|
			|**As suas recompensas:**
			|• **A cada 125 presentes:** 10k sonhos (Até 2000 presentes)
			|• **400 presentes:** Um emblema exclusivo no seu `+perfil`
			|• **777 presentes e 2500 presentes:** Um design para o `+perfil` exclusivo de Natal, que você pode ativar em <https://loritta.website/user/@me/dashboard/profiles>
			|Achou legal? Use `natal participar` para entrar na brincadeira e me ajudar a fazer uma festa de Natal incrível para a Lori! ${Emotes.LORI_TEMMIE}
		""".trimMargin())
		}
	}
}