package net.perfectdreams.loritta.morenitta.modules

import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.HoneypotConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.BanCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.KickCommand
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class HoneypotModule(val loritta: LorittaBot) : MessageReceivedModule {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun matches(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
		val guild = event.guild ?: return false

		val member = event.member
		if (member != null && (member.isOwner || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MESSAGE_MANAGE)))
			return false

		// Don't kick ourselves!
		if (guild.selfMember.idLong == event.author.idLong)
			return false

		val honeypotConfig = loritta.transaction {
			HoneypotConfigs.selectAll()
				.where { HoneypotConfigs.id eq guild.idLong and (HoneypotConfigs.enabled eq true) }
				.firstOrNull()
		} ?: return false

		val channels = honeypotConfig[HoneypotConfigs.channels]
		if (channels.isEmpty())
			return false

		val channel = event.channel
		return channels.contains(channel.idLong) || (channel is ThreadChannel && channels.contains(channel.parentChannel.idLong))
	}

	override suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
		val guild = event.guild ?: return false

		val honeypotConfig = loritta.transaction {
			HoneypotConfigs.selectAll()
				.where { HoneypotConfigs.id eq guild.idLong }
				.firstOrNull()
		} ?: return false

		val selfMember = guild.selfMember
		val targetMember = event.member

		if (targetMember != null && !selfMember.canInteract(targetMember)) {
			logger.warn { "Honeypot triggered in ${guild.id} by ${event.author.id} but the bot cannot interact with the member (role hierarchy)" }
			return false
		}

		if (selfMember.hasPermission(event.message.guildChannel, Permission.MESSAGE_MANAGE)) {
			try {
				event.message.delete().queue()
			} catch (e: Exception) {
				logger.warn(e) { "Failed to delete honeypot message in ${guild.id}" }
			}
		}

		val action = honeypotConfig[HoneypotConfigs.action]
		val deleteDays = honeypotConfig[HoneypotConfigs.deleteDays].coerceIn(0, 7)
		val reason = honeypotConfig[HoneypotConfigs.reason] ?: i18nContext.get(I18nKeysData.Modules.Honeypot.DefaultReason)
		val settings = AdminUtils.retrieveModerationInfo(loritta, serverConfig)
		val punisher = selfMember.user
		val target = event.author

		when (action) {
			PunishmentAction.BAN -> {
				if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
					logger.warn { "Honeypot wants to BAN in ${guild.id} but we lack BAN_MEMBERS!" }
					return false
				}
				BanCommand.ban(loritta, i18nContext, settings, guild, punisher, locale, target, reason, false, deleteDays)
			}
			PunishmentAction.KICK -> {
				if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
					logger.warn { "Honeypot wants to KICK in ${guild.id} but we lack KICK_MEMBERS!" }
					return false
				}
				KickCommand.kick(loritta, guild, i18nContext, punisher, settings, locale, target, reason, false, null)
			}
			PunishmentAction.PURGE_KICK -> {
				if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
					logger.warn { "Honeypot wants to PURGE_KICK in ${guild.id} but we lack BAN_MEMBERS" }
					return false
				}
				KickCommand.kick(loritta, guild, i18nContext, punisher, settings, locale, target, reason, false, deleteDays.toLong())
			}
			else -> {
				logger.warn { "Honeypot in ${guild.id} has unsupported action $action, ignoring..." }
				return false
			}
		}

		return true
	}
}
