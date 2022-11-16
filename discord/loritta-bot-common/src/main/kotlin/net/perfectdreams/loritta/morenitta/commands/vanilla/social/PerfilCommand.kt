package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class PerfilCommand(loritta: LorittaBot) : AbstractCommand(loritta, "profile", listOf("perfil"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.profile.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.profile.examples")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.USER) {
			optional = true
		}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		var userProfile = context.lorittaUser.profile

		val contextUser = context.getUserAt(0)
		val user = contextUser ?: context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getOrCreateLorittaProfile(contextUser.id)
		}

		if (AccountUtils.checkAndSendMessageIfUserIsBanned(context, userProfile))
			return

		val settings = loritta.newSuspendedTransaction { userProfile.settings }

		if (contextUser == null && context.args.isNotEmpty() && (context.args.first() == "shop" || context.args.first() == "loja")) {
			context.reply(LorittaReply(context.locale["commands.command.profile.profileshop", "${loritta.config.loritta.website.url}user/@me/dashboard/profiles"], Emotes.LORI_OWO))
			return
		}

		// We need the mutual guilds to retrieve the user's guild badges.
		// However because bots can be in a LOT of guilds (causing GC pressure), so we will just return a empty array.
		// Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
		//
		// After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
		val mutualGuildsInAllClusters = if (user.isBot)
			setOf()
		else
			loritta.newSuspendedTransaction {
				GuildProfiles.slice(GuildProfiles.guildId)
					.select { GuildProfiles.userId eq user.idLong and (GuildProfiles.isInGuild eq true) }
					.map { it[GuildProfiles.guildId] }
					.toSet()
			}

		val result = loritta.profileDesignManager.createProfile(
			loritta,
			context.i18nContext,
			context.locale,
			loritta.profileDesignManager.transformUserToProfileUserInfoData(context.userHandle),
			loritta.profileDesignManager.transformUserToProfileUserInfoData(user),
			context.guildOrNull?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) }
		)

		context.sendFile(result.image.inputStream(), "lori_profile.${result.imageFormat.extension}", "📝 **|** " + context.getAsMention(true) + context.locale["commands.command.profile.profile"]) // E agora envie o arquivo
	}
}