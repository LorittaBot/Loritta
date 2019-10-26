package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.dao.BirthdayConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.QuirkyStuff
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.jetbrains.exposed.sql.transactions.transaction

class LoriToolsQuirkyStuffCommand(val m: QuirkyStuff) : LorittaDiscordCommand(arrayOf("loritoolsqs"), CommandCategory.MAGIC) {
	override val onlyOwner: Boolean
		get() = true

	@Subcommand(["enable_boost"])
	suspend fun enableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostActivate(member)
	}

	@Subcommand(["disable_boost"])
	suspend fun disableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostDeactivate(member)
	}

	@Subcommand(["send_sponsored_message"])
	suspend fun sendSponsoredMessage(context: DiscordCommandContext) {
		m.sponsorsAdvertisement?.broadcastSponsoredMessage()
	}

	@Subcommand(["enable_birthday_in_channel"])
	suspend fun enableBirthdayInChannel(context: DiscordCommandContext, channelId: String) {
		transaction(Databases.loritta) {
			val serverConfig = ServerConfig.findById(context.discordGuild!!.idLong)!!

			if (serverConfig.birthdayConfig != null) {
				serverConfig.birthdayConfig!!.enabled = true
				serverConfig.birthdayConfig!!.channelId = channelId.toLong()
			} else {
				serverConfig.birthdayConfig = BirthdayConfig.new {
					this.enabled = true
					this.channelId = channelId.toLong()
				}
			}
		}

		context.reply(
				"Ativado!"
		)
	}

	@Subcommand(["set_birthday_role"])
	suspend fun setBirthdayRole(context: DiscordCommandContext, roleId: String) {
		transaction(Databases.loritta) {
			val serverConfig = ServerConfig.findById(context.discordGuild!!.idLong)!!

			serverConfig.birthdayConfig!!.roles = arrayOf(roleId.toLong())
		}

		context.reply(
				"Role ativada!"
		)
	}

	@Subcommand(["cycle_banner"])
	suspend fun cycleBanner(context: DiscordCommandContext, args: Array<String>) {
		m.changeBanner?.changeBanner()
	}
}