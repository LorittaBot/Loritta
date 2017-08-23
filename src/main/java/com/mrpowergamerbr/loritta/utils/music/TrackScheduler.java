package com.mrpowergamerbr.loritta.utils.music;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlinKt;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
	public final AudioPlayer player;
	@Getter
	public final BlockingQueue<AudioTrackWrapper> queue;
	@Getter
	public final Guild guild;
	@Getter
	public AudioTrackWrapper currentTrack;
	
	/**
	 * @param player The audio player this scheduler uses
	 */
	public TrackScheduler(Guild guild, AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.guild = guild;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrackWrapper track) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.

		if (player.getPlayingTrack() != null) {
			if (getCurrentTrack() != null) {
				if (getCurrentTrack().isAutoPlay()) {
					// Quem liga para músicas do autoplay? Cancele ela agora!
					player.stopTrack();
				}
 			}
		}
		if (!player.startTrack(track.getTrack(), true)) {
			queue.offer(track);
		} else {
			currentTrack = track;

			ServerConfig config = LorittaLauncher.loritta.getServerConfigForGuild(guild.getId());

			if (config.musicConfig.getLogToChannel()) {
				TextChannel textChannel = guild.getTextChannelById(config.musicConfig.getChannelId());

				if (textChannel.canTalk()) {
					Thread t = new Thread() {
						public void run() {
							int seconds = 0;

							while (true) {
								if (seconds >= 5) {
									return;
								}

								if (!track.getMetadata().isEmpty()) {
									MessageEmbed embed = LorittaUtilsKotlin.createTrackInfoEmbed(guild, LorittaLauncher.loritta.getLocaleById(config.localeId), true);

									textChannel.sendMessage(embed).complete();
									return;
								}
								seconds++;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					};
					t.start();
				}
			}
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		
		AudioTrackWrapper audioTrackWrapper = queue.poll();
		player.startTrack((audioTrackWrapper == null ? null : audioTrackWrapper.getTrack()), false);
		this.currentTrack = audioTrackWrapper;
		
		// Então quer dizer que nós iniciamos uma música vazia?
		// Okay então, vamos pegar nossas próprias coisas
		if (audioTrackWrapper == null) {
			// Ok, Audio Track é null!
			// Vamos pegar o ServerConfig deste servidor
			ServerConfig conf = LorittaLauncher.getInstance().getServerConfigForGuild(guild.getId());
			
			if (conf.musicConfig().getAutoPlayWhenEmpty() && !conf.musicConfig().getUrls().isEmpty()) {
				String trackUrl = conf.musicConfig().getUrls().get(Loritta.getRandom().nextInt(0, conf.musicConfig().getUrls().size()));
				
				// E agora carregue a música
				LorittaLauncher.getInstance().loadAndPlayNoFeedback(guild, conf, trackUrl); // Só vai meu parça
			}
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
}