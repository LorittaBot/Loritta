package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor

class PlaylistCommand : AbstractCommand("playlist", listOf("list"), CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("PLAYLIST_DESCRIPTION")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)
		val embed = LorittaUtilsKotlin.createPlaylistInfoEmbed(context)
		val message = context.sendMessage(embed)
		if (manager.scheduler.currentTrack != null) { // Só adicione os reactions caso esteja tocando alguma música
			context.metadata.put("currentTrack", manager.scheduler.currentTrack!!) // Salvar a track atual
			message.onReactionAddByAuthor(context) {
				LorittaUtilsKotlin.handleMusicReaction(context, it, message)
			}
			message.addReaction("\uD83E\uDD26").complete()
			message.addReaction("\uD83D\uDCBF").complete()
		}
	}
}