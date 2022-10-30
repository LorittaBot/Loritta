package net.perfectdreams.loritta.cinnamon.discord.utils.images

import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Generates an image collage of the provided users, used for funny memes
 */
inline fun userAvatarCollage(
    width: Int,
    height: Int,
    action: UserAvatarCollage.() -> (Unit)
) = UserAvatarCollage(width, height)
    .apply(action)

class UserAvatarCollage(val width: Int, val height: Int) {
    private val maxSlots = width * height
    private val slots = mutableListOf<UserAvatarSlot>()

    /**
     * Inserts a slot into the collage
     *
     * @param user      the user
     * @param fontColor the color of the font used for the [text]
     * @param text      the text that will be overlaid on top of the user avatar
     */
    fun slot(
        user: User,
        fontColor: Color,
        text: String
    ) {
        if (slots.size == maxSlots)
            error("Too many slots! Max slots: $maxSlots")

        slots.add(UserAvatarSlot(user, fontColor, text))
    }

    /**
     * Inserts a slot into the collage
     *
     * @param i18nContext the i18n context for the [text]
     * @param user        the user
     * @param fontColor   the color of the font used for the [text]
     * @param text        the i18n string key that will be overlaid on top of the user avatar
     */
    fun localizedSlot(
        i18nContext: I18nContext,
        user: User,
        fontColor: Color,
        text: StringI18nData
    ) = slot(user, fontColor, i18nContext.get(text))

    /**
     * Inserts a slot into the collage
     *
     * @param i18nContext        the i18n context for the [text]
     * @param user               the user
     * @param fontColor          the color of the font used for the [text]
     * @param profileSettingsMap a map of User IDs -> PuddingProfileSettings
     * @param male               the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.MALE]
     * @param female             the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.FEMALE]
     * @param unknown            the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.UNKNOWN], or if they don't have any profile set. Defaults to [male]
     */
    fun localizedGenderedSlot(
        i18nContext: I18nContext,
        user: User,
        fontColor: Color,
        profileSettingsMap: Map<Long, PuddingProfileSettings>,
        male: StringI18nData,
        female: StringI18nData,
        unknown: StringI18nData = male
    ) = localizedGenderedSlot(
        i18nContext,
        user,
        fontColor,
        profileSettingsMap[user.id.toLong()]?.gender,
        male,
        female,
        unknown
    )

    /**
     * Inserts a slot into the collage
     *
     * @param i18nContext        the i18n context for the [text]
     * @param user               the user
     * @param fontColor          the color of the font used for the [text]
     * @param gender             the gender of the user
     * @param male               the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.MALE]
     * @param female             the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.FEMALE]
     * @param unknown            the i18n string key that will be overlaid on top of the user avatar if the user has their gender set to [Gender.UNKNOWN], or if they don't have any profile set. Defaults to [male]
     */
    fun localizedGenderedSlot(
        i18nContext: I18nContext,
        user: User,
        fontColor: Color,
        gender: Gender?,
        male: StringI18nData,
        female: StringI18nData,
        unknown: StringI18nData = male
    ) = localizedSlot(
        i18nContext,
        user,
        fontColor,
        when (gender ?: Gender.UNKNOWN) {
            Gender.MALE -> male
            Gender.FEMALE -> female
            Gender.UNKNOWN -> unknown
        }
    )

    suspend fun generate(loritta: LorittaBot): BufferedImage {
        if (slots.size != maxSlots)
            error("Not enough slots! Currently there are ${slots.size} slots, but it should be $maxSlots slots!")

        // Download all avatars in parallel
        // There can be multiple users with the same avatar, in that case, we will download the avatar once (optimization, yay!)
        val userAvatars = coroutineScope {
            val jobs = slots.map { it.user }
                .distinct()
                .map {
                    it to async {
                        ImageUtils.downloadImage(
                            it.effectiveAvatar.cdnUrl.toUrl {
                                format = Image.Format.PNG
                                size = Image.Size.Size128
                            },
                            overrideTimeoutsForSafeDomains = true
                        )
                    }
                }

            jobs.associate { it.first to it.second.await() }
        }

        // Every avatar is a 128x128 image
        val imageWidth = width * 128
        val imageHeight = height * 128

        val base = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)

        var currentX = 0
        var currentY = 0

        for (slot in slots) {
            User128AvatarText.draw(
                loritta,
                base,
                currentX,
                currentY,
                slot.user,
                userAvatars[slot.user] ?: ImageUtils.DEFAULT_DISCORD_AVATAR,
                slot.text,
                slot.fontColor
            )

            currentX += 128

            if (currentX >= imageWidth) {
                currentX = 0
                currentY += 128
            }
        }

        return base
    }

    data class UserAvatarSlot(
        val user: User,
        val fontColor: Color,
        val text: String
    )
}