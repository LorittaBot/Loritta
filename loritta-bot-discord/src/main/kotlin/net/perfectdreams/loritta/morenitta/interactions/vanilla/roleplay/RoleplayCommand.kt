package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.serializable.UserId

class RoleplayCommand {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roleplay

        // This compat is used for:
        // - The slash command
        // - The user command
        suspend fun executeCompat(
            context: UnleashedContext,
            attributes: RoleplayActionAttributes,
            receiver: User
        ) {
            context.deferChannelMessage(false)

            val (achievementTargets, message) = RoleplayUtils.handleRoleplayMessage(
                context.loritta,
                context.i18nContext,
                RetributeRoleplayData(
                    context.user.idLong,
                    context.user.idLong,
                    receiver.idLong,
                    1
                ),
                context.loritta.randomRoleplayPicturesClient,
                attributes
            )

            context.reply(false) {
                message()
            }

            for ((achievementReceiver, achievement) in achievementTargets) {
                if (context.user.idLong == achievementReceiver)
                    context.giveAchievementAndNotify(achievement, ephemeral = true)
                else
                    AchievementUtils.giveAchievementToUser(context.loritta, UserId(achievementReceiver), achievement)
            }

            // Easter Egg: Small chance for Loritta to retribute the action (1%)
            val shouldLorittaRetribute = receiver.idLong == context.loritta.config.loritta.discord.applicationId.toLong() && attributes in RoleplayUtils.RETRIBUTABLE_ACTIONS_BY_LORITTA_EASTER_EGG && context.loritta.random.nextInt(0, 100) == 0

            if (shouldLorittaRetribute) {
                // Wait 5s just so it feels more "natural"
                delay(5_000)

                // We don't care about achievements, because none of the actions that Loritta do *should* trigger a achievement
                val (_, lorittaMessage) = RoleplayUtils.handleRoleplayMessage(
                    context.loritta,
                    context.i18nContext,
                    RetributeRoleplayData(
                        context.user.idLong, // This doesn't really matter because it will be changed in the handleRoleplayMessage
                        receiver.idLong,
                        context.user.idLong,
                        2 // Increase the combo count
                    ),
                    context.loritta.randomRoleplayPicturesClient,
                    attributes
                )

                context.reply(false) {
                    lorittaMessage()
                }
            }
        }
    }

    class RoleplaySlashCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
        override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.ROLEPLAY) {
            enableLegacyMessageSupport = true
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

            subcommand(I18N_PREFIX.Hug.Label, I18N_PREFIX.Hug.Description) {
                executor = RoleplayHugExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("hug")
                    add("abraço")
                    add("abraçar")
                }
            }

            subcommand(I18N_PREFIX.Kiss.Label, I18N_PREFIX.Kiss.Description) {
                executor = RoleplayKissExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("kiss")
                    add("beijo")
                    add("beijar")
                }
            }

            subcommand(I18N_PREFIX.Slap.Label, I18N_PREFIX.Slap.Description) {
                executor = RoleplaySlapExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("slap")
                    add("tapa")
                    add("tapinha")
                }
            }

            subcommand(I18N_PREFIX.Headpat.Label, I18N_PREFIX.Headpat.Description) {
                executor = RoleplayHeadPatExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("headpat")
                    add("headpet")
                    add("cafuné")
                    add("pat")
                }
            }

            subcommand(I18N_PREFIX.Highfive.Label, I18N_PREFIX.Highfive.Description) {
                executor = RoleplayHighFiveExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("highfive")
                    add("hifive")
                    add("tocaaqui")
                }
            }

            subcommand(I18N_PREFIX.Attack.Label, I18N_PREFIX.Attack.Description) {
                executor = RoleplayAttackExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("attack")
                    add("atacar")
                }
            }

            subcommand(I18N_PREFIX.Dance.Label, I18N_PREFIX.Dance.Description) {
                executor = RoleplayDanceExecutor()

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("dance")
                    add("dancar")
                }
            }
        }

        class RoleplayHugExecutor : RoleplayPictureExecutor(
            RoleplayUtils.HUG_ATTRIBUTES
        )

        class RoleplayKissExecutor : RoleplayPictureExecutor(
            RoleplayUtils.KISS_ATTRIBUTES
        )

        class RoleplaySlapExecutor : RoleplayPictureExecutor(
            RoleplayUtils.SLAP_ATTRIBUTES
        )

        class RoleplayHeadPatExecutor : RoleplayPictureExecutor(
            RoleplayUtils.HEAD_PAT_ATTRIBUTES
        )

        class RoleplayHighFiveExecutor : RoleplayPictureExecutor(
            RoleplayUtils.HIGH_FIVE_ATTRIBUTES
        )

        class RoleplayAttackExecutor : RoleplayPictureExecutor(
            RoleplayUtils.ATTACK_ATTRIBUTES
        )

        class RoleplayDanceExecutor : RoleplayPictureExecutor(
            RoleplayUtils.DANCE_ATTRIBUTES
        )
    }

    class RoleplayUserCommand(val loritta: LorittaBot) : UserCommandDeclarationWrapper {
        override fun command() = userCommand(I18N_PREFIX.DoRoleplayAction, CommandCategory.ROLEPLAY, RoleplayUserExecutor(loritta)) {
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        }

        class RoleplayUserExecutor(val loritta: LorittaBot) : LorittaUserCommandExecutor() {
            override suspend fun execute(context: ApplicationCommandContext, user: User) {
                val guild = context.guildOrNull
                if (guild != null && !guild.isDetached && !context.channel.canTalk()) {
                    // Fixes a bug where the deferred message is public on channels that the user does not have permission to talk in, and Loritta is on the server
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.Command.Roleplay.YouCantUseThisHere),
                            Emotes.Error
                        )
                    }
                    return
                }

                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Roleplay.WhatActionDoYouWantForTheUser(user.asMention)),
                        Emotes.LoriReading
                    )

                    val attributesChunkedByActionRowLine = RoleplayUtils.ALL_ATTRIBUTES.chunked(5)

                    for (attributes in attributesChunkedByActionRowLine) {
                        val buttons = attributes.map {
                            loritta.interactivityManager.buttonForUser(
                                context.user,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(it.buttonLabel),
                                {
                                    loriEmoji = it.embedEmoji
                                }
                            ) { context ->
                                executeCompat(context, it, user)
                            }
                        }

                        actionRow(buttons)
                    }
                }
            }
        }
    }
}