package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils

class MusicInfoCommand : CommandBase() {
	override fun getLabel(): String {
		return "tocando"
	}

	override fun getDescription(): String {
		return "Fala a música que está tocando agora."
	}

	override fun getExample(): List<String> {
		return listOf("", "playlist", "todos")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (context.args.size == 1) {
			if (context.args[0].equals("playlist", ignoreCase = true)) {
				val songs = manager.scheduler.queue.toList() // Para não remover tudo da nossa BlockingQueue
				var txt = "Na fila...\n"
				if (manager.player.playingTrack == null) {
					txt = "Não tem nenhuma música na fila..."
				} else {
					txt += "▶ " + manager.player.playingTrack.info.title + " (" + manager.scheduler.currentTrack.user.name + ")\n"
				}
				for (song in songs) {
					txt += "⏸ " + song.track.info.title + " (" + song.user.name + ")\n"
				}
				context.sendMessage(context.getAsMention(true) + txt)
			}
			if (context.args[0].equals("todos", ignoreCase = true)) {
				var txt = "Em outras quebradas por aí...\n"
				for (mm in LorittaLauncher.loritta.musicManagers.values) {
					if (mm.player.playingTrack != null) {
						txt += "**" + mm.scheduler.guild.name + "** ▶ " + mm.player.playingTrack.info.title + " (pedido por " + mm.scheduler.currentTrack.user.name + ")\n"
					}
				}
				context.sendMessage(context.getAsMention(true) + txt)
			}
		} else {
			if (!context.config.musicConfig.isEnabled) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " O meu sistema de músicas está desativado nesta guild... Pelo visto não teremos a `DJ Loritta` por aqui... \uD83D\uDE1E")
				return
			}
			if (manager.player.playingTrack == null) {
				context.sendMessage(context.getAsMention(true) + "Nenhuma música está tocando... Que tal tocar uma? `+tocar música`")
			} else {
				val message = context.sendMessage(context.getAsMention(true) + "Atualmente estou tocando " + manager.player.playingTrack.info.title + " [" + (manager.player.playingTrack.duration - manager.player.playingTrack.position) / 1000 + "s]! (pedido por " + manager.scheduler.currentTrack.user.name + ") <" + manager.player.playingTrack.info.uri + ">" + if (context.config.musicConfig.voteToSkip) "\n\uD83D\uDCAB **Quer pular a música? Então use \uD83E\uDD26 nesta mensagem!** (Se 75% das pessoas no canal de música reagirem com \uD83E\uDD26, eu irei pular a música!)" else "")
				LorittaLauncher.loritta.musicMessagesCache.put(message.id, manager.scheduler.currentTrack)
				message.addReaction("\uD83E\uDD26").complete()
			}
		}
	}
}
