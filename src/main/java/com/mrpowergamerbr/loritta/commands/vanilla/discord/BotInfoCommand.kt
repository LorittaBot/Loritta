package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

class BotInfoCommand : AbstractCommand("botinfo") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("BOTINFO_DESCRIPTION")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val embed = EmbedBuilder()

		var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime

		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val sb = StringBuilder(64)
		sb.append(days)
		sb.append("d ")
		sb.append(hours)
		sb.append("h ")
		sb.append(minutes)
		sb.append("m ")
		sb.append(seconds)
		sb.append("s")

		embed.setAuthor("${locale["BOTINFO_TITLE"]} 💁", "https://loritta.website/", "https://loritta.website/assets/img/loritta_gabizinha_v1.png")
		embed.setThumbnail("https://loritta.website/assets/img/loritta_gabizinha_v1.png")
		embed.setColor(Color(0, 193, 223))
		embed.setDescription(locale["BOTINFO_EMBED_INFO", lorittaShards.getGuilds().size, LorittaLauncher.loritta.lorittaShards.getUsers().size, sb.toString(), LorittaLauncher.loritta.commandManager.commandMap.size])
		embed.addField("\uD83C\uDFC5 ${context.locale.get("BOTINFO_HONORABLE_MENTIONS")}", context.locale.get("BOTINFO_MENTIONS", context.userHandle.name, context.userHandle.discriminator), false)
		embed.setFooter("${locale["BOTINFO_CREATEDBY"]} - https://mrpowergamerbr.com/", lorittaShards.getUserById("123170274651668480")!!.effectiveAvatarUrl)
		context.sendMessage(embed.build())
	}
}