package com.mrpowergamerbr.loritta.frontend.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jooby.Request;
import org.jooby.Response;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderWrapper;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

public class GlobalHandler {

	public static void render(Request req, Response res) {
		String path = req.path();
		Object render = null;

		HashMap<String, Object> defaultContext = new HashMap<String, Object>();
		defaultContext.put("clientId", Loritta.getClientId());
		defaultContext.put("websiteUrl", LorittaWebsite.websiteUrl);
		defaultContext.put("totalServers", LorittaLauncher.getInstance().getJda().getGuilds().size());
		defaultContext.put("totalUsers", LorittaLauncher.getInstance().getJda().getUsers().size());
		defaultContext.put("epochMillis", System.currentTimeMillis());
		
		long jvmUpTime = ManagementFactory.getRuntimeMXBean().getUptime();

		long days = TimeUnit.MILLISECONDS.toDays(jvmUpTime);
		jvmUpTime -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime);
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime);
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime);

		StringBuilder sb = new StringBuilder(64);
		sb.append(days);
		sb.append(" dias");
		sb.append(", " + hours);
		sb.append(" horas");
		sb.append(", " + minutes);
		sb.append(" minutos");
		sb.append(" e " + seconds);
		sb.append(" segundos");

		defaultContext.put("uptime", sb.toString());
		System.out.println(path);
		if (path.equalsIgnoreCase("/")) {
			render = HomeView.render(req, res);
		}
		if (path.equalsIgnoreCase("/servers")) {
			render = ServerListView.render(req, res);
		}
		// Authentication
		System.out.println("OAuth2!");
		if (path.startsWith("/auth")) {
			if (req.param("code").isSet()) { // Se o code está marcado, então é um pedido de autenticação via OAuth2
				String code = req.param("code").value();

				TemmieDiscordAuth temmie = new TemmieDiscordAuth(code, "https://loritta.website/auth", Loritta.getClientId(), Loritta.getClientSecret(), true);

				temmie.doTokenExchange(); // Faça o token exchange
				System.out.println("Token exchanged!");
				UUID uuid = UUID.randomUUID();
				LorittaWebsite.getOAuth2().put(uuid.toString(), temmie); // TODO: Tenho certeza que existe um jeito melhor para isto. :/
				req.session().set("discordAuthCode", uuid.toString()); // Vamos guardar o auth token
				System.out.println("OAuth2!");
				try {
					res.redirect("https://loritta.website/config/servidores");
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
				return;
			} else if (req.session().get("discordAuthCode").isSet()) { // Se discordAuthCode existe, então já existe uma conta OAuth2 ativada!
				System.out.println("Trying to reuse Authentication Code...");
				Object temmieObj = LorittaWebsite.getOAuth2().getOrDefault(req.session().get("discordAuthCode").value(), null);
				if (temmieObj == null) { // Se é null, quer dizer que o TemmieDiscordAuth expirou
					System.out.println("null!");
					try {
						res.redirect("https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fauth&scope=identify%20guilds&response_type=code&client_id=297153970613387264");
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
					return;
				} else {
					System.out.println("not null!");
					TemmieDiscordAuth temmie = (TemmieDiscordAuth) temmieObj;

					if (false /* !temmie.isValid() */) {
						System.out.println("but it isn't valid!");
						// Vamos tentar pegar um novo access token
						try {
							System.out.println("doing token exchange!");
							temmie.doTokenExchangeUsingRefreshToken();
						} catch (Exception e) {
							e.printStackTrace();
							// Inválido, redirecione para o oauth2 request
							try {
								res.redirect("https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fauth&scope=identify%20guilds&response_type=code&client_id=297153970613387264");
							} catch (Throwable ex) {
								ex.printStackTrace();
							}
							return;
						}
					} else {
						// Código OAuth2 é válido, vamos redirecionar para o gerenciador de servidores
						try {
							System.out.println("OAuth2 valid!");
							res.redirect("https://loritta.website/config/servidores");
						} catch (Throwable ex) {
							ex.printStackTrace();
						}
						return;
					}
				}
			}
			try {
				res.redirect("https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fauth&scope=identify%20guilds&response_type=code&client_id=297153970613387264");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			return;
		}
		if (path.startsWith("/config")) {
			// Behind Authentication
			try {
				System.out.println("render!");
				TemmieDiscordAuth temmie = (TemmieDiscordAuth) LorittaWebsite.getOAuth2().get(req.session().get("discordAuthCode").value());
				// code
				// guild_id
				// permissions
				if (path.equalsIgnoreCase("/config/servidores")) {
					if (req.param("guild_id").isSet()) {
						// Se está marcado, então a Loritta foi adicionada em algum servidor
						try {
							res.redirect("https://loritta.website/config/servidor/" + req.param("guild_id").value());
						} catch (Throwable e) {
							e.printStackTrace();
						}
						return;
					}
					render = ChooseServerView.render(req, res, temmie);
				}
				if (path.startsWith("/config/servidor")) {
					String[] split = path.split("/");
					if (split.length >= 4) {
						String guildId = split[3];
						System.out.println("reading guildId..." + guildId);
						if (LorittaLauncher.getInstance().getJda().getGuildById(guildId) != null) {
							// oh shit
							render = ConfigureServerView.render(req, res, temmie, guildId);
						} else {
							try {
								res.redirect("https://discordapp.com/oauth2/authorize?client_id=" + Loritta.getClientId() + "&scope=bot&guild_id=" + guildId + "&response_type=code&redirect_uri=https://loritta.website/config/servidores&permissions=2097176631");
							} catch (Throwable e) {
								e.printStackTrace();
							}
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				render = "<meta http-equiv=\"refresh\" content=\"0; url=https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fauth&scope=identify%20guilds&response_type=code&client_id=297153970613387264\" />";
			}
		}

		PebbleTemplate compiledTemplate;
		StringWriter writer;
		String output;

		if(render instanceof RenderWrapper) {
			try {
				Map<String, Object> context = ((RenderWrapper)render).context;

				defaultContext.putAll(context);

				compiledTemplate = ((RenderWrapper)render).pebble;
				writer = new StringWriter();
				compiledTemplate.evaluate(writer, defaultContext);
				output = writer.toString();
				try {
					res.send(output);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			} catch (Exception var11) {
				StringWriter db1 = new StringWriter();
				PrintWriter coll1 = new PrintWriter(db1);
				var11.printStackTrace(coll1);
				try {
					res.send(db1.toString());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if(render == null) {
			HashMap<String, Object> context = new HashMap<String, Object>();

			defaultContext.putAll(context);

			compiledTemplate = null;

			/* try {
				// compiledTemplate = Millennium.engine.getTemplate("404.html");
			} catch (PebbleException var14) {
				var14.printStackTrace();
			} */

			writer = new StringWriter();

			try {
				compiledTemplate.evaluate(writer, defaultContext);
			} catch (PebbleException var12) {
				var12.printStackTrace();
			} catch (IOException var13) {
				var13.printStackTrace();
			}

			output = writer.toString();
			try {
				res.send(output);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				res.send(render);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}