package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

class BotInfoCommand : CommandBase() {
	override fun getLabel(): String {
		return "botinfo"
	}

	override fun getAliases(): List<String> {
		return listOf("info")
	}

	override fun getDescription(): String {
		return "Mostra informações interessantes (e algumas bem inúteis) sobre a Loritta."
	}

	override fun run(context: CommandContext) {
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

		embed.setAuthor("Olá, eu me chamo Loritta! 💁", "https://loritta.website/", "https://loritta.website/assets/img/loritta_guild_v4.png")
		embed.setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")
		embed.setColor(Color(0, 193, 223))
		embed.setDescription("Olá, eu me chamo Loritta (ou para amigos mais próximos, \"Lori\") e sou apenas um bot para o Discord fofo e com várias funcionalidades supimpas!\n\n" +
				"Eu estou em **${LorittaLauncher.getInstance().lorittaShards.getGuilds().size} servidores** e eu conheço **${LorittaLauncher.getInstance().lorittaShards.getUsers().size} pessoas diferentes** (Wow, quanta gente)! Eu fui feita usando **JDA** em **Java & Kotlin** e, se você quiser ver meu código-fonte, [clique aqui](http://bit.ly/lorittagit)!\n\n" +
				"Meu website é https://loritta.website/ e, se você quiser saber mais sobre mim, [clique aqui](http://bit.ly/lorittad) para entrar no meu servidor no Discord!\n\n" +
				"Já fazem **${sb.toString()}** desde que eu acordei \uD83D\uDE34 (ou seja, meu uptime atual) e atualmente eu tenho **${LorittaLauncher.getInstance().commandManager.commandMap.size} comandos diferentes**!")
		embed.addField("Menções Honrosas", "`MrPowerGamerBR#4185` Se não fosse por ele, eu nem iria existir!\n"
				+ "`Giovanna_GGold#2454 (Gabriela Giulian)` Ela que fez esta **linda** \uD83D\uDE0D arte minha da miniatura! [Clique aqui para ver o desenho!](https://loritta.website/assets/img/loritta_fixed_final_cropped.png) (e ela capturou toda a minha fofura & beleza \uD83D\uDE0A)!\n"
				+ "`" + context.userHandle.name + "#" + context.userHandle.discriminator + "` Por estar falando comigo! \uD83D\uDE04", false)
		embed.setFooter("Loritta foi criada por MrPowerGamerBR - https://mrpowergamerbr.com/", "https://mrpowergamerbr.com/assets/img/avatar.png")
		context.sendMessage(embed.build())
	}
}