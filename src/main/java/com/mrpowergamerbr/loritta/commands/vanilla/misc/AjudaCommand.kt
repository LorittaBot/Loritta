package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.PrivateChannel
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.awt.Color

class AjudaCommand : AbstractCommand("ajuda", listOf("help", "comandos", "commands"), CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AJUDA_DESCRIPTION"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		try {
			val privateChannel = context.userHandle.openPrivateChannel().complete()

			if (!context.isPrivateChannel) {
				context.event.textChannel.sendMessage(context.getAsMention(true) + "${locale["AJUDA_SENT_IN_PRIVATE"]} \uD83D\uDE09").complete()
			}

			val description = context.locale[
					"AJUDA_INTRODUCE_MYSELF",
					context.userHandle.asMention,
					Loritta.config.addBotUrl,
					context.guild?.name ?: "\uD83E\uDD37"]

			val builder = EmbedBuilder()
					.setColor(Color(0, 193, 223))
					.setTitle("💁 ${context.locale.get("AJUDA_MY_HELP")}")
					.setDescription(description)
					.setThumbnail("https://loritta.website/assets/img/loritta_gabizinha_v1.png")

			val pleaseDonate = EmbedBuilder()
					.setColor(Color(114, 137, 218))
					.setThumbnail("https://loritta.website/assets/img/loritta_pobre.png")
					.setTitle("<:lori_triste:370344565967814659> ${locale["AJUDA_DonationTitle"]}")
					.setDescription(locale["AJUDA_PleaseDonate"])

			val discordServerList = EmbedBuilder()
					.setColor(Color(0, 121, 183))
					.setImage("https://loritta.website/assets/img/loritta_mendigagem_cover.png")
					.setAuthor("Loritta")
					.setTitle("<:loritta:331179879582269451> Loritta's Server List", "https://loritta.website/servers")
					.setDescription("Está com tédio e quer encontrar um servidor no Discord para você entrar e se divertir? Querendo divulgar o seu novo servidor no Discord para que outras pessoas possam entrar? Então visite a Loritta's Server List!\n\nhttps://loritta.website/servers")

			privateChannel.sendMessage(builder.build()).complete()
			privateChannel.sendMessage(pleaseDonate.build()).complete()

			// TODO: Remover verificação após ter a lista traduzida
			if (context.config.localeId == "default" || context.config.localeId == "pt-pt" || context.config.localeId == "pt-funk") {
				privateChannel.sendMessage(discordServerList.build()).complete()
			}

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
		val reactionEmotes = mapOf(
				CommandCategory.DISCORD to "<:discord_logo:412576344120229888>",
				CommandCategory.ROBLOX to "<:roblox_logo:412576693803286528>",
				CommandCategory.UNDERTALE to "<:undertale_heart:412576128340066304>",
				CommandCategory.POKEMON to "<:pokeball:412575443024216066>",
				CommandCategory.MINECRAFT to "<:minecraft_logo:412575161041289217>",
				CommandCategory.SOCIAL to "<a:lori_ablobcouple:412577132007653383>",
				CommandCategory.FUN to "<:vieirinha:412574915879763982>",
				CommandCategory.ADMIN to "\uD83D\uDC6E",
				CommandCategory.IMAGES to "\uD83C\uDFA8",
				CommandCategory.MUSIC to "\uD83C\uDFA7",
				CommandCategory.UTILS to "\uD83D\uDD27",
				CommandCategory.MISC to "\uD83D\uDDC3",
				CommandCategory.ANIME to "\uD83D\uDCFA"
		)

		val embeds = ArrayList<MessageEmbed>();
		var embed = EmbedBuilder()
		embed.setTitle(reactionEmotes.getOrDefault(cat, ":loritta:331179879582269451") + " " + context.locale[cat.fancyTitle], null)
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

		var image = when (cat) {
			CommandCategory.SOCIAL -> "https://loritta.website/assets/img/social.png"
			CommandCategory.POKEMON -> "https://loritta.website/assets/img/pokemon.png"
			CommandCategory.MINECRAFT -> "https://loritta.website/assets/img/loritta_pudim.png"
			CommandCategory.FUN -> "https://loritta.website/assets/img/vieirinha.png"
			CommandCategory.UTILS -> "https://loritta.website/assets/img/utils.png"
			CommandCategory.MUSIC -> "https://loritta.website/assets/img/loritta_headset.png"
			CommandCategory.ANIME -> "https://loritta.website/assets/img/loritta_anime.png"
			else -> "https://loritta.website/assets/img/loritta_gabizinha_v1.png"
		}

		embed.setColor(color)
		embed.setThumbnail(image)

		var description = "*" + context.locale[cat.description] + "*\n\n"
		val categoryCmds = loritta.commandManager.commandMap.filter { cmd -> cmd.category == cat }

		if (!categoryCmds.isEmpty()) {
			for (cmd in categoryCmds) {
				if (!conf.disabledCommands.contains(cmd.javaClass.simpleName)) {
					var toBeAdded = "**" + conf.commandPrefix + cmd.label + "**" + (if (cmd.getUsage() != null) " `" + cmd.getUsage() + "`" else "") + " » " + cmd.getDescription(context.locale) + "\n"
					if ((description + toBeAdded).length > 2048) {
						embed.setDescription(description);
						embeds.add(embed.build());
						embed = EmbedBuilder();
						embed.setColor(color)
						description = "";
					}
					description += toBeAdded
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
		var description = ""

		var categories = CommandCategory.values().filter { it != CommandCategory.MAGIC }

		if (!context.config.musicConfig.isEnabled) {
			categories = categories.filter { it != CommandCategory.MUSIC }
		}

		// Não mostrar categorias vazias
		categories = categories.filter { category -> loritta.commandManager.commandMap.any { it.category == category } }

		val reactionEmotes = mapOf(
				CommandCategory.DISCORD to ":discord_logo:412576344120229888",
				CommandCategory.ROBLOX to ":roblox_logo:412576693803286528",
				CommandCategory.UNDERTALE to ":undertale_heart:412576128340066304",
				CommandCategory.POKEMON to ":pokeball:412575443024216066",
				CommandCategory.MINECRAFT to ":minecraft_logo:412575161041289217",
				CommandCategory.SOCIAL to "a:lori_ablobcouple:412577132007653383",
				CommandCategory.FUN to ":vieirinha:412574915879763982",
				CommandCategory.ADMIN to "\uD83D\uDC6E",
				CommandCategory.IMAGES to "\uD83C\uDFA8",
				CommandCategory.MUSIC to "\uD83C\uDFA7",
				CommandCategory.UTILS to "\uD83D\uDD27",
				CommandCategory.MISC to "\uD83D\uDDC3",
				CommandCategory.ANIME to "\uD83D\uDCFA"
		)

		for (category in categories) {
			val cmdCountInCategory = loritta.commandManager.commandMap.filter { it.category == category && !disabledCommands.contains(it) }.count()
			val reactionEmote = reactionEmotes.getOrDefault(category, ":loritta:331179879582269451")
			val emoji = if (reactionEmote.startsWith(":") || reactionEmote.startsWith("a:")) { "<$reactionEmote>" } else { reactionEmote }
			val commands = if (cmdCountInCategory == 1) "comando" else "comandos"
			description += "$emoji **" + context.locale[category.fancyTitle] + "** ($cmdCountInCategory $commands)\n"
		}

		val embed = EmbedBuilder().apply {
			setTitle(context.locale["AJUDA_SelectCategory"])
			setDescription(description)
			setColor(Color(0, 193, 223))
		}

		val message = privateChannel.sendMessage(embed.build()).complete()
		if (!context.metadata.containsKey("guildId") && !context.isPrivateChannel) {
			context.metadata["guildId"] = context.guild.id
		}
		loritta.messageContextCache[message.id] = context

		for (category in categories) {
			// TODO: Corrigir exception ao usar a reaction antes de terminar de enviar todas as reactions
			val reactionEmote = reactionEmotes.getOrDefault(category, "loritta:331179879582269451")
			message.addReaction(reactionEmote).complete()
		}
		message.addReaction("\uD83D\uDD22").complete() // all categories
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user.id != context.userHandle.id)
			return

		if (e !is MessageReactionAddEvent)
			return

		logger.info("Processando ajuda de ${e.user.name}#${e.user.discriminator} (${e.user.id})...")

		msg.delete().complete()

		if (context.metadata["deleteMessagesOnClick"] != null) {
			val deleteMessagesOnClick = context.metadata["deleteMessagesOnClick"]!! as List<String>

			deleteMessagesOnClick.forEach {
				e.channel.deleteMessageById(it).queue() // Usaremos queue já que nós não tempos certeza se a mensagem ainda existe (espero que sim!)
			}
		}

		if (e.reactionEmote.name == "\uD83D\uDD19") {
			sendInfoBox(context, msg.privateChannel)
			return
		}

		if (e.reactionEmote.name == "\uD83D\uDD22") {
			for (category in CommandCategory.values().filter { it != CommandCategory.MAGIC }) {
				getCommandsFor(context, category).forEach {
					context.sendMessage(it)
				}
			}
			return
		}

		val reactionEmotes = mapOf(
				CommandCategory.DISCORD to "discord_logo",
				CommandCategory.ROBLOX to "roblox_logo",
				CommandCategory.UNDERTALE to "undertale_heart",
				CommandCategory.POKEMON to "pokeball",
				CommandCategory.MINECRAFT to "minecraft_logo",
				CommandCategory.SOCIAL to "lori_ablobcouple",
				CommandCategory.FUN to "vieirinha",
				CommandCategory.ADMIN to "\uD83D\uDC6E",
				CommandCategory.IMAGES to "\uD83C\uDFA8",
				CommandCategory.MUSIC to "\uD83C\uDFA7",
				CommandCategory.UTILS to "\uD83D\uDD27",
				CommandCategory.MISC to "\uD83D\uDDC3",
				CommandCategory.ANIME to "\uD83D\uDCFA"
		)

		val entry = reactionEmotes.entries.firstOrNull { it.value ==  e.reactionEmote.name }
		if (entry != null) {
			// Algumas categorias possuem vários comandos, fazendo que seja necessário enviar vários embeds
			val embeds = getCommandsFor(context, entry.key)
			var lastMessage: Message? = null

			// Para que não fique estranho, nós iremos criar uma lista com todos os IDs que deverão ser deletados após voltar uma categoria, caso exista mais de um embed enviado
			val deleteMessagesOnClick = mutableListOf<String>()

			for (embed in embeds) {
				if (lastMessage != null)
					deleteMessagesOnClick.add(lastMessage.id)

				lastMessage = context.sendMessage(embed)
			}

			context.metadata["deleteMessagesOnClick"] = deleteMessagesOnClick
			if (lastMessage != null) {
				lastMessage.addReaction("\uD83D\uDD19").complete()
				loritta.messageContextCache[lastMessage.id] = context
			}
		}
	}
}