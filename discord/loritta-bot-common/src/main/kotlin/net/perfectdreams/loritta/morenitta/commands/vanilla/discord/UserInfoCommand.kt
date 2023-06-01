package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.UserCommand
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks

class UserInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "userinfo", listOf("memberinfo"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.userinfo.description")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
                        LorittaReply(
                                locale["commands.command.userinfo.unknownUser", context.args[0].stripCodeMarks()],
                                Constants.ERROR
                        )
				)
				return
			}
			user = context.userHandle
		}

		val member = if (context.guild.isMember(user)) {
			context.guild.getMember(user)
		} else {
			null
		}

		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "user info")

		context.sendMessage(
			MessageCreate {
				apply(UserCommand.createUserInfoMessage(CommandContextCompat.LegacyMessageCommandContextCompat(context), UserAndMember(user, member)))
			}
		)
	}
}