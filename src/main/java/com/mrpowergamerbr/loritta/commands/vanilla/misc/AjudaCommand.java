package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class AjudaCommand extends CommandBase {
	public static final String SEND_IN_PRIVATE = "enviarNoPrivado";
	public static final String TELL_SENT_IN_PRIVATE = "avisarQueFoiEnviadoNoPrivado";

	@Override
	public String getLabel() {
		return "ajuda";
	}

	@Override
	public String getDescription() {
		return "Mostra todos os comandos disponíveis que eu posso executar, lembrando que isto só irá mostrar os comandos habilitados no servidor que você executou a ajuda!";
	}

	@Override
	public void run(CommandContext context) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setThumbnail("http://i.imgur.com/LUHLEs9.png");
		embed.setColor(new Color(186, 0, 239));

		List<CommandBase> disabledCommands = LorittaLauncher.getInstance().getCommandManager().getCommandsDisabledIn(context.getConfig());

		MessageEmbed discordCmds = getCommandsFor(context.getConfig(), disabledCommands, CommandCategory.DISCORD, "https://lh3.googleusercontent.com/_4zBNFjA8S9yjNB_ONwqBvxTvyXYdC7Nh1jYZ2x6YEcldBr2fyijdjM2J5EoVdTpnkA=w300");
		MessageEmbed minecraftCmds = getCommandsFor(context.getConfig(), disabledCommands, CommandCategory.MINECRAFT, "http://i.imgur.com/gKBHNzL.png");
		MessageEmbed undertaleCmds = getCommandsFor(context.getConfig(), disabledCommands, CommandCategory.UNDERTALE, "http://vignette2.wikia.nocookie.net/animal-jam-clans-1/images/0/08/Annoying_dog_101.gif/revision/latest?cb=20151231033006");
		MessageEmbed funCmds = getCommandsFor(context.getConfig(), disabledCommands, CommandCategory.FUN, "http://i.imgur.com/gKBHNzL.png");
		MessageEmbed miscCmds = getCommandsFor(context.getConfig(), disabledCommands, CommandCategory.MISC, "http://i.imgur.com/ssNe7dx.png");

		EmbedBuilder aboutMe = new EmbedBuilder();
		aboutMe.setTitle("Sobre o Criador", null);
		aboutMe.setThumbnail("http://i.imgur.com/nhBZ8i4.png");
		aboutMe.setDescription("Loritta foi criado pelo MrPowerGamerBR. :wink:");
		aboutMe.addField("Website", "http://mrpowergamerbr.com/", true);
		aboutMe.addField("Discord", "MrPowerGamerBR#4185", true);
		aboutMe.addField("Twitter", "@mrpowergamerbr", true);

		EmbedBuilder sparklyPower = new EmbedBuilder();
		sparklyPower.setTitle("Reclames do Plim Plim #1", null);
		sparklyPower.setThumbnail("http://sparklypower.net/SparklyPower_Logo_250.png");
		sparklyPower.setDescription("Gostou da qualidade do Loritta? Gosta de Minecraft? Survival? Que tal jogar no SparklyPower então? :slight_smile:");
		sparklyPower.addField("Website", "https://sparklypower.net/", true);
		sparklyPower.addField("IP", "jogar.sparklypower.net (Versão 1.11.2)", true);

		EmbedBuilder additionalInfoEmbed = new EmbedBuilder();
		additionalInfoEmbed.setTitle("Informações Adicionais", null);
		additionalInfoEmbed.setDescription("[Todos os comandos da Loritta](https://loritta.website/comandos)\n"
				+ "[Discord da nossa querida Loritta](https://discord.gg/3rXgN8x)\n"
				+ "[Adicione a Loritta no seu servidor!](https://loritta.website/auth)\n"
				+ "[Amou o Loritta? Tem dinheirinho de sobra? Então doe!](https://loritta.website/doar)\n"
				+ "[Website do MrPowerGamerBR](https://mrpowergamerbr.com/)");

		CommandOptions cmdOptions = context.getConfig().getCommandOptionsFor(this);

		if (discordCmds != null) { context.sendMessage(discordCmds); }
		if (minecraftCmds != null) { context.sendMessage(minecraftCmds); }
		if (undertaleCmds != null) { context.sendMessage(undertaleCmds); }
		if (funCmds != null) { context.sendMessage(funCmds); }
		if (miscCmds != null) { context.sendMessage(miscCmds); }

		context.sendMessage(sparklyPower.build());
		context.sendMessage(additionalInfoEmbed.build());

		if (true /* cmdOptions.getAsBoolean(TELL_SENT_IN_PRIVATE) */) {
			context.getEvent().getTextChannel().sendMessage(context.getAsMention(true) + "Enviei para você no privado! ;)").complete();
		}
	}

	public MessageEmbed getCommandsFor(ServerConfig conf, List<CommandBase> availableCommands, CommandCategory cat, String image) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(cat.getFancyTitle(), null);
		embed.setThumbnail(image);
		embed.setColor(new Color(186, 0, 239));

		List<CommandBase> categoryCmds = LorittaLauncher.getInstance().getCommandManager().getCommandMap().stream().filter((cmd) -> cmd.getCategory() == cat).collect(Collectors.toList());

		if (!categoryCmds.isEmpty()) {
			for (CommandBase cmd : categoryCmds) {
				if (!conf.disabledCommands().contains(cmd.getClass().getSimpleName())) {
					embed.addField(conf.commandPrefix() + cmd.getLabel(), cmd.getDescription(), false);
				}
			}
			return embed.build();
		} else {
			return null;
		}
	}
}
