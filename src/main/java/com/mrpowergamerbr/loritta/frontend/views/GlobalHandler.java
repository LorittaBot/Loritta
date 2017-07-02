package com.mrpowergamerbr.loritta.frontend.views;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.PaniniUtils;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.LorittaProfile;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import lombok.experimental.ExtensionMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.jooby.Request;
import org.jooby.Response;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Based on PaniniCMS's GlobalHandler: https://github.com/PaniniCMS/Panini/blob/master/src/com/paninicms/views/GlobalHandler.java
 */
@ExtensionMethod(PaniniUtils.class)
public class GlobalHandler {
    public static void render(Request req, Response res) {
        try { // The super try block(tm)
            String[] arguments = req.path().split("/");
            if (arguments.length > 0) {
                arguments = ArrayUtils.remove(arguments, 0); // O primeiro sempre será vazio, então vamos remover ele
            }

            Map<String, Object> contextVars = new HashMap<String, Object>(); // Variáveis usadas no Pebble

            contextVars.put("websiteUrl", LorittaWebsite.getWebsiteUrl());
            contextVars.put("clientId", Loritta.getClientId());
            contextVars.put("websiteUrl", LorittaWebsite.getWebsiteUrl());
            contextVars.put("totalServers", LorittaLauncher.getInstance().getLorittaShards().getGuilds().size());
            contextVars.put("totalUsers", LorittaLauncher.getInstance().getLorittaShards().getUsers().size());
            contextVars.put("epochMillis", System.currentTimeMillis());
            Object temmieObj = null;
            if (req.session().isSet("discordAuthValue")) {
                temmieObj = LorittaWebsite.getOAuth2()
                        .getOrDefault(req.session().get("discordAuthCode").value(), null);
            }
            contextVars.put("temmie", temmieObj);

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
            contextVars.put("uptime", sb.toString());

            RenderContext context = new RenderContext()
                    .contextVars(contextVars)
                    .request(req)
                    .response(res)
                    .arguments(arguments);

            Object obj = null;
            if (arguments.length == 0) {
                obj = HomeView.render(context);
            } else if (arguments.is(0, "cmdvis")) {
                obj = CommandVisualizerRender.render(context);
            } else if (arguments.is(0, "doar")) {
                obj = DonateView.render(context);
            } else if (arguments.is(0, "comandos")) {
                obj = CommandsView.render(context);
            } else if (arguments.is(0, "servers")) {
                obj = ServerListView.render(context);
            } else if (arguments.is(0, "auth")) {
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
            } else if (arguments.is(0, "config")) {
                // Behind Authentication
                try {
                    TemmieDiscordAuth temmie = (TemmieDiscordAuth) LorittaWebsite.getOAuth2().get(req.session().get("discordAuthCode").value());
                    context.contextVars().put("temmie", temmie);
                    LorittaProfile profile = LorittaLauncher.getInstance().getLorittaProfileForUser(temmie.getCurrentUserIdentification().getId());
                    context.contextVars().put("userProfile", profile);

                    if (!profile.isBanned()) { // Se o usuário não está banido...
                        // code
                        // guild_id
                        // permissions
                        if (context.request().path().equalsIgnoreCase("/config/servidores")) {
                            if (req.param("guild_id").isSet()) {
                                // Se está marcado, então a Loritta foi adicionada em algum servidor
                                try {
                                    res.redirect("https://loritta.website/config/servidor/" + req.param("guild_id").value());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            obj = ChooseServerView.render(context, temmie);
                        }
                        if (context.request().path().startsWith("/config/servidor")) {
                            String[] split = context.request().path().split("/");
                            if (split.length >= 4) {
                                String guildId = split[3];
                                System.out.println("reading guildId..." + guildId);
                                if (LorittaLauncher.getInstance().getLorittaShards().getGuildById(guildId) != null) {
                                    // oh shit
                                    obj = ConfigureServerView.render(context, temmie, guildId);
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
                    } else { // Mas se ele estiver, carregue a página de bans
                        obj = BannedView.render(context, temmie);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    obj = "<meta http-equiv=\"refresh\" content=\"0; url=https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fauth&scope=identify%20guilds&response_type=code&client_id=297153970613387264\" />";
                }
            }
            String output = null;

            if (obj instanceof PebbleTemplate) {
                PebbleTemplate compiledTemplate = (PebbleTemplate) obj;
                StringWriter writer = new StringWriter();
                compiledTemplate.evaluate(writer, context.contextVars());
                output = writer.toString();
            } else if (obj instanceof String) {
                output = (String) obj;
            }

            try {
                if (output == null) {
                    output = "Missing output";
                }
                res.send(output);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            StringWriter db1 = new StringWriter();
            PrintWriter coll1 = new PrintWriter(db1);
            e.printStackTrace(coll1);
            try {
                res.send(db1.toString());
            } catch (Throwable ex) {
                e.printStackTrace();
            }
        }
    }
}