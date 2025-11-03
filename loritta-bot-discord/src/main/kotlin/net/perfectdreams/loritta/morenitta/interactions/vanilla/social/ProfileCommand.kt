package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.HiddenUserBadges
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.profiles.ProfileCreator
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.RankPaginationUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import java.sql.Connection
import java.sql.ResultSet
import java.time.Instant
import java.util.*
import kotlin.math.ceil

class ProfileCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        suspend fun createMessage(
            loritta: LorittaBot,
            context: UnleashedContext,
            i18nContext: I18nContext,
            sender: User,
            userToBeViewed: User,
            profileCreator: ProfileCreator,
            result: ProfileDesignManager.ProfileCreationResult
        ): suspend InlineMessage<*>.() -> (Unit) = {
            files += FileUpload.fromData(result.image.inputStream(), "profile.${result.imageFormat.extension}")
                .setDescription(i18nContext.get(I18nKeysData.Commands.Command.Profileview.ProfileImageAltText(userToBeViewed.name)))

            if (userToBeViewed == sender) {
                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        sender,
                        context.alwaysEphemeral,
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
                                UserId(
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
                                guild?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) },
                                profileCreator
                            )

                            val message = createMessage(loritta, context, it.i18nContext, it.user, it.user, profileCreator, result)

                            hook.jdaHook.editOriginal(
                                MessageEdit {
                                    message()
                                }
                            ).setReplace(true).await()
                        }
                    },
                    Button.of(
                        ButtonStyle.LINK,
                        "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profiles?utm_source=discord&utm_medium=profile-command&utm_campaign=daily-item-shop&utm_content=self-profile",
                        i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeProfileLayout),
                        Emotes.LoriIdentificationCard.toJDA()
                    ),
                    Button.of(
                        ButtonStyle.LINK,
                        "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/backgrounds?utm_source=discord&utm_medium=profile-command&utm_campaign=daily-item-shop&utm_content=self-profile",
                        i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeBackground),
                        Emoji.fromUnicode("\uD83D\uDDBC\uFE0F")
                    ),
                    Button.of(
                        ButtonStyle.LINK,
                        "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/daily-shop?utm_source=discord&utm_medium=profile-command&utm_campaign=daily-item-shop&utm_content=self-profile",
                        i18nContext.get(I18nKeysData.Commands.Command.Profileview.LorittaDailyItemShop),
                        Emotes.ShoppingBags.toJDA()
                    )
                )
            }
        }
    }

    private val I18N_PREFIX = I18nKeysData.Commands.Command.Profile
    private val PROFILE_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Profileview
    private val PROFILE_BADGES_I18N_PREFIX = I18nKeysData.Commands.Command.Profilebadges
    private val PROFILE_SHOP_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Profileshoprank
    private val ABOUT_ME_I18N_PREFIX = I18nKeysData.Commands.Command.Aboutme

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("785a6cf3-8cec-4cbc-8b70-ee97dfa27582")) {
        enableLegacyMessageSupport = true

        examples = PROFILE_VIEW_I18N_PREFIX.Examples

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(PROFILE_VIEW_I18N_PREFIX.Label, PROFILE_VIEW_I18N_PREFIX.Description, UUID.fromString("0b3d2b12-f46f-4a16-80fd-a9088cc699b1")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("profile")
                add("perfil")
            }

            executor = ProfileViewExecutor()
        }

        subcommand(ABOUT_ME_I18N_PREFIX.Label, ABOUT_ME_I18N_PREFIX.Description, UUID.fromString("3818d227-e42f-4326-a7f0-49dce58f693b")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("sobremim")
                add("aboutme")
            }

            examples = ABOUT_ME_I18N_PREFIX.Examples

            executor = AboutMeExecutor()
        }

        subcommand(PROFILE_BADGES_I18N_PREFIX.Label, PROFILE_BADGES_I18N_PREFIX.Description, UUID.fromString("6ab5bb70-3baf-4794-8389-848f75ba13a2")) {
            alternativeLegacyLabels.apply {
                add("insígnias")
            }

            executor = ProfileBadgesExecutor()
        }

        subcommand(PROFILE_SHOP_RANK_I18N_PREFIX.Label, PROFILE_SHOP_RANK_I18N_PREFIX.Description, UUID.fromString("e8ab00e5-ad4c-42b2-b72d-921d5674bb0e")) {
            executor = ShopRankExecutor(loritta)
        }
    }

    inner class ProfileViewExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", PROFILE_VIEW_I18N_PREFIX.Options.User.Text)
            val profileDesign = optionalString("profile_design", PROFILE_VIEW_I18N_PREFIX.Options.ProfileDesign.Text) {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    // Get which profile designs the user owns (self, or the mentioned user)
                    val matchedUserId = try {
                        // Because this is an autocomplete function, we cannot use "asUser" because that fails with "Could not resolve User from option type USER" because
                        // the user is only resolved after sending the command
                        //
                        // To work around this, we parse it as a string and convert to a long, because the option is just the user's ID (without the user object)
                        it.event.getOption("user")?.asString?.toLongOrNull()
                    } catch (e: IllegalStateException) {
                        null
                    } ?: it.event.user.idLong

                    // Get bought profile designs
                    val boughtDesignsInternalNames = loritta.pudding.transaction {
                        ProfileDesignsPayments.select(ProfileDesignsPayments.profile).where {
                            ProfileDesignsPayments.userId eq matchedUserId
                        }.toSet().map { it[ProfileDesignsPayments.profile].value }
                    }.toMutableList()

                    // Add the default profile design ID
                    boughtDesignsInternalNames.add(0, ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)

                    val boughtDesignsToInternalNames = mutableMapOf<String, String>()

                    // We could filter on the database itself instead of filtering it here... but whatever, it doesn't really matter
                    for (boughtDesign in boughtDesignsInternalNames.filter { internalName ->
                        it.locale["profileDesigns.$internalName.title"].startsWith(focusedOptionValue)
                    }.take(DiscordResourceLimits.Command.Options.ChoicesCount)) {
                        boughtDesignsToInternalNames[it.locale["profileDesigns.$boughtDesign.title"]] = boughtDesign
                    }

                    return@autocomplete boughtDesignsToInternalNames
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userToBeViewed = args[options.user]?.user ?: context.user
            val profileDesignInternalName = args[options.profileDesign]

            if (AccountUtils.checkAndSendMessageIfUserIsBanned(loritta, context, userToBeViewed))
                return

            context.deferChannelMessage(false)

            val guild = context.guildOrNull

            val profileCreator = if (profileDesignInternalName == null) {
                // If null, get the user's active profile design
                val userProfile = loritta.getOrCreateLorittaProfile(userToBeViewed.id.toLong())
                val profileSettings = loritta.newSuspendedTransaction { userProfile.settings }

                loritta.profileDesignManager.designs.firstOrNull {
                    it.internalName == (profileSettings.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)
                } ?: loritta.profileDesignManager.defaultProfileDesign
            } else {
                // If not null, we need to validate if the user has the selected profile design!
                // Get bought profile designs
                val boughtDesignsInternalNames = loritta.pudding.transaction {
                    ProfileDesignsPayments.select(ProfileDesignsPayments.profile).where {
                        ProfileDesignsPayments.userId eq userToBeViewed.idLong
                    }.toSet().map { it[ProfileDesignsPayments.profile].value }
                }.toMutableList()

                // Add the default profile design ID
                boughtDesignsInternalNames.add(0, ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)

                // Bot owners (MrPowerGamerBR yay!!!) can use any profile design without having them in their inventory
                if (loritta.isOwner(userToBeViewed.idLong) || profileDesignInternalName in boughtDesignsInternalNames) {
                    loritta.profileDesignManager.designs.firstOrNull {
                        it.internalName == profileDesignInternalName
                    } ?: loritta.profileDesignManager.defaultProfileDesign
                } else {
                    // Fallback to the default
                    loritta.profileDesignManager.defaultProfileDesign
                }
            }

            val result = loritta.profileDesignManager.createProfile(
                loritta,
                context.i18nContext,
                context.locale,
                loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                loritta.profileDesignManager.transformUserToProfileUserInfoData(userToBeViewed),
                guild?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) },
                profileCreator
            )

            val message = createMessage(
                loritta,
                context,
                context.i18nContext,
                context.user,
                userToBeViewed,
                profileCreator,
                result
            )

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

            val userSettings = context.loritta.pudding.users.getOrCreateUserProfile(UserId(context.user.idLong))
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
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val page = (args[options.page]?.minus(1)) ?: 0

            context.reply(false) {
                createBadgeListMessage(context, page)()
            }
        }

        private suspend fun createBadgeListMessage(context: UnleashedContext, page: Long = 0): suspend InlineMessage<*>.() -> (Unit) {
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
                        GuildProfiles.select(GuildProfiles.guildId)
                            .where { GuildProfiles.userId eq context.user.id.toLong() and (GuildProfiles.isInGuild eq true) }
                            .map { it[GuildProfiles.guildId] }
                            .toSet()
                    }

                val badges = loritta.profileDesignManager.getUserBadges(
                    loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                    context.lorittaUser.profile,
                    mutualGuildsInAllClusters,
                    false
                )

                if (badges.isEmpty()) {
                    styled(
                        context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.YouDontHaveAnyBadges),
                        Emotes.LoriSob
                    )
                } else {
                    // Calculate pagination values
                    val badgesPerPage = 25
                    val totalPages = ceil(badges.size.toDouble() / badgesPerPage).toLong()
                    val safePageIndex = page.coerceIn(0, totalPages - 1)
                    val startIndex = (safePageIndex * badgesPerPage).toInt()
                    val endIndex = minOf(startIndex + badgesPerPage, badges.size)
                    val currentPageBadges = badges.subList(startIndex, endIndex)

                    // Display page information if there are multiple pages
                    if (totalPages > 1) {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(safePageIndex + 1)
                            ),
                            Emotes.LoriReading
                        )
                    }

                    embed {
                        title = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.YourBadges)
                        description = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgesDescription).joinToString("\n\n")
                        color = LorittaColors.LorittaAqua.rgb
                    }

                    actionRow(
                        loritta.interactivityManager.stringSelectMenuForUser(
                            context.user,
                            context.alwaysEphemeral,
                            {
                                placeholder = context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.ChooseABadge)

                                for (badge in currentPageBadges) {
                                    addOption(
                                        context.i18nContext.get(badge.title),
                                        badge.id.toString(),
                                        context.i18nContext.get(badge.description).shortenWithEllipsis(100),
                                        if (badge is Badge.LorittaBadge)
                                            loritta.emojiManager.get(badge.emoji).toJDA()
                                        else
                                            null
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

                    // Add pagination buttons if there are multiple pages
                    if (totalPages > 1) {
                        actionRow(
                            loritta.interactivityManager.buttonForUser(
                                context.user,
                                context.alwaysEphemeral,
                                ButtonStyle.PRIMARY,
                                "",
                                {
                                    loriEmoji = Emotes.ChevronLeft
                                    disabled = safePageIndex <= 0
                                }
                            ) {
                                it.deferEdit().editOriginal(
                                    MessageEdit {
                                        createBadgeListMessage(context, safePageIndex - 1)()
                                    }
                                ).setReplace(true).await()
                            },
                            loritta.interactivityManager.buttonForUser(
                                context.user,
                                context.alwaysEphemeral,
                                ButtonStyle.PRIMARY,
                                "",
                                {
                                    loriEmoji = Emotes.ChevronRight
                                    disabled = safePageIndex >= totalPages - 1
                                }
                            ) {
                                it.deferEdit().editOriginal(
                                    MessageEdit {
                                        createBadgeListMessage(context, safePageIndex + 1)()
                                    }
                                ).setReplace(true).await()
                            }
                        )
                    }

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
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
            val isBadgeVisible = loritta.transaction {
                HiddenUserBadges.selectAll()
                    .where {
                        HiddenUserBadges.userId eq context.user.idLong and (HiddenUserBadges.badgeId eq badge.id)
                    }
                    .count() == 0L
            }

            return {
                embed {
                    title = buildString {
                        if (badge is Badge.LorittaBadge) {
                            append(context.loritta.emojiManager.get(badge.emoji).toJDA().formatted)
                            append(" ")
                        }

                        append(context.i18nContext.get(badge.title))
                    }
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
                        context.alwaysEphemeral,
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
                        context.alwaysEphemeral,
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

                components += loritta.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    if (isBadgeVisible) {
                        context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.HideBadgeInProfile)
                    } else {
                        context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.ShowBadgeInProfile)
                    }
                ) { context ->
                    context.deferChannelMessage(true)

                    val hasBeenHidden = loritta.newSuspendedTransaction {
                        val hiddenEntry = HiddenUserBadges.selectAll()
                            .where {
                                HiddenUserBadges.userId eq context.user.idLong and (HiddenUserBadges.badgeId eq badge.id)
                            }
                            .limit(1)
                            .firstOrNull()

                        if (hiddenEntry != null) {
                            HiddenUserBadges.deleteWhere {
                                HiddenUserBadges.id eq hiddenEntry[HiddenUserBadges.id]
                            }
                            return@newSuspendedTransaction false
                        } else {
                            HiddenUserBadges.insert {
                                it[HiddenUserBadges.userId] = context.user.idLong
                                it[HiddenUserBadges.badgeId] = badge.id
                                it[HiddenUserBadges.hiddenAt] = Instant.now()
                            }
                            return@newSuspendedTransaction true
                        }
                    }

                    if (hasBeenHidden) {
                        context.reply(true) {
                            styled(
                                context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgeHasBeenHidden),
                            )
                        }
                    } else {
                        context.reply(true) {
                            styled(
                                context.i18nContext.get(PROFILE_BADGES_I18N_PREFIX.BadgeHasBeenUnhidden),
                            )
                        }
                    }
                }

                actionRow(components)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val pageNumber = args.getOrNull(0)?.toLongOrNull()

            return if (pageNumber != null) {
                mapOf(options.page to pageNumber)
            } else {
                LorittaLegacyMessageCommandExecutor.NO_ARGS
            }
        }
    }

    class ShopRankExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val page = (args[options.page]?.minus(1)) ?: 0

            context.reply(false) {
                createRankMessage(context, page)()
            }
        }

        private suspend fun createRankMessage(
            context: UnleashedContext,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val users = mutableListOf<RankingGenerator.UserRankInformation>()
            var totalUsers: Long? = null

            // We do raw SQL queries because implementing these with Exposed would a bit painful
            loritta.transaction {
                // Get the JDBC connection from the current transaction
                val jdbcConnection: Connection = (TransactionManager.current().connection as JdbcConnectionImpl).connection

                jdbcConnection.prepareStatement(
                    """
                        SELECT COUNT(*) FROM (
                                SELECT FROM (
                                SELECT "user" FROM backgroundpayments INNER JOIN backgrounds ON backgrounds.internal_name = backgroundpayments.background WHERE backgrounds.enabled = true AND available_to_buy_via_dreams = true
                                UNION ALL
                                SELECT "user" FROM profiledesignspayments INNER JOIN profiledesigns ON profiledesigns.internal_name = profiledesignspayments.profile WHERE profiledesigns.enabled = true AND available_to_buy_via_dreams = true
                            ) combined GROUP BY "user"
                        ) AS a;
                    """.trimIndent()
                ).use { statement ->
                    val resultSet: ResultSet = statement.executeQuery()

                    // Iterate over the ResultSet
                    while (resultSet.next()) {
                        // Access columns by index or name
                        val count = resultSet.getLong("count")

                        totalUsers = count
                    }
                }

                // Use the connection to create a statement and execute your raw SQL query
                jdbcConnection.prepareStatement(
                    """
                    SELECT
                      background_subquery."user",
                      COALESCE(background_total, 0) AS background_total, 
                      COALESCE(design_total, 0) AS design_total,
                      COALESCE(background_total, 0) + COALESCE(design_total, 0) AS total_items,
                      GREATEST(max_bought_at_background, max_bought_at_profile) AS bought_at
                    FROM (
                      SELECT 
                        "user",
                        COUNT(*) AS background_total,
                        MAX(bought_at) AS max_bought_at_background
                      FROM backgroundpayments
                      INNER JOIN backgrounds ON backgrounds.internal_name = backgroundpayments.background WHERE backgrounds.enabled = true AND available_to_buy_via_dreams = true
                      GROUP BY "user"
                    ) background_subquery
                    FULL OUTER JOIN (
                      SELECT 
                        "user", 
                        COUNT(*) AS design_total,
                        MAX(bought_at) AS max_bought_at_profile
                      FROM profiledesignspayments
                      INNER JOIN profiledesigns ON profiledesigns.internal_name = profiledesignspayments.profile WHERE profiledesigns.enabled = true AND available_to_buy_via_dreams = true
                      GROUP BY "user"
                    ) design_subquery
                      ON background_subquery.user = design_subquery.user
                    ORDER BY total_items DESC, bought_at ASC LIMIT 5 OFFSET ?;
                """
                ).use { statement ->
                    statement.setLong(1, page * 5)

                    val resultSet: ResultSet = statement.executeQuery()

                    // Iterate over the ResultSet
                    while (resultSet.next()) {
                        // Access columns by index or name
                        val user = resultSet.getLong("user")
                        val totalItems = resultSet.getLong("total_items")
                        // val boughtAt = resultSet.getLong("bought_at")

                        users.add(
                            RankingGenerator.UserRankInformation(
                                user,
                                context.i18nContext.get(I18nKeysData.Commands.Command.Profileshoprank.BoughtThings(totalItems))
                            )
                        )
                    }
                }
            }

            // Calculates the max page
            val maxPage = ceil((totalUsers ?: 1) / 5.0)

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    context.i18nContext.get(I18nKeysData.Commands.Command.Profileshoprank.RankTitle),
                    null,
                    users
                ) {
                    null
                }
            ) {
                createRankMessage(context, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return mapOf()
        }
    }
}
