package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.util.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.UserCommand

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

		val member = context.guildOrNull?.getMember(getAvatar)

		context.sendMessage(
			MessageCreate {
				apply(
					UserCommand.createAvatarMessage(
						CommandContextCompat.LegacyMessageCommandContextCompat(context),
						UserAndMember(getAvatar, member),
						UserCommand.Companion.AvatarTarget.GLOBAL_AVATAR
					)
				)
			}
		)
	}
}