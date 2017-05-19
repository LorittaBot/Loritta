package com.mrpowergamerbr.loritta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import org.bson.Document;
import org.jibble.jmegahal.JMegaHal;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandManager;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.listeners.DiscordListener;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.YouTubeUtils;
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig;
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper;
import com.mrpowergamerbr.loritta.utils.music.GuildMusicManager;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.utils.YouTubeItem;
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.managers.AudioManager;

@Getter
@Setter
public class Loritta {
	@Getter
	@Setter
	private static LorittaConfig config;

	private String clientToken; // Client token da sessão atual
	private JDA jda;
	private CommandManager commandManager; // Nosso command manager
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8); // Threads
	private MongoClient mongo; // MongoDB
	private Datastore ds; // MongoDB²
	private Morphia morphia; // MongoDB³
	@Getter // Sim, getter de novo, já que o lombok não cria getters para variáveis estáticas
	public static final SplittableRandom random = new SplittableRandom(); // Um splittable random global, para não precisar ficar criando vários (menos GC)
	private JMegaHal hal = new JMegaHal(); // JMegaHal, usado nos comandos de frase tosca
	private static String playingGame = "loritta.website | Shantae: Half-Genie Hero";
	public static final String FOLDER = "/home/servers/loritta/assets/"; // Pasta usada na Loritta
	@Getter
	private static final Gson gson = new Gson(); // Gson
	@Getter
	private static TemmieYouTube youtube = null; // API key do YouTube, usado em alguns comandos
	@Getter
	private static String clientId;
	@Getter
	private static String clientSecret;
	@Getter
	private static TemmieMercadoPago temmieMercadoPago; // Usado na página de "doar"
	private AudioPlayerManager playerManager;
	private Map<Long, GuildMusicManager> musicManagers;

	public Loritta(LorittaConfig config) {
		Loritta.setConfig(config);
		this.setClientToken(config.getClientToken());
		Loritta.clientId = config.getClientId();
		Loritta.clientSecret = config.getClientSecret();
		Loritta.youtube = new TemmieYouTube(config.getYoutubeKey());
		Loritta.setPlaying(config.getCurrentlyPlaying());
		Loritta.temmieMercadoPago = new TemmieMercadoPago(config.getMercadoPagoClientId(), config.getMercadoPagoClientToken());
	}

	public void start() {		
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.OFF);
		
		System.out.println("Initializing MongoDB...");

		mongo = new MongoClient(); // Hora de iniciar o MongoClient
		morphia = new Morphia(); // E o Morphia
		ds = morphia.createDatastore(mongo, "loritta"); // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")

		System.out.println("Success! Starting Loritta (Discord Bot)..."); // Agora iremos iniciar o bot
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(clientToken).buildBlocking();
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
			System.exit(1); // Caso dê login exception, vamos fechar o app :(
			return;
		}
		loadCommandManager(); // Carregue todos os comandos
		System.out.println("Loritta (Discord Bot) started!"); // Yay!

		System.out.println("Success! Starting Loritta (Website)..."); // E agora iremos iniciar o frontend (website)
		Runnable website = () -> { LorittaWebsite.init(config.getWebsiteUrl(), config.getFrontendFolder()); };
		new Thread(website, "Website Thread").start(); // ...não foi tão difícil fazer isso :P

		Runnable presenceUpdater = () -> {  // Agora iremos iniciar o presence updater
			while (true) {
				jda.getPresence().setGame(new GameImpl(Loritta.playingGame, "http://sparklypower.net/", GameType.DEFAULT));
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(presenceUpdater, "Presence Updater").start(); // Pronto!

		Runnable playlistMagic = () -> {  // Agora iremos iniciar o playlist magic
			while (true) {
				for (Guild guild : jda.getGuilds()) {
					ServerConfig conf = getServerConfigForGuild(guild.getId());
					
					if (conf.musicConfig().isEnabled()) {
						connectToVoiceChannel(conf.musicConfig().getMusicGuildId(), guild.getAudioManager());
						getGuildAudioPlayer(guild);
					}
				}
				for (GuildMusicManager mm : musicManagers.values()) {
					if (mm.player.getPlayingTrack() == null) {
						ServerConfig conf = getServerConfigForGuild(mm.scheduler.getGuild().getId());

						if (conf.musicConfig().isAutoPlayWhenEmpty() && !conf.musicConfig().getUrls().isEmpty()) {
							String trackUrl = conf.musicConfig().getUrls().get(Loritta.getRandom().nextInt(0, conf.musicConfig().getUrls().size()));

							// E agora carregue a música
							LorittaLauncher.getInstance().loadAndPlayNoFeedback(mm.scheduler.getGuild(), conf, trackUrl); // Só vai meu parça
						}
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(playlistMagic, "Playlist Magic").start(); // Pronto!

		this.musicManagers = new HashMap<>();

		this.playerManager = new DefaultAudioPlayerManager();

		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);

		jda.addEventListener(new DiscordListener(this)); // Hora de registrar o nosso listener
		// Ou seja, agora a Loritta estará aceitando comandos
	}

	/**
	 * Carrega um ServerConfig de uma guild
	 * 
	 * @param guildId
	 * @return ServerConfig
	 */
	public ServerConfig getServerConfigForGuild(String guildId) {
		try {
			Document doc = mongo.getDatabase("loritta").getCollection("servers").find(Filters.eq("_id", guildId)).first();
			if (doc != null) {
				ServerConfig config = ds.get(ServerConfig.class, doc.get("_id"));
				return config;
			} else {
				return new ServerConfig().guildId(guildId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Cria o CommandManager
	 */
	public void loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		commandManager = new CommandManager();
	}

	/**
	 * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
	 * 
	 * @param textChannel Canal de texto
	 * @param name Nome do Webhook
	 * @return TemmieWebhook pronto para ser usado
	 */
	public static TemmieWebhook getOrCreateWebhook(TextChannel textChannel, String name) {
		if (!textChannel.getGuild().getMember(textChannel.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_WEBHOOKS)) { // Se a Loritta não pode acessar as webhooks do servidor, retorne null
			return null;
		}
		List<Webhook> webhookList = textChannel.getGuild().getWebhooks().complete();

		List<Webhook> webhooks = webhookList.stream().filter((webhook) -> webhook.getChannel() == textChannel).collect(Collectors.toList());
		Webhook webhook = null;

		if (webhooks.isEmpty()) {
			webhook = textChannel.getGuild().getController().createWebhook(textChannel, name).complete();
		} else {
			webhook = webhooks.get(0);
		}

		TemmieWebhook temmie = new TemmieWebhook(webhook.getUrl(), true);

		return temmie;
	}

	public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(guild, playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	public void loadAndPlay(CommandContext context, ServerConfig conf, final TextChannel channel, final String trackUrl) {
		loadAndPlay(context, conf, channel, trackUrl, false);
	}

	public void loadAndPlay(CommandContext context, ServerConfig conf, final TextChannel channel, final String trackUrl, boolean alreadyChecked) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				if (conf.musicConfig().isHasMaxSecondRestriction()) {
					if (track.getDuration() > TimeUnit.SECONDS.toMillis(conf.musicConfig().getMaxSeconds())) {
						channel.sendMessage(context.getAsMention(true) + "Música grande demais!").queue();
						return;
					}
				}
				channel.sendMessage(context.getAsMention(true) + "💿 Adicionado na fila " + track.getInfo().title).queue();

				play(channel.getGuild(), conf, musicManager, new AudioTrackWrapper(track, false, context.getUserHandle()));
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}

				channel.sendMessage(context.getAsMention(true) + "💿 Adicionado na fila " + firstTrack.getInfo().title + " (primeira música da playlist " + playlist.getName() + ")").queue();

				play(channel.getGuild(), conf, musicManager, new AudioTrackWrapper(firstTrack, false, context.getUserHandle()));
			}

			@Override
			public void noMatches() {
				if (!alreadyChecked) {
					// Ok, não encontramos NADA relacionado a essa música
					// Então vamos pesquisar!
					List<YouTubeItem> item = YouTubeUtils.searchVideosOnYouTube(trackUrl);

					if (!item.isEmpty()) {
						loadAndPlay(context, conf, channel, item.get(0).getId().getVideoId(), true);
						return;
					}
				}
				channel.sendMessage(context.getAsMention(true) + "Nada encontrado! " + trackUrl).queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				channel.sendMessage(context.getAsMention(true) + "Deu ruim: " + exception.getMessage()).queue();
			}
		});
	}

	public void loadAndPlayNoFeedback(Guild guild, ServerConfig conf, final String trackUrl) {
		GuildMusicManager musicManager = getGuildAudioPlayer(guild);

		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				play(guild, conf, musicManager, new AudioTrackWrapper(track, true, jda.getSelfUser()));
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				play(guild, conf, musicManager, new AudioTrackWrapper(playlist.getTracks().get(0), true, jda.getSelfUser()));
			}

			@Override
			public void noMatches() {
			}

			@Override
			public void loadFailed(FriendlyException exception) {
			}
		});
	}

	private void play(Guild guild, ServerConfig conf, GuildMusicManager musicManager, AudioTrackWrapper trackWrapper) {
		connectToVoiceChannel(conf.musicConfig().getMusicGuildId(), guild.getAudioManager());

		musicManager.scheduler.queue(trackWrapper);
	}

	public void skipTrack(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.nextTrack();

		channel.sendMessage("🤹 Música pulada!").queue();
	}

	private static void connectToVoiceChannel(String id, AudioManager audioManager) {
		/* if (audioManager.isConnected() && !audioManager.getConnectedChannel().getId().equals(id)) { // Se a Loritta está conectada em um canal de áudio mas não é o que nós queremos...
			audioManager.closeAudioConnection(); // Desconecte do canal atual!
		}
		if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) { */
			for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
				if (voiceChannel.getId().equals(id)) {
					audioManager.openAudioConnection(voiceChannel);
					break;
				}
			}
		// }
	}

	public static String getPlaying() {
		return playingGame;
	}

	public static void setPlaying(String newGame) {
		playingGame = newGame;
	}
}
