package net.perfectdreams.loritta.plugin.christmas2019.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019
import net.perfectdreams.loritta.tables.Christmas2019Players
import net.perfectdreams.loritta.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class Christmas2019Command : LorittaDiscordCommand(arrayOf("natal"), CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale) = "Natal 2019"

	@Subcommand
	suspend fun root(context: DiscordCommandContext) {
		if (!Christmas2019.isEventActive()) {
			context.sendMessage("Natal já acabou, sorry ;w;")
			return
		}

		val arg0 = context.args.getOrNull(0)

		if (arg0 == "participar") {
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