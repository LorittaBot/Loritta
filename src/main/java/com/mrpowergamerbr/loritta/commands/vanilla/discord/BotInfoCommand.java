package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BotInfoCommand extends CommandBase {

    @Override
    public String getLabel() {
        return "botinfo";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("info");
    }

    @Override
    public String getDescription() {
        return "Mostra informações interessantes (e algumas bem inúteis) sobre a Loritta.";
    }

    @Override
    public void run(CommandContext context) {
        EmbedBuilder embed = new EmbedBuilder();

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
        sb.append("d ");
        sb.append(hours);
        sb.append("h ");
        sb.append(minutes);
        sb.append("m ");
        sb.append(seconds);
        sb.append("s");

        embed.setAuthor("Olá, eu sou a Loritta! 💁", null, "https://loritta.website/assets/img/loritta_guild_v4.png");
        embed.setDescription("Olá, eu me chamo Loritta (ou para amigos mais próximos, \"Lori\") e eu sou uma garota de *lembrete para mim: colocar quantos anos eu tenho aqui* anos!");
        embed.setImage("http://loritta.website/assets/img/loritta_heart.png");
        embed.setColor(new Color(186, 0, 239));
        embed.addField("📝 Nome", "Loritta#" + context.event.getGuild().getSelfMember().getUser().getDiscriminator(), true);
        embed.addField("🌎 Servidores", String.valueOf(LorittaLauncher.getInstance().getLorittaShards().getGuilds().size()) + " servidores", true);
        embed.addField("👥 Usuários", String.valueOf(LorittaLauncher.getInstance().getLorittaShards().getUsers().size()) + " usuários", true);
        embed.addField("👾 Website", "https://loritta.website", true);
        embed.addField("\uD83D\uDCAC Servidor", "http://bit.ly/lorittad", true);
        embed.addField("\uD83D\uDCD8 Bibiloteca", "JDA (Java)", true);
        embed.addField("📚 Linguagem", "Java + Kotlin", true);
        embed.addField("\uD83D\uDD25 Shard", String.valueOf(context.event.getJDA().getShardInfo().getShardId()), true);
        embed.addField("\uD83D\uDCBE GitHub", "http://bit.ly/lorittagit", true);
        embed.addField("\uD83D\uDCF6 Uptime", sb.toString(), true);
        embed.addField("🏋 Comandos executados desde o último restart", String.valueOf(Loritta.getExecutedCommands()), true);
        embed.addField("Menções Honrosas", "`MrPowerGamerBR#4185` Se não fosse por ele, eu nem iria existir!\n"
                + "`Giovanna_GGold#2454 (Gabriela Giulian)` Ela que fez esta **linda** \uD83D\uDE0D arte minha que você irá apreciar logo abaixo (e ela capturou toda a minha beleza & fofura \uD83D\uDE0A)!\n"
				+ "`" + context.getUserHandle().getName() + "#" + context.getUserHandle().getDiscriminator() + "` Por estar falando comigo! \uD83D\uDE04", false);
        embed.setFooter("Loritta foi criada por MrPowerGamerBR - https://mrpowergamerbr.com/", "https://mrpowergamerbr.com/assets/img/avatar.png");
        context.sendMessage(embed.build());
    }

}
