package com.mrpowergamerbr.loritta.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import lavalink.client.player.IPlayer
import lavalink.client.player.LavalinkPlayer
import lavalink.client.player.event.PlayerEventListenerAdapter
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
/**
 * @param player The audio player this scheduler uses
 */
class TrackScheduler(val guild: Guild, val player: LavalinkPlayer) : PlayerEventListenerAdapter() {
	val queue: BlockingQueue<AudioTrackWrapper>
	var currentTrack: AudioTrackWrapper? = null
	var isLoadingNextTrack = false

	init {
		this.queue = LinkedBlockingQueue()
	}

	override fun onTrackStart(player: IPlayer, track: AudioTrack) {
		loritta.executor.execute {
			val serverConfig = loritta.getServerConfigForGuild(guild.id)

			if (serverConfig.musicConfig.logToChannel) {
				val textChannel = guild.getTextChannelById(serverConfig.musicConfig.channelId)

				if (textChannel.canTalk() && guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS)) {
					LorittaUtilsKotlin.fillTrackMetadata(currentTrack ?: return@execute)
					if (currentTrack!!.metadata.isNotEmpty()) {
						val embed = LorittaUtilsKotlin.createTrackInfoEmbed(guild, LorittaLauncher.loritta.getLegacyLocaleById(serverConfig.localeId), true)

						textChannel.sendMessage(embed).queue()
					}
				}
			}
		}
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	fun queue(track: AudioTrackWrapper, config: MongoServerConfig) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		if (player.playingTrack != null && currentTrack != null && !currentTrack!!.isAutoPlay) { // Quem liga para músicas do autoplay? Cancele ela agora!
			queue.offer(track)
		} else {
			currentTrack = track
			player.playTrack(track.track)
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	fun nextTrack() {
		// A música irá trocar! Então iremos retirar a nossa currentTrack
		currentTrack = null

		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		val audioTrackWrapper = queue.poll()

		if (audioTrackWrapper == null) { // Caso seja null, quer dizer que não existe "próxima" música na fila, então vamos parar a atual
			// Mas "nextTrack" pode ser chamado ao usar "pular"
			// Ou seja...
			if (player.playingTrack != null) // Se está tocando uma música
				player.stopTrack() // Vamos parar ela!

			// Para evitar colocar várias músicas na playlist em seguida, vamos verificar se estamos carregando a próxima música antes de fazer algo
			isLoadingNextTrack = true
			loritta.executor.execute { // Vamos agora verificar se devemos tocar uma música aleatória
				val serverConfig = loritta.getServerConfigForGuild(guild.id)

				// Então quer dizer que nós iniciamos uma música vazia?
				// Okay então, vamos pegar nossas próprias coisas
				LorittaUtilsKotlin.startRandomSong(guild, serverConfig)
				isLoadingNextTrack = false
			}
			return
		}

		this.currentTrack = audioTrackWrapper // Se não, vamos tocar a próxima música da fila!
		player.playTrack(audioTrackWrapper.track)
	}

	override fun onTrackEnd(player: IPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack()
		}
	}
}