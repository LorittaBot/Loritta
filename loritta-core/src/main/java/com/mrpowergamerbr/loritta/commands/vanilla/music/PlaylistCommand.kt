package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.api.commands.CommandCategory

class PlaylistCommand : AbstractCommand("playlist", listOf("list", "queue"), CommandCategory.MUSIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("PLAYLIST_DESCRIPTION")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)
		val embed = LorittaUtilsKotlin.createPlaylistInfoEmbed(context)
		val message = context.sendMessage(embed)
		if (manager.scheduler.currentTrack != null) { // Só adicione os reactions caso esteja tocando alguma música
			context.metadata.put("currentTrack", manager.scheduler.currentTrack!!) // Salvar a track atual
			message.onReactionAddByAuthor(context) {
				LorittaUtilsKotlin.handleMusicReaction(context, it, message)
			}
			message.addReaction("\uD83E\uDD26").queue()
			message.addReaction("\uD83D\uDCBF").queue()
		}
	}
}