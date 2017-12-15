package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.PrivateChannel
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.awt.Color
import java.util.stream.Collectors

class AjudaCommand : AbstractCommand("ajuda", listOf("help", "comandos")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AJUDA_DESCRIPTION"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		try {
			val privateChannel = context.userHandle.openPrivateChannel().complete()

			if (!context.isPrivateChannel) {
				context.event.textChannel.sendMessage(context.getAsMention(true) + "${locale.AJUDA_SENT_IN_PRIVATE.msgFormat()} \uD83D\uDE09").complete()
			}

			var description = context.locale[
					"AJUDA_INTRODUCE_MYSELF",
					context.userHandle.asMention,
					"https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975",
					context?.guild?.name ?: "\uD83E\uDD37"]

			var builder = EmbedBuilder()
					.setColor(Color(0, 193, 223))
					.setTitle("💁 ${context.locale.get("AJUDA_MY_HELP")}")
					.setDescription(description)
					.setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")

			privateChannel.sendMessage(builder.build()).complete()

			sendInfoBox(context, privateChannel)
		} catch (e: ErrorResponseException) {
			if (e.errorResponse.code == 50007) { // Usuário tem as DMs desativadas
				context.event.textChannel.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}" + context.locale["AJUDA_ERROR_WHEN_OPENING_DM"]).complete()
				return
			}
			throw e
		}
	}

	fun getCommandsFor(context: CommandContext, cat: CommandCategory): MutableList<MessageEmbed> {
		val embeds = ArrayList<MessageEmbed>();
		var embed = EmbedBuilder()
		embed.setTitle(cat.fancyTitle, null)
		val conf = context.config

		var color = when (cat) {
			CommandCategory.DISCORD -> Color(121, 141, 207)
			CommandCategory.SOCIAL -> Color(231, 150, 90)
			CommandCategory.UNDERTALE -> Color(250, 250, 250)
			CommandCategory.POKEMON -> Color(255, 13, 0)
			CommandCategory.MINECRAFT -> Color(50, 141, 145)
			CommandCategory.ROBLOX -> Color(226, 35, 26)
			CommandCategory.MISC -> Color(255, 176, 0)
			CommandCategory.UTILS -> Color(176, 146, 209)
			CommandCategory.MUSIC -> Color(124, 91, 197)
			else -> Color(186, 0, 239)
		}

		var image = "http://loritta.website/assets/img/loritta_guild_v4.png"

		if (cat == CommandCategory.SOCIAL) {
			image = "https://loritta.website/assets/img/social.png"
		}

		if (cat == CommandCategory.POKEMON) {
			image = "https://loritta.website/assets/img/pokemon.png"
		}

		if (cat == CommandCategory.MINECRAFT) {
			image = "https://loritta.website/assets/img/loritta_pudim.png"
		}

		if (cat == CommandCategory.FUN) {
			image = "https://loritta.website/assets/img/vieirinha.png"
		}

		if (cat == CommandCategory.UTILS) {
			image = "https://loritta.website/assets/img/utils.png"
		}

		if (cat == CommandCategory.MUSIC) {
			image = "https://loritta.website/assets/img/loritta_headset.png"
		}

		embed.setColor(color)
		embed.setThumbnail(image)

		var description = "*" + cat.description + "*\n\n";
		val categoryCmds = LorittaLauncher.getInstance().commandManager.commandMap.stream().filter { cmd -> cmd.getCategory() == cat }.collect(Collectors.toList<AbstractCommand>())

		if (!categoryCmds.isEmpty()) {
			for (cmd in categoryCmds) {
				if (!conf.disabledCommands.contains(cmd.javaClass.simpleName)) {
					var toBeAdded = "[" + conf.commandPrefix + cmd.label + "]()" + (if (cmd.getUsage() != null) " `" + cmd.getUsage() + "`" else "") + " - " + cmd.getDescription(context) + "\n";
					if ((description + toBeAdded).length > 2048) {
						embed.setDescription(description);
						embeds.add(embed.build());
						embed = EmbedBuilder();
						embed.setColor(color)
						description = "";
					}
					description += "[" + conf.commandPrefix + cmd.label + "]()" + (if (cmd.getUsage() != null) " `" + cmd.getUsage() + "`" else "") + " - " + cmd.getDescription(context) + "\n";
				}
			}
			embed.setDescription(description)
			embeds.add(embed.build());
			return embeds
		} else {
			return embeds
		}
	}

	fun sendInfoBox(context: CommandContext, privateChannel: PrivateChannel) {
		val disabledCommands = loritta.commandManager.getCommandsDisabledIn(context.config)
		var description = "Escolha uma categoria...\n\n"

		var categories = CommandCategory.values().filter { it != CommandCategory.MAGIC }

		if (!context.config.musicConfig.isEnabled) {
			categories = categories.filter { it != CommandCategory.MUSIC }
		}

		// Não mostrar categorias vazias
		categories = categories.filter { category -> loritta.commandManager.commandMap.filter { it.getCategory() == category }.isNotEmpty() }

		val reactionEmotes = mapOf<CommandCategory, String>(
				CommandCategory.DISCORD to "discord:375448103517552642",
				CommandCategory.ROBLOX to "roblox:375313891925688331",
				CommandCategory.UNDERTALE to "undertale_heart:343839169719697408",
				CommandCategory.POKEMON to "pokeball:343837491905691648",
				CommandCategory.MINECRAFT to "grass:383612358318227457",
				CommandCategory.SOCIAL to "blobBlush2:375602225940267018",
				CommandCategory.FUN to "vieirinha:339905091425271820",
				CommandCategory.ADMIN to "\uD83D\uDC6E",
				CommandCategory.IMAGES to "\uD83C\uDFA8",
				CommandCategory.MUSIC to "\uD83C\uDFA7",
				CommandCategory.UTILS to "\uD83D\uDD27",
				CommandCategory.MISC to "\uD83D\uDDC3"
		)

		for (category in categories) {
			val cmdCountInCategory = loritta.commandManager.commandMap.filter { it.getCategory() == category && !disabledCommands.contains(it) }.count()
			val reactionEmote = reactionEmotes.getOrDefault(category, "loritta:331179879582269451")
			val emoji = if (reactionEmote.contains(":")) { "<:$reactionEmote>" } else { reactionEmote }
			val commands = if (cmdCountInCategory == 1) "comando" else "comandos"
			description += "$emoji **" + category.fancyTitle + "** ($cmdCountInCategory $commands)\n"
		}

		val embed = EmbedBuilder().apply {
			setDescription(description)
		}

		val message = privateChannel.sendMessage(embed.build()).complete()
		if (!context.metadata.containsKey("guildId") && !context.isPrivateChannel) {
			context.metadata["guildId"] = context.guild.id
		}
		loritta.messageContextCache[message.id] = context

		for (category in categories) {
			val reactionEmote = reactionEmotes.getOrDefault(category, "loritta:331179879582269451")
			message.addReaction(reactionEmote).complete()
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user.id != context.userHandle.id)
			return

		if (e !is MessageReactionAddEvent)
			return

		println("Processing reaction for " + e.user.name + "...")
		msg.delete().complete()

		if (e.reactionEmote.name == "\uD83D\uDD19") {
			sendInfoBox(context, msg.privateChannel)
		}

		val reactionEmotes = mapOf<CommandCategory, String>(
				CommandCategory.DISCORD to "discord",
				CommandCategory.ROBLOX to "roblox",
				CommandCategory.UNDERTALE to "undertale_heart",
				CommandCategory.POKEMON to "pokeball",
				CommandCategory.MINECRAFT to "grass",
				CommandCategory.SOCIAL to "blobBlush2",
				CommandCategory.FUN to "vieirinha",
				CommandCategory.ADMIN to "\uD83D\uDC6E",
				CommandCategory.IMAGES to "\uD83C\uDFA8",
				CommandCategory.MUSIC to "\uD83C\uDFA7",
				CommandCategory.UTILS to "\uD83D\uDD27",
				CommandCategory.MISC to "\uD83D\uDDC3"
		)

		val entry = reactionEmotes.entries.firstOrNull { it.value ==  e.reactionEmote.name }
		if (entry != null) {
			val embeds = getCommandsFor(context, entry.key)[0]
			val message = context.sendMessage(embeds)
			message.addReaction("\uD83D\uDD19").complete()
			loritta.messageContextCache[message.id] = context
		}
	}

	companion object {
		val SEND_IN_PRIVATE = "enviarNoPrivado"
		val TELL_SENT_IN_PRIVATE = "avisarQueFoiEnviadoNoPrivado"
	}
}