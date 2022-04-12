package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.entities.Icon
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.NotableUserIds
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import kotlin.time.Duration.Companion.minutes

object UserDataUtils {
    suspend fun getInteractionDataOrRetrieveViaRestIfItDoesNotExist(
        loritta: LorittaCinnamon,
        decodedInteractionData: SwitchAvatarInteractionIdData,
        isLookingGuildProfileAvatar: Boolean
    ): ViewingUserAvatarData {
        val storedInteractionData = loritta.services.interactionsData.getInteractionData(decodedInteractionData.interactionDataId)

        if (storedInteractionData == null) {
            // ID is not present, try pulling the user data via REST
            val guildId = decodedInteractionData.guildId

            var member: DiscordGuildMember? = null
            val user = loritta.rest.user.getUser(decodedInteractionData.viewingAvatarOfId)

            if (guildId != null)
                try {
                    member = loritta.rest.guild.getGuildMember(guildId, decodedInteractionData.viewingAvatarOfId)
                } catch (e: KtorRequestException) {
                    if (e.error?.code != JsonErrorCode.UnknownMember)
                        throw e
                }
            
            val memberAvatarHash = member?.avatar?.value
            // If we tried looking at the user's guild profile avatar, but if it doesn't exist, fallback to global user avatar data
            return if (isLookingGuildProfileAvatar && memberAvatarHash != null) {
                ViewingGuildProfileUserAvatarData(
                    user.username,
                    user.discriminator.toInt(),
                    user.avatar,
                    memberAvatarHash
                )
            } else {
                ViewingGlobalUserAvatarData(
                    user.username,
                    user.discriminator.toInt(),
                    user.avatar,
                    memberAvatarHash
                )
            }
        }

        return Json.decodeFromJsonElement(storedInteractionData)
    }

    /**
     * Creates an avatar preview embed from the data in [data]
     */
    fun createAvatarPreviewMessage(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        lorittaId: Snowflake,
        interactionData: SwitchAvatarInteractionIdData,
        data: ViewingUserAvatarData
    ): suspend MessageBuilder.() -> (Unit) {
        val now = Clock.System.now()

        // Unwrap our data so it is easier to access
        val userId = interactionData.viewingAvatarOfId
        val userName = data.userName
        val userDiscriminator = data.discriminator

        val userAvatar: Icon?
        val avatarHash: String?

        // Convert our stored data into Icons, and store them in the "avatarHash" variable so we can check later if it is a GIF or not
        when (data) {
            is ViewingGuildProfileUserAvatarData -> {
                avatarHash = data.memberAvatarId
                userAvatar = Icon.MemberAvatar(
                    interactionData.guildId!!,
                    userId,
                    data.memberAvatarId
                )
            }
            is ViewingGlobalUserAvatarData -> {
                avatarHash = data.userAvatarId
                userAvatar = UserUtils.convertUserAvatarToIcon(
                    interactionData.viewingAvatarOfId,
                    avatarHash,
                    userDiscriminator
                )
            }
        }

        // Create the avatar message
        return {
            embed {
                title = "\uD83D\uDDBC $userName"

                // Specific User Avatar Easter Egg Texts
                val easterEggFooterTextKey = when {
                    // Easter Egg: Looking up yourself
                    interactionData.userId == interactionData.viewingAvatarOfId -> UserCommand.I18N_PREFIX.Avatar.YourselfEasterEgg

                    // Easter Egg: Loritta/Application ID
                    // TODO: Show who made the fan art during the Fan Art Extravaganza
                    userId == lorittaId -> UserCommand.I18N_PREFIX.Avatar.LorittaEasterEgg

                    // Easter Egg: Pantufa
                    userId == NotableUserIds.PANTUFA -> UserCommand.I18N_PREFIX.Avatar.PantufaEasterEgg

                    // Easter Egg: Gabriela
                    userId == NotableUserIds.GABRIELA -> UserCommand.I18N_PREFIX.Avatar.GabrielaEasterEgg

                    // Easter Egg: Carl-bot
                    userId == NotableUserIds.CARLBOT -> UserCommand.I18N_PREFIX.Avatar.CarlbotEasterEgg

                    // Easter Egg: Dank Memer
                    userId == NotableUserIds.DANK_MEMER -> UserCommand.I18N_PREFIX.Avatar.DankMemerEasterEgg

                    // Easter Egg: Mantaro
                    userId == NotableUserIds.MANTARO -> UserCommand.I18N_PREFIX.Avatar.MantaroEasterEgg

                    // Easter Egg: Erisly
                    userId == NotableUserIds.ERISLY -> UserCommand.I18N_PREFIX.Avatar.ErislyEasterEgg

                    // Easter Egg: Kuraminha
                    userId == NotableUserIds.KURAMINHA -> UserCommand.I18N_PREFIX.Avatar.KuraminhaEasterEgg

                    // Nothing else, just use null
                    else -> null
                }

                // If the text is present, set it as the footer!
                if (easterEggFooterTextKey != null)
                    footer(i18nContext.get(easterEggFooterTextKey))

                color = Color(114, 137, 218) // TODO: Move this to an object

                val imageUrl = userAvatar.cdnUrl.toUrl {
                    // Images that start with "a_" means that are an animated image
                    this.format = if (avatarHash?.startsWith("a_") == true) Image.Format.GIF else Image.Format.PNG
                    this.size = Image.Size.Size2048
                }
                image = imageUrl

                actionRow {
                    // "Open Avatar in Browser" button
                    linkButton(
                        url = imageUrl
                    ) {
                        label = i18nContext.get(I18nKeysData.Commands.Command.User.Avatar.OpenAvatarInBrowser)
                    }

                    // Additional avatar switch buttons, if needed
                    val memberAvatarId = data.memberAvatarId
                    if (data is ViewingGuildProfileUserAvatarData) {
                        // If the user is currently viewing the user's guild profile avatar, add a button to switch to the original avatar
                        val id = loritta.services.interactionsData.insertInteractionData(
                            Json.encodeToJsonElement<ViewingUserAvatarData>(
                                ViewingGlobalUserAvatarData(
                                    data.userName,
                                    data.discriminator,
                                    data.userAvatarId,
                                    data.memberAvatarId
                                )
                            ).jsonObject,
                            now,
                            now + 15.minutes // Expires after 15m
                        )

                        interactiveButton(
                            ButtonStyle.Primary,
                            SwitchToGlobalAvatarExecutor,
                            ComponentDataUtils.encode(
                                interactionData
                                    .copy(interactionDataId = id)
                            )
                        ) {
                            label = i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGlobalAvatar)
                        }
                    } else if (memberAvatarId != null) {
                        // If the user is currently viewing the user's avatar, and the user has a guild profile avatar, add a button to switch to the guild profile avatar
                        val id = loritta.services.interactionsData.insertInteractionData(
                            Json.encodeToJsonElement<ViewingUserAvatarData>(
                                ViewingGuildProfileUserAvatarData(
                                    data.userName,
                                    data.discriminator,
                                    data.userAvatarId,
                                    memberAvatarId
                                )
                            ).jsonObject,
                            now,
                            now + 15.minutes // Expires after 15m
                        )

                        interactiveButton(
                            ButtonStyle.Primary,
                            SwitchToGuildProfileAvatarExecutor,
                            ComponentDataUtils.encode(
                                interactionData
                                    .copy(interactionDataId = id)
                            )
                        ) {
                            label = i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGuildProfileAvatar)
                        }
                    }
                }
            }
        }
    }

    /**
     * We don't store the data in [ViewingUserAvatarData] here because it won't fit in the Custom ID! (avatar IDs are one hecc of a chonky boi)
     */
    @Serializable
    data class SwitchAvatarInteractionIdData(
        override val userId: Snowflake,
        val viewingAvatarOfId: Snowflake,
        val guildId: Snowflake?,
        val interactionDataId: Long
    ) : SingleUserComponentData

    @Serializable
    sealed class ViewingUserAvatarData {
        abstract val userName: String
        abstract val discriminator: Int
        abstract val userAvatarId: String?
        abstract val memberAvatarId: String?
    }

    @Serializable
    data class ViewingGlobalUserAvatarData(
        override val userName: String,
        override val discriminator: Int,
        override val userAvatarId: String?,
        override val memberAvatarId: String?
    ) : ViewingUserAvatarData()

    @Serializable
    data class ViewingGuildProfileUserAvatarData(
        override val userName: String,
        override val discriminator: Int,
        override val userAvatarId: String?,
        override val memberAvatarId: String
    ) : ViewingUserAvatarData()
}