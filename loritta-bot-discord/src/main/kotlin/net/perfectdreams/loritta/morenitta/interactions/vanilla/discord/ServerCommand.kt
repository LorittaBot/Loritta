package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.components.buttons.Button
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.retrieveMemberCount
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.toLocalized
import java.util.*

class ServerCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Server
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label,
        TodoFixThisData,
        CommandCategory.DISCORD,
        UUID.fromString("ccafc456-ae0f-4359-9b9b-3274c0af550a")
    ) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD)

        subcommand(I18N_PREFIX.Icon.Label, I18N_PREFIX.Icon.Description, UUID.fromString("59be79b3-4a5e-4cda-ba5a-40e17175e134")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("servericon")
                add("guildicon")
            }

            executor = ServerIconExecutor()
        }

        subcommand(I18N_PREFIX.Banner.Label, I18N_PREFIX.Banner.Description, UUID.fromString("c7e13909-3845-4faa-a66f-6190bee35faf")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("serverbanner")
                add("guildbanner")
            }

            executor = ServerBannerExecutor()
        }

        subcommand(I18N_PREFIX.Splash.Label, I18N_PREFIX.Splash.Description, UUID.fromString("b7949cda-6887-4f6f-955a-80cc54f35c5c")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("serversplash")
            }

            executor = ServerSplashExecutor()
        }

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description, UUID.fromString("a3b7c9d1-2e4f-5a6b-8c0d-1e2f3a4b5c6d")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("serverinfo")
                add("guildinfo")
            }

            executor = ServerInfoExecutor()
        }

        subcommandGroup(I18N_PREFIX.Role.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Role.Info.Label, I18N_PREFIX.Role.Info.Description, UUID.fromString("46d82f6b-bf0a-4013-b647-a2e87a481f83")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("roleinfo")
                }

                executor = RoleInfoExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Channel.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Channel.Info.Label, I18N_PREFIX.Channel.Info.Description, UUID.fromString("9086b8f9-9545-4e9b-b722-8c7b10405c70")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("channelinfo")
                }

                executor = ServerChannelInfoExecutor()
            }
        }
    }

    inner class ServerIconExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val serverIconId = optionalString("server_id", I18N_PREFIX.Icon.Options.ServerId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userProvidedServerIconId = args[options.serverIconId]

            data class GuildIconWithName(
                val id: Long,
                val guildName: String,
                val iconId: String?,
            )

            val iconData: GuildIconWithName?

            if (userProvidedServerIconId != null) {
                val userProvidedServerIconIdAsLong = userProvidedServerIconId.toLongOrNull()
                    ?: context.fail(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Icon.InvalidId),
                            Emotes.Error
                        )
                    }

                if (userProvidedServerIconIdAsLong == context.guildId) {
                    // No need to query if it is on this instance
                    iconData = GuildIconWithName(
                        context.guild.idLong,
                        context.guild.name,
                        context.guild.iconId
                    )
                } else {
                    val guild = loritta.lorittaShards.queryGuildById(userProvidedServerIconIdAsLong)
                        ?: context.fail(true) {
                            // Unknown guild
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Icon.UnknownGuild),
                                Emotes.LoriSob
                            )
                        }

                    val idElement = guild.get("id")
                    val guildNameElement = guild.get("name")
                    val iconIdElement = guild.get("iconId")
                    iconData = GuildIconWithName(
                        idElement.long,
                        guildNameElement.string,
                        iconIdElement.nullString
                    )
                }
            } else {
                iconData = GuildIconWithName(
                    context.guild.idLong,
                    context.guild.name,
                    context.guild.iconId
                )
            }

            val (guildId, guildName, iconId) = iconData
            if (iconId == null) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Icon.NoIcon(Emotes.LoriPat)
                        )
                    )
                }
            }

            val extension = if (iconId.startsWith("a_")) "gif" else "png"
            val urlIcon = "https://cdn.discordapp.com/icons/$guildId/$iconId.$extension?size=2048"

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} $guildName"
                    image = urlIcon
                    color = Constants.DISCORD_BLURPLE.rgb
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
            return mapOf(options.serverIconId to args.getOrNull(0))
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
                    color = Constants.DISCORD_BLURPLE.rgb
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
                    color = Constants.DISCORD_BLURPLE.rgb
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

    inner class ServerInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val serverId = optionalString("server_id", I18N_PREFIX.Info.Options.ServerId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            // Baseado no comando ?serverinfo do Dyno
            val userProvidedServerId = args[options.serverId]

            data class GuildInfo(
                val id: Long,
                val name: String,
                val iconUrl: String?,
                val splashUrl: String?,
                val shardId: Int,
                val ownerId: Long,
                val textChannelCount: Int,
                val voiceChannelCount: Int,
                val timeCreated: Long,
                val timeJoined: Long,
                val memberCount: Int
            )

            val guildInfo: GuildInfo

            if (userProvidedServerId != null) {
                val userProvidedServerIdAsLong = userProvidedServerId.toLongOrNull()
                    ?: context.fail(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Icon.InvalidId),
                            Emotes.Error
                        )
                    }

                if (userProvidedServerIdAsLong == context.guildId) {
                    // No need to query if it is on this instance
                    guildInfo = GuildInfo(
                        context.guild.idLong,
                        context.guild.name,
                        context.guild.iconUrl,
                        context.guild.splashUrl,
                        context.guild.jda.shardInfo.shardId,
                        context.guild.ownerIdLong,
                        context.guild.textChannels.size,
                        context.guild.voiceChannels.size,
                        context.guild.timeCreated.toEpochSecond() * 1000,
                        context.guild.selfMember.timeJoined.toEpochSecond() * 1000,
                        context.guild.memberCount
                    )
                } else {
                    val guild = loritta.lorittaShards.queryGuildById(userProvidedServerIdAsLong)
                        ?: context.fail(true) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Info.UnknownGuild(userProvidedServerId)),
                                Emotes.LoriSob
                            )
                        }

                    val count = guild.get("count").asJsonObject
                    guildInfo = GuildInfo(
                        guild.get("id").long,
                        guild.get("name").string,
                        guild.get("iconUrl").nullString,
                        guild.get("splashUrl").nullString,
                        guild.get("shardId").int,
                        guild.get("ownerId").string.toLong(),
                        count.get("textChannels").int,
                        count.get("voiceChannels").int,
                        guild.get("timeCreated").long,
                        guild.get("timeJoined").long,
                        count.get("members").int
                    )
                }
            } else {
                guildInfo = GuildInfo(
                    context.guild.idLong,
                    context.guild.name,
                    context.guild.iconUrl,
                    context.guild.splashUrl,
                    context.guild.jda.shardInfo.shardId,
                    context.guild.ownerIdLong,
                    context.guild.textChannels.size,
                    context.guild.voiceChannels.size,
                    context.guild.timeCreated.toEpochSecond() * 1000,
                    context.guild.selfMember.timeJoined.toEpochSecond() * 1000,
                    context.guild.memberCount
                )
            }

            val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, guildInfo.id)
            val owner = loritta.lorittaShards.retrieveUserInfoById(guildInfo.ownerId)
            val ownerProfile = loritta.getLorittaProfile(guildInfo.ownerId)
            val ownerGender = loritta.newSuspendedTransaction { ownerProfile?.settings?.gender ?: Gender.UNKNOWN }

            val ownerLabel = if (ownerGender == Gender.FEMALE)
                context.i18nContext.get(I18N_PREFIX.Info.OwnerFemale)
            else
                context.i18nContext.get(I18N_PREFIX.Info.Owner)

            val timeCreatedFormatted = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(guildInfo.timeCreated)
            val timeJoinedFormatted = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(guildInfo.timeJoined)

            context.reply(false) {
                embed {
                    title = "${Emotes.Discord} ${guildInfo.name}"
                    thumbnail = guildInfo.iconUrl
                    image = guildInfo.splashUrl?.replace("jpg", "png")?.plus("?size=2048")
                    color = Constants.DISCORD_BLURPLE.rgb

                    field {
                        name = "${Emotes.LoriId} ID"
                        value = "`${guildInfo.id}`"
                        inline = true
                    }

                    field {
                        name = "${Emotes.Computer} Shard ID"
                        value = "${guildInfo.shardId} — Loritta Cluster ${cluster.id} (`${cluster.name}`)"
                        inline = true
                    }

                    field {
                        name = "\uD83D\uDC51 $ownerLabel"
                        value = if (owner != null) "`${owner.name}` (${guildInfo.ownerId})" else "${guildInfo.ownerId}"
                        inline = true
                    }

                    field {
                        name = "\uD83D\uDCAC ${context.i18nContext.get(I18N_PREFIX.Info.Channels)} (${guildInfo.textChannelCount + guildInfo.voiceChannelCount})"
                        value = "\uD83D\uDCDD **${context.i18nContext.get(I18N_PREFIX.Info.TextChannels)}:** ${guildInfo.textChannelCount}\n${Emotes.SpeakingHead} **${context.i18nContext.get(I18N_PREFIX.Info.VoiceChannels)}:** ${guildInfo.voiceChannelCount}"
                        inline = true
                    }

                    field {
                        name = "${Emotes.LoriCalendar} ${context.i18nContext.get(I18N_PREFIX.Info.CreatedAt)}"
                        value = timeCreatedFormatted
                        inline = true
                    }

                    field {
                        name = "${Emotes.Sparkles} ${context.i18nContext.get(I18N_PREFIX.Info.JoinedAt)}"
                        value = timeJoinedFormatted
                        inline = true
                    }

                    field {
                        name = "${Emotes.BustsInSilhouette} ${context.i18nContext.get(I18N_PREFIX.Info.Members)} (${guildInfo.memberCount})"
                        value = ""
                        inline = true
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(options.serverId to args.getOrNull(0))
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
                        role.color?.rgb
                    else
                        Constants.DISCORD_BLURPLE.rgb

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
                        value = "${role.retrieveMemberCount()}"
                    }

                    // We don't include the member count because that's the cached member count, not the real member count
                    // So, to avoid confusion, we don't show it

                    var isFirst = true

                    val rolePermissionsLocalized = buildString {
                        appendLine("**${Emotes.Lock} " + context.i18nContext.get(I18N_PREFIX.Role.Info.Permissions) + "**")

                        var count = 0

                        // This SUCKS but somehow we need to have a template to know if it is going to fit or not
                        val temporaryIfDoesNotFitMaxPossibleValue = buildString {
                            append(", ")
                            append(context.i18nContext.get(I18N_PREFIX.Role.Info.AndXMorePermissions(role.permissions.size)))
                        }

                        val maxLength = DiscordResourceLimits.Embed.Description - temporaryIfDoesNotFitMaxPossibleValue.length
                        for (permission in role.permissions) {
                            val temporary = buildString {
                                if (!isFirst)
                                    append(", ")

                                append("`")
                                append(permission.getLocalizedName(context.i18nContext))
                                append("`")
                            }

                            if (maxLength > this.length + temporary.length) {
                                append(temporary)
                                count++
                            } else {
                                if (!isFirst)
                                    append(", ")
                                append(context.i18nContext.get(I18N_PREFIX.Role.Info.AndXMorePermissions(role.permissions.size - count)))
                                break
                            }

                            isFirst = false
                        }
                    }

                    if (role.permissions.isNotEmpty())
                        this.description = rolePermissionsLocalized
                }

                val roleIconUrl = role.icon?.iconUrl

                if (roleIconUrl != null) {
                    actionRow(
                        Button.link(
                            "$roleIconUrl?size=2048",
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
            color = Constants.DISCORD_BLURPLE.rgb
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