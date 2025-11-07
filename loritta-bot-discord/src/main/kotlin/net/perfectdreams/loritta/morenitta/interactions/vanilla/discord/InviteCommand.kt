package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.components.buttons.Button
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.util.*

class InviteCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Invite
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.DISCORD, UUID.fromString("e1ee9bd0-06f0-4f5d-b613-41bc989e21c4")) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description, UUID.fromString("44a486e9-b5d5-4bc5-94bb-4e6b4b918d9e")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("inviteinfo")
            }

            executor = InviteInfoExecutor()
        }
    }

    inner class InviteInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val invite = string("invite", I18N_PREFIX.Info.Options.Invite)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = args[options.invite]

            val inviteCode = if (text.contains("/")) {
                DiscordInviteUtils.getInviteCodeFromUrl(text)
            } else {
                text
            }

            if (inviteCode == null || !DiscordInviteUtils.inviteCodeRegex.matches(inviteCode)) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Info.DoesntExists(
                            args[options.invite].shortenAndStripCodeBackticks(100)
                        )
                    ),
                    Emotes.Error
                )
            }

            context.deferChannelMessage(true)

            val invite = try {
                Invite.resolve(context.jda, inviteCode, true)
                    .submit(false)
                    .await()
            } catch (e: Exception) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Info.DoesntExists(
                                args[options.invite].shortenAndStripCodeBackticks(100)
                            )
                        ),
                        Emotes.Error
                    )

                }
            }

            val guild = invite.guild

            val extension = if (guild?.iconId?.startsWith("a_") == true) "gif" else "png"
            val iconUrl = "https://cdn.discordapp.com/icons/${guild!!.id}/${guild.iconId}.$extension?size=2048"

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} ${invite.guild!!.name}"
                    color = Constants.DISCORD_BLURPLE.rgb

                    if (guild.iconId != null)
                        thumbnail = iconUrl

                    field {
                        name = "${Emotes.LoriId} ID"
                        value = "`${invite.guild!!.id}`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} ${context.i18nContext.get(
                            I18N_PREFIX.Info.ServerMembers
                        )} (${invite.guild!!.memberCount})"
                        value = "${Emotes.PersonTippingHand} ${context.i18nContext.get(
                            I18N_PREFIX.Info.OnlineMembersPresence(
                                invite.guild!!.onlineCount.toString(),
                            )
                        )}\n${Emotes.Sleeping} ${context.i18nContext.get(
                            I18N_PREFIX.Info.OfflineMembersPresence(
                                (invite.guild!!.memberCount - invite.guild!!.onlineCount).toString(),
                            )
                        )}"

                        inline = true
                    }

                    if (invite.channel != null) field {
                        name = "${Emotes.SpeakingHead} ${context.i18nContext.get(
                            I18N_PREFIX.Info.InvitationChannel
                        )}"
                        value = "#${invite.channel!!.name} (`${invite.channel!!.id}`)"

                        inline = true
                    }

                    if (invite.inviter != null) field {
                        name = "${Emotes.Wave} ${context.i18nContext.get(
                            I18N_PREFIX.Info.WhoInvited
                        )}"
                        value = "${invite.inviter!!.name} (`${invite.inviter!!.id}`)"

                        inline = true
                    }

                    field {
                        name = "${Emotes.Sparkles} ${context.i18nContext.get(
                            I18N_PREFIX.Info.GuildFeatures
                        )}"
                        value = invite.guild!!.features.joinToString(", ") {
                            "`$it`"
                        }
                    }

                    if (invite.guild!!.splashUrl != null) image = invite.guild!!.splashUrl + "?size=2048"

                    footer {
                        val discordGuild = context.loritta.lorittaShards.queryGuildById(invite.guild!!.idLong)

                        name = if (discordGuild != null)
                            "${Emotes.Blush} ${context.i18nContext.get(
                                I18N_PREFIX.Info.InThisServer
                            )}"
                        else
                            "${Emotes.Sob} ${context.i18nContext.get(
                                I18N_PREFIX.Info.NotOnTheServer
                            )}"
                    }
                }

                if (invite.guild?.icon != null) actionRow(
                    Button.link(
                        iconUrl,
                        context.i18nContext.get(ServerCommand.I18N_PREFIX.Icon.OpenIconInBrowser)
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val invite = args.getOrNull(0)

            if (invite == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.invite to invite
            )
        }
    }
}