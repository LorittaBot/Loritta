package com.mrpowergamerbr.loritta;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandManager;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.listeners.DiscordListener;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.*;
import com.mrpowergamerbr.loritta.utils.amino.AminoRepostThread;
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig;
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper;
import com.mrpowergamerbr.loritta.utils.music.GuildMusicManager;
import com.mrpowergamerbr.loritta.utils.reminders.Reminder;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem;
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.managers.AudioManager;
import org.bson.Document;
import org.jibble.jmegahal.JMegaHal;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public class Loritta {
    @Getter
    @Setter
    public static LorittaConfig config;

    private String clientToken; // Client token da sessão atual
    public LorittaShards lorittaShards = new LorittaShards();
    public CommandManager commandManager; // Nosso command manager
    public final ScheduledExecutorService executor = Executors.newScheduledThreadPool(32); // Threads
    public MongoClient mongo; // MongoDB
    public Datastore ds; // MongoDB²
    public Morphia morphia; // MongoDB³
    @Getter // Sim, getter de novo, já que o lombok não cria getters para variáveis estáticas
    public static final SplittableRandom random = new SplittableRandom(); // Um splittable random global, para não precisar ficar criando vários (menos GC)
    public JMegaHal hal = new JMegaHal(); // JMegaHal, usado nos comandos de frase tosca
    private static List<String> playingGame = new ArrayList<String>();
    private static int currentIndex = 0;
    public static final String FOLDER = "/home/servers/loritta/assets/"; // Pasta usada na Loritta
    public static final String TEMP = "/home/servers/loritta/temp/"; // Pasta usada para coisas temporarias
    @Getter
    public static final Gson gson = new Gson(); // Gson
    @Getter
    public static TemmieYouTube youtube = null; // API key do YouTube, usado em alguns comandos
    @Getter
    private static String clientId;
    @Getter
    private static String clientSecret;
    @Getter
    private static TemmieMercadoPago temmieMercadoPago; // Usado na página de "doar"
    private AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    // Usado para guardar mensagens enviadas pela Loritta ara reactions & outras coisas
    public ConcurrentMap<Object, Object> messageContextCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build().asMap();

    public ServerConfig dummyServerConfig; // Config utilizada em comandos no privado

    public Loritta(LorittaConfig config) {
        loadFromConfig(config);
    }

    public void loadFromConfig(LorittaConfig config) {
        Loritta.setConfig(config);
        this.setClientToken(config.getClientToken());
        Loritta.clientId = config.getClientId();
        Loritta.clientSecret = config.getClientSecret();
        Loritta.youtube = new TemmieYouTube(config.getYoutubeKey());
        Loritta.playingGame = config.getCurrentlyPlaying();
        Loritta.temmieMercadoPago = new TemmieMercadoPago(config.getMercadoPagoClientId(), config.getMercadoPagoClientToken());
    }

    public void generateDummyServerConfig() {
        dummyServerConfig = new ServerConfig().guildId("-1").commandPrefix("");
    }

    public void start() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        System.out.println("Initializing MongoDB...");

        mongo = new MongoClient(); // Hora de iniciar o MongoClient
        morphia = new Morphia(); // E o Morphia
        ds = morphia.createDatastore(mongo, "loritta"); // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")
        generateDummyServerConfig();

        System.out.println("Success! Starting Loritta (Discord Bot)..."); // Agora iremos iniciar o bot
        try {
            int maxShards = 1;
            System.out.println("Starting Shard 1...");
            JDA shard1 = new JDABuilder(AccountType.BOT).useSharding(0, 2).setToken(clientToken).buildBlocking();
            lorittaShards.getShards().add(shard1);
            System.out.println("Starting Shard 2...");
            JDA shard2 = new JDABuilder(AccountType.BOT).useSharding(1, 2).setToken(clientToken).buildBlocking();
            lorittaShards.getShards().add(shard2);
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
            System.exit(1); // Caso dê login exception, vamos fechar o app :(
            return;
        }
        loadCommandManager(); // Carregue todos os comandos
        System.out.println("Loritta (Discord Bot) started!"); // Yay!

        System.out.println("Success! Starting Loritta (Website)..."); // E agora iremos iniciar o frontend (website)
        Runnable website = () -> {
            LorittaWebsite.init(config.getWebsiteUrl(), config.getFrontendFolder());
        };
        new Thread(website, "Website Thread").start(); // ...não foi tão difícil fazer isso :P

        new AminoRepostThread().start(); // Iniciar Amino Repost Thread

        new NewYouTubeVideosThread().start(); // Iniciar New YouTube Videos Thread

        new NewRssFeedThread().start(); // Iniciar Feed Rss

        new UpdateStatusThread().start(); // Iniciar thread para atualizar o status da Loritta

        new DiscordBotsInfoThread().start(); // Iniciar thread para atualizar os servidores no Discord Bots

        Runnable reminders = () -> {
            while (true) {
                FindIterable<Document> list = mongo.getDatabase("loritta").getCollection("users").find(Filters.exists("reminders"));
                for (Document doc : list) {
                    LorittaProfile profile = this.getLorittaProfileForUser(doc.getString("_id"));
                    List<Reminder> toRemove = new ArrayList<Reminder>();
                    for (Reminder reminder : profile.getReminders()) {
                        if (System.currentTimeMillis() >= reminder.getRemindMe()) {
                            toRemove.add(reminder);

                            Guild guild = lorittaShards.getGuildById(reminder.getGuild());

                            if (guild != null) {
                                TextChannel textChannel = guild.getTextChannelById(reminder.getTextChannel());

                                if (textChannel != null) {
                                    textChannel.sendMessage(
                                            "\uD83D\uDD14 | <@" + profile.getUserId() + "> Lembrete! `" + reminder.getReason() + "`").complete();
                                }
                            }
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        profile.getReminders().removeAll(toRemove);
                        ds.save(profile);
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        };
        new Thread(reminders, "Reminders Thread").start();

        Runnable onlineUpdater = () -> {  // Agora iremos iniciar o presence updater
            while (true) {
                for (User user : lorittaShards.getUsers()) {
                    LorittaProfile lorittaProfile = getLorittaProfileForUser(user.getId());
                    List<Guild> mutualGuilds = lorittaShards.getMutualGuilds(user); // Pegar as guilds que o usuário e a Loritta estão (para poder pegar o jogo)
                    List<LorittaProfile> toUpdate = new ArrayList<LorittaProfile>();
                    if (!mutualGuilds.isEmpty()) {
                        Member member = mutualGuilds.get(0).getMember(user);
                        if (member.getOnlineStatus() != OnlineStatus.OFFLINE) {
                            lorittaProfile.setTempoOnline(lorittaProfile.getTempoOnline() + 5); // Em segundos
                            Game game = member.getGame();

                            if (game != null) {
                                String gameName = game.getName();
                                gameName = gameName.replace(".", "[---DOT---]");
                                gameName = gameName.replace("$", "[---DOLLAR---]");
                                lorittaProfile.getGames().put(gameName, 5 + lorittaProfile.getGames().getOrDefault(gameName, 0L));
                            }
                            ds.save(lorittaProfile);
                        }
                    }
                    ds.save(toUpdate);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(onlineUpdater, "Game & Time Updater").start(); // Pronto!

        Runnable playlistMagic = () -> {  // Agora iremos iniciar o playlist magic
            while (true) {
                for (Guild guild : lorittaShards.getGuilds()) {
                    ServerConfig conf = getServerConfigForGuild(guild.getId());

                    if (conf.musicConfig().isEnabled()) {
						getGuildAudioPlayer(guild); // Criar Audio Player para a guild
                        connectToVoiceChannel(conf.musicConfig().getMusicGuildId(), guild.getAudioManager());
                    }
                }
                for (GuildMusicManager mm : musicManagers.values()) {
                    if (mm.player.getPlayingTrack() == null) {
                        ServerConfig conf = getServerConfigForGuild(mm.scheduler.getGuild().getId());

                        if (conf.musicConfig().getAutoPlayWhenEmpty() && !conf.musicConfig().getUrls().isEmpty()) {
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

        // Vamos registrar o nosso event listener em todas as shards!
        for (JDA jda : lorittaShards.getShards()) {
            jda.addEventListener(new DiscordListener(this)); // Hora de registrar o nosso listener
        }
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
     * Carrega um LorittaProfile de um usuário
     *
     * @param userId
     * @return LorittaProfile
     */
    public LorittaProfile getLorittaProfileForUser(String userId) {
        try {
            Document doc = mongo.getDatabase("loritta").getCollection("users").find(Filters.eq("_id", userId)).first();
            if (doc != null) {
                LorittaProfile profile = ds.get(LorittaProfile.class, doc.get("_id"));
                return profile;
            } else {
                return new LorittaProfile(userId);
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
     * @param name        Nome do Webhook
     * @return TemmieWebhook pronto para ser usado
     */
    public static TemmieWebhook getOrCreateWebhook(TextChannel textChannel, String name) {
        if (textChannel == null || !textChannel.getGuild().getMember(textChannel.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_WEBHOOKS)) { // Se a Loritta não pode acessar as webhooks do servidor, retorne null
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
                if (conf.musicConfig().getHasMaxSecondRestriction()) {
                    if (track.getDuration() > TimeUnit.SECONDS.toMillis(conf.musicConfig().getMaxSeconds())) {
                        channel.sendMessage(context.getAsMention(true) + "Música grande demais!").queue();
                        return;
                    }
                }
                channel.sendMessage(context.getAsMention(true) + "💿 Adicionado na fila `" + track.getInfo().title + "`").queue();

                play(channel.getGuild(), conf, musicManager, new AudioTrackWrapper(track, false, context.getUserHandle(), new HashMap<String, String>()));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!conf.musicConfig.getAllowPlaylists()) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }

                    channel.sendMessage(
                            context.getAsMention(true) + "💿 Adicionado na fila `" + firstTrack.getInfo().title + "` (primeira música da playlist " + playlist.getName() + ")").queue();

                    play(channel.getGuild(), conf, musicManager,
                            new AudioTrackWrapper(firstTrack, false, context.getUserHandle(), new HashMap<String, String>()));
                } else {
                    int ignored = 0;
                    for (AudioTrack track : playlist.getTracks()) {
                        if (conf.musicConfig().getHasMaxSecondRestriction()) {
                            if (track.getDuration() > TimeUnit.SECONDS.toMillis(conf.musicConfig().getMaxSeconds())) {
                                ignored++;
                                continue;
                            }
                        }
                        play(channel.getGuild(), conf, musicManager,
                                new AudioTrackWrapper(track, false, context.getUserHandle(), new HashMap<String, String>()));
                    }

                    if (ignored == 0) {
                        channel.sendMessage(
                                context.getAsMention(true) + "💿 Adicionado na fila " + playlist.getTracks().size() + " músicas!").queue();
                    } else {
                        channel.sendMessage(
                                context.getAsMention(true) + "💿 Adicionado na fila " + playlist.getTracks().size() + " músicas! (ignorado " + ignored + " faixas por serem muito grande!)").queue();
                    }
                }
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
                channel.sendMessage(context.getAsMention(true) + "Nada encontrado! `" + trackUrl + "`").queue();
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
                play(guild, conf, musicManager, new AudioTrackWrapper(track, true, guild.getSelfMember().getUser(), new HashMap<String, String>()));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                play(guild, conf, musicManager, new AudioTrackWrapper(playlist.getTracks().get(random.nextInt(0, playlist.getTracks().size())), true, guild.getSelfMember().getUser(), new HashMap<String, String>()));
            }

            @Override
            public void noMatches() {
				if (conf.musicConfig().getUrls().contains(trackUrl)) {
					conf.musicConfig().getUrls().remove(trackUrl);
					ds.save(conf);
				}
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (conf.musicConfig().getUrls().contains(trackUrl)) {
                	conf.musicConfig().getUrls().remove(trackUrl);
                	ds.save(conf);
				}
            }
        });
    }

    private void play(Guild guild, ServerConfig conf, GuildMusicManager musicManager, AudioTrackWrapper trackWrapper) {
        connectToVoiceChannel(conf.musicConfig().getMusicGuildId(), guild.getAudioManager());

        musicManager.scheduler.queue(trackWrapper);

        LorittaUtilsKotlin.fillTrackMetadata(trackWrapper);
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("🤹 Música pulada!").queue();
    }

    public void skipTrack(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.nextTrack();
    }

    private static void connectToVoiceChannel(String id, AudioManager audioManager) {
        if (audioManager.isConnected() && !audioManager.getConnectedChannel().getId().equals(id)) { // Se a Loritta está conectada em um canal de áudio mas não é o que nós queremos...
            audioManager.closeAudioConnection(); // Desconecte do canal atual!
        }
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
}
