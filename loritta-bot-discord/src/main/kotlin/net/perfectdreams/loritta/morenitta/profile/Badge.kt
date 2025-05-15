package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import java.awt.image.BufferedImage
import java.util.*

sealed class Badge(
	val id: UUID,
	val title: StringI18nData,
	val titlePlural: StringI18nData?,
	val description: StringI18nData,
	val priority: Int
) {
	abstract suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean

	abstract suspend fun getImage(): BufferedImage?

	abstract class LorittaBadge(
		id: UUID,
		title: StringI18nData,
		titlePlural: StringI18nData?,
		description: StringI18nData,
		val badgeFileName: String,
		val emoji: LorittaEmojiReference,
		priority: Int,
	) : Badge(id, title, titlePlural, description, priority) {
		override suspend fun getImage() = readImageFromResources("/badges/$badgeFileName")
	}

	// Guild badges have "0, guildId" as the UUID
	class GuildBadge(
		val loritta: LorittaBot,
		val guildId: Long,
		title: StringI18nData,
		titlePlural: StringI18nData?,
		description: StringI18nData,
		val badgeFile: String,
		val badgeMediaType: String,
		val dssNamespace: String,
		priority: Int
	) : Badge(UUID(0, guildId), title, titlePlural, description, priority) {
		override suspend fun checkIfUserDeservesBadge(
			user: ProfileUserInfoData,
			profile: Profile,
			mutualGuilds: Set<Long>,
		) = true

		override suspend fun getImage(): BufferedImage? {
			val extension = MediaTypeUtils.convertContentTypeToExtension(badgeMediaType)
			return LorittaUtils.downloadImage(loritta, "${loritta.config.loritta.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBadge(guildId, badgeFile).join()}.$extension", bypassSafety = true)
		}
	}
}