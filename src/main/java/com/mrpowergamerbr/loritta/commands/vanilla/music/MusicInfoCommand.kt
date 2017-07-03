package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

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
		return CommandCategory.MUSIC
	}

	override fun run(context: CommandContext) {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (context.args.size == 1) {
			if (context.args[0].equals("playlist", ignoreCase = true)) {
				val embed = createPlaylistInfoEmbed(context)
				val message = context.sendMessage(embed)
				context.metadata.put("currentTrack", manager.scheduler.currentTrack) // Salvar a track atual
				if (manager.scheduler.currentTrack != null) { // Só adicione os reactions caso esteja tocando alguma música
					message.addReaction("\uD83E\uDD26").complete()
					message.addReaction("\uD83D\uDCBF").complete();
				}
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
				val embed = createTrackInfoEmbed(context)
				val message = context.sendMessage(embed)
				context.metadata.put("currentTrack", manager.scheduler.currentTrack) // Salvar a track atual
				message.addReaction("\uD83E\uDD26").complete()
				message.addReaction("\uD83D\uDD22").complete();
			}
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.reactionEmote.name != "\uD83E\uDD26") { // Se é diferente de facepalm...
			if (context.handle == e.member) { // Então só deixe quem exectou o comando mexer!
				if (e.reactionEmote.name == "\uD83D\uDD22") {
					msg.editMessage(createPlaylistInfoEmbed(context)).complete()
					msg.reactions.forEach {
						if (it.emote.name != "\uD83E\uDD26") {
							it.removeReaction().complete()
						}
					}
					e.reaction.removeReaction(e.user).complete()
					msg.addReaction("\uD83D\uDCBF").complete();
				} else if (e.reactionEmote.name == "\uD83D\uDCBF") {
					val embed = createTrackInfoEmbed(context)
					msg.reactions.forEach {
						if (it.emote.name != "\uD83E\uDD26") {
							it.removeReaction().complete()
						}
					}
					e.reaction.removeReaction(e.user).queue()
					msg.editMessage(embed).complete()
					msg.addReaction("\uD83D\uDD22").queue();
				}
			}
		} else { // Se for facepalm...
			val atw = context.metadata.get("currentTrack") as AudioTrackWrapper
			val count = e.reaction.users.complete().filter { !it.isBot }.size
			val conf = context.config

			if (count > 0 && conf.musicConfig.voteToSkip && LorittaLauncher.loritta.getGuildAudioPlayer(e.guild).scheduler.currentTrack === atw) {
				val vc = e.guild.getVoiceChannelById(conf.musicConfig.musicGuildId)

				if (e.reactionEmote.name != "\uD83E\uDD26") { // Só permitir reactions de "facepalm"
					return
				}

				if (e.member.voiceState.channel !== vc) {
					e.reaction.removeReaction(e.user).complete()
					return
				}

				if (vc != null) {
					val inChannel = vc.members.filter{ !it.user.isBot }.size
					val required = Math.round(inChannel.toDouble() * (conf.musicConfig.required.toDouble() / 100))

					if (count >= required) {
						LorittaLauncher.getInstance().skipTrack(e.guild)
						e.textChannel.sendMessage("🤹 Música pulada!").complete()
					}
				}
			}
		}
	}

	fun createTrackInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val playingTrack = manager.player.playingTrack;
		val metaTrack = manager.scheduler.currentTrack;
		val embed = EmbedBuilder()
		embed.setTitle("\uD83C\uDFB5 ${playingTrack.info.title}", playingTrack.info.uri)
		embed.setColor(Color(93, 173, 236))
		val millis = playingTrack.duration

		val fancy = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
		);

		val elapsedMillis = playingTrack.position;

		val elapsed = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(elapsedMillis),
				TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis))
		);

		embed.addField("\uD83D\uDD52 Duração", "`$elapsed`/`$fancy`", true);

		if (playingTrack.sourceManager.sourceName == "youtube") {
			// Se a source é do YouTube, então vamos pegar informações sobre o vídeo!
			embed.addField("\uD83D\uDCFA Visualizações", metaTrack.metadata.get("viewCount"), true);
			embed.addField("\uD83D\uDE0D Gostei", metaTrack.metadata.get("likeCount"), true);
			embed.addField("\uD83D\uDE20 Não Gostei", metaTrack.metadata.get("dislikeCount"), true);
			embed.addField("\uD83D\uDCAC Comentários", metaTrack.metadata.get("commentCount"), true);
			embed.setThumbnail(metaTrack.metadata.get("thumbnail"))
			embed.setAuthor("${playingTrack.info.author}", null, metaTrack.metadata.get("channelIcon"))
		}

		embed.addField("\uD83D\uDCAB Quer pular a música?", "**Então use \uD83E\uDD26 nesta mensagem!** (Se 75% das pessoas no canal de música reagirem com \uD83E\uDD26, eu irei pular a música!)", false)
		return embed.build()
	}

	fun createPlaylistInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val embed = EmbedBuilder()

		embed.setTitle("\uD83C\uDFB6 Na fila...")
		embed.setColor(Color(93, 173, 236))

		val songs = manager.scheduler.queue.toList()
		val currentTrack = manager.scheduler.currentTrack
		if (currentTrack != null) {
			var text = "[${currentTrack.track.info.title}](${currentTrack.track.info.uri}) (pedido por ${currentTrack.user.asMention})\n";
			text += songs.joinToString("\n", transform = { "[${it.track.info.title}](${it.track.info.uri}) (pedido por ${it.user.asMention})" })
			embed.setDescription(text)
		} else {
			embed.setDescription("Nenhuma música...");
		}
		return embed.build();
	}
}