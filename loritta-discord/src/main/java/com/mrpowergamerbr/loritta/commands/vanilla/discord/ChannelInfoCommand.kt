package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class ChannelInfoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("channelinfo", "channel"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.channelinfo.description")

		arguments {
			argument(ArgumentType.TEXT) {
				optional = true
			}
		}

		examples {
			listOf(
					"",
					"297732013006389252"
			)
		}

		canUseInPrivateChannel = false

		executesDiscord {
			val context = this
			val channelId = args.getOrNull(0)
					?.replace("<#", "")
					?.replace(">", "")
					?: context.discordMessage.channel.id
			val channel = context.guild.getTextChannelById(channelId)!!

			val channelCreatedDiff = DateUtils.formatDateDiff(channel.timeCreated.toInstant().toEpochMilli(), loritta.getLegacyLocaleById(context.serverConfig.localeId))

			val builder = EmbedBuilder()

			val channelTopic = if (channel.topic == null) {
				"Tópico não definido!"
			} else {
				"```\n${channel.topic}```"
			}

			builder.setColor(Constants.DISCORD_BLURPLE)
			builder.setTitle("\uD83D\uDC81 ${context.locale["$LOCALE_PREFIX.channelinfo.channelInfo", "#${channel.name}"]}")
			builder.setDescription(channelTopic)
			builder.addField("\uD83D\uDD39 ${context.locale["$LOCALE_PREFIX.channelinfo.channelMention"]}", "`${channel.asMention}`", true)
			builder.addField("\uD83D\uDCBB ${context.locale["$LOCALE_PREFIX.userinfo.discordId"]}", "`${channel.id}`", true)
			builder.addField("\uD83D\uDD1E NSFW", if (channel.isNSFW) context.locale["loritta.fancyBoolean.true"] else context.locale["loritta.fancyBoolean.false"], true)
			builder.addField("\uD83D\uDCC5 ${context.locale["$LOCALE_PREFIX.channelinfo.channelCreated"]}", channelCreatedDiff, true)
			builder.addField("\uD83D\uDD39 Guild", "`${channel.guild.name}`", true)
			context.sendMessage(context.user.asMention, builder.build())
		}
	}
}