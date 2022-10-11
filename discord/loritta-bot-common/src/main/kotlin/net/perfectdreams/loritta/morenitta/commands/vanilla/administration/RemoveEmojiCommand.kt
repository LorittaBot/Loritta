package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot

class RemoveEmojiCommand(loritta: LorittaBot) : AbstractCommand(loritta, "removeemoji", listOf("deleteemoji", "deletaremoji", "removeremoji", "delemoji"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.removeemoji.description")

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.ManageEmojisAndStickers)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.ManageEmojisAndStickers)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var deletedEmotes = 0

		context.message.emotes.forEach {
			val emote = context.guildOrNull?.getEmoteById(it.id)
			if (emote != null) {
				emote.delete()
				deletedEmotes++
			}
		}

		if (deletedEmotes != 0) {
			context.reply(
                    LorittaReply(
                            locale["commands.command.removeemoji.success", deletedEmotes, if (deletedEmotes == 1) "emoji" else "emojis"],
                            "\uD83D\uDDD1"
                    )
			)
		} else {
			context.explain()
		}
	}
}