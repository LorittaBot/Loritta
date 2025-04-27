package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.selects.EntitySelectMenu
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GiveawayTemplates
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.ColorUtils
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayTemplate
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.serializable.GiveawayRoleExtraEntry
import net.perfectdreams.loritta.serializable.GiveawayRoles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.awt.Color
import java.time.Instant

sealed class GiveawayBuilderScreen(val m: LorittaBot) {
    companion object {
        // This name is wonky as hell lol
        const val MAX_EXTRA_ENTRIES_ENTRIES = 5
        const val MAX_GIVEAWAY_TEMPLATES = 5
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway
        private val SETUP_I18N_PREFIX = I18N_PREFIX.Setup
    }

    abstract suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> (Unit)

    fun createGiveawayButtonQuickEdit(
        context: UnleashedContext,
        builder: GiveawayBuilder,
        label: String,
        style: TextInputStyle,
        value: String?,
        block: suspend (GiveawayBuilder, ModalContext, String) -> (Unit),
    ): Button {
        return m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            ButtonStyle.PRIMARY,
            context.i18nContext.get(SETUP_I18N_PREFIX.Change),
        ) {
            val option = modalString(
                label,
                style,
                value = value
            )

            it.sendModal(
                context.i18nContext.get(SETUP_I18N_PREFIX.ModalTitle),
                listOf(
                    ActionRow.of(
                        option.toJDA()
                    )
                )
            ) { it, args ->
                val value = args[option]

                block.invoke(builder, it, value)
            }
        }
    }

    fun createGiveawayToggleButton(
        context: UnleashedContext,
        builder: GiveawayBuilder,
        isEnabled: Boolean,
        block: suspend (GiveawayBuilder, ComponentContext) -> Unit
    ): Button {
        return m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            if (isEnabled)
                ButtonStyle.PRIMARY
            else
                ButtonStyle.SECONDARY,
            if (isEnabled)
                context.i18nContext.get(SETUP_I18N_PREFIX.Enabled)
            else
                context.i18nContext.get(SETUP_I18N_PREFIX.Disabled),
        ) {
            block.invoke(builder, it)
        }
    }

    fun createGiveawayBuilderButtons(context: UnleashedContext, builder: GiveawayBuilder): GiveawayBuilderButtons {
        /**
         * Makes a button be interactive if [disabledCheck] block result is false
         */
        fun makeInteractiveOrMakeDisabled(button: Button, disabledCheck: () -> (Boolean), block: suspend (ComponentContext) -> (Unit)): Button {
            return if (disabledCheck.invoke()) {
                button.asDisabled()
            } else {
                m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    button,
                    block
                )
            }
        }

        val configureAppearanceButton = makeInteractiveOrMakeDisabled(
            UnleashedButton.of(
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.Appearance),
                Emotes.Art
            ),
            { this is Appearance }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(Appearance(m).render(context, builder)) }).await()
        }

        val configureGeneralButton = makeInteractiveOrMakeDisabled(
            UnleashedButton.of(
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.General),
                Emotes.LoriCoffee
            ),
            { this is General }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(General(m).render(context, builder)) }).await()
        }

        val configureRolesButton = makeInteractiveOrMakeDisabled(
            UnleashedButton.of(
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.AllowedAndDeniedRoles),
                m.emojiManager.get(LorittaEmojis.Role)
            ),
            { this is Roles }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(Roles(m).render(context, builder)) }).await()
        }

        val configureExtraEntriesButton = makeInteractiveOrMakeDisabled(
            UnleashedButton.of(
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntries),
                Emotes.LoriRich
            ),
            { this is ExtraEntries }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(ExtraEntries(m).render(context, builder)) }).await()
        }

        val configureMiscellaneousButton = makeInteractiveOrMakeDisabled(
            UnleashedButton.of(
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.Miscellaneous),
                Emotes.LoriDerp
            ),
            { this is Miscellaneous }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(Miscellaneous(m).render(context, builder)) }).await()
        }

        val templatesButton = m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            ButtonStyle.SECONDARY,
            context.i18nContext.get(SETUP_I18N_PREFIX.Templates),
            {
                loriEmoji = Emotes.Sparkles
            }
        ) {
            it.deferEdit().editOriginal(MessageEdit { apply(Templates(m).render(context, builder)) }).await()
        }

        val startGiveawayButton = m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            ButtonStyle.SUCCESS,
            context.i18nContext.get(SETUP_I18N_PREFIX.StartGiveaway),
            {
                loriEmoji = Emotes.Tada
            }
        ) { context ->
            context.deferChannelMessage(true)

            // Validate if everything is OK
            val channel = builder.channel
            if (channel == null) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(SETUP_I18N_PREFIX.YouNeedToSetupAChannelBeforeStarting),
                        Emotes.Error
                    )
                }
                return@buttonForUser
            }

            if (!validateGiveawayChannel(context, channel))
                return@buttonForUser

            val roleIds = builder.roleIds ?: emptyList()
            for (roleId in roleIds) {
                val role = context.guild.getRoleById(roleId)
                if (role != null)
                    if (!validateGiveawayRole(context, role))
                        return@buttonForUser
            }

            val epoch = TimeUtils.convertToMillisRelativeToNow(builder.duration)

            val giveaway = m.giveawayManager.spawnGiveaway(
                context.locale,
                context.i18nContext,
                channel,
                builder.name,
                builder.description,
                builder.imageUrl,
                builder.thumbnailUrl,
                builder.color,
                builder.reaction,
                epoch,
                builder.numberOfWinners,
                null,
                builder.roleIds?.ifEmpty { null }?.map { it.toString() }, // This is nasty
                builder.allowedRolesIds?.ifEmpty { null }?.let {
                    GiveawayRoles(
                        it,
                        builder.allowedRolesIsAndCondition
                    )
                },
                builder.deniedRolesIds?.ifEmpty { null }?.let {
                    GiveawayRoles(
                        it,
                        builder.deniedRolesIsAndCondition
                    )
                },
                builder.needsToGetDailyBeforeParticipating,
                null,
                null,
                null,
                null,
                builder.extraEntries.map {
                    GiveawayRoleExtraEntry(
                        it.roleId,
                        it.weight
                    )
                }
            )

            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayCreated("https://discord.com/channels/${giveaway.guildId}/${giveaway.textChannelId}/${giveaway.messageId}")),
                    Emotes.LoriHappyJumping
                )

                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayCreatedCreateTemplateIfYouWantToSaveItLater),
                    Emotes.LoriCoffee
                )
            }
        }

        return GiveawayBuilderButtons(
            configureAppearanceButton,
            configureGeneralButton,
            configureRolesButton,
            configureExtraEntriesButton,
            configureMiscellaneousButton,
            templatesButton,
            startGiveawayButton,
        )
    }

    fun InlineMessage<*>.appendDefaultButtons(context: UnleashedContext, builder: GiveawayBuilder) {
        val buildGiveawayButtons = createGiveawayBuilderButtons(context, builder)

        this.components += row(
            buildGiveawayButtons.configureAppearanceButton,
            buildGiveawayButtons.configureAttributesButton,
            buildGiveawayButtons.configureRolesButton,
            buildGiveawayButtons.configureExtraEntriesButton,
            buildGiveawayButtons.configureMiscellaneousButton
        )

        this.components += row(
            buildGiveawayButtons.templatesButton
        )

        this.components += row(
            buildGiveawayButtons.startGiveawayButton
        )
    }

    fun OptionExplanationCombo(title: String, description: String? = null, value: String? = null): TextDisplay {
        return TextDisplay(
            buildString {
                appendLine("**$title**")
                if (description != null) {
                    appendLine("-# $description")
                }
                if (value != null) {
                    appendLine(value)
                }
            }
        )
    }

    suspend fun validateGiveawayChannel(context: UnleashedContext, channel: GuildMessageChannel): Boolean {
        val lorittaAsMember = context.guild.selfMember

        if (!channel.canTalk()) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.LorittaCantSpeakOnChannel(channel.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }

        if (!channel.canTalk(context.member)) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.UserCantSpeakOnChannel(channel.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }

        if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.LorittaDoesNotHavePermissionToSendEmbeds(channel.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }

        if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.LorittaDoesNotHavePermissionToViewChannelHistory(channel.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }

        return true
    }

    suspend fun validateGiveawayRole(context: UnleashedContext, role: Role): Boolean {
        if (!context.guild.selfMember.canInteract(role) || role.isManaged) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.LorittaCantInteractWithRole(role.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }

        if (!context.member.hasPermission(Permission.MANAGE_ROLES) || !context.member.canInteract(role)) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(SETUP_I18N_PREFIX.UserCantInteractWithRole(role.asMention)),
                    Constants.ERROR
                )
            }
            return false
        }
        return true
    }

    class Appearance(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val editGiveawayNameButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayName),
                TextInputStyle.SHORT,
                builder.name
            ) { builder, context, value ->
                builder.name = value

                context.deferEdit().editOriginal(render(context, builder))
            }

            val editGiveawayDescriptionButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayDescription),
                TextInputStyle.PARAGRAPH,
                builder.description
            ) { builder, context, value ->
                builder.description = value

                context.deferEdit().editOriginal(render(context, builder))
            }

            val editGiveawayImageButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayEmbedImage),
                TextInputStyle.SHORT,
                builder.imageUrl
            ) { builder, context, value ->
                builder.imageUrl = value.ifBlank { null }

                context.deferEdit().editOriginal(render(context, builder))
            }

            val editGiveawayThumbnailButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayThumbnailImage),
                TextInputStyle.SHORT,
                builder.thumbnailUrl
            ) { builder, context, value ->
                builder.thumbnailUrl = value.ifBlank { null }

                context.deferEdit().editOriginal(render(context, builder))
            }

            val editGiveawayColorButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayEmbedColor),
                TextInputStyle.SHORT,
                String.format("#%02x%02x%02x", builder.color.red, builder.color.green, builder.color.blue)
            ) { builder, context, value ->
                builder.color = value.let { ColorUtils.getColorFromString(it) } ?: LorittaColors.LorittaAqua.toJavaColor()

                context.deferEdit().editOriginal(render(context, builder))
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay("### ${Emotes.Art} ${context.i18nContext.get(SETUP_I18N_PREFIX.Appearance)}")

                    +Section(
                        editGiveawayNameButton
                    ) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayName),
                            value = builder.name
                        )
                    }

                    +Section(editGiveawayDescriptionButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayDescription),
                            value = builder.description
                        )
                    }

                    val imageUrl = builder.imageUrl
                    val thumbnailUrl = builder.thumbnailUrl

                    +Section(editGiveawayImageButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmbedImage),
                            value = if (imageUrl != null) {
                                imageUrl
                            } else {
                                "*${context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmbedImageNoImage)}*"
                            }
                        )
                    }

                    if (imageUrl != null) {
                        +MediaGallery {
                            +this.item(imageUrl)
                        }
                    }

                    +Section(editGiveawayThumbnailButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayThumbnailImage),
                            value = if (thumbnailUrl != null) {
                                thumbnailUrl
                            } else {
                                "*${context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmbedThumbnailNoThumbnail)}*"
                            }
                        )
                    }

                    if (thumbnailUrl != null) {
                        +MediaGallery {
                            +this.item(thumbnailUrl)
                        }
                    }

                    +Section(editGiveawayColorButton) {
                        +TextDisplay(
                            "**${context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmbedColor)}**\n${
                                String.format(
                                    "#%02x%02x%02x",
                                    builder.color.red,
                                    builder.color.green,
                                    builder.color.blue
                                )
                            }"
                        )
                    }
                }

                appendDefaultButtons(context, builder)
            }
        }
    }

    class General(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val editGiveawayReactionButton = m.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.Change)
            ) { context ->
                val originalHook = context.deferEdit()

                // This works because we have already ack'd the context
                val reply = context.reply(false) {
                    styled(
                        context.i18nContext.get(SETUP_I18N_PREFIX.ReactWithTheReactionYouWantOnThisMessage),
                        Emotes.LoriLurk
                    )

                    styled(
                        context.i18nContext.get(SETUP_I18N_PREFIX.ReminderThatTheEmojiShouldBeAccessible),
                        Emotes.LoriCoffee
                    )
                } as InteractionMessage.FollowUpInteractionMessage

                // We do this the old-fashioned way
                reply.message.onReactionAddByAuthor(context) {
                    builder.reaction = it.reaction.emoji.formatted

                    reply.message.delete().queue()

                    originalHook.editOriginal(MessageEdit { apply(render(context, builder)) }).await()
                }
            }

            val editGiveawayDurationButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayDuration.Title),
                TextInputStyle.SHORT,
                builder.duration
            ) { builder, context, value ->
                builder.duration = value

                context.deferEdit().editOriginal(render(context, builder))
            }

            val editGiveawayQuantityOfWinnersButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayWinners.Title),
                TextInputStyle.SHORT,
                builder.numberOfWinners.toString()
            ) { builder, context, value ->
                val newValue = value.toIntOrNull()
                if (newValue == null) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouNeedToUseAInteger),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                if (0 >= newValue) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCannotUseNegativeNumberOfWinners),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                if (newValue > 100) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCannotUseNumberOfWinnersTooLarge),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                builder.numberOfWinners = newValue

                context.deferEdit().editOriginal(render(context, builder))
            }

            val rolesToBeGivenToTheWinners = m.interactivityManager.entitySelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    this.setEntityTypes(EntitySelectMenu.SelectTarget.ROLE)
                    this.setRequiredRange(0, 25)
                    val roleIds = builder.roleIds
                    if (roleIds != null) {
                        val knownRoles = roleIds.mapNotNull { context.guild.getRoleById(it) }
                        this.setDefaultValues(knownRoles.map { EntitySelectMenu.DefaultValue.from(it) })
                    }
                }
            ) { context, values ->
                val roles = values.filterIsInstance<Role>()
                for (role in roles) {
                    if (!validateGiveawayRole(context, role))
                        return@entitySelectMenuForUser
                }

                builder.roleIds = roles.map { it.idLong }

                context.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            val channelSelectMenu = m.interactivityManager.entitySelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    this.setChannelTypes(
                        ChannelType.TEXT,
                        ChannelType.NEWS
                    )

                    val channel = builder.channel
                    if (channel != null)
                        this.setDefaultValues(EntitySelectMenu.DefaultValue.from(channel))
                }
            ) { context, values ->
                val channel = values.first() as GuildMessageChannel

                if (!validateGiveawayChannel(context, channel))
                    return@entitySelectMenuForUser

                builder.channel = channel

                context.deferEdit().editOriginal(MessageEdit { render(context, builder) })
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay("### ${Emotes.LoriCoffee} ${context.i18nContext.get(SETUP_I18N_PREFIX.General)}")

                    +Section(editGiveawayQuantityOfWinnersButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayWinners.Title),
                            value = builder.numberOfWinners.toString()
                        )
                    }

                    +Section(editGiveawayDurationButton) {
                        val epoch = TimeUtils.convertToLocalDateTimeRelativeToNow(builder.duration)

                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayDuration.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayDuration.Description),
                            value = "${builder.duration} (${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(epoch.toInstant())})"
                        )
                    }

                    +Section(editGiveawayReactionButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmoji.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayEmoji.Description),
                            builder.reaction
                        )
                    }

                    +OptionExplanationCombo(
                        context.i18nContext.get(SETUP_I18N_PREFIX.RolesThatTheWinnersWillWin.Title),
                        context.i18nContext.get(SETUP_I18N_PREFIX.RolesThatTheWinnersWillWin.Description)
                    )

                    +row(
                        rolesToBeGivenToTheWinners
                    )

                    +OptionExplanationCombo(
                        context.i18nContext.get(SETUP_I18N_PREFIX.GiveawayChannel.Title)
                    )
                    +row(
                        channelSelectMenu
                    )
                }

                appendDefaultButtons(context, builder)
            }
        }
    }

    class Roles(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val channel1SelectMenu = m.interactivityManager.entitySelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    this.setEntityTypes(EntitySelectMenu.SelectTarget.ROLE)
                    this.setRequiredRange(0, 25)

                    val roleIds = builder.allowedRolesIds
                    if (roleIds != null) {
                        val knownRoles = roleIds.mapNotNull { context.guild.getRoleById(it) }
                        this.setDefaultValues(knownRoles.map { EntitySelectMenu.DefaultValue.from(it) })
                    }
                }
            ) { it, values ->
                builder.allowedRolesIds = values.map { it.idLong }

                it.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            val channel2SelectMenu = m.interactivityManager.entitySelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    this.setEntityTypes(EntitySelectMenu.SelectTarget.ROLE)
                    this.setRequiredRange(0, 25)

                    val roleIds = builder.deniedRolesIds
                    if (roleIds != null) {
                        val knownRoles = roleIds.mapNotNull { context.guild.getRoleById(it) }
                        this.setDefaultValues(knownRoles.map { EntitySelectMenu.DefaultValue.from(it) })
                    }
                }
            ) { it, values ->
                builder.deniedRolesIds = values.map { it.idLong }

                it.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            val toggleAllowedRolesIsAndCondition = createGiveawayToggleButton(
                context,
                builder,
                builder.allowedRolesIsAndCondition
            ) { builder, context ->
                builder.allowedRolesIsAndCondition = !builder.allowedRolesIsAndCondition

                context.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            val toggleDeniedRolesIsAndCondition = createGiveawayToggleButton(
                context,
                builder,
                builder.deniedRolesIsAndCondition
            ) { builder, context ->
                builder.deniedRolesIsAndCondition = !builder.deniedRolesIsAndCondition

                context.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay("### ${m.emojiManager.get(LorittaEmojis.Role)} ${context.i18nContext.get(SETUP_I18N_PREFIX.AllowedAndDeniedRoles)}")

                    +OptionExplanationCombo(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedRoles.Title))
                    +row(
                        channel1SelectMenu
                    )

                    +Section(toggleAllowedRolesIsAndCondition) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.AllowedRolesIsAndCondition.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.AllowedRolesIsAndCondition.Description)
                        )
                    }

                    +OptionExplanationCombo(context.i18nContext.get(SETUP_I18N_PREFIX.DeniedRoles.Title))
                    +row(
                        channel2SelectMenu
                    )

                    +Section(toggleDeniedRolesIsAndCondition) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.DeniedRolesIsAndCondition.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.DeniedRolesIsAndCondition.Description)
                        )
                    }

                    +Separator(true, Separator.Spacing.SMALL)

                    +TextDisplay(
                        buildString {
                            val allowedRolesIds = builder.allowedRolesIds?.ifEmpty { null }
                            val deniedRolesIds = builder.deniedRolesIds?.ifEmpty { null }

                            if (allowedRolesIds == null && deniedRolesIds == null) {
                                appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedDeniedRolesPreview.EveryoneCanParticipate))
                            } else {
                                if (allowedRolesIds != null) {
                                    val roleList = buildString {
                                        var isFirst = true
                                        for (roleId in allowedRolesIds) {
                                            if (!isFirst)
                                                append(", ")
                                            append("<@&${roleId}>")
                                            isFirst = false
                                        }
                                    }

                                    if (builder.allowedRolesIsAndCondition) {
                                        appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedDeniedRolesPreview.NeedsToHaveAllOfTheseRolesToParticipate(roleList)))
                                    } else {
                                        appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedDeniedRolesPreview.NeedsToHaveOneOfTheseRolesToParticipate(roleList)))
                                    }
                                }

                                if (deniedRolesIds != null) {
                                    val roleList = buildString {
                                        var isFirst = true
                                        for (roleId in deniedRolesIds) {
                                            if (!isFirst)
                                                append(", ")
                                            append("<@&${roleId}>")
                                            isFirst = false
                                        }
                                    }

                                    if (builder.deniedRolesIsAndCondition) {
                                        appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedDeniedRolesPreview.CannotHaveAllOfTheseRolesToParticipate(roleList)))
                                    } else {
                                        appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.AllowedDeniedRolesPreview.CannotHaveOneOfTheseRolesToParticipate(roleList)))
                                    }
                                }
                            }
                        }
                    )
                }

                appendDefaultButtons(context, builder)
            }
        }
    }

    class ExtraEntries(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val addNewExtraEntryUnleashedButton = UnleashedButton.of(
                ButtonStyle.PRIMARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.AddExtraEntry)
            )

            val addNewExtraEntry = if (builder.extraEntries.size >= MAX_EXTRA_ENTRIES_ENTRIES) {
                addNewExtraEntryUnleashedButton.asDisabled()
            } else {
                m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    addNewExtraEntryUnleashedButton
                ) {
                    builder.extraEntryBuilder.roleId = null
                    builder.extraEntryBuilder.weight = 2

                    it.deferEdit().editOriginal(MessageEdit { apply(AddNewExtraEntry(m).render(context, builder)) }).await()
                }
            }

            val sections = mutableListOf<Section>()
            for (extraEntry in builder.extraEntries) {
                sections.add(
                    Section(
                        m.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.DANGER,
                            context.i18nContext.get(SETUP_I18N_PREFIX.Remove)
                        ) {
                            builder.extraEntries.remove(extraEntry)

                            it.deferEdit().editOriginal(MessageEdit { apply(ExtraEntries(m).render(context, builder)) }).await()
                        }
                    ) {
                        +TextDisplay(
                            buildString {
                                appendLine("**${context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntriesEntry.Title("<@&${extraEntry.roleId}>", extraEntry.roleId.toString()))}**")
                                appendLine(context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntriesEntry.ExtraEntries(extraEntry.weight)))
                            }
                        )
                    }
                )
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay(
                        buildString {
                            appendLine("### ${Emotes.LoriRich} ${context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntries)}")
                            var isFirst = true
                            for (line in context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntriesIntro)) {
                                if (!isFirst)
                                    appendLine()
                                appendLine(line)
                                isFirst = false
                            }
                        }
                    )

                    if (sections.isEmpty()) {
                        +TextDisplay("*${context.i18nContext.get(I18nKeysData.Commands.Command.Transactions.NoTransactionsFunnyMessages).random()}*")
                    } else {
                        for (section in sections) {
                            +section
                        }
                    }

                    +Separator(true, Separator.Spacing.SMALL)

                    +row(
                        addNewExtraEntry
                    )
                }

                appendDefaultButtons(context, builder)
            }
        }
    }

    class AddNewExtraEntry(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val addNewExtraEntry = m.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.Add)
            ) { context ->
                if (builder.extraEntries.size >= MAX_EXTRA_ENTRIES_ENTRIES) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouAlreadyHaveTooManyExtraEntries),
                            Emotes.Error
                        )
                    }
                    return@buttonForUser
                }

                val rId = builder.extraEntryBuilder.roleId
                if (rId == null) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCanOnlyAddExtraEntryAfterSelectingARole),
                            Emotes.Error
                        )
                    }
                    return@buttonForUser
                }

                if (builder.extraEntries.any { it.roleId == rId }) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.TryingToAddExtraEntryThatAlreadyHasARole),
                            Emotes.Error
                        )
                    }
                    return@buttonForUser
                }

                builder.extraEntries.add(
                    ExtraEntry(
                        rId,
                        builder.extraEntryBuilder.weight,
                    )
                )

                context.deferEdit().editOriginal(MessageEdit { apply(ExtraEntries(m).render(context, builder)) }).await()
            }

            val editWeightButton = createGiveawayButtonQuickEdit(
                context,
                builder,
                context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntryQuantity.Title),
                TextInputStyle.SHORT,
                builder.extraEntryBuilder.weight.toString()
            ) { builder, context, value ->
                val newWeight = value.toIntOrNull()

                if (newWeight == null) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouNeedToUseAInteger),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                if (newWeight == 1) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCannotUseOneAsWeight),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                if (0 >= newWeight) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCannotUseNegativeWeight),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                if (newWeight > 100_000) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SETUP_I18N_PREFIX.YouCannotUseWeightTooLarge),
                            Emotes.Error
                        )
                    }
                    return@createGiveawayButtonQuickEdit
                }

                builder.extraEntryBuilder.weight = value.toInt()

                context.deferEdit().editOriginal(render(context, builder))
            }

            val extraEntryRoleSelector = m.interactivityManager.entitySelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    this.setEntityTypes(EntitySelectMenu.SelectTarget.ROLE)
                }
            ) { it, values ->
                builder.extraEntryBuilder.roleId = values.first().idLong

                it.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            val goBack = m.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.SECONDARY,
                context.i18nContext.get(SETUP_I18N_PREFIX.GoBack)
            ) {
                it.deferEdit().editOriginal(MessageEdit { apply(ExtraEntries(m).render(context, builder)) }).await()
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb
                    +TextDisplay("### ${Emotes.LoriRich} ${context.i18nContext.get(SETUP_I18N_PREFIX.AddExtraEntry)}")

                    +OptionExplanationCombo(
                        context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntryRole.Title),
                        context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntryRole.Description),
                    )

                    +row(
                        extraEntryRoleSelector
                    )

                    +Section(editWeightButton) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntryQuantity.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.ExtraEntryQuantity.Description(builder.extraEntryBuilder.weight)),
                        )
                    }

                    +row(
                        addNewExtraEntry
                    )
                }

                this.components += row(
                    goBack
                )
            }
        }
    }

    class Miscellaneous(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val toggleNeedsToGetDailyBeforeParticipating = createGiveawayToggleButton(
                context,
                builder,
                builder.needsToGetDailyBeforeParticipating
            ) { builder, context ->
                builder.needsToGetDailyBeforeParticipating = !builder.needsToGetDailyBeforeParticipating

                context.deferEdit().editOriginal(MessageEdit { apply(render(context, builder)) }).await()
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb
                    +TextDisplay("### ${Emotes.LoriDerp} ${context.i18nContext.get(SETUP_I18N_PREFIX.Miscellaneous)}")

                    +Section(toggleNeedsToGetDailyBeforeParticipating) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(SETUP_I18N_PREFIX.NeedsToGetDailyBeforeParticipating.Title),
                            context.i18nContext.get(SETUP_I18N_PREFIX.NeedsToGetDailyBeforeParticipating.Description),
                        )
                    }
                }

                appendDefaultButtons(context, builder)
            }
        }
    }

    class Templates(m: LorittaBot) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val templates = m.transaction {
                GiveawayTemplates.selectAll()
                    .toList()
                    .map {
                        GiveawayTemplateInformation(
                            it[GiveawayTemplates.id].value,
                            it[GiveawayTemplates.name],
                            it[GiveawayTemplates.createdBy],
                            Json.decodeFromString(it[GiveawayTemplates.template]),
                        )
                    }
            }

            val saveGiveawayAsTemplate = if (templates.size == MAX_GIVEAWAY_TEMPLATES) {
                Button.of(
                    ButtonStyle.PRIMARY,
                    "disabled__",
                    context.i18nContext.get(SETUP_I18N_PREFIX.Template.SaveConfigurationAsTemplate)
                ).asDisabled()
            } else {
                m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(SETUP_I18N_PREFIX.Template.SaveConfigurationAsTemplate)
                ) { context ->
                    val option = modalString(
                        context.i18nContext.get(SETUP_I18N_PREFIX.Template.TemplateName),
                        TextInputStyle.SHORT,
                        value = builder.name,
                        range = 3..50
                    )

                    context.sendModal(
                        context.i18nContext.get(SETUP_I18N_PREFIX.ModalTitle),
                        listOf(ActionRow.of(option.toJDA()))
                    ) { context, args ->
                        val name = args[option]
                        val hook = context.deferEdit()

                        m.transaction {
                            val existingTemplates = GiveawayTemplates.selectAll()
                                .where {
                                    GiveawayTemplates.guildId eq context.guildId!!
                                }
                                .count()

                            if (existingTemplates >= MAX_GIVEAWAY_TEMPLATES.toLong()) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(SETUP_I18N_PREFIX.Template.YouAlreadyHaveTooManyTemplates),
                                        Constants.ERROR
                                    )
                                }
                                return@transaction
                            }

                            GiveawayTemplates.insert {
                                it[GiveawayTemplates.guildId] = context.guildId!!
                                it[GiveawayTemplates.createdBy] = context.user.idLong
                                it[GiveawayTemplates.createdAt] = Instant.now()
                                it[GiveawayTemplates.name] = name
                                it[GiveawayTemplates.template] = Json.encodeToString(
                                    GiveawayTemplate(
                                        builder.name,
                                        builder.description,
                                        builder.imageUrl,
                                        builder.thumbnailUrl,
                                        builder.color.rgb,
                                        builder.duration,
                                        builder.reaction,
                                        builder.channel?.idLong,
                                        builder.numberOfWinners,
                                        builder.roleIds ?: listOf(),
                                        builder.allowedRolesIds ?: listOf(),
                                        builder.allowedRolesIsAndCondition,
                                        builder.deniedRolesIds ?: listOf(),
                                        builder.deniedRolesIsAndCondition,
                                        builder.needsToGetDailyBeforeParticipating,
                                        builder.extraEntries.map {
                                            GiveawayTemplate.GiveawayRoleExtraEntry(
                                                it.roleId,
                                                it.weight
                                            )
                                        }
                                    )
                                )
                            }
                        }

                        hook.editOriginal(MessageEdit { apply(Templates(m).render(context, builder)) })
                    }
                }
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay("### ${Emotes.Sparkles} ${context.i18nContext.get(SETUP_I18N_PREFIX.Template.CreatedTemplates)}")

                    for (template in templates) {
                        +Section(
                            m.interactivityManager.buttonForUser(
                                context.user,
                                context.alwaysEphemeral,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(SETUP_I18N_PREFIX.Template.View)
                            ) {
                                it.deferEdit().editOriginal(MessageEdit { apply(ViewGiveawayTemplate(m, template).render(context, builder)) }).await()
                            }
                        ) {
                            +TextDisplay(
                                buildString {
                                    appendLine("**${template.name}**")
                                }
                            )
                        }
                    }

                    +Separator(true, Separator.Spacing.SMALL)

                    +row(
                        saveGiveawayAsTemplate
                    )
                }

                val goBack = m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(SETUP_I18N_PREFIX.GoBack)
                ) {
                    it.deferEdit().editOriginal(MessageEdit { apply(Appearance(m).render(context, builder)) }).await()
                }

                this.components += row(
                    goBack
                )
            }
        }
    }

    class ViewGiveawayTemplate(m: LorittaBot, val giveawayInformation: GiveawayTemplateInformation) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            val userInfo = m.lorittaShards.retrieveUserInfoById(giveawayInformation.createdById)

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay("### ${Emotes.Sparkles} Template ${giveawayInformation.name}")

                    +Section(
                        Thumbnail(userInfo?.effectiveAvatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png")
                    ) {
                        + TextDisplay(context.i18nContext.get(SETUP_I18N_PREFIX.Template.TemplateCreatedBy("<@${giveawayInformation.createdById}>", giveawayInformation.createdById.toString())))
                    }

                    +row(
                        m.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.DANGER,
                            context.i18nContext.get(SETUP_I18N_PREFIX.Template.Delete)
                        ) {
                            it.deferEdit().editOriginal(MessageEdit { apply(DeleteGiveawayTemplatePrompt(m, giveawayInformation).render(context, builder)) }).await()
                        },
                        m.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(SETUP_I18N_PREFIX.Template.Usar)
                        ) {
                            val hook = it.deferEdit()

                            // Update the last used at time
                            m.transaction {
                                GiveawayTemplates.update({
                                    GiveawayTemplates.id eq giveawayInformation.id
                                }) {
                                    it[GiveawayTemplates.lastUsedAt] = Instant.now()
                                }
                            }

                            builder.name = giveawayInformation.template.name
                            builder.description = giveawayInformation.template.description
                            builder.imageUrl = giveawayInformation.template.imageUrl
                            builder.thumbnailUrl = giveawayInformation.template.thumbnailUrl
                            builder.color = Color(giveawayInformation.template.color)
                            builder.duration = giveawayInformation.template.duration
                            builder.reaction = giveawayInformation.template.reaction
                            builder.channel = giveawayInformation.template.channelId?.let { channelId ->
                                context.guild.getGuildMessageChannelById(channelId)
                            }
                            builder.numberOfWinners = giveawayInformation.template.numberOfWinners
                            builder.roleIds = giveawayInformation.template.roleIds

                            builder.allowedRolesIds = giveawayInformation.template.allowedRoleIds
                            builder.allowedRolesIsAndCondition = giveawayInformation.template.allowedRolesIsAndCondition

                            builder.deniedRolesIds = giveawayInformation.template.deniedRoleIds
                            builder.deniedRolesIsAndCondition = giveawayInformation.template.deniedRolesIsAndCondition

                            builder.needsToGetDailyBeforeParticipating = giveawayInformation.template.needsToGetDailyBeforeParticipating

                            builder.extraEntries = giveawayInformation.template.extraEntries.map {
                                ExtraEntry(it.roleId, it.weight)
                            }.toMutableList()

                            hook.editOriginal(MessageEdit { apply(Appearance(m).render(context, builder)) }).await()
                        }
                    )
                }

                val goBack = m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(SETUP_I18N_PREFIX.GoBack)
                ) {
                    it.deferEdit().editOriginal(MessageEdit { apply(Templates(m).render(context, builder)) }).await()
                }

                this.components += row(
                    goBack
                )
            }
        }
    }

    class DeleteGiveawayTemplatePrompt(m: LorittaBot, val giveawayInformation: GiveawayTemplateInformation) : GiveawayBuilderScreen(m) {
        override suspend fun render(context: UnleashedContext, builder: GiveawayBuilder): InlineMessage<*>.() -> Unit {
            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = builder.color.rgb

                    +TextDisplay(context.i18nContext.get(SETUP_I18N_PREFIX.Template.AreYouSureYouWantToDeleteTheTemplate))

                    +row(
                        m.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.DANGER,
                            context.i18nContext.get(SETUP_I18N_PREFIX.Template.Delete)
                        ) {
                            val hook = it.deferEdit()

                            m.transaction {
                                GiveawayTemplates.deleteWhere {
                                    GiveawayTemplates.guildId eq context.guildId!! and (GiveawayTemplates.id eq giveawayInformation.id)
                                }
                            }

                            hook.editOriginal(MessageEdit { apply(Templates(m).render(context, builder)) }).await()
                        }
                    )
                }

                val goBack = m.interactivityManager.buttonForUser(
                    context.user,
                    context.alwaysEphemeral,
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(SETUP_I18N_PREFIX.GoBack)
                ) {
                    it.deferEdit().editOriginal(MessageEdit { apply(ViewGiveawayTemplate(m, giveawayInformation).render(context, builder)) }).await()
                }

                this.components += row(
                    goBack
                )
            }
        }
    }


    class GiveawayBuilder(
        var name: String,
        var description: String
    ) {
        var imageUrl: String? = null
        var thumbnailUrl: String? = null
        var color = LorittaColors.LorittaAqua.toJavaColor()
        var duration = "1 hora"
        var reaction = "\uD83C\uDF89"
        var channel: GuildMessageChannel? = null
        var numberOfWinners = 1
        var roleIds: List<Long>? = null

        var allowedRolesIds: List<Long>? = null
        var allowedRolesIsAndCondition = true

        var deniedRolesIds: List<Long>? = null
        var deniedRolesIsAndCondition = false

        var needsToGetDailyBeforeParticipating = false

        var extraEntries = mutableListOf<ExtraEntry>()

        var extraEntryBuilder = ExtraEntryBuilder()
    }

    class ExtraEntryBuilder {
        var roleId: Long? = null
        var weight: Int = 2
    }

    data class ExtraEntry(
        val roleId: Long,
        val weight: Int
    )

    data class GiveawayBuilderButtons(
        val configureAppearanceButton: Button,
        val configureAttributesButton: Button,
        val configureRolesButton: Button,
        val configureExtraEntriesButton: Button,
        val configureMiscellaneousButton: Button,
        val templatesButton: Button,
        val startGiveawayButton: Button,
    )

    data class GiveawayTemplateInformation(
        val id: Long,
        val name: String,
        val createdById: Long,
        val template: GiveawayTemplate
    )
}