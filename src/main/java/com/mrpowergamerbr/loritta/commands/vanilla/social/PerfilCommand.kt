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
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
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

		fun getUserBadges(user: User, profile: Profile): List<BufferedImage> {
			// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
			val member = lorittaShards.getMutualGuilds(user).firstOrNull()?.getMember(user)

			fun hasRole(guildId: String, roleId: String): Boolean {
				val lorittaGuild = lorittaShards.getGuildById(guildId)
				return if (lorittaGuild != null) {
					if (lorittaGuild.isMember(user)) {
						val member = lorittaGuild.getMember(user)
						val role = lorittaGuild.getRoleById(roleId)
						member.roles.contains(role)
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
					val discordBotsResponse = HttpRequest.get("https://discordbots.org/api/bots/${Loritta.config.clientId}/votes?onlyids=1")
							.authorization(Loritta.config.discordBotsOrgKey)
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
			val hasLoriStickerArt = hasRole(Constants.PORTUGUESE_SUPPORT_GUILD_ID, Constants.LORI_STICKERS_ROLE_ID)
			val isPocketDreamsStaff = hasRole(Constants.SPARKLYPOWER_GUILD_ID, "332650495522897920")
			val usesPocketDreamsRichPresence = if (member != null) {
				val game = member.game
				if (game != null && game.isRich) {
					game.asRichPresence().applicationId == "415617983411388428"
				} else {
					false
				}
			} else {
				false
			}

			val badges = mutableListOf<BufferedImage>()
			if (user.patreon || user.id == Loritta.config.ownerId) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush.png"))
			if (user.supervisor) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
			if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
			if (user.support) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
			if (hasLoriStickerArt) badges += ImageIO.read(File(Loritta.ASSETS + "sticker_badge.png"))
			if (user.donator) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush2.png"))
			if (isLorittaPartner) badges += ImageIO.read(File(Loritta.ASSETS + "lori_hype.png"))
			if (isTranslator) badges += ImageIO.read(File(Loritta.ASSETS + "translator.png"))
			if (user.artist) badges += ImageIO.read(File(Loritta.ASSETS + "artist_badge.png"))

			val mutualGuilds = lorittaShards.getMutualGuilds(user)

			transaction(Databases.loritta) {
				val results = (ServerConfigs innerJoin DonationConfigs)
						.select {
                            DonationConfigs.customBadge eq true and (ServerConfigs.id inList mutualGuilds.map { it.idLong })
                        }

				val configs = ServerConfig.wrapRows(results)

				for (config in configs) {
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
			if (usesPocketDreamsRichPresence) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_rp.png"))
			if (user.id == Loritta.config.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

		// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
		val member = lorittaShards.getMutualGuilds(user).firstOrNull()?.getMember(user)
		val badges = getUserBadges(user, userProfile)

		val file = File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")

		var aboutMe: String? = null

		if (userProfile.userId == Loritta.config.clientId.toLong()) {
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

		var type = context.rawArgs.getOrNull(1) ?: context.rawArgs.getOrNull(0) ?: "default"
		if (!map.containsKey(type))
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

		context.sendFile(profile, "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.legacyLocale["PEFIL_PROFILE"] + " ${if (type != "default") "*Atenção: Isto é um design em testes e futuramente será vendido na loja da Loritta!*" else ""}") // E agora envie o arquivo
	}

	class DiscordBotVote(
			val id: String
	)
}