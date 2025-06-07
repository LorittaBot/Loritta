package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageRoleplayActions
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.RoleplayAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayCommand.Companion.I18N_PREFIX
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.data.api.AnimeSource
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Color
import java.time.LocalDateTime
import java.time.ZonedDateTime

object RoleplayUtils {
    val HUG_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.HUG,
        RoleplayCommand.I18N_PREFIX.Hug.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Hug.ButtonLabel,
        RandomRoleplayPicturesClient::hug,
        I18nKeysData.Commands.Command.Roleplay.Hug::Response,
        Color(255, 141, 230),
        Emotes.Blush,
        true
    )

    val HEAD_PAT_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.HEAD_PAT,
        RoleplayCommand.I18N_PREFIX.Headpat.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Headpat.ButtonLabel,
        RandomRoleplayPicturesClient::headPat,
        I18nKeysData.Commands.Command.Roleplay.Headpat::Response,
        Color(156, 39, 176),
        Emotes.LoriPat,
        true
    )

    val HIGH_FIVE_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.HIGH_FIVE,
        RoleplayCommand.I18N_PREFIX.Highfive.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Highfive.ButtonLabel,
        RandomRoleplayPicturesClient::highFive,
        I18nKeysData.Commands.Command.Roleplay.Highfive::Response,
        Color(165, 255, 76),
        Emotes.LoriHi,
        false
    )

    val SLAP_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.SLAP,
        RoleplayCommand.I18N_PREFIX.Slap.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Slap.ButtonLabel,
        RandomRoleplayPicturesClient::slap,
        I18nKeysData.Commands.Command.Roleplay.Slap::Response,
        Color(244, 67, 54),
        Emotes.LoriPunch,
        false
    )

    val ATTACK_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.ATTACK,
        RoleplayCommand.I18N_PREFIX.Attack.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Attack.ButtonLabel,
        RandomRoleplayPicturesClient::attack,
        I18nKeysData.Commands.Command.Roleplay.Attack::Response,
        Color(244, 67, 54),
        Emotes.LoriRage,
        false
    )

    val DANCE_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.DANCE,
        RoleplayCommand.I18N_PREFIX.Dance.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Dance.ButtonLabel,
        RandomRoleplayPicturesClient::dance,
        I18nKeysData.Commands.Command.Roleplay.Dance::Response,
        Color(255, 152, 0),
        Emotes.Dancer,
        false
    )

    val KISS_ATTRIBUTES = RoleplayActionAttributes(
        RoleplayAction.KISS,
        RoleplayCommand.I18N_PREFIX.Kiss.Options.User.Text,
        RoleplayCommand.I18N_PREFIX.Kiss.ButtonLabel,
        RandomRoleplayPicturesClient::kiss,
        I18nKeysData.Commands.Command.Roleplay.Kiss::Response,
        Color(233, 30, 99),
        Emotes.LoriKiss,
        true
    )

    val RETRIBUTABLE_ACTIONS_BY_LORITTA_EASTER_EGG = listOf(
        HUG_ATTRIBUTES,
        HEAD_PAT_ATTRIBUTES,
        HIGH_FIVE_ATTRIBUTES,
        DANCE_ATTRIBUTES
    )

    val ALL_ATTRIBUTES = listOf(
        HUG_ATTRIBUTES,
        HEAD_PAT_ATTRIBUTES,
        HIGH_FIVE_ATTRIBUTES,
        SLAP_ATTRIBUTES,
        ATTACK_ATTRIBUTES,
        DANCE_ATTRIBUTES,
        KISS_ATTRIBUTES
    )

    suspend fun handleRoleplayMessage(
        loritta: LorittaBot,
        context: UnleashedContext,
        i18nContext: I18nContext,
        data: RetributeRoleplayData,
        client: RandomRoleplayPicturesClient,
        roleplayActionAttributes: RoleplayActionAttributes
    ): RoleplayResponse {
        var (_, giver, receiver) = data
        var embedResponse = roleplayActionAttributes.embedResponse

        val achievements = mutableListOf<AchievementTarget>()

        // I will be honest, this is kind of a hack
        // However it doesn't really matter, does it? ;P
        //
        // Easter eggs to specific actions
        when (roleplayActionAttributes) {
            // ===[ KISS ]===
            KISS_ATTRIBUTES -> {
                if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    return RoleplayResponse(listOf(AchievementTarget(giver, AchievementType.TRIED_KISSING_LORITTA)), false, 0) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.Command.Roleplay.Kiss.ResponseLori),
                            Emotes.LoriBonk
                        )
                    }
                }

                if (receiver == giver) {
                    embedResponse = { giverMention, _ -> I18nKeysData.Commands.Command.Roleplay.Kiss.ResponseSelf(giverMention) }
                } else {
                    val giverMarriage = loritta.pudding.marriages.getMarriageByUser(net.perfectdreams.loritta.serializable.UserId(giver))
                    val receiverMarriage = loritta.pudding.marriages.getMarriageByUser(net.perfectdreams.loritta.serializable.UserId(receiver))

                    // "Talarico é o cara que cobiça a mulher do próximo e as vezes até dos amigos."
                    // "Grass cutter"/"Grass cutter" in english
                    if (receiverMarriage != null && giverMarriage?.id != receiverMarriage.id) {
                        // Talarico achievement
                        achievements.add(AchievementTarget(giver, AchievementType.GRASS_CUTTER))
                    }

                    achievements.add(AchievementTarget(giver, AchievementType.GAVE_FIRST_KISS))
                    achievements.add(AchievementTarget(receiver, AchievementType.RECEIVED_FIRST_KISS))
                }
            }

            // ===[ SLAP ]===
            SLAP_ATTRIBUTES -> {
                if (giver == receiver) {
                    embedResponse = { giverMention, _ -> I18nKeysData.Commands.Command.Roleplay.Slap.ResponseSelf(giverMention) }
                } else if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    achievements.add(AchievementTarget(giver, AchievementType.TRIED_HURTING_LORITTA))

                    val oldGiver = giver
                    val oldReceiver = receiver

                    receiver = oldGiver
                    giver = oldReceiver

                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Slap::ResponseLori
                }
            }

            // ===[ ATTACK ]===
            ATTACK_ATTRIBUTES -> {
                if (giver == receiver) {
                    embedResponse = { giverMention, _ -> I18nKeysData.Commands.Command.Roleplay.Attack.ResponseSelf(giverMention) }
                } else if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    achievements.add(AchievementTarget(giver, AchievementType.TRIED_HURTING_LORITTA))

                    val oldGiver = giver
                    val oldReceiver = receiver

                    receiver = oldGiver
                    giver = oldReceiver

                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Attack::ResponseLori
                }
            }

            // ===[ HUG ]===
            HUG_ATTRIBUTES -> {
                if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Hug::ResponseLori
                } else if (giver == receiver) {
                    // If the giver is the same as the receiver, let's switch it to Loritta hugging the user
                    giver = loritta.config.loritta.discord.applicationId.toLong()

                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Hug::ResponseSelfLori
                }
            }

            // ===[ HEAD PAT ]===
            HEAD_PAT_ATTRIBUTES -> {
                if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Headpat::ResponseLori
                } else if (giver == receiver) {
                    // If the giver is the same as the receiver, let's switch it to Loritta hugging the user
                    giver = loritta.config.loritta.discord.applicationId.toLong()

                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Headpat::ResponseSelfLori
                }
            }

            // ===[ HIGH FIVE ]===
            HIGH_FIVE_ATTRIBUTES -> {
                if (receiver == loritta.config.loritta.discord.applicationId.toLong()) {
                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Highfive::ResponseLori
                } else if (giver == receiver) {
                    // If the giver is the same as the receiver, let's switch it to Loritta hugging the user
                    giver = loritta.config.loritta.discord.applicationId.toLong()

                    embedResponse = I18nKeysData.Commands.Command.Roleplay.Highfive::ResponseSelfLori
                }
            }

            // ===[ DANCE ]===
            DANCE_ATTRIBUTES -> {
                if (giver == receiver) {
                    embedResponse = { giverMention, _ -> I18nKeysData.Commands.Command.Roleplay.Dance.ResponseSelf(giverMention) }
                }
            }
        }

        val gender1 = loritta.pudding.users.getOrCreateUserProfile(net.perfectdreams.loritta.serializable.UserId(giver)).getProfileSettings().gender
        val gender2 = loritta.pudding.users.getOrCreateUserProfile(net.perfectdreams.loritta.serializable.UserId(receiver)).getProfileSettings().gender

        val result = roleplayActionAttributes.actionBlock.invoke(
            client,
            when (gender1) {
                Gender.MALE -> net.perfectdreams.randomroleplaypictures.common.Gender.MALE
                Gender.FEMALE -> net.perfectdreams.randomroleplaypictures.common.Gender.FEMALE
                Gender.UNKNOWN -> net.perfectdreams.randomroleplaypictures.common.Gender.UNKNOWN
            },
            when (gender2) {
                Gender.MALE -> net.perfectdreams.randomroleplaypictures.common.Gender.MALE
                Gender.FEMALE -> net.perfectdreams.randomroleplaypictures.common.Gender.FEMALE
                Gender.UNKNOWN -> net.perfectdreams.randomroleplaypictures.common.Gender.UNKNOWN
            }
        )

        val (picturePath, pictureSource) = result

        val (giveOutAffinityReward, givenAffinity) = context.loritta.transaction {
            val selfMarriage = MarriageParticipants
                .innerJoin(UserMarriages)
                .selectAll()
                .where {
                    UserMarriages.active eq true and (MarriageParticipants.user eq context.user.idLong)
                }
                .firstOrNull() ?: return@transaction Pair(false, 0)

            val marriageParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage eq selfMarriage[UserMarriages.id]
                }
                .toList()

            val marriageParticipantsIds = marriageParticipants.map { it[MarriageParticipants.user] }
                .toSet()

            val marriageParticipantsIdsExceptMyself = marriageParticipantsIds.toMutableSet()
                .apply {
                    this.remove(context.user.idLong)
                }

            if (receiver !in marriageParticipantsIdsExceptMyself)
                return@transaction Pair(false, 0)

            // They are our partner!
            var giveOutAffinityReward = false
            var givenAffinity = 0

            val now = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
            val todayAtMidnight = now
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .toInstant()

            if (roleplayActionAttributes.givesAffinityReward) {
                val sentRoleplayActionTodayNotIncludingCurrent = MarriageRoleplayActions.selectAll()
                    .where {
                        MarriageRoleplayActions.marriage eq selfMarriage[UserMarriages.id] and (MarriageRoleplayActions.sentAt greaterEq todayAtMidnight) and (MarriageRoleplayActions.sentBy eq context.user.idLong)
                    }
                    .count() != 0L

                giveOutAffinityReward = !sentRoleplayActionTodayNotIncludingCurrent
            }

            if (giveOutAffinityReward) {
                if (now.isAfter(ZonedDateTime.of(2025, 6, 8, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE))) {
                    // Have any of our other partners sent affinity today?
                    val sentRoleplayActionOtherPartners = MarriageRoleplayActions.selectAll()
                        .where {
                            MarriageRoleplayActions.marriage eq selfMarriage[UserMarriages.id] and (MarriageRoleplayActions.sentAt greaterEq todayAtMidnight) and (MarriageRoleplayActions.sentBy neq context.user.idLong)
                        }
                        .count() != 0L

                    givenAffinity = if (sentRoleplayActionOtherPartners) {
                        // They did!
                        3
                    } else {
                        // They did not :(
                        1
                    }
                } else {
                    // TODO: Remove this after the date has been elapsed!
                    givenAffinity = 1
                }
            }

            if (givenAffinity != 0) {
                UserMarriages.update({ UserMarriages.id eq selfMarriage[UserMarriages.id] }) {
                    it[UserMarriages.affinity] = UserMarriages.affinity + givenAffinity
                }
            }

            MarriageRoleplayActions.insert {
                it[MarriageRoleplayActions.marriage] = selfMarriage[UserMarriages.id]
                it[MarriageRoleplayActions.action] = roleplayActionAttributes.type
                it[MarriageRoleplayActions.sentAt] = now.toInstant()
                it[MarriageRoleplayActions.sentBy] = context.user.idLong
                it[MarriageRoleplayActions.affinityReward] = giveOutAffinityReward
            }

            return@transaction Pair(giveOutAffinityReward, givenAffinity)
        }

        return RoleplayResponse(achievements, giveOutAffinityReward, givenAffinity) {
            embed {
                description = buildString {
                    if (data.combo >= 3) {
                        append("**_[COMBO ${data.combo}X]_**")
                        append(" ")
                    }

                    append(roleplayActionAttributes.embedEmoji.toString())
                    append(" ")
                    append("**${i18nContext.get(embedResponse.invoke("<@$giver>", "<@$receiver>"))}**")
                }

                image = client.baseUrl + "/img/$picturePath"

                color = roleplayActionAttributes.embedColor.rgb

                footer("${i18nContext.get(RoleplayCommand.I18N_PREFIX.GenderTip)} - $picturePath")
            }

            actionRow(
                if (giver != receiver) {
                    loritta.interactivityManager.buttonForUser(
                        receiver,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        i18nContext.get(RoleplayCommand.I18N_PREFIX.Retribute),
                        {
                            loriEmoji = roleplayActionAttributes.embedEmoji
                        }
                    ) { context ->
                        context.invalidateComponentCallback()

                        // Retribute
                        val (achievementTargets, giveOutAffinityReward, givenAffinity, message) = handleRoleplayMessage(
                            context.loritta,
                            context,
                            context.i18nContext,
                            RetributeRoleplayData(
                                receiver,
                                receiver,
                                giver,
                                data.combo + 1
                            ),
                            client,
                            roleplayActionAttributes
                        )

                        context.reply(false) {
                            message()
                        }

                        for ((achievementReceiver, achievement) in achievementTargets) {
                            if (context.user.idLong == achievementReceiver)
                                context.giveAchievementAndNotify(achievement, ephemeral = true)
                            else
                                AchievementUtils.giveAchievementToUser(context.loritta, net.perfectdreams.loritta.serializable.UserId(achievementReceiver), achievement)
                        }

                        if (giveOutAffinityReward) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.YouReceivedAffinityPointsForRoleplaying(givenAffinity, context.loritta.commandMentions.marriageView)),
                                    Emotes.LoriHappy
                                )
                            }
                        }
                    }
                } else {
                    loritta.interactivityManager.disabledButton(
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        i18nContext.get(RoleplayCommand.I18N_PREFIX.Retribute),
                    ) {
                        loriEmoji = roleplayActionAttributes.embedEmoji
                    }
                },
                if (pictureSource != null) {
                    loritta.interactivityManager.button(
                        context.alwaysEphemeral,
                        ButtonStyle.SECONDARY,
                        i18nContext.get(RoleplayCommand.I18N_PREFIX.PictureSource),
                        {
                            loriEmoji = Emotes.LoriReading
                        }
                    ) { context ->
                        context.reply(true) {
                            content = when (pictureSource) {
                                is AnimeSource -> pictureSource.name
                            }
                        }
                    }
                } else {
                    loritta.interactivityManager.disabledButton(
                        context.alwaysEphemeral,
                        ButtonStyle.SECONDARY,
                        i18nContext.get(RoleplayCommand.I18N_PREFIX.PictureSource)
                    ) {
                        loriEmoji = Emotes.LoriReading
                    }
                }
            )
        }
    }

    data class RoleplayResponse(
        val achievements: List<AchievementTarget>,
        val giveOutAffinityReward: Boolean,
        val givenAffinity: Int,
        val builder: InlineMessage<*>.() -> (Unit)
    )

    data class AchievementTarget(
        val target: Long,
        val achievement: AchievementType
    )
}