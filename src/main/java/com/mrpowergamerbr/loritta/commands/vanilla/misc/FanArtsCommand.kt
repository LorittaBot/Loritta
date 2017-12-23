package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaFanArt
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder

class FanArtsCommand : AbstractCommand("fanarts") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["FANARTS_Description"]
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var list = loritta.fanArts.shuffled()

		sendFanArtEmbed(context, locale, list, 0)
	}

	fun sendFanArtEmbed(context: CommandContext, locale: BaseLocale, list: List<LorittaFanArt>, item: Int) {
		val fanArt = list[item]
		val embed = EmbedBuilder().apply {
			setTitle("\uD83D\uDDBC<:loritta:331179879582269451> Fan Art")

			val user = lorittaShards.retriveUserById(fanArt.artistId)!!

			val displayName = fanArt.fancyName ?: user.name
			val discord = user.name + "#" + user.discriminator

			setDescription(locale["FANARTS_EmbedDescription", displayName, discord])
			setImage("https://loritta.website/assets/img/fanarts/${fanArt.fileName}")
			setColor(Constants.LORITTA_AQUA)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		var allowForward = false
		var allowBack = false
		if (item != 0) {
			message.addReaction("⏪").complete()
			allowBack = true
		}
		if (list.size > item + 1) {
			message.addReaction("⏩").complete()
			allowForward = true
		}

		message.onReactionAddByAuthor(context) {
			message.delete().complete()

			if (allowForward && it.reactionEmote.name == "⏩") {
				sendFanArtEmbed(context, locale, list, item + 1)
			}
			if (allowBack && it.reactionEmote.name == "⏪") {
				sendFanArtEmbed(context, locale, list, item - 1)
			}
		}
	}
}