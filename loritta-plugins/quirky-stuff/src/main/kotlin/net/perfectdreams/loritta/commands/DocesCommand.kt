package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.Halloween2019
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.BoostedCandyChannels
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Halloween2019Players
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DocesCommand : LorittaDiscordCommand(arrayOf("doces"), CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale) = "Halloween 2019"

	@Subcommand
	suspend fun root(context: DiscordCommandContext) {
		val calendar = Calendar.getInstance()

		if (calendar.get(Calendar.MONTH) != 9) {
			context.sendMessage("Halloween já acabou, sorry ;w;")
			return
		}

		val arg0 = context.args.getOrNull(0)

		if (!context.isPrivateChannel) {
			if (arg0 == "boost") {
				if (calendar.get(Calendar.DAY_OF_MONTH) in 29..31 || loritta.config.isOwner(context.userHandle.idLong)) {
					val howMuchDidTheUserCollect = transaction(Databases.loritta) {
						CollectedCandies.select {
							(CollectedCandies.user eq context.lorittaUser.profile.id.value)
						}.count()
					}

					if (howMuchDidTheUserCollect >= Halloween2019.BOOST_LEVEL || loritta.config.isOwner(context.userHandle.idLong)) {
						val arg1 = context.args.getOrNull(1)

						if (arg1 == null) {
							context.reply(
									LoriReply(
											"Mencione o canal que você deseja dar boost! `${context.config.commandPrefix}doces boost #CanalAqui`"
									)
							)
							return
						}

						var channel: TextChannel? = null

						val channels = context.discordGuild!!.getTextChannelsByName(arg1, true)

						if (channels.isNotEmpty()) {
							channel = channels[0]
						} else {
							val id = arg1
									.replace("<", "")
									.replace("#", "")
									.replace(">", "")

							if (id.isValidSnowflake()) {
								channel = context.discordGuild!!.getTextChannelById(id)
							}
						}

						if (channel == null) {
							context.reply(
									LoriReply(
											"Canal inválido!",
											Constants.ERROR
									)
							)
							return
						}

						if (!channel.canTalk()) {
							context.reply(
									LoriReply(
											"Eu não posso falar no canal de texto!",
											Constants.ERROR
									)
							)
							return
						}

						if (!channel.canTalk(context.handle)) {
							context.reply(
									LoriReply(
											"Você não pode falar no canal de texto!",
											Constants.ERROR
									)
							)
							return
						}

						val boostChannel = transaction(Databases.loritta) {
							BoostedCandyChannels.select {
								BoostedCandyChannels.channelId eq channel.idLong and (BoostedCandyChannels.expiresAt greaterEq System.currentTimeMillis())
							}.firstOrNull()
						}

						if (boostChannel != null) {
							context.reply(
									LoriReply(
											"Já estão dando boost no canal que você escolheu!",
											Constants.ERROR
									)
							)
							return
						}

						val userBoost = transaction(Databases.loritta) {
							BoostedCandyChannels.select {
								BoostedCandyChannels.user eq context.userHandle.idLong and (BoostedCandyChannels.givenAt greaterEq System.currentTimeMillis() - 3_600_000)
							}.firstOrNull()
						}

						if (userBoost != null) {
							context.reply(
									LoriReply(
											"Você precisa esperar uma hora desde o seu último boost para poder dar um boost novamente!"
									)
							)
							return
						}

						transaction(Databases.loritta) {
							BoostedCandyChannels.insert {
								it[BoostedCandyChannels.user] = context.lorittaUser.profile.id
								it[guildId] = context.guild!!.id
								it[channelId] = channel.idLong
								it[givenAt] = System.currentTimeMillis()
								it[expiresAt] = System.currentTimeMillis() + 300_000
							}
						}

						context.reply(
								LoriReply(
										"O boost foi dado! ${Emotes.LORI_HAPPY}"
								)
						)

						channel.sendMessageAsync(
								"${context.userHandle.asMention} deu um **boost de doces** no canal! Doces terão 10x mais chance de cair durante cinco minutos! ${Halloween2019.CANDIES.joinToString(" ")}"
						)
					} else {
						context.reply(
								LoriReply(
										"Cadê a sua máquina de doces? É necessário ter uma para conseguir dar boost em canais! Me disseram que você consegue uma ao conseguir **1500 doces**..."
								)
						)
					}
				} else {
					context.reply(
							LoriReply(
									"As máquinas ainda não estão prontas! A sociedade atual não irá conseguir aguentar este poder divino..."
							)
					)
				}
				return
			}

			context.reply(
					LoriReply(
							"Tais segredos só podem ser revelados via mensagem direta. \uD83D\uDC40"
					)
			)
			return
		}

		if (arg0 == "participar") {
			transaction(Databases.loritta) {
				Halloween2019Players.insert {
					it[user] = context.lorittaUser.profile.id
				}
			}

			context.sendMessage("""Obrigada por querer me ajudar a conseguir doces, finalmente alguém quis me ajudar a saciar a minha vontade de comer doces nesse Halloween. ${Emotes.LORI_CRYING}
				|
				|Você pode ver quantos doces você possui utilizando `doces`, para conseguir doces, fique de olho nos doces (${Halloween2019.CANDIES.joinToString(" ")}) que caem por aí, ao encontrar um, reaja para conseguir um doce!
				|
				|Doces apenas caem de mensagens de usuários que também estão participando do evento, então convide seus amigxs para participar!
				|
				|Feliz Halloween! ${Emotes.LORI_TEMMIE} https://cdn.discordapp.com/attachments/583406099047252044/634593178871267348/loriwtc3.png
			""".trimMargin())
			return
		}

		val isAlreadyParticipating = transaction(Databases.loritta) {
			Halloween2019Players.select {
				Halloween2019Players.user eq context.lorittaUser.profile.id
			}.count() != 0
		}

		if (isAlreadyParticipating) {
			val candies = transaction(Databases.loritta) {
				CollectedCandies.select {
					CollectedCandies.user eq context.lorittaUser.profile.id
				}.count()
			}

			context.reply(
					LoriReply(
							"Você possui **$candies Doces**!",
							"\uD83C\uDF6C"
					)
			)
		} else {
			context.sendMessage("""Olá ${context.asMention}! ${Emotes.LORI_OWO}
			|
			|Você conseguiu encontrar a minha mensagem secreta, pensava que ninguém iria conseguir encontrar! Pelo ou menos posso dormir feliz sabendo que a minha mensagem não foi em vão. ${Emotes.LORI_SMILE}
			|
			|Mas vamos direto ao assunto: Eu queria participar do Halloween deste ano, vestir fantasia, pegar doces... Mas infelizmente eu não posso, já que se eu ficar offline, as pessoas começam a me xingar... <:sad_cat6:585951728365600779>
			|
			|Por isso eu estou aqui pedindo para que **você** me ajude a conseguir doces neste ano! ${Emotes.DEFAULT_DANCE}
			|
			|Você percebeu como as pessoas deixam cair doces no chão? Então, pegue os doces (${Halloween2019.CANDIES.joinToString(" ")}) das mensagens para mim reagindo neles!
			|
			|**As suas recompensas:**
			|• **A cada 125 doces:** 10k sonhos
			|• **400 doces:** Um emblema exclusivo no seu `+perfil`
			|• **777 doces:** Um design para o `+perfil` exclusivo de Halloween, que você pode ativar em <https://loritta.website/user/@me/dashboard/profiles>
			|
			|Achou legal? Use `doces participar` para entrar na brincadeira e me ajudar a esbaldar em doces! ${Emotes.LORI_TEMMIE}
		""".trimMargin())
		}
	}
}