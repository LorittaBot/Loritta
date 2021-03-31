package com.mrpowergamerbr.loritta;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.entities.Game;
import org.bson.Document;
import org.jibble.jmegahal.JMegaHal;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandManager;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.listeners.DiscordListener;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.LorittaConfig;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube;
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

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
	public static final String FOLDER = "/home/loritta/assets/"; // Pasta usada na Loritta
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
	
	private static final List<String> mstKeys = new ArrayList<String>(); // http://trans.pantherman594.com/translateKeys

	static {
		mstKeys.add("Tharsen;6mutEwVfEVk3S9nIdolEc9EdVVHANwNyW69H15ssB7c=");
		mstKeys.add("Rell1936;ZiE7WrcOTj0QYuJsgan0PhQDRv2A34ZVIQKBWL7wS9o=");
		mstKeys.add("Faber1974;So3rU12SPzo2xjIICgyTPEoYxSIkAIKgopS1NOO/3jA=");
		mstKeys.add("Factiong;FSVgPoxrYOWdIx1FGH2GtEmmLAhSfW5lpJkAL61Wtmg=");
		mstKeys.add("Neittle;J6QqMu/9oFwjffjenwjAMm3I3KSh/jhnUEgRJrqGvR8=");
		mstKeys.add("Gasselve;MPAp7JAhBlkloO+iW+K1sGf4GVg8ipb8YRFY7CY1jn8=");
		mstKeys.add("Mork1971;ATniH+AEAZ/t87U90UUMi5ZY2vGzNc+3ivnWP2NV1Pk=");
		mstKeys.add("Lintioned;MWLlqI+juS/3uDFRty4hQMBF62OtRfflWfYgr3V5q7U=");
		mstKeys.add("Jealifted77;zLQ7XLgRwWZmigP+PKiwGFT+Hk/Pu1+6/TpO9qu8ftE=");
	}

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
		System.out.println("Initializing MongoDB...");

		mongo = new MongoClient(); // Hora de iniciar o MongoClient
		morphia = new Morphia(); // E o Morphia
		ds = morphia.createDatastore(mongo, "loritta"); // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")

		System.out.println("Success! Starting Loritta (Discord Bot)..."); // Agora iremos iniciar o bot
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(clientToken).buildBlocking();
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
			System.exit(1); // Caso dê login exception, vamos fechar o app :(
			return;
		}
		loadCommandManager(); // Carregue todos os comandos
		System.out.println("Loritta (Discord Bot) started!"); // Yay!

		System.out.println("Success! Starting Loritta (Website)..."); // E agora iremos iniciar o frontend (website)
		/* Runnable website = () -> { LorittaWebsite.init(config.getWebsiteUrl(), config.getFrontendFolder()); };
		new Thread(website, "Website Thread").start(); // ...não foi tão difícil fazer isso :P */

		Runnable presenceUpdater = () -> {  // Agora iremos iniciar o presence updater
			while (true) {
				jda.getPresence().setGame(Game.playing(Loritta.playingGame));
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(presenceUpdater, "Presence Updater").start(); // Pronto!

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
			// TODO: Server Config
			/* ServerConfig config = new ServerConfig();
			config.guildId(guildId);
			config.commandPrefix("..");
			config.modules().add("BotInfoCommand");
			config.debugOptions().enableAllModules(true);

			{
				CommandOptions cmdOpti = new CommandOptions();
				cmdOpti.options().put(AjudaCommand.TELL_SENT_IN_PRIVATE, true);
				cmdOpti.options().put(AjudaCommand.SEND_IN_PRIVATE, true);
				// cmdOpti.options().put(AvatarCommand.HIDE_IMAGE, true);
				config.commandOptions().put("AjudaCommand", cmdOpti);
			}

			{
				YouTubeCommandOptions cmdOpt = new YouTubeCommand.YouTubeCommandOptions();
				cmdOpt.doNotEmbed(true);
				config.commandOptions().put("YouTubeCommand", cmdOpt);
			}

			{
				CustomCommand customCmd = new CustomCommand();
				customCmd.commandName("parappa");

				CodeBlock codeBlock = new CodeBlock(); // Nosso CodeBlock

				ReplyCode reply = new ReplyCode("Yeah, I know... I gotta believe!");
				codeBlock.codes.add(reply);

				ReactionCode react = new ReactionCode("parappa", true);
				codeBlock.codes.add(react);

				customCmd.codes().add(codeBlock);

				config.customCommands().add(customCmd);
			}

			CommandOptions tristeRealidadeOpti = new CommandOptions();
			config.commandOptions().put("TristeRealidadeCommand", tristeRealidadeOpti);

			config.explainOnCommandRun(true);
			config.commandOutputInPrivate(false);
			config.mentionOnCommandOutput(true);

			// Test Whistler #1
			{
				Whistler whistler = new Whistler("Olá Mundo!"); // Criando o whistler

				CodeBlock codeBlock = new CodeBlock(); // Nosso CodeBlock

				ContainsPrecondition precondition = new ContainsPrecondition("hello world", true); // Nosso pré condition

				codeBlock.preconditions.add(precondition);

				ChancePrecondition preconditionWow = new ChancePrecondition(25);
				codeBlock.preconditions.add(preconditionWow);

				ReactionCode reaction = new ReactionCode("🌍", false);
				codeBlock.codes.add(reaction);

				ReplyCode reply = new ReplyCode("Olá mundo!");
				codeBlock.codes.add(reply);

				whistler.codes.add(codeBlock);

				config.whistlers().add(whistler); // Pronto!
			}

			// Test Whistler #2
			// kk eae girl
			{
				Whistler whistler = new Whistler("kk eae girl"); // Criando o whistler

				CodeBlock codeBlock = new CodeBlock(); // Nosso CodeBlock

				ChancePrecondition precondition = new ChancePrecondition(0.666);

				codeBlock.preconditions.add(precondition);

				ReplyCode reply = new ReplyCode("kk eae girl");
				codeBlock.codes.add(reply);

				whistler.codes.add(codeBlock);

				config.whistlers().add(whistler); // Pronto!
			} // http://i.imgur.com/hSiDzcT.png

			// TODO: Apenas para debug ;)
			ds.save(config); */
			Document doc = mongo.getDatabase("loritta").getCollection("servers").find(Filters.eq("_id", guildId)).first();
			if (doc != null) {
				ServerConfig config = ds.get(ServerConfig.class, doc.get("_id"));
				return config;
			} else {
				ArrayList<String> enabledModules = new ArrayList<String>();
				for (CommandBase cmdBase : commandManager.getCommandMap()) {
					enabledModules.add(cmdBase.getClass().getSimpleName());
				}
				return new ServerConfig().guildId(guildId).modules(enabledModules);
			}
			// TODO: AjudaOptions & AvatarOptions
			// Gson gson = new Gson();
			// String json = gson.toJson(config);
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

	public static String getMicrosoftTranslateServiceKey() {
		String key = mstKeys.get(0);
		mstKeys.remove(0);
		mstKeys.add(key);
		return key;
	}

	/**
	 * Retorna um TemmieWebhook com um webhook de um canal especificado, caso não exista nenhum webhook válido para o canal, ele irá criar um.
	 * 
	 * @param textChannel Canal de texto
	 * @param name Nome do Webhook
	 * @return TemmieWebhook pronto para ser usado
	 */
	public static TemmieWebhook getOrCreateWebhook(TextChannel textChannel, String name) {
		List<Webhook> webhookList = textChannel.getGuild().getWebhooks().complete();

		List<Webhook> webhooks = webhookList.stream().filter((webhook) -> webhook.getChannel() == textChannel).collect(Collectors.toList());
		Webhook webhook = null;

		if (webhooks.isEmpty()) {
			webhook = textChannel.createWebhook(name).complete();
		} else {
			webhook = webhooks.get(0);
		}

		TemmieWebhook temmie = new TemmieWebhook(webhook.getUrl(), true);

		return temmie;
	}

	public static String getPlaying() {
		return playingGame;
	}

	public static void setPlaying(String newGame) {
		playingGame = newGame;
	}

	public static void warnOwnerNoPermission(Guild guild, TextChannel textChannel, ServerConfig serverConf) {
		for (Member member : guild.getMembers()) {
			if (member.isOwner()) {
				member.getUser().openPrivateChannel().complete().sendMessage("Hey, eu estou sem permissão no **" + textChannel.getName() + "** na guild **" + guild.getName() + "**! Você pode configurar o meu grupo para poder falar lá? Obrigada! 😊").complete();
			}
		}
	}
}
