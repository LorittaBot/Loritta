package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.util.*
import net.perfectdreams.loritta.morenitta.LorittaBot

class AvatarCommand(loritta: LorittaBot) : AbstractCommand(loritta, "avatar", category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.avatar"
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.avatar.description")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = true
			}
		}
	}

	override fun getExamplesKey() = LocaleKeyData("commands.command.avatar.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "user avatar")

		var getAvatar = context.getUserAt(0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
		}

		val embed = EmbedBuilder()
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setDescription("**${context.locale["$LOCALE_PREFIX.clickHere", "${getAvatar.effectiveAvatarUrl}?size=2048"]}**")

		// Easter Egg: Pantufa
		if (getAvatar.idLong == 390927821997998081L)
			embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.pantufaCute"]}* ${Emotes.LORI_TEMMIE}")

		// Easter Egg: Gabriela
		if (getAvatar.idLong == 481901252007952385L)
			embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.gabrielaCute"]}* ${Emotes.LORI_PAT}")

		// Easter Egg: Pollux
		if (getAvatar.idLong == 271394014358405121L || getAvatar.idLong == 354285599588483082L || getAvatar.idLong == 578913818961248256L)
			embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.polluxCute"]}* ${Emotes.LORI_HEART}")

		// Easter Egg: Loritta
		if (getAvatar.id == loritta.config.loritta.discord.applicationId.toString()) {
			val calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.LORITTA_TIMEZONE))
			val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

			embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.lorittaCute"]}* ${Emotes.LORI_SMILE}")
		}

		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage("${getAvatar.effectiveAvatarUrl}?size=2048")
		context.sendMessage(context.getAsMention(true), embed.build())
	}
}