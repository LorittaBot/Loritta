package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.images.*
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage

object RankingGenerator {
    val VALID_RANKING_PAGES = 1L..100L

    /**
     * Generates a ranking image
     */
    suspend fun generateRanking(
        loritta: LorittaCinnamon,
        title: String,
        guildIconUrl: String?,
        rankedUsers: List<UserRankInformation>,
        onNullUser: (suspend (Snowflake) -> (CachedUserInfo?))? = null
    ): BufferedImage {
        val rankHeader = readImageFromResources("/rank/rank_header.png")
        val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
        val graphics = base.createGraphics()
            .withTextAntialiasing()

        val serverIconUrl = guildIconUrl?.replace("jpg", "png")

        val serverIcon = (serverIconUrl?.let { ImageUtils.downloadImage(it) } ?: ImageUtils.DEFAULT_DISCORD_AVATAR)
            .getResizedInstance(141, 141, InterpolationType.BILINEAR)

        graphics.drawImage(serverIcon, 259, -52, null)

        graphics.drawImage(rankHeader, 0, 0, null)

        val oswaldRegular10 = loritta.graphicsFonts.oswaldRegular.deriveFont(10F)
        val oswaldRegular16 = loritta.graphicsFonts.oswaldRegular.deriveFont(16F)
        val oswaldRegular20 = loritta.graphicsFonts.oswaldRegular.deriveFont(20F)

        graphics.font = oswaldRegular16

        ImageUtils.drawCenteredString(loritta, graphics, title, Rectangle(0, 0, 268, 37), oswaldRegular16, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

        var idx = 0
        var currentY = 37

        for (profile in rankedUsers) {
            if (idx >= 5) {
                break
            }

            val member = loritta.getCachedUserInfo(profile.userId) ?: onNullUser?.invoke(profile.userId)

            if (member != null) {
                val puddingUserProfile = loritta.services.users.getOrCreateUserProfile(member.id)
                val rankBackground = loritta.getUserProfileBackground(puddingUserProfile)
                graphics.drawImage(rankBackground.getResizedInstance(400, 300, InterpolationType.BILINEAR)
                    .getSubimage(0, idx * 52, 400, 53), 0, currentY, null)

                graphics.color = Color(0, 0, 0, 127)
                graphics.fillRect(0, currentY, 400, 53)

                graphics.color = Color(255, 255, 255)

                graphics.font = oswaldRegular20

                ImageUtils.drawString(loritta, graphics, member.name, 143, currentY + 21, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

                graphics.font = oswaldRegular16

                if (profile.subtitle != null)
                    ImageUtils.drawString(loritta, graphics, profile.subtitle, 144, currentY + 38, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

                graphics.font = oswaldRegular10

                // Show the user's ID in the subsubtitle
                ImageUtils.drawString(loritta, graphics, (profile.subsubtitle?.let { "$it // " } ?: "") + "ID: ${profile.userId}", 145, currentY + 48, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

                val userAvatar = DiscordUserAvatar(loritta.kord, Snowflake(member.id.value), member.discriminator, member.avatarId)
                val avatar = (ImageUtils.downloadImage(userAvatar.cdnUrl.toUrl { format = Image.Format.PNG }) ?: ImageUtils.DEFAULT_DISCORD_AVATAR).getResizedInstance(143, 143, InterpolationType.BILINEAR)

                var editedAvatar = BufferedImage(143, 143, BufferedImage.TYPE_INT_ARGB)
                val avatarGraphics = editedAvatar.graphics as Graphics2D

                val path = Path2D.Double()
                path.moveTo(0.0, 45.0)
                path.lineTo(132.0, 45.0)
                path.lineTo(143.0, 98.0)
                path.lineTo(0.0, 98.0)
                path.closePath()

                avatarGraphics.clip = path

                avatarGraphics.drawImage(avatar, 0, 0, null)

                editedAvatar = editedAvatar.getSubimage(0, 45, 143, 53)
                graphics.drawImage(editedAvatar, 0, currentY, null)
                idx++
                currentY += 53
            }
        }
        return base
    }

    /**
     * Checks if the user is trying to retrieve a valid ranking page
     *
     * To avoid overloading the database with big useless ranking queries, we only allow
     * pages from 1 to 100 to be retrieved
     *
     * @param input the page input
     * @return if the input is in a valid range
     */
    suspend fun isValidRankingPage(input: Long) = input in VALID_RANKING_PAGES

    data class UserRankInformation(
        val userId: Snowflake,
        val subtitle: String? = null,
        val subsubtitle: String? = null
    )
}