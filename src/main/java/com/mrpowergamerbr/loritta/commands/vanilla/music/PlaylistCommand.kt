package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

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
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val embed = LorittaUtilsKotlin.createPlaylistInfoEmbed(context)
		val message = context.sendMessage(embed)
		context.metadata.put("currentTrack", manager.scheduler.currentTrack) // Salvar a track atual
		if (manager.scheduler.currentTrack != null) { // Só adicione os reactions caso esteja tocando alguma música
			message.addReaction("\uD83E\uDD26").complete()
			message.addReaction("\uD83D\uDCBF").complete();
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		LorittaUtilsKotlin.handleMusicReaction(context, e, msg)
	}
}