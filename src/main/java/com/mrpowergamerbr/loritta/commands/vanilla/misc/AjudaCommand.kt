package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.awt.Color
import java.io.File
import java.util.stream.Collectors

class AjudaCommand : CommandBase() {

	override fun getLabel(): String {
		return "ajuda"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["AJUDA_DESCRIPTION"]
	}

	override fun getAliases(): List<String> {
		return listOf("help", "comandos")
	}

	override fun run(context: CommandContext) {
		try {
			val privateChannel = context.userHandle.openPrivateChannel().complete()

			if (!context.isPrivateChannel) {
				context.event.textChannel.sendMessage(context.getAsMention(true) + "${context.locale.AJUDA_SENT_IN_PRIVATE.msgFormat()} \uD83D\uDE09").complete()
			}

			var description = context.locale.get(
					"AJUDA_INTRODUCE_MYSELF",
					context.userHandle.asMention,
					"https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975",
					context?.guild?.name ?: "\uD83E\uDD37"
			)

			var builder = EmbedBuilder()
					.setColor(Color(0, 193, 223))
					.setTitle("💁 ${context.locale.get("AJUDA_MY_HELP")}")
					.setDescription(description)
					.setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")

			privateChannel.sendMessage(builder.build()).complete()

			val disabledCommands = loritta.commandManager.getCommandsDisabledIn(context.config)

			val adminCmds = getCommandsFor(context, disabledCommands, CommandCategory.ADMIN, "http://i.imgur.com/Ql6EiAw.png")
			val socialCmds = getCommandsFor(context, disabledCommands, CommandCategory.SOCIAL, "http://i.imgur.com/Ql6EiAw.png")
			val discordCmds = getCommandsFor(context, disabledCommands, CommandCategory.DISCORD, "https://lh3.googleusercontent.com/_4zBNFjA8S9yjNB_ONwqBvxTvyXYdC7Nh1jYZ2x6YEcldBr2fyijdjM2J5EoVdTpnkA=w300")
			val minecraftCmds = getCommandsFor(context, disabledCommands, CommandCategory.MINECRAFT, "http://i.imgur.com/gKBHNzL.png")
			val undertaleCmds = getCommandsFor(context, disabledCommands, CommandCategory.UNDERTALE, "http://vignette2.wikia.nocookie.net/animal-jam-clans-1/images/0/08/Annoying_dog_101.gif/revision/latest?cb=20151231033006")
			val pokemonCmds = getCommandsFor(context, disabledCommands, CommandCategory.POKEMON, "http://i.imgur.com/2l5kKCp.png")
			val robloxCmds = getCommandsFor(context, disabledCommands, CommandCategory.ROBLOX, "https://media.discordapp.net/attachments/297732013006389252/352269723385462787/download.png")
			val musicCmds = getCommandsFor(context, disabledCommands, CommandCategory.MUSIC, "http://i.imgur.com/C9idIUF.png")
			val funCmds = getCommandsFor(context, disabledCommands, CommandCategory.FUN, "http://i.imgur.com/ssNe7dx.png")
			val imagesCmds = getCommandsFor(context, disabledCommands, CommandCategory.IMAGES, "http://i.imgur.com/ssNe7dx.png")
			val miscCmds = getCommandsFor(context, disabledCommands, CommandCategory.MISC, "http://i.imgur.com/Qs8MyFy.png")
			val utilsCmds = getCommandsFor(context, disabledCommands, CommandCategory.UTILS, "http://i.imgur.com/eksGMGw.png")

			val additionalInfoEmbed = EmbedBuilder()
			additionalInfoEmbed.setTitle("Informações Adicionais", null)
					.setColor(Color(0, 193, 223))
			additionalInfoEmbed.setDescription("[Todos os comandos da Loritta](https://loritta.website/comandos)\n"
					+ "[Discord da nossa querida Loritta](https://discord.gg/3rXgN8x)\n"
					+ "[Adicione a Loritta no seu servidor!](https://loritta.website/auth)\n"
					+ "[Amou o Loritta? Tem dinheirinho de sobra? Então doe!](https://loritta.website/doar)\n"
					+ "[Website do MrPowerGamerBR](https://mrpowergamerbr.com/)")

			if (adminCmds != null) {
				fastEmbedSend(context, adminCmds);
			}
			if (socialCmds != null) {
				fastEmbedSend(context, socialCmds);
			}
			if (discordCmds != null) {
				fastEmbedSend(context, discordCmds);
			}
			if (minecraftCmds != null) {
				fastEmbedSend(context, minecraftCmds);
			}
			if (undertaleCmds != null) {
				fastEmbedSend(context, undertaleCmds);
			}
			if (pokemonCmds != null) {
				fastEmbedSend(context, pokemonCmds);
			}
			if (robloxCmds != null) {
				fastEmbedSend(context, robloxCmds);
			}
			if (musicCmds != null) {
				fastEmbedSend(context, musicCmds);
			}
			if (funCmds != null) {
				fastEmbedSend(context, funCmds);
			}
			if (imagesCmds != null) {
				fastEmbedSend(context, imagesCmds);
			}
			if (miscCmds != null) {
				fastEmbedSend(context, miscCmds);
			}
			if (utilsCmds != null) {
				fastEmbedSend(context, utilsCmds);
			}

			context.sendMessage(additionalInfoEmbed.build())
		} catch (e: ErrorResponseException) {
			if (e.errorResponse.code == 50007) { // Usuário tem as DMs desativadas
				context.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}" + context.locale["AJUDA_ERROR_WHEN_OPENING_DM"])
				return
			}
			throw e
		}
	}

	/**
	 * Envia uma embed com imagens de uma maneira mais rápido
	 *
	 * Para fazer isto, nós enviamos uma embed sem imagens e depois editamos com as imagens, já que o Discord "escaneia" as
	 * imagens antes de enviar para o destinatário... usando o "truque" o usuário irá receber sem as imagens e depois irá receber
	 * a versão editada com imagens, economizando tempo ao tentar enviar várias embeds de uma só vez
	 */
	fun fastEmbedSend(context: CommandContext, embeds: List<MessageEmbed>): List<Message> {
		var messages = ArrayList<Message>();
		for (embed in embeds) {
			var clone = EmbedBuilder(embed)
			clone.setImage(null)
			clone.setThumbnail(null)
			var sentMsg = context.sendMessage(clone.build())
			sentMsg.editMessage(embed).queue(); // Vamos enviar em uma queue para não atrasar o envio
			messages.add(sentMsg);
		}
		return messages;
	}

	fun getCommandsFor(context: CommandContext, availableCommands: List<CommandBase>, cat: CommandCategory, image: String): MutableList<MessageEmbed> {
		val embeds = ArrayList<MessageEmbed>();
		var embed = EmbedBuilder()
		embed.setTitle(cat.fancyTitle, null)
		embed.setThumbnail(image)
		val conf = context.config

		var color = Color(255, 255, 255);

		if (cat == CommandCategory.DISCORD) {
			color = Color(121, 141, 207);
		} else if (cat == CommandCategory.SOCIAL) {
			color = Color(231, 150, 90);
		} else if (cat == CommandCategory.UNDERTALE) {
			color = Color(250, 250, 250);
		} else if (cat == CommandCategory.POKEMON) {
			color = Color(255, 13, 0);
		} else if (cat == CommandCategory.MINECRAFT) {
			color = Color(50, 141, 145);
		} else if (cat == CommandCategory.ROBLOX) {
			color = Color(226, 35, 26);
		} else if (cat == CommandCategory.MISC) {
			color = Color(255, 176, 0);
		} else if (cat == CommandCategory.UTILS) {
			color = Color(176, 146, 209);
		} else if (cat == CommandCategory.MUSIC) {
			color = Color(124, 91, 197)
		} else {
			color = Color(186, 0, 239);
		}

		embed.setColor(color)

		var description = "*" + cat.description + "*\n\n";
		val categoryCmds = LorittaLauncher.getInstance().commandManager.commandMap.stream().filter { cmd -> cmd.getCategory() == cat }.collect(Collectors.toList<CommandBase>())

		if (!categoryCmds.isEmpty()) {
			for (cmd in categoryCmds) {
				if (!conf.disabledCommands.contains(cmd.javaClass.simpleName)) {
					var toBeAdded = "[" + conf.commandPrefix + cmd.getLabel() + "]()" + (if (cmd.getUsage() != null) " `" + cmd.getUsage() + "`" else "") + " - " + cmd.getDescription(context) + "\n";
					if ((description + toBeAdded).length > 2048) {
						embed.setDescription(description);
						embeds.add(embed.build());
						embed = EmbedBuilder();
						embed.setColor(color)
						description = "";
					}
					description += "[" + conf.commandPrefix + cmd.getLabel() + "]()" + (if (cmd.getUsage() != null) " `" + cmd.getUsage() + "`" else "") + " - " + cmd.getDescription(context) + "\n";
				}
			}
			embed.setDescription(description)
			embeds.add(embed.build());
			return embeds
		} else {
			return embeds
		}
	}

	companion object {
		val SEND_IN_PRIVATE = "enviarNoPrivado"
		val TELL_SENT_IN_PRIVATE = "avisarQueFoiEnviadoNoPrivado"
	}
}
