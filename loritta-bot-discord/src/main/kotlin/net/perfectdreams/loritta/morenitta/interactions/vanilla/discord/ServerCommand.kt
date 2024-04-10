package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.toHexadecimalCode

class ServerCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Server
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label,
        TodoFixThisData,
        CommandCategory.DISCORD
    ) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        subcommand(I18N_PREFIX.Icon.Label, I18N_PREFIX.Icon.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("servericon")
                add("guildicon")
            }

            executor = ServerIconExecutor()
        }

        subcommand(I18N_PREFIX.Banner.Label, I18N_PREFIX.Banner.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("serverbanner")
                add("guildbanner")
            }

            executor = ServerBannerExecutor()
        }

        subcommand(I18N_PREFIX.Splash.Label, I18N_PREFIX.Splash.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("serversplash")
            }

            executor = ServerSplashExecutor()
        }

        subcommandGroup(I18N_PREFIX.Role.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Role.Info.Label, I18N_PREFIX.Role.Info.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("roleinfo")
                }

                executor = RoleInfoExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Channel.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Channel.Info.Label, I18N_PREFIX.Channel.Info.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("channelinfo")
                }

                executor = ServerChannelInfoExecutor()
            }
        }
    }

    inner class ServerIconExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val iconId = context.guild.iconId ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Icon.NoIcon(Emotes.LoriPat)
                    )
                )
            }

            val extension = if (iconId.startsWith("a_")) "gif" else "png"
            val urlIcon = "https://cdn.discordapp.com/icons/${context.guild.id}/$iconId.$extension?size=2048"

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} ${context.guild.name}"
                    image = urlIcon
                    color = Constants.NEW_DISCORD_BURPLE.toHexadecimalCode()
                }

                actionRow(
                    Button.link(
                        urlIcon,
                        context.i18nContext.get(I18N_PREFIX.Icon.OpenIconInBrowser)
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }

    inner class ServerBannerExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val bannerId = context.guild.bannerId ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Banner.NoBanner(Emotes.LoriPat)
                    ),
                    Emotes.LoriSob
                )
            }

            val extension = if (bannerId.startsWith("a_")) "gif" else "png"
            val bannerUrl = "https://cdn.discordapp.com/banners/${context.guild.id}/${context.guild.bannerId}.$extension?size=2048"

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} ${context.guild.name}"
                    color = Constants.NEW_DISCORD_BURPLE.toHexadecimalCode()
                    image = bannerUrl
                }

                actionRow(
                    Button.link(
                        bannerUrl,
                        context.i18nContext.get(
                            I18N_PREFIX.Banner.OpenBannerInBrowser
                        )
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }

    inner class ServerSplashExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val splashId = context.guild.splashId ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Splash.NoSplash(Emotes.LoriPat)
                    )
                )
            }

            val extension = if (splashId.startsWith("a_")) "gif" else "png"
            val urlIcon = "https://cdn.discordapp.com/splashes/${context.guild.id}/${context.guild.splashId}.$extension?size=2048"

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} ${context.guild.name}"
                    image = urlIcon
                    color = Constants.NEW_DISCORD_BURPLE.toHexadecimalCode()
                }

                actionRow(
                    Button.link(
                        urlIcon,
                        context.i18nContext.get(I18N_PREFIX.Splash.OpenSplashInBrowser)
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }

    inner class RoleInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val role = role("role", I18N_PREFIX.Role.Info.Options.Role)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val role = args[options.role]
            val timeCreated = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(role.timeCreated)

            context.reply(false) {
                embed {
                    title = "${Emotes.BriefCase} ${role.name}"
                    color = if (role.color != null)
                        role.color?.toHexadecimalCode()
                    else
                        Constants.NEW_DISCORD_BURPLE.toHexadecimalCode()

                    if (role.icon != null) {
                        thumbnail = role.icon!!.iconUrl + "?size=2048"
                    }

                    field {
                        name = "${Emotes.Eyes} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Mention)
                        value = "`<@&${role.id}>`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.LoriId} " + context.i18nContext.get(I18N_PREFIX.Role.Info.RoleId)
                        value = "`${role.id}`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.Eyes} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Hoisted)
                        value = context.i18nContext.get(role.isHoisted.toLocalized())

                        inline = true
                    }

                    field {
                        name = "${Emotes.BotTag} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Managed)
                        value = context.i18nContext.get(role.isManaged.toLocalized())

                        inline = true
                    }

                    if (role.color != null) field {
                        name = "${Emotes.Art} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Color)
                        value = "`#${Integer.toHexString(role.color?.rgb!!).uppercase().substring(2)}`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.LoriCalendar} " + context.i18nContext.get(I18N_PREFIX.Role.Info.CreatedAt)
                        value = timeCreated

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Members)
                        value = context.guild.getMembersWithRoles(role).size.toString()

                        inline = true
                    }

                    val rolePermissionsLocalized = role.permissions.joinToString(", ") {
                        "`${it.getLocalizedName(context.i18nContext)}`"
                    }

                    if (role.permissions.isNotEmpty()) field {
                        name = "${Emotes.Lock} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Permissions)
                        value = rolePermissionsLocalized
                    }
                }

                if (role.icon != null) {
                    actionRow(
                        Button.link(
                            role.icon!!.iconUrl + "?size=2048",
                            context.i18nContext.get(I18N_PREFIX.Role.Info.OpenRoleIconInBrowser)
                        )
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = args.getOrNull(0)

            if (arg0 == null) {
                context.explain()
                return null
            }

            val mentionedRole = context.mentions.roles.firstOrNull()

            if (arg0 == mentionedRole?.asMention) {
                return mapOf(
                    options.role to mentionedRole
                )
            }

            if (arg0.isValidSnowflake()) {
                val role = context.guild.getRoleById(arg0)
                if (role != null) {
                    return mapOf(
                        options.role to role
                    )
                }
            }

            return null
        }
    }

    inner class ServerChannelInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val channel = optionalChannel("channel", I18N_PREFIX.Channel.Info.Options.Channel)
        }

        override val options = Options()

        fun sendChannelEmbed(context: UnleashedContext, channel: GuildChannel) = Embed {
            title = "${Emotes.Discord} ${channel.name}"
            color = Constants.NEW_DISCORD_BURPLE.toHexadecimalCode()
            description = if ((channel as? TextChannel) == null)
                ""
            else "```${channel.topic}```"

            field {
                name = "${Emotes.SmallBlueDiamond} " + context.i18nContext.get(
                    I18N_PREFIX.Channel.Info.ChannelMention
                )
                value = "`<#${channel.id}>`"

                inline = true
            }

            field {
                name = "${Emotes.LoriId} " + context.i18nContext.get(
                    I18N_PREFIX.Channel.Info.ChannelId
                )
                value = "`${channel.id}`"
            }

            if ((channel as? TextChannel) != null && channel.isNSFW) field {
                name = "${Emotes.UnderAge} NSFW"
                value = context.i18nContext.get(channel.isNSFW.toLocalized())

                inline = true
            }

            when (channel.type) {
                ChannelType.VOICE -> {
                    channel as VoiceChannel

                    field {
                        name = "${Emotes.Microphone2} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Voice.BitRate
                        )
                        value = channel.bitrate.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Voice.UserLimit
                        )
                        value = if (channel.userLimit == 0)
                            context.i18nContext.get(I18nKeys.Common.Unlimited)
                        else channel.userLimit.toString()

                        inline = true
                    }

                    if (channel.regionRaw != null) field {
                        name = "${Emotes.EarthAmericas} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Voice.Region
                        )
                        value = channel.regionRaw!!

                        inline = true
                    }
                }
                ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD -> {
                    channel as ThreadChannel

                    field {
                        name = "${Emotes.PageFacingUp} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Thread.MessageCount
                        )

                        val messageCount = channel.messageCount
                        // The request limit only goes up to 50 messages
                        value = if (messageCount >= 50) "$messageCount+"
                        else messageCount.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Thread.MemberCount
                        )

                        val memberCount = channel.memberCount
                        // The request limit only goes up to 50 membersCount
                        value = if (memberCount >= 50) "$memberCount+"
                        else memberCount.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.Dividers} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Thread.Archived
                        )
                        value = context.i18nContext.get(
                            channel.isArchived.toLocalized()
                        )

                        inline = true
                    }

                    field {
                        name = "${Emotes.Lock} " + context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Thread.Locked
                        )
                        value = context.i18nContext.get(
                            channel.isLocked.toLocalized()
                        )

                        inline = true
                    }
                }
                else -> {}
            }

            if ((channel as? TextChannel) != null) {
                field {
                    name = "${Emotes.Snail} " + context.i18nContext.get(
                        I18N_PREFIX.Channel.Info.Text.SlowMode
                    )
                    val rateLimitPerUser = channel.slowmode.toLong()
                    value = if (rateLimitPerUser == 0L)
                        context.i18nContext.get(I18nKeys.Common.Disabled)
                    else
                        context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.Text.SlowModeSeconds(rateLimitPerUser)
                        )

                    inline = true
                }

                field {
                    name = "${Emotes.Trophy} " + context.i18nContext.get(I18N_PREFIX.Channel.Info.Position)
                    value = "${channel.position}º"

                    inline = true
                }

                field {
                    name = "${Emotes.LoriCalendar} " + context.i18nContext.get(I18N_PREFIX.Channel.Info.CreatedAt)
                    value = "<t:${channel.timeCreated.toEpochSecond()}:D>"

                    inline = true
                }
            }
        }

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val argumentChannelId = args[options.channel]?.id

            val channel = try {
                if (argumentChannelId != null)
                    context.guild.getGuildChannelById(argumentChannelId)
                else
                    context.guild.getGuildChannelById(context.channel.id)
            } catch (e: Exception) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Channel.Info.MissingAccessToChannel
                        ) + " ${Emotes.LoriSob}",
                        Emotes.Error
                    )
                }
            }!!

            context.reply(false) {
                embeds.plusAssign(sendChannelEmbed(context, channel))
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val channelId = context.mentions.channels.firstOrNull()?.id ?: args.getOrNull(0) ?: context.channel.id

            val channel = context.guild.getGuildChannelById(channelId)!!

            return mapOf(
                options.channel to channel
            )
        }
    }
}