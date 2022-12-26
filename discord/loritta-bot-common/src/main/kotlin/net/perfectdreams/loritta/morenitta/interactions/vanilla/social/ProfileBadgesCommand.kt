package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.util.*

class ProfileBadgesCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Profilebadges

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL) {
        executor = ProfileBadgesExecutor()
    }

    inner class ProfileBadgesExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val badges = loritta.profileDesignManager.getUserBadges(
                loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                context.lorittaUser.profile,
                setOf()
            )

            context.reply(false) {
                embed {
                    description = "Veja as suas badges e tals"
                }

                /* for (badge in badges) {
                    styled("**${context.i18nContext.get(badge.title)}**")
                    styled(context.i18nContext.get(badge.description))
                } */

                actionRow(
                    loritta.interactivityManager.stringSelectMenu(
                        {
                            placeholder = "Insígnia"

                            for (badge in badges) {
                                addOption(context.i18nContext.get(badge.title), badge.id.toString(), context.i18nContext.get(badge.description).shortenWithEllipsis(100))
                            }
                        }
                    ) { componentContext, strings ->
                        val badgeIdAsString = strings.first()
                        val badge = badges.first { it.id == UUID.fromString(badgeIdAsString) }

                        componentContext.deferEdit()
                            .editOriginal(
                                MessageEdit {
                                    embed {
                                        title = context.i18nContext.get(badge.title)
                                        description = context.i18nContext.get(badge.description)
                                        thumbnail = "attachment://badge.png"
                                    }

                                    val badgeImage = badge.getImage()
                                    if (badgeImage != null)
                                        files += FileUpload.fromData(badgeImage.toByteArray(ImageFormatType.PNG), "badge.png")

                                    actionRow(
                                        loritta.interactivityManager.buttonForUser(
                                            componentContext.user,
                                            ButtonStyle.PRIMARY,
                                            "Equipar Insígnia"
                                        ) {
                                            it.reply(true) { content = "TODO: Equip" }
                                        }
                                    )
                                }
                            )
                            .setReplace(true)
                            .await()
                    }
                )
            }
        }
    }
}