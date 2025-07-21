package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Section
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.modals.options.optionalModalString
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

sealed class MarriageConfigureScreen(val m: LorittaBot, val marriageId: Long) {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Marriage
        private val SETUP_I18N_PREFIX = I18N_PREFIX.Configure
    }

    abstract suspend fun render(context: UnleashedContext): InlineMessage<*>.() -> (Unit)

    fun createGiveawayButtonQuickEdit(
        context: UnleashedContext,
        label: String,
        style: TextInputStyle,
        isOptional: Boolean,
        value: String?,
        range: IntRange?,
        block: suspend (ModalContext, String) -> (Unit),
    ): Button {
        return m.interactivityManager.buttonForUser(
            context.user,
            context.alwaysEphemeral,
            ButtonStyle.PRIMARY,
            context.i18nContext.get(SETUP_I18N_PREFIX.Change),
        ) {
            val option = if (isOptional) {
                optionalModalString(
                    label,
                    style,
                    value = value,
                    range = range
                )
            } else {
                modalString(
                    label,
                    style,
                    value = value,
                    range = range
                )
            }

            it.sendModal(
                context.i18nContext.get(SETUP_I18N_PREFIX.ModalTitle),
                listOf(
                    ActionRow.of(
                        option.toJDA()
                    )
                )
            ) { it, args ->
                val value = args[option] ?: ""

                block.invoke(it, value)
            }
        }
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

    class General(m: LorittaBot, marriageId: Long) : MarriageConfigureScreen(m, marriageId) {
        override suspend fun render(context: UnleashedContext): InlineMessage<*>.() -> Unit {
            val (marriage, validBadges) = m.transaction {
                val marriage = UserMarriages.selectAll()
                    .where {
                        UserMarriages.id eq marriageId
                    }
                    .first()

                val participants = MarriageParticipants
                    .innerJoin(Profiles, { MarriageParticipants.user }, { Profiles.id })
                    .selectAll()
                    .where {
                        MarriageParticipants.marriage eq marriage[UserMarriages.id]
                    }
                    .toList()

                val validBadges = mutableSetOf<Badge.LorittaBadge>()

                for (participant in participants) {
                    val userId = participant[MarriageParticipants.user]
                    val user = m.lorittaShards.retrieveUserById(userId)!!
                    val profileUserInfoData = m.profileDesignManager.transformUserToProfileUserInfoData(user)
                    val profile = Profile.wrapRow(participant)

                    validBadges.addAll(
                        m.profileDesignManager.badges.filterIsInstance<Badge.LorittaBadge>().filter {
                            it.checkIfUserDeservesBadge(
                                profileUserInfoData,
                                profile,
                                setOf()
                            )
                        }
                    )
                }

                return@transaction Pair(marriage, validBadges)
            }

            val editGiveawayNameButton = createGiveawayButtonQuickEdit(
                context,
                context.i18nContext.get(SETUP_I18N_PREFIX.CoupleTitle.Title),
                TextInputStyle.SHORT,
                true,
                marriage[UserMarriages.coupleName],
                3..20
            ) { context, value ->
                val defer = context.deferEdit()

                m.transaction {
                    UserMarriages.update({ UserMarriages.id eq this@General.marriageId }) {
                        it[UserMarriages.coupleName] = value.ifBlank { null }
                    }
                }

                defer.editOriginal(render(context))
            }

            return {
                this.useComponentsV2 = true

                this.components += Container {
                    this.accentColor = LorittaColors.LorittaPink.rgb

                    +TextDisplay("### ${Emotes.MarriageRing} ${context.i18nContext.get(I18N_PREFIX.Configure.General)}")

                    +Section(
                        editGiveawayNameButton
                    ) {
                        +OptionExplanationCombo(
                            context.i18nContext.get(I18N_PREFIX.Configure.CoupleTitle.Title),
                            context.i18nContext.get(I18N_PREFIX.Configure.CoupleTitle.Description),
                            value = marriage[UserMarriages.coupleName] ?: "*${context.i18nContext.get(I18N_PREFIX.Configure.CoupleTitle.NotSet)}*"
                        )
                    }

                    +OptionExplanationCombo(
                        context.i18nContext.get(I18N_PREFIX.Configure.EquippedBadge.Title),
                        context.i18nContext.get(I18N_PREFIX.Configure.EquippedBadge.Description)
                    )

                    +ActionRow.of(
                        m.interactivityManager.stringSelectMenuForUser(
                            context.user,
                            context.alwaysEphemeral,
                            {
                                this.minValues = 0
                                this.maxValues = 1
                                val equippedBadge = marriage[UserMarriages.coupleBadge]

                                for (badge in validBadges) {
                                    this.addOption(
                                        context.i18nContext.get(badge.titlePlural ?: badge.title),
                                        badge.id.toString(),
                                        context.i18nContext.get(badge.description).shortenWithEllipsis(100),
                                        m.emojiManager.get(badge.emoji).toJDA()
                                    )
                                }

                                if (equippedBadge != null)
                                    this.setDefaultValues(equippedBadge.toString())
                            }
                        ) { context, values ->
                            val value = values.firstOrNull()

                            val defer = context.deferEdit()

                            m.transaction {
                                UserMarriages.update({ UserMarriages.id eq this@General.marriageId }) {
                                    it[UserMarriages.coupleBadge] = if (value != null) UUID.fromString(value) else null
                                }
                            }

                            defer.editOriginal(MessageEdit { apply(render(context)) }).await()
                        }
                    )
                }
            }
        }
    }
}