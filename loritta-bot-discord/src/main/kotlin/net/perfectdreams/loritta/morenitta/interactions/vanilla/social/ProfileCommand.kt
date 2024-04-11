package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.kord.common.entity.Snowflake
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

class ProfileCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        suspend fun createMessage(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            sender: User,
            userToBeViewed: User,
            result: ProfileDesignManager.ProfileCreationResult
        ): suspend InlineMessage<*>.() -> (Unit) = {
            files += FileUpload.fromData(result.image.inputStream(), "profile.${result.imageFormat.extension}")
                .setDescription(i18nContext.get(I18nKeysData.Commands.Command.Profileview.ProfileImageAltText(userToBeViewed.name)))

            if (userToBeViewed == sender) {
                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        sender,
                        ButtonStyle.SECONDARY,
                        i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeAboutMe),
                        {
                            loriEmoji = Emotes.LoriReading
                        }
                    ) {
                        val aboutMeOption = modalString(
                            it.i18nContext.get(I18nKeysData.Profiles.AboutMe),
                            TextInputStyle.PARAGRAPH,
                            value = result.aboutMe
                        )

                        it.sendModal(
                            it.i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeAboutMe),
                            listOf(ActionRow.of(aboutMeOption.toJDA()))
                        ) { it, args ->
                            val hook = it.deferEdit()

                            val newAboutMe = args[aboutMeOption]

                            val userSettings = it.loritta.pudding.users.getOrCreateUserProfile(
                                net.perfectdreams.loritta.serializable.UserId(
                                    it.user.idLong
                                )
                            )
                                .getProfileSettings()

                            userSettings.setAboutMe(newAboutMe)

                            val guild = it.guildOrNull

                            val result = loritta.profileDesignManager.createProfile(
                                loritta,
                                it.i18nContext,
                                it.locale,
                                loritta.profileDesignManager.transformUserToProfileUserInfoData(it.user),
                                loritta.profileDesignManager.transformUserToProfileUserInfoData(it.user),
                                guild?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) }
                            )

                            val message = createMessage(loritta, it.i18nContext, it.user, it.user, result)

                            hook.jdaHook.editOriginal(
                                MessageEdit {
                                    message()
                                }
                            ).setReplace(true).await()
                        }
                    }
                )
            }
        }
    }

    private val I18N_PREFIX = I18nKeysData.Commands.Command.Profile
    private val PROFILE_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Profileview
    private val PROFILE_BADGES_I18N_PREFIX = I18nKeysData.Commands.Command.Profilebadges
    private val ABOUT_ME_I18N_PREFIX = I18nKeysData.Commands.Command.Aboutme

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL) {
        enableLegacyMessageSupport = true

        examples = PROFILE_VIEW_I18N_PREFIX.Examples

        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        subcommand(PROFILE_VIEW_I18N_PREFIX.Label, PROFILE_VIEW_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("profile")
                add("perfil")
            }

            executor = ProfileViewExecutor()
        }

        subcommand(ABOUT_ME_I18N_PREFIX.Label, ABOUT_ME_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("sobremim")
                add("aboutme")
            }

            examples = ABOUT_ME_I18N_PREFIX.Examples

            executor = AboutMeExecutor()
        }

        subcommand(PROFILE_BADGES_I18N_PREFIX.Label, PROFILE_BADGES_I18N_PREFIX.Description) {
            alternativeLegacyLabels.apply {
                add("insígnias")
            }

            executor = ProfileBadgesExecutor()
        }
    }

    inner class ProfileViewExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", PROFILE_VIEW_I18N_PREFIX.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userToBeViewed = args[options.user]?.user ?: context.user

            if (AccountUtils.checkAndSendMessageIfUserIsBanned(loritta, context, userToBeViewed))
                return

            context.deferChannelMessage(false)

            val guild = context.guildOrNull

            val result = loritta.profileDesignManager.createProfile(
                loritta,
                context.i18nContext,
                context.locale,
                loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                loritta.profileDesignManager.transformUserToProfileUserInfoData(userToBeViewed),
                guild?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) }
            )

            val message = createMessage(loritta, context.i18nContext, context.user, userToBeViewed, result)

            context.reply(false) {
                message()
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.user to context.getUserAndMember(0)
            )
        }
    }

    inner class AboutMeExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val aboutMe = string("about_me", ABOUT_ME_I18N_PREFIX.Options.Aboutme.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val newAboutMe = args[options.aboutMe]

            val userSettings =
                context.loritta.pudding.users.getOrCreateUserProfile(UserId(Snowflake(context.user.idLong)))
                    .getProfileSettings()

            userSettings.setAboutMe(newAboutMe)

            context.reply(true) {
                styled(
                    context.i18nContext.get(ABOUT_ME_I18N_PREFIX.SuccessfullyChanged(newAboutMe)),
                    Emotes.Tada
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.aboutMe to context.args.joinToString(" ")
            )
        }
    }

    inner class ProfileBadgesExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            context.reply(false) {
                createBadgeListMessage(context)()
            }
        }

        private suspend fun createBadgeListMessage(context: UnleashedContext): suspend InlineMessage<*>.() -> (Unit) {
            return {
                // We need the mutual guilds to retrieve the user's guild badges.
                // However, because bots can be in a LOT of guilds (causing GC pressure), so we will just return an empty array.
                // Bots could also cause a lot of badges to be downloaded, because they are in a lot of guilds.
                //
                // After all, does it *really* matter that bots won't have any badges? ¯\_(ツ)_/¯
                val mutualGuildsInAllClusters = if (context.user.isBot) // This should never happen!
                    setOf()
                else
                    loritta.pudding.transaction {
                        GuildProfiles.slice(GuildProfiles.guildId)
                            .select { GuildProfiles.userId eq context.user.id.toLong() and (GuildProfiles.isInGuild eq true) }
                            .map { it[GuildProfiles.guildId] }
                            .toSet()
                    }

                val badges = loritta.profileDesignManager.getUserBadges(
                    loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                    context.lorittaUser.profile,
                    mutualGuildsInAllClusters
                )

                if (badges.isEmpty()) {
                    styled(
                        context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.YouDontHaveAnyBadges),
                        Emotes.LoriSob
                    )
                } else {
                    embed {
                        title = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.YourBadges)
                        description =
                            context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgesDescription).joinToString("\n\n")
                        color = LorittaColors.LorittaAqua.rgb
                    }

                    actionRow(
                        loritta.interactivityManager.stringSelectMenuForUser(
                            context.user,
                            {
                                placeholder = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.ChooseABadge)

                                for (badge in badges.take(25)) {
                                    addOption(
                                        context.i18nContext.get(badge.title),
                                        badge.id.toString(),
                                        context.i18nContext.get(badge.description).shortenWithEllipsis(100)
                                    )
                                }
                            }
                        ) { componentContext, strings ->
                            val badgeIdAsString = strings.first()
                            val badge = badges.first { it.id == UUID.fromString(badgeIdAsString) }

                            componentContext.deferEdit()
                                .editOriginal(
                                    MessageEdit {
                                        createBadgeViewMessage(context, badge)()
                                    }
                                )
                                .setReplace(true)
                                .await()
                        }
                    )

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.UnequipBadge)
                        ) {
                            loritta.newSuspendedTransaction {
                                UserSettings
                                    .innerJoin(Profiles)
                                    .update({ Profiles.id eq context.user.idLong }) {
                                        it[UserSettings.activeBadge] = null
                                    }
                            }

                            it.reply(true) {
                                content = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgeUnequipped)
                            }
                        }
                    )
                }
            }
        }

        private suspend fun createBadgeViewMessage(
            context: UnleashedContext,
            badge: Badge
        ): suspend InlineMessage<*>.() -> (Unit) {
            return {
                embed {
                    title = context.i18nContext.get(badge.title)
                    description = context.i18nContext.get(badge.description)
                    thumbnail = "attachment://badge.png"
                    color = LorittaColors.LorittaAqua.rgb
                }

                val badgeImage = badge.getImage()
                if (badgeImage != null)
                    files += FileUpload.fromData(badgeImage.toByteArray(ImageFormatType.PNG), "badge.png")
                        .setDescription(context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgeImageAltText(context.i18nContext.get(badge.title))))

                val components = mutableListOf(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.SECONDARY,
                        builder = {
                            this.loriEmoji = Emotes.ChevronLeft
                        }
                    ) {
                        it.deferEdit().editOriginal(
                            MessageEdit {
                                createBadgeListMessage(context)()
                            }
                        ).setReplace(true).await()
                    }
                )

                if (badge !is Badge.GuildBadge) {
                    components += loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.EquipBadge)
                    ) {
                        loritta.newSuspendedTransaction {
                            UserSettings
                                .innerJoin(Profiles)
                                .update({ Profiles.id eq context.user.idLong }) {
                                    it[UserSettings.activeBadge] = badge.id
                                }
                        }

                        it.reply(true) { content = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgeEquipped) }
                    }
                }

                actionRow(components)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}