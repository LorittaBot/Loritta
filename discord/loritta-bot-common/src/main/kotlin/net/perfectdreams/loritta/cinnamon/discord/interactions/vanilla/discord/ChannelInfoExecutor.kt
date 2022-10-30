package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord

import dev.kord.common.Color
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.optional.value
import dev.kord.rest.request.KtorRequestException
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.ServerCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot

class ChannelInfoExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val channel = optionalChannel("channel", ServerCommand.I18N_PREFIX.Channel.Info.Options.Channel)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        context.deferChannelMessage()

        val argumentChannelId = args[options.channel]?.id
        val channel = try {
            if (argumentChannelId != null)
                rest.channel.getChannel(argumentChannelId) // Channels not retrieved from the REST API only has a few fields, so we will query them from the REST API!
            else
                rest.channel.getChannel(context.channelId)
        } catch (e: KtorRequestException) {
            context.fail {
                styled(
                    context.i18nContext.get(ServerCommand.I18N_PREFIX.Channel.Info.MissingAccessToChannel) + " ${Emotes.LoriSob}",
                    Emotes.Error
                )
            }
        }

        context.sendEmbed {
            title = "${Emotes.PersonTippingHand} " + context.i18nContext.get(
                ServerCommand.I18N_PREFIX.Channel.Info.InfoFrom(channel.name.value.toString())
            )
            color = Color(114, 137, 218) // TODO: Move this to an object

            description = if (channel.topic.value == null)
                ""
            else "```${channel.topic.value}```"

            field {
                name = "${Emotes.SmallBlueDiamond} " + context.i18nContext.get(
                    ServerCommand.I18N_PREFIX.Channel.Info.ChannelMention
                )
                value = "`<#${channel.id.value}>`"

                inline = true
            }

            field {
                name = "${Emotes.LoriId} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Channel.Info.ChannelId)
                value = "`${channel.id.value}`"

                inline = true
            }

            if (channel.nsfw.value != null)
                field("${Emotes.UnderAge} NSFW", true) {
                    context.i18nContext.get(channel.nsfw.value!!.toLocalized())
                }

            when (channel.type) {
                ChannelType.GuildVoice -> {
                    field {
                        name = "${Emotes.Microphone2} " + context.i18nContext.get(
                            ServerCommand.I18N_PREFIX.Channel.Info.Voice.BitRate
                        )
                        value = channel.bitrate.value.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} " + context.i18nContext.get(
                            ServerCommand.I18N_PREFIX.Channel.Info.Voice.UserLimit
                        )
                        value = if (channel.userLimit.value == 0)
                            context.i18nContext.get(I18nKeys.Common.Unlimited)
                        else channel.userLimit.value.toString()

                        inline = true
                    }

                    if (channel.rtcRegion.value != null)
                        field {
                            name = "${Emotes.EarthAmericas} " + context.i18nContext.get(
                                ServerCommand.I18N_PREFIX.Channel.Info.Voice.Region
                            )
                            value = channel.rtcRegion.value.toString()

                            inline = true
                        }
                }

                ChannelType.PublicGuildThread, ChannelType.PrivateThread -> {
                    field {
                        name = "${Emotes.PageFacingUp} " + context.i18nContext.get(
                            ServerCommand.I18N_PREFIX.Channel.Info.Thread.MessageCount
                        )

                        val messageCount = channel.messageCount.value.toString()
                        // The request limit only goes up to 50 messages
                        value = if (messageCount == "50") "$messageCount+"
                        else messageCount

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} " + context.i18nContext.get(
                            ServerCommand.I18N_PREFIX.Channel.Info.Thread.MemberCount
                        )

                        val memberCount = channel.memberCount.value.toString()
                        // The request limit only goes up to 50 membersCount
                        value = if (memberCount == "50") "$memberCount+"
                        else memberCount

                        inline = true
                    }

                    if (channel.threadMetadata.value != null) {
                        field {
                            name = "${Emotes.Dividers} " + context.i18nContext.get(
                                ServerCommand.I18N_PREFIX.Channel.Info.Thread.Archived
                            )
                            value = context.i18nContext.get(
                                channel.threadMetadata.value!!.archived.toLocalized()
                            )

                            inline = true
                        }

                        field {
                            name = "${Emotes.Lock} " + context.i18nContext.get(
                                ServerCommand.I18N_PREFIX.Channel.Info.Thread.Locked
                            )
                            value = context.i18nContext.get(
                                channel.threadMetadata.value!!.locked.value!!.toLocalized()
                            )

                            inline = true
                        }
                    }
                }

                else -> {} // For now let's ignore them
            }

            if (channel.rateLimitPerUser.value != null)
                field {
                    name = "${Emotes.Snail} " + context.i18nContext.get(
                        ServerCommand.I18N_PREFIX.Channel.Info.Text.SlowMode
                    )
                    val rateLimitPerUser = channel.rateLimitPerUser.value
                    value = if (rateLimitPerUser?.inWholeSeconds == 0L || rateLimitPerUser == null)
                        context.i18nContext.get(I18nKeys.Common.Disabled)
                    else
                        context.i18nContext.get(
                            ServerCommand.I18N_PREFIX.Channel.Info.Text.SlowModeSeconds(rateLimitPerUser)
                        )

                    inline = true
                }

            if (channel.position.value != null)
                field {
                    name =
                        "${Emotes.Trophy} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Channel.Info.Position)
                    value = "${channel.position.value.toString()}º"

                    inline = true
                }

            field {
                name =
                    "${Emotes.LoriCalendar} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Channel.Info.CreatedAt)
                value = "<t:${channel.id.timestamp.toEpochMilliseconds() / 1000}:D>"

                inline = true
            }
        }
    }
}