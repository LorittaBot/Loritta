package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.ChannelType
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.TimeUtils
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndRemoveCodeBackticks
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.GiveawayCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.giveaway.JoinButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.giveaway.JoinButtonData
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.platform.utils.getUserProfile

class GiveawaySetupExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(GiveawaySetupExecutor::class) {
        object Options : CommandOptions() {
            val title = string("title", GiveawayCommand.I18N_PREFIX.Setup.Options.Title)
                .register()

            val numberOfWinners = integer("winners", GiveawayCommand.I18N_PREFIX.Setup.Options.NumberOfWinners)
                .register()

            val time = string("time", GiveawayCommand.I18N_PREFIX.Setup.Options.Time)
                .register()

            val description = optionalString("description", GiveawayCommand.I18N_PREFIX.Setup.Options.Description)
                .register()

            val channel = optionalChannel("channel", GiveawayCommand.I18N_PREFIX.Setup.Options.Channel)
                .register()

            val awardRole1 = optionalRole("award_role1", GiveawayCommand.I18N_PREFIX.Setup.Options.AwardRoles)
                .register()

            val awardRole2 = optionalRole("award_role2", GiveawayCommand.I18N_PREFIX.Setup.Options.AwardRoles)
                .register()

            val awardSonhosPerWinner = optionalInteger(
                "award_sonhos_per_winner",
                GiveawayCommand.I18N_PREFIX.Setup.Options.AwardSonhosPerUser
            )
                .register()

            val notAllowedRole1 = optionalRole(
                "not_allowed_role1",
                GiveawayCommand.I18N_PREFIX.Setup.Options.NotAllowedRoles
            )
                .register()

            val notAllowedRole2 = optionalRole(
                "not_allowed_role2",
                GiveawayCommand.I18N_PREFIX.Setup.Options.NotAllowedRoles
            )
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.failEphemerally {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val channelId = args[Options.channel]?.id ?: context.interaKTionsContext.channelId
        val giveawayChannel = rest.channel.getChannel(channelId)

        val numberOfWinners = args[Options.numberOfWinners]

        val awardRole1 = args[Options.awardRole1]
        val awardRole2 = args[Options.awardRole2]
        val awardRoles = arrayOf(
            awardRole1?.id?.asString,
            awardRole2?.id?.asString
        ).mapNotNull { it }.toTypedArray()
        val awardSonhosPerWinner = args[Options.awardSonhosPerWinner]

        val time = TimeUtils.convertToMillisRelativeToNow(args[Options.time])

        val notAllowedRoles = arrayOf(
            args[Options.notAllowedRole1]?.id,
            args[Options.notAllowedRole2]?.id
        ).mapNotNull { it }

        val hostSonhos = context.loritta.services.users.getUserProfile(context.user)?.data?.money ?: 0L

        if (giveawayChannel.type != ChannelType.GuildText)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.InvalidChannel),
                    Emotes.Error
                )
            }

        /* if (giveawayChannel.hasPermissions(Permission.SendMessages) == null)
            context.failEphemerally {
                content = context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.MissingChannelPermisesions)
            } */

        if (args[Options.numberOfWinners] !in 1..100)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.InvalidNumberOfWinners),
                    Emotes.Error
                )
            }

        if (awardSonhosPerWinner != null) {
            if (awardSonhosPerWinner !in 1000..hostSonhos)
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.InvalidAwardPerSonhos),
                        Emotes.Error
                    )
                }

            if (hostSonhos < (awardSonhosPerWinner * numberOfWinners))
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.InsufficientSonhos),
                        Emotes.Error
                    )
                }
        }

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.GiveawaySuccessfullyCreated),
                Emotes.Tada
            )
        }

        val giveawayTitle = args[Options.title].shortenAndRemoveCodeBackticks(DiscordResourceLimits.Embed.Title)

        val giveawayMessage = rest.channel.createMessage(
            giveawayChannel.id
        ) {
            embed {
                title = "${Emotes.Gift} " + giveawayTitle
                description = args[Options.description]?.shortenAndRemoveCodeBackticks(
                    DiscordResourceLimits.Embed.Description
                )

                color = Color(114, 137, 218) // TODO: Move this to an object

                field {
                    name = "${Emotes.AlarmClock} " + context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.RemainingTime)
                    value = "<t:${time / 1000}:R>"

                    inline = true
                }

                if (notAllowedRoles.isNotEmpty()) {
                    field {
                        name = "${Emotes.Error} " + context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.NotAllowedRoles)
                        value = notAllowedRoles.joinToString(", ") { "<@&${it.asString}>" }

                        inline = true
                    }
                }

                if (awardRoles.isNotEmpty())
                    field {
                        name = "${Emotes.BriefCase} " + context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.AwardRoles)
                        value = awardRoles.joinToString(", ") { "<@&$it>" }

                        inline = true
                    }

                if (awardSonhosPerWinner != null)
                    field {
                        name = "${Emotes.LoriRich} " + context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Setup.SonhosPerWinner)
                        value = awardSonhosPerWinner.toString()

                        inline = true
                    }
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Success,
                    JoinButtonClickExecutor,
                    ComponentDataUtils.encode(
                        JoinButtonData(
                            notAllowedRoles
                        )
                    )
                ) {
                    label = "\uD83C\uDF89"
                }
            }
        }

        context.loritta.services.giveaways.createGiveaway(
            giveawayMessage.id.value.toLong(),
            giveawayChannel.id.value.toLong(),
            context.guildId.value.toLong(),
            giveawayTitle,
            numberOfWinners.toInt(),
            time,
            context.user.id.value.toLong(),
            awardRoles,
            awardSonhosPerWinner
        )
    }
}