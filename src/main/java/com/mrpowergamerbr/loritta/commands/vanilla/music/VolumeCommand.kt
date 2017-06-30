package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.Permission
import java.util.*

class VolumeCommand : CommandBase() {
	override fun getLabel(): String {
		return "volume"
	}

	override fun getDescription(): String {
		return "Altera o volume da música"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("100", "66", "33")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun run(context: CommandContext) {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (context.args.size >= 1) {
			try {
				val vol = Integer.valueOf(context.args[0])!!
				if (vol > 100) {
					context.sendMessage(context.getAsMention(true) + " você quer ficar surdo? Bem, você pode querer, mas eu também estou escutando e eu não quero.")
					return
				}
				if (0 > vol) {
					context.sendMessage(context.getAsMention(true) + " não cara, colocar números negativos não irá deixar a música tão mutada que ela é banida do planeta terra.")
					return
				}
				if (manager.player.volume > vol) {
					context.sendMessage(context.getAsMention(true) + " irei diminuir o volume do batidão! Desculpe se eu te incomodei com a música alta...")
				} else {
					context.sendMessage(context.getAsMention(true) + " irei aumentar o volume do batidão! Se segura aí que agora você vai sentir as ondas sonoras!")
				}
				manager.player.volume = Integer.valueOf(context.args[0])!!
			} catch (e: Exception) {
				context.sendMessage(context.getAsMention(true) + " Ok, vamos alterar o volume para 💩 então... coloque um número válido por favor!")
			}
		} else {
			context.explain()
		}
	}
}