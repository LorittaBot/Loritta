package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.GuildData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.autocomplete.GuildAutocompleteContext
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithDatabaseData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.AdminUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis

class BanExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        // May be multiple in the same string
        val users = string("users", TodoFixThisData)

        // TODO: Pre-defined reasons with autocomplete
        val reason = optionalString("reason", TodoFixThisData) {
            cinnamonAutocomplete { autocompleteContext, focusedCommandOption ->
                val interaKTionsContext = autocompleteContext.interaKTionsContext as? GuildAutocompleteContext ?: return@cinnamonAutocomplete emptyMap()

                loritta.services.serverConfigs.getPredefinedPunishmentMessagesByGuildId(
                    interaKTionsContext.guildId.value
                ).filter {
                    it.short.startsWith(focusedCommandOption.value, true)
                }.associate {
                    "[${it.short}] ${it.message}".shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length) to it.short
                }
            }

            allowedLength = 0..512
        }

        // TODO: Delete days
        val skipConfirmation = optionalBoolean("skip_confirmation", TodoFixThisData)
        val sendViaDirectMessage = optionalBoolean("send_via_direct_message", TodoFixThisData)
        val sendToPunishmentLog = optionalBoolean("send_to_punishment_log", TodoFixThisData)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        context.deferChannelMessageEphemerally()

        if (Permission.BanMembers !in context.interaKTionsContext.appPermissions)
            context.failEphemerally {
                content = "Eu não tenho permissão para banir membros!"
            }

        val users = AdminUtils.checkAndRetrieveAllValidUsersFromString(context, args[options.users])

        if (users.isEmpty()) {
            context.failEphemerally {
                content = "Nenhum usuário encontrado!"
            }
        }

        // TODO: Check if the user can interact with the user (banning them)
        // To do this, we need to query the guild information

        // Technically the "guild" shouldn't be null here, ever
        val guild = Guild(GuildData.from(rest.guild.getGuild(context.guildId)), loritta.kord)

        val interactResults = AdminUtils.canInteract(
            guild,
            listOf(
                loritta.kord.getSelf(), // TODO: Cache getSelf somewhere
                context.member
            ),
            users.map { it.queryMember(guild.id) ?: it.user }
        )

        val nonInteractableUsers = interactResults.filterValues { it.any { it.result != AdminUtils.InteractionCheckResult.SUCCESS } }
        val interactableUsers = interactResults - nonInteractableUsers.keys

        if (interactableUsers.isEmpty()) {
            context.failEphemerally {
                styled(
                    "Nenhum dos usuários podem ser punidos!",
                    Emotes.LoriSob
                )

                for (nonInteractableUser in nonInteractableUsers) {
                    val whyTheyArentGoingToBePunished =
                        nonInteractableUser.value.first { it.result != AdminUtils.InteractionCheckResult.SUCCESS }

                    AdminUtils.appendCheckResultReason(
                        loritta,
                        context.i18nContext,
                        context.member,
                        this,
                        whyTheyArentGoingToBePunished
                    )
                }
            }
        }

        val moderationConfig = loritta.services.serverConfigs.getModerationConfigByGuildId(guild.id.value)

        val sendPunishmentViaDirectMessage = args[options.sendViaDirectMessage] ?: moderationConfig?.sentPunishmentViaDm ?: false
        val sendPunishmentToPunishLog = args[options.sendToPunishmentLog] ?: moderationConfig?.sendPunishmentToPunishLog ?: false

        val reason = args[options.reason]?.let { rawReason ->
            loritta.services.serverConfigs.getPredefinedPunishmentMessagesByGuildId(context.guildId.value)
                .firstOrNull { it.short.equals(rawReason, true) }
                ?.message ?: rawReason
        }

        val confirmBanData = ConfirmBanData(
            reason,
            sendPunishmentViaDirectMessage,
            sendPunishmentToPunishLog,
            moderationConfig?.punishLogChannelId?.let { Snowflake(it) },
            guild.data,
            context.user.data,
            interactableUsers.keys.map {
                val member = it as? Member

                ConfirmBanData.UserWithMemberData(
                    it.data,
                    member?.memberData
                )
            }
        )

        // TODO: If the user has selected the option to "skip_confirmation" or they have quickpunishment enabled, skip the confirmation below
        if (args[options.skipConfirmation] == true) {
            AdminUtils.banUsers(
                loritta,
                confirmBanData
            )

            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.Category.Moderation.SuccessfullyPunished),
                    Emotes.LoriBanHammer
                )
            }
        } else {
            context.sendEphemeralMessage {
                styled(
                    "Você está prestes a banir ${interactableUsers.keys.joinToString { it.mention }} do seu servidor pelo motivo `$reason`!",
                    Emotes.LoriBanHammer
                )

                if (sendPunishmentViaDirectMessage) {
                    styled(
                        context.i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentWillBeSentViaDirectMessage)
                    )
                } else {
                    styled(
                        context.i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentWillNotBeSentViaDirectMessage)
                    )
                }

                if (sendPunishmentToPunishLog) {
                    styled(
                        context.i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentWillBeSentToPunishmentLog)
                    )
                } else {
                    styled(
                        context.i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentWillNotBeSentToPunishmentLog)
                    )
                }

                for (nonInteractableUser in nonInteractableUsers) {
                    val whyTheyArentGoingToBePunished = nonInteractableUser.value.first { it.result != AdminUtils.InteractionCheckResult.SUCCESS }

                    AdminUtils.appendCheckResultReason(
                        loritta,
                        context.i18nContext,
                        context.member,
                        this,
                        whyTheyArentGoingToBePunished
                    )
                }

                styled(
                    "Cansado de confirmar punições? Então use `/quickpunishment`!"
                )

                actionRow {
                    interactiveButtonWithDatabaseData(
                        loritta,
                        ButtonStyle.Primary,
                        ConfirmBanButtonExecutor,
                        confirmBanData
                    ) {
                        loriEmoji = Emotes.LoriBanHammer

                        label = "Confirmar Punição"
                    }
                }
            }
        }
    }
}