package com.mrpowergamerbr.loritta.audio

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.misc.YouTubeUtils
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.io.Lavalink
import mu.KotlinLogging
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.managers.AudioManager
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

class AudioManager(val loritta: Loritta) {
	var playerManager = DefaultAudioPlayerManager()
	val musicManagers = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build<Long, GuildMusicManager>().asMap()
	var songThrottle = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(10L, TimeUnit.SECONDS).build<String, Long>().asMap()
	val playlistCache = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(100).build<String, AudioPlaylist>().asMap()
	val lavalink = Lavalink(Loritta.config.clientId, Loritta.config.shards, { shardId: Int -> lorittaShards.shards.first { shardId == it.shardInfo.shardId } })

	companion object {
		private val logger = KotlinLogging.logger {}
	}

	init {
		playerManager.frameBufferDuration = 1000
		playerManager.setItemLoaderThreadPoolSize(1000)

		AudioSourceManagers.registerRemoteSources(playerManager)
		AudioSourceManagers.registerLocalSource(playerManager)

		lavalink.addNode(URI("ws://127.0.0.1:2334"), Loritta.config.mixerWebhookSecret)
	}

	/**
	 * Returns the GuildMusicManager of a guild, if the GuildMusicManager instance doesn't exist, it will be created
	 *
	 * @param  guild the guild
	 * @return the guild music manager
	 * @see    GuildMusicManager
	 */
	@Synchronized
	fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
		val guildId = java.lang.Long.parseLong(guild.getId())
		var musicManager = musicManagers[guildId]

		if (musicManager == null) {
			musicManager = GuildMusicManager(guild)
			musicManagers[guildId] = musicManager
		}

		return musicManager
	}

	/**
	 * Checks and loads an specific track
	 *
	 * @param context        the context of the command
	 * @param trackUrl       the track URL
	 * @param alreadyChecked if we already searched for this track before (and failed)
	 * @param override       (optional) forces the song to be played
	 */
	fun loadAndPlay(context: CommandContext, trackUrl: String, alreadyChecked: Boolean = false, override: Boolean = false) {
		if (!alreadyChecked) {
			if (!checkVoiceChannelState(context))
				return
		}

		val channel = context.event.channel
		val guild = context.guild
		val musicConfig = context.config.musicConfig
		val musicManager = getGuildAudioPlayer(guild);

		context.guild.audioManager.isSelfMuted = false // Desmutar a Loritta
		context.guild.audioManager.isSelfDeafened = false // E desilenciar a Loritta

		if (playlistCache.containsKey(trackUrl)) {
			playPlaylist(context, musicManager, playlistCache[trackUrl]!!, override)
			return
		}

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				if (musicConfig.hasMaxSecondRestriction) { // Se esta guild tem a limitação de áudios...
					if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						val final = String.format("%02d:%02d", ((musicConfig.maxSeconds / 60) % 60), (musicConfig.maxSeconds % 60));
						channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MUSIC_MAX", final]).queue();
						return;
					}
				}
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_ADDED", track.info.title.stripCodeMarks().escapeMentions()]).queue()

				play(context, musicManager, AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()), override)
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				playlistCache[trackUrl] = playlist
				playPlaylist(context, musicManager, playlist, override)
			}

			override fun noMatches() {
				if (!alreadyChecked) {
					// Ok, não encontramos NADA relacionado a essa música
					// Então vamos pesquisar!
					val items = YouTubeUtils.searchVideosOnYouTube(trackUrl);

					if (items.isNotEmpty()) {
						loadAndPlay(context, items[0].id.videoId, true, override)
						return;
					}
				}
				channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MUSIC_NOTFOUND", trackUrl]).queue();
			}

			override fun loadFailed(exception: FriendlyException) {
				channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MUSIC_ERROR", exception.message]).queue();
			}
		})
	}

	/**
	 * Plays an playlist
	 *
	 * @param context        the context of the command
	 * @param musicManager   the guild music manager
	 * @param playlist       the playlist instance
	 * @param override       (optional) forces the song to be played
	 */
	fun playPlaylist(context: CommandContext, musicManager: GuildMusicManager, playlist: AudioPlaylist, override: Boolean = false) {
		val channel = context.event.channel
		val musicConfig = context.config.musicConfig

		if (!musicConfig.allowPlaylists && !override) { // Se esta guild NÃO aceita playlists
			var track = playlist.selectedTrack

			if (track == null) {
				track = playlist.tracks[0]
			}

			channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_ADDED", track.info.title.stripCodeMarks().escapeMentions()]).queue()

			play(context, musicManager, AudioTrackWrapper(track.makeClone(), false, context.userHandle, HashMap<String, String>()), override)
		} else { // Mas se ela aceita...
			var ignored = 0;
			for (track in playlist.tracks) {
				if (musicConfig.hasMaxSecondRestriction) {
					if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						ignored++;
						continue;
					}
				}

				play(context, musicManager, AudioTrackWrapper(track.makeClone(), false, context.userHandle, HashMap<String, String>()), override);
			}

			if (ignored == 0) {
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_PLAYLIST_ADDED", playlist.tracks.size]).queue()
			} else {
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_PLAYLIST_ADDED_IGNORED", playlist.tracks.size, ignored]).queue()
			}
		}
	}

	/**
	 * Checks and loads an specific track without providing feedback to the user, used in automatic playlists
	 *
	 * @param guild    the guild
	 * @param config     the configuration of the guild
	 * @param trackUrl the track URL
	 */
	fun loadAndPlayNoFeedback(guild: Guild, config: ServerConfig, trackUrl: String) {
		val musicManager = getGuildAudioPlayer(guild)

		if (playlistCache.contains(trackUrl)) {
			val playlist = playlistCache[trackUrl]!!
			loadAndPlayNoFeedback(guild, config, playlist.tracks[Loritta.RANDOM.nextInt(0, playlist.tracks.size)].info.uri)
			return
		}

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				play(guild, config, musicManager, AudioTrackWrapper(track, true, guild.selfMember.user, HashMap<String, String>()))
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				playlistCache[trackUrl] = playlist
				loadAndPlayNoFeedback(guild, config, playlist.tracks[Loritta.RANDOM.nextInt(0, playlist.tracks.size)].info.uri)
			}

			override fun noMatches() {
			}

			override fun loadFailed(exception: FriendlyException) {
			}
		})
	}

	fun play(context: CommandContext, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper, override: Boolean = false) {
		play(context.guild, context.config, musicManager, trackWrapper, override)
	}

	/**
	 * Plays a song
	 *
	 * @param guild        the guild
	 * @param conf         the configuration of the guild
	 * @param musicManager the guild music manager
	 * @param trackWrapper the wrapped instance of the track
	 * @param override     (optional) forces the song to be played
	 */
	fun play(guild: Guild, conf: ServerConfig, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper, override: Boolean = false) {
		if (musicManager.scheduler.queue.size > 25)
			return

		val musicGuildId = conf.musicConfig.musicGuildId!!

		if (override) {
			logger.info("Force Playing ${trackWrapper.track.info.title} - in guild ${guild.name} (${guild.id})")
		} else {
			logger.info("Playing ${trackWrapper.track.info.title} - in guild ${guild.name} (${guild.id})")
		}

		connectToVoiceChannel(musicGuildId, guild.audioManager);

		if (override) {
			musicManager.player.playTrack(trackWrapper.track)
		} else {
			musicManager.scheduler.queue(trackWrapper, conf)
		}
	}

	/**
	 * Skips the current track
	 *
	 * @param context        the context of the command
	 */
	fun skipTrack(context: CommandContext) {
		val musicManager = getGuildAudioPlayer(context.guild);
		musicManager.scheduler.nextTrack()

		context.reply(
				LoriReply(
						context.locale["PULAR_MUSICSKIPPED"],
						"\uD83E\uDD39"
				)
		)
	}

	/**
	 * Connects to a voice channel via ID
	 *
	 * @param id           the channel ID
	 * @param audioManager the audio manager
	 */
	fun connectToVoiceChannel(id: String, audioManager: AudioManager) {
		val link = loritta.audioManager.lavalink.getLink(audioManager.guild)
		if (audioManager.isConnected && audioManager.connectedChannel.id != id) { // Se a Loritta está conectada em um canal de áudio mas não é o que nós queremos...
			link.disconnect() // Desconecte do canal atual!
		}

		if (!audioManager.isAttemptingToConnect && audioManager.isConnected && !audioManager.guild.selfMember.voiceState.inVoiceChannel()) {
			// Corrigir bug que simplesmente eu desconecto de um canal de voz magicamente

			// Quando isto acontecer, nós iremos vazar, vlw flw
			link.disconnect()
		}

		val channel = audioManager.guild.getVoiceChannelById(id) ?: return
		link.connect(channel)
	}

	/**
	 * Checks and provides feedback to the user about the current voice channel state
	 *
	 * @param context the command context
	 * @return if the voice channel state is valid
	 */
	fun checkVoiceChannelState(context: CommandContext): Boolean {
		if (!context.handle.voiceState.inVoiceChannel() || context.handle.voiceState.channel.id != context.config.musicConfig.musicGuildId) {
			// Se o cara não estiver no canal de voz ou se não estiver no canal de voz correto...
			val channel = context.guild.getVoiceChannelById(context.config.musicConfig.musicGuildId)

			if (channel != null) {
				context.reply(
						LoriReply(
								context.locale["TOCAR_NOTINCHANNEL", channel.name.stripCodeMarks()],
								Constants.ERROR
						)
				)
			} else {
				context.reply(
						LoriReply(
								context.locale["TOCAR_InvalidChannel"],
								Constants.ERROR
						)
				)
			}
			return false
		}

		return true
	}
}