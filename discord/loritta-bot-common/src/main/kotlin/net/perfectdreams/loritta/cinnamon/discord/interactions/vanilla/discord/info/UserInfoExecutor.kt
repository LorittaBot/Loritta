package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info

import dev.kord.common.Color
import dev.kord.common.DiscordBitSet
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlag
import dev.kord.common.kColor
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar.MessageTargetType
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar.SwitchToGlobalAvatarExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar.SwitchToGuildProfileAvatarExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar.UserDataUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.StoredGenericInteractionData
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import kotlin.time.Duration.Companion.minutes

interface UserInfoExecutor {
    val http: HttpClient

    suspend fun handleUserExecutor(
        context: ApplicationCommandContext,
        user: User,
        member: Member?,
        isEphemeral: Boolean,
    ) {
        val now = Clock.System.now()

        val flags = user.publicFlags?.flags ?: emptyList()
        val flagsToEmotes = flags.mapNotNull {
            when (it) {
                UserFlag.DiscordEmployee -> Emotes.DiscordEmployee
                UserFlag.DiscordPartner -> Emotes.DiscordPartner
                UserFlag.HypeSquad -> Emotes.HypeSquad
                UserFlag.BugHunterLevel1 -> Emotes.BugHunterLevel1
                UserFlag.HouseBravery -> Emotes.HouseBravery
                UserFlag.HouseBrilliance -> Emotes.HouseBrilliance
                UserFlag.HouseBalance -> Emotes.HouseBalance
                UserFlag.EarlySupporter -> Emotes.EarlySupporter
                // I don't know how we could represent this
                // UserFlag.TeamUser -> ???
                UserFlag.BugHunterLevel2 -> Emotes.BugHunterLevel2
                UserFlag.VerifiedBotDeveloper -> Emotes.VerifiedBotDeveloper
                UserFlag.DiscordCertifiedModerator -> Emotes.LoriCoffee
                else -> null
            }
        }

        // Discord's System User (643945264868098049) does not have the "System" flag, so we will add a special handling for it
        val isSystemUser = UserFlag.System in flags || (user.username == "Discord" && user.discriminator == "0000")

        // Get application information
        val applicationInfo = if (user.isBot && !isSystemUser) {
            // It looks like system user do not have an application bound to it, so we will just ignore it
            getApplicationInfo(user.id)
        } else null

        val roles = if (context is GuildApplicationCommandContext && member != null)
            context.loritta.cache.getRoles(
                context.guildId,
                member.roleIds
            )
        else null

        val topRole = roles?.maxByOrNull { it.position }
        // Did you know that you can't have a fully black role on Discord? The color "0" is used for "not set"!
        val topRoleForColor = roles?.filter { it.color != null }?.maxByOrNull { it.position }

        val message: suspend MessageBuilder.() -> (Unit) = {
            embed {
                author(context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InfoAboutTheUser))
                title = buildString {
                    if (user.isBot) {
                        append(
                            when {
                                isSystemUser -> Emotes.VerifiedSystemTag
                                UserFlag.VerifiedBot in flags -> Emotes.VerifiedBotTag
                                else -> Emotes.BotTag
                            }
                        )
                    } else {
                        append(Emotes.WumpusBasic)
                    }

                    if (flagsToEmotes.isNotEmpty()) {
                        for (emote in flagsToEmotes)
                            append(emote.toString())
                    }

                    append(" ")

                    append(user.username)
                }
                url = "https://discord.com/users/${user.id}"

                field("${Emotes.LoriId} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.DiscordId)}", "`${user.id}`", true)
                field("${Emotes.LoriLabel} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.DiscordTag)}", "`${user.username}#${user.discriminator}`", true)
                field("${Emotes.LoriCalendar} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.AccountCreationDate)}", "<t:${user.id.timestamp.epochSeconds}:f> (<t:${user.id.timestamp.epochSeconds}:R>)", true)

                thumbnailUrl = user.effectiveAvatar.url
                color = LorittaColors.DiscordBlurple.toKordColor()
            }

            if (member != null) {
                val communicationDisabledUntil = member.communicationDisabledUntil

                embed {
                    author(context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InfoAboutTheMember))
                    title = member.nickname ?: user.username

                    field(
                        "${Emotes.LoriCalendar} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.AccountJoinDate)}",
                        "<t:${member.joinedAt.epochSeconds}:f> (<t:${member.joinedAt.epochSeconds}:R>)",
                        true
                    )

                    if (communicationDisabledUntil != null) {
                        field(
                            "${Emotes.LoriBonk} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.TimedOutUntil)}",
                            buildString {
                                append("<t:${member.communicationDisabledUntil?.epochSeconds}:f> (<t:${member.communicationDisabledUntil?.epochSeconds}:R>)")
                                if (Clock.System.now() > communicationDisabledUntil) {
                                    append("\n")
                                    append("*(${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.TimeoutTip)})*")
                                }
                            },
                            true
                        )
                    }

                    val premiumSince = member.premiumSince

                    if (premiumSince != null) {
                        field(
                            "${Emotes.LoriWow} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.BoostingSince)}",
                            "<t:${premiumSince.epochSeconds}:f> (<t:${premiumSince.epochSeconds}:R>)",
                            true
                        )
                    }

                    if (topRole != null) {
                        field(
                            "${Emotes.LoriSunglasses} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.HighestRole)}",
                            "<@&${topRole.id}>",
                            true
                        )

                        color = topRoleForColor?.color?.kColor
                    }

                    field(
                        "${Emotes.LoriZap} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InterestingTidbits)}",
                        """${fancify(!member.isPending)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.CompletedMembershipScreening)}
                                |${fancify(communicationDisabledUntil != null && communicationDisabledUntil >= Clock.System.now())} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.IsTimedOut)}
                            """.trimMargin(),
                        false
                    )

                    thumbnailUrl = (member.avatar ?: user.effectiveAvatar).url
                }
            }

            if (user.isBot && !isSystemUser) {
                embed {
                    author(context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InfoAboutTheApplication))

                    if (applicationInfo != null) {
                        title = applicationInfo.name
                        description = applicationInfo.description

                        if (applicationInfo.guildId != null) {
                            field(
                                "${Emotes.LoriId} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.SupportGuildId)}",
                                "`${applicationInfo.guildId}`",
                                true
                            )
                        }

                        val tags = applicationInfo.tags
                        if (tags != null) {
                            field(
                                "${Emotes.LoriLabel} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.Tags)}",
                                tags.joinToString(),
                                true
                            )
                        }

                        if (applicationInfo.slug != null) {
                            field(
                                "\uD83D\uDC1B ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.Slug)}",
                                "`${applicationInfo.slug}`",
                                true
                            )
                        }

                        val bitSet = DiscordBitSet(applicationInfo.flags.toString())
                        val hasGatewayPresence = bitSet.contains(DiscordBitSet(1 shl 12))
                        val hasGatewayPresenceLimited = bitSet.contains(DiscordBitSet(1 shl 13))
                        val hasGuildMembers = bitSet.contains(DiscordBitSet(1 shl 14))
                        val hasGuildMembersLimited = bitSet.contains(DiscordBitSet(1 shl 15))
                        // val verificationPendingGuildLimit = bitSet.contains(DiscordBitSet(1 shl 16))
                        val hasGatewayMessageContent = bitSet.contains(DiscordBitSet(1 shl 18))
                        val hasGatewayMessageContentLimited = bitSet.contains(DiscordBitSet(1 shl 19))

                        // While it would be nice to show the "Bot is actually using the intent but it is in less than 100 servers", it is not that reliable.
                        // Loritta has the "hasGuildMembersLimited" intent, however she is in more than 100 guilds
                        field(
                            "${Emotes.LoriZap} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InterestingTidbits)}",
                            """${fancify(applicationInfo.botPublic)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.Public)}
                                |${fancify(applicationInfo.botRequireCodeGrant)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.RequiresOAuth2CodeGrant)}
                                |${fancify(UserFlag.BotHttpInteractions in flags)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.UsesInteractionsOverHttp)}
                                |${fancify(hasGatewayPresence || hasGatewayPresenceLimited)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.IntentGatewayPresences)}
                                |${fancify(hasGuildMembers || hasGuildMembersLimited)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.IntentGuildMembers)}
                                |${fancify(hasGatewayMessageContent || hasGatewayMessageContentLimited)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.IntentMessageContent)}
                            """.trimMargin(),
                            false
                        )

                        field(
                            "\uD83D\uDCBB ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.PublicKey)}",
                            "`${applicationInfo.verifyKey}`",
                            false
                        )

                        if (applicationInfo.icon != null)
                            thumbnailUrl = "https://cdn.discordapp.com/app-icons/${applicationInfo.id}/${applicationInfo.icon}.png"

                        color = LorittaColors.DiscordOldBlurple.toKordColor()
                    } else {
                        title = "${Emotes.Error} Whoops"
                        description = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.NoMatchingApplicationFound)
                    }
                }
            }

            val loritta = context.loritta

            // ===[ VIEW AVATAR BUTTONS ]===
            actionRow {
                // ===[ GLOBAL AVATAR ]===
                val globalInteractionId = loritta.pudding.interactionsData.insertInteractionData(
                    Json.encodeToJsonElement<UserDataUtils.ViewingUserAvatarData>(
                        UserDataUtils.ViewingGlobalUserAvatarData(
                            user.username,
                            user.discriminator.toInt(),
                            user.data.avatar,
                            member?.memberData?.avatar?.value
                        )
                    ).jsonObject,
                    now,
                    now + 15.minutes // Expires after 15m
                )

                interactiveButton(
                    ButtonStyle.Primary,
                    SwitchToGlobalAvatarExecutor,
                    ComponentDataUtils.encode(
                        UserDataUtils.SwitchAvatarInteractionIdData(
                            context.user.id,
                            user.id,
                            (context as? GuildApplicationCommandContext)?.guildId,
                            MessageTargetType.SEND_MESSAGE_EPHEMERAL,
                            globalInteractionId
                        )
                    )
                ) {
                    label = context.i18nContext.get(I18nKeysData.Innercommands.Innercommand.Inneruser.Inneravatar.ViewUserGlobalAvatar)
                }

                // ===[ GUILD AVATAR ]===
                val guildAvatarHash = member?.memberData?.avatar?.value
                if (guildAvatarHash != null) {
                    val localInteractionId = loritta.pudding.interactionsData.insertInteractionData(
                        Json.encodeToJsonElement<UserDataUtils.ViewingUserAvatarData>(
                            UserDataUtils.ViewingGuildProfileUserAvatarData(
                                user.username,
                                user.discriminator.toInt(),
                                user.data.avatar,
                                guildAvatarHash
                            )
                        ).jsonObject,
                        now,
                        now + 15.minutes // Expires after 15m
                    )

                    interactiveButton(
                        ButtonStyle.Primary,
                        SwitchToGuildProfileAvatarExecutor,
                        ComponentDataUtils.encode(
                            UserDataUtils.SwitchAvatarInteractionIdData(
                                context.user.id,
                                user.id,
                                (context as? GuildApplicationCommandContext)?.guildId,
                                MessageTargetType.SEND_MESSAGE_EPHEMERAL,
                                localInteractionId
                            )
                        )
                    ) {
                        label = context.i18nContext.get(I18nKeysData.Innercommands.Innercommand.Inneruser.Inneravatar.ViewUserGuildProfileAvatar)
                    }
                }
            }

            if (member != null) {
                actionRow {
                    val memberPermissionsInteractionId = loritta.pudding.interactionsData.insertInteractionData(
                        Json.encodeToJsonElement(
                            GuildMemberPermissionsData(
                                member.roleIds,
                                member.getPermissions(), // TODO: FIX THIS! GET THE PERMISSIONS FROM THE INTERACTION THEMSELVES!! However Kord Core doesn't expose this field... yet
                                topRoleForColor?.color?.rgb
                            )
                        ).jsonObject,
                        now,
                        now + 15.minutes // Expires after 15m
                    )

                    interactiveButton(
                        ButtonStyle.Primary,
                        ShowGuildMemberPermissionsExecutor,
                        ComponentDataUtils.encode(
                            StoredGenericInteractionData(
                                ComponentDataUtils.KTX_SERIALIZATION_SIMILAR_PROTOBUF_STRUCTURE_ISSUES_WORKAROUND_DUMMY,
                                memberPermissionsInteractionId
                            )
                        )
                    ) {
                        label = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.MemberPermissions)
                    }
                }
            }

            val inviteUrl = applicationInfo?.customInstallUrl ?: if (applicationInfo?.installParams != null) {
                "https://discord.com/api/oauth2/authorize?client_id=${applicationInfo.id}&scope=${applicationInfo.installParams.scopes.joinToString("+")}&permissions=${applicationInfo.installParams.permissions.code}"
            } else null

            val termsOfServiceUrl = applicationInfo?.termsOfServiceUrl
            val privacyPolicyUrl = applicationInfo?.privacyPolicyUrl

            if (inviteUrl != null || termsOfServiceUrl != null || privacyPolicyUrl != null) {
                actionRow {
                    if (applicationInfo?.botPublic == true) {
                        if (inviteUrl != null) {
                            linkButton(inviteUrl) {
                                label = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.AddToServer)
                            }
                        }
                    }

                    if (termsOfServiceUrl != null) {
                        linkButton(termsOfServiceUrl) {
                            label = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.TermsOfService)
                        }
                    }

                    if (privacyPolicyUrl != null) {
                        linkButton(privacyPolicyUrl) {
                            label = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.PrivacyPolicy)
                        }
                    }
                }
            }
        }

        if (isEphemeral) {
            context.sendEphemeralMessage {
                message()
            }
        } else {
            context.sendMessage {
                message()
            }
        }
    }

    private fun fancify(bool: Boolean) =
        if (bool)
            Emotes.CheckMark
        else
            Emotes.Error

    suspend fun getApplicationInfo(snowflake: Snowflake): ApplicationInfo? {
        val applicationInfoResponse = http.get("https://discord.com/api/v10/applications/$snowflake/rpc")

        if (applicationInfoResponse.status != HttpStatusCode.OK)
            return null

        return JsonIgnoreUnknownKeys.decodeFromString(applicationInfoResponse.bodyAsText())
    }

    @kotlinx.serialization.Serializable
    data class ApplicationInfo(
        val id: Snowflake,
        val name: String,
        val icon: String? = null,
        val description: String,
        val type: Int?,
        @SerialName("cover_image")
        val coverImage: String? = null,
        @SerialName("primary_sku_id")
        val primarySkuId: String? = null,
        val slug: String? = null,
        @SerialName("guild_id")
        val guildId: Snowflake? = null,
        @SerialName("bot_public")
        val botPublic: Boolean,
        @SerialName("bot_require_code_grant")
        val botRequireCodeGrant: Boolean,
        @SerialName("custom_install_url")
        val customInstallUrl: String? = null,
        @SerialName("install_params")
        val installParams: InstallParams? = null,
        @SerialName("verify_key")
        val verifyKey: String,
        @SerialName("terms_of_service_url")
        val termsOfServiceUrl: String? = null,
        @SerialName("privacy_policy_url")
        val privacyPolicyUrl: String? = null,
        val flags: Long,
        val tags: List<String>? = null
    )

    @kotlinx.serialization.Serializable
    data class InstallParams(
        val scopes: List<String>,
        val permissions: Permissions
    )
}