package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.DefaultProfileCreator
import com.mrpowergamerbr.loritta.profile.MSNProfileCreator
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.profile.OrkutProfileCreator
import com.mrpowergamerbr.loritta.tables.DonationConfigs
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerfilCommand : AbstractCommand("profile", listOf("perfil"), CommandCategory.SOCIAL) {
	companion object {
		var userVotes: MutableList<DiscordBotVote>? = null
		var lastQuery = 0L

		fun getUserBadges(user: User, profile: Profile, mutualGuilds: List<Guild> = lorittaShards.getMutualGuilds(user)): List<BufferedImage> {
			// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
			fun hasRole(guildId: String, roleId: String): Boolean {
				val lorittaGuild = lorittaShards.getGuildById(guildId)
				return if (lorittaGuild != null) {
					if (lorittaGuild.isMember(user)) {
						val member = lorittaGuild.getMember(user)
						val role = lorittaGuild.getRoleById(roleId)
						member?.roles?.contains(role) ?: false
					} else {
						false
					}
				} else {
					false
				}
			}

			try {
				// biscord bots
				if (System.currentTimeMillis() - lastQuery > 60000) {
					val discordBotsResponse = HttpRequest.get("https://discordbots.org/api/bots/${loritta.discordConfig.discord.clientId}/votes?onlyids=1")
							.authorization(loritta.discordConfig.discordBotList.apiKey)
							.body()

					lastQuery = System.currentTimeMillis()
					userVotes = GSON.fromJson(discordBotsResponse)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}


			val upvotedOnDiscordBots = try {
				if (userVotes != null) {
					userVotes!!.any { it.id == user.id }
				} else {
					false
				}
			} catch (e: Exception) {
				false
			}

			val hasNotifyMeRole = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "334734175531696128")
			val isLorittaPartner = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "434512654292221952")
			val isTranslator = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "385579854336360449")
			val isGitHubContributor = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, "505144985591480333")
			val hasLoriStickerArt = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, Constants.LORI_STICKERS_ROLE_ID)
			val isPocketDreamsStaff = hasRole(Constants.SPARKLYPOWER_GUILD_ID, "332650495522897920")

			val badges = mutableListOf<BufferedImage>()
			if (user.patreon || loritta.config.isOwner(user.id)) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush.png"))
			if (user.supervisor) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
			if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
			if (user.support) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
			if (hasLoriStickerArt) badges += ImageIO.read(File(Loritta.ASSETS + "sticker_badge.png"))

			val money = loritta.getActiveMoneyFromDonations(user.idLong)

			if (money != 0.0) {
				badges += ImageIO.read(File(Loritta.ASSETS + "donator.png"))

				if (money >= 99.99) {
					badges += ImageIO.read(File(Loritta.ASSETS + "super_donator.png"))
				}
			}

			if (isLorittaPartner) badges += ImageIO.read(File(Loritta.ASSETS + "lori_hype.png"))
			if (isTranslator) badges += ImageIO.read(File(Loritta.ASSETS + "translator.png"))
			if (isGitHubContributor) badges += ImageIO.read(File(Loritta.ASSETS + "github_contributor.png"))
			if (user.artist) badges += ImageIO.read(File(Loritta.ASSETS + "artist_badge.png"))

			transaction(Databases.loritta) {
				var specialCase = false

				val results = if (user.idLong == loritta.discordConfig.discord.clientId.toLong()) { // Como estamos em MUITOS servidores, um in list dá problema! E como a gente é fofis, vamos apenas pegar todos os servidores
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								// Então iremos pegar apenas
								DonationConfigs.customBadge eq true
							}
				} else if (30_000 > mutualGuilds.size) { // Se está em menos de 30k servidores, o PostgreSQL ainda suporta pegar via inList
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								DonationConfigs.customBadge eq true and (ServerConfigs.id inList mutualGuilds.map { it.idLong })
							}
				} else {
					specialCase = true
					// Aqui temos bots grandes demais para suportar, nós *iremos* pegar todos, mas iremos filtrar client side (oof)
					(ServerConfigs innerJoin DonationConfigs)
							.select {
								// Então iremos pegar apenas
								DonationConfigs.customBadge eq true
							}
				}

				val configs = ServerConfig.wrapRows(results)

				for (config in configs) {
					if (specialCase && mutualGuilds.any { it.idLong == config.id.value })
						continue

					val donationKey = config.donationKey
					if (donationKey != null && donationKey.isActive() && donationKey.value >= LorittaPrices.CUSTOM_BADGE) {
						val badgeFile = File(Loritta.ASSETS, "badges/custom/${config.guildId}.png")

						if (badgeFile.exists()) {
							badges += ImageIO.read(badgeFile)
						}
					}
				}
			}

			if (hasNotifyMeRole) badges += ImageIO.read(File(Loritta.ASSETS + "notify_me.png"))
			if (user.id == loritta.discordConfig.discord.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
			if (user.isBot) badges += ImageIO.read(File(Loritta.ASSETS + "robot_badge.png"))
			val marriage = transaction(Databases.loritta) { profile.marriage }
			if (marriage != null) {
				if (System.currentTimeMillis() - marriage.marriedSince > 2_592_000_000) {
					badges += ImageIO.read(File(Loritta.ASSETS + "blob_snuggle.png"))
				}
				badges += ImageIO.read(File(Loritta.ASSETS + "ring.png"))
			}
			if (upvotedOnDiscordBots) badges += ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))

			return badges
		}
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PERFIL_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		var userProfile = context.lorittaUser.profile

		val contextUser = context.getUserAt(0)
		val user = if (contextUser != null) contextUser else context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getOrCreateLorittaProfile(contextUser.id)
		}

		val settings = transaction(Databases.loritta) { userProfile.settings }

		if (contextUser != null && userProfile.isBanned) {
			context.reply(
					LoriReply(
							"${contextUser.asMention} está **banido**",
							"\uD83D\uDE45"
					),
					LoriReply(
							"**Motivo:** `${userProfile.bannedReason}`",
							"✍"
					)
			)
			return
		}
		if (contextUser == null && context.args.isNotEmpty() && context.args.first() == "shop") {
			context.reply(LoriReply(context.locale["commands.social.profile.profileshop","${loritta.config.loritta.website.url}user/@me/dashboard/profiles"], Emotes.LORI_OWO))
			return
		}

		// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
		val mutualGuilds = lorittaShards.getMutualGuilds(user)
		val member = mutualGuilds.firstOrNull()?.getMember(user)
		val badges = getUserBadges(user, userProfile, mutualGuilds)

		val file = File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")

		var aboutMe: String? = null

		if (userProfile.userId == loritta.discordConfig.discord.clientId.toLong()) {
			aboutMe = locale["PERFIL_LORITTA_DESCRIPTION"]
		}

		if (userProfile.userId == 390927821997998081L) {
			aboutMe = "Olá, eu me chamo Pantufa, sou da equipe do SparklyPower (e eu sou a melhor ajudante de lá! :3), e, é claro, a melhor amiga da Lori!"
		}

		if (settings.aboutMe != null && settings.aboutMe != "A Loritta é minha amiga!") {
			aboutMe = settings.aboutMe
		}

		if (aboutMe == null) {
			aboutMe = "A Loritta é a minha amiga! Sabia que você pode alterar este texto usando \"${context.config.commandPrefix}sobremim\"? :3"
		}

		val background = when {
			file.exists() -> ImageIO.read(File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
			else -> {
				// Background padrão
				ImageIO.read(File(Loritta.ASSETS + "default_background.png"))
			}
		}

		val map = mapOf(
				"default" to NostalgiaProfileCreator::class.java,
				"modern" to DefaultProfileCreator::class.java,
				"msn" to MSNProfileCreator::class.java,
				"orkut" to OrkutProfileCreator::class.java
		)

		var type = if (user.idLong == context.userHandle.idLong) {
			context.args.getOrNull(0)
		} else { null } ?: map.entries.firstOrNull { settings.activeProfile == it.value.simpleName }?.key ?: "default"

		if (!map.containsKey(type) || !settings.boughtProfiles.contains(map[type]!!.simpleName))
			type = "default"

		val creator = map[type]!!
		val profileCreator = creator.newInstance()
		val profile = profileCreator.create(
				context.userHandle,
				user,
				userProfile,
				context.guild,
				context.config,
				badges,
				locale,
				background,
				aboutMe,
				member
		)

		context.sendFile(profile, "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.legacyLocale["PEFIL_PROFILE"]) // E agora envie o arquivo
	}

	class DiscordBotVote(
			val id: String
	)
}