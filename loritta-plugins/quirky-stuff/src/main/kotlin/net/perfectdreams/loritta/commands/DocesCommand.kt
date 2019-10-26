package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.Halloween2019
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Halloween2019Players
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DocesCommand : LorittaDiscordCommand(arrayOf("doces"), CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale) = "Halloween 2019"

	@Subcommand
	suspend fun root(context: DiscordCommandContext) {
		if (!context.isPrivateChannel)
			return

		val calendar = Calendar.getInstance()

		if (calendar.get(Calendar.MONTH) != 9) {
			context.sendMessage("Halloween já acabou, sorry ;w;")
			return
		}

		val arg0 = context.args.getOrNull(0)

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