package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory

class DiscriminatorCommand : AbstractCommand("discriminator", listOf("discrim", "discriminador"), category = CommandCategory.DISCORD) {
	override fun getUsage(): String {
		return "<usuário>"
	}

    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["DISCRIM_DESCRIPTION"]
    }

	override fun getExamples(): List<String> {
		return listOf("", "@Loritta")
	}

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var user = context.userHandle
		var discriminator = user.discriminator

		val userFromContext = context.getUserAt(0)

		if (userFromContext != null) {
			user = userFromContext
			discriminator = user.discriminator
		} else if (context.args.isNotEmpty()) {
			val arg0 = context.args[0]

			discriminator = arg0.replace(Regex("\\D+"),"")

			if (discriminator.length != 4) {
				context.reply(
						LoriReply(
								message = locale["DISCRIM_InvalidDiscriminator", arg0],
								prefix = Constants.ERROR
						)
				)
				return
			}
		}

		val users = LorittaLauncher.loritta.lorittaShards.getUsers().filter { it.discriminator == discriminator }

		sendDiscriminatorEmbed(context, locale, user, discriminator, users, 0)
    }

	suspend fun sendDiscriminatorEmbed(context: CommandContext, locale: LegacyBaseLocale, user: User, discriminator: String, users: List<User>, page: Int) {
		val embed = EmbedBuilder().apply {
			setColor(Constants.DISCORD_BLURPLE)
			setTitle("\uD83D\uDC81 ${locale["DISCRIM_UsersWithDiscriminator", discriminator]}")
			var description = locale["DISCRIM_EmbedDescription", users.size, discriminator, context.userHandle.discriminator]

			if (context.userHandle.discriminator == discriminator) {
				val randomNames = users.filter { it != user }
				description += " ${locale["DISCRIM_SelfDiscriminator", randomNames[Loritta.RANDOM.nextInt(randomNames.size)].name]}"
			}

			val display = users.subList((page * 10), Math.min(users.size, 10 * (page + 1)))

			description += "\n"
			for (user in display) {
				description += "\n`${user.name}#${user.discriminator}`"
			}
			setDescription(description)
		}

		val message = context.sendMessage(embed.build())

		var allowForward = false
		var allowBack = false
		if (page != 0) {
			message.addReaction("⏪").queue()
			allowBack = true
		}
		if (users.size > 10 * (page + 1)) {
			message.addReaction("⏩").queue()
			allowForward = true
		}

		message.onReactionAddByAuthor(context) {
			message.delete().queue()

			if (allowForward && it.reactionEmote.isEmote("⏩")) {
				sendDiscriminatorEmbed(context, locale, user, discriminator, users, page + 1)
			}
			if (allowBack && it.reactionEmote.isEmote("⏪")) {
				sendDiscriminatorEmbed(context, locale, user, discriminator, users, page - 1)
			}
		}
	}
}