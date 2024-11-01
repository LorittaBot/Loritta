package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.utils.ApplicationInfoUtils
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asPomeloOrLegacyTag
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.extensions.joinToStringLimitedByCharacters
import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded

object UserInfoExecutor {
    suspend fun createUserInfoMessage(
        context: UnleashedContext,
        userAndMember: UserAndMember,
    ): InlineMessage<*>.() -> (Unit) {
        val user = userAndMember.user
        val member = userAndMember.member

        val now = Clock.System.now()

        val flags = user.flags
        val flagsToEmotes = flags.mapNotNull {
            when (it) {
                User.UserFlag.STAFF -> Emotes.DiscordEmployee
                User.UserFlag.PARTNER -> Emotes.DiscordPartner
                User.UserFlag.HYPESQUAD -> Emotes.HypeSquad
                User.UserFlag.BUG_HUNTER_LEVEL_1 -> Emotes.BugHunterLevel1
                User.UserFlag.BUG_HUNTER_LEVEL_2 -> Emotes.BugHunterLevel2
                User.UserFlag.HYPESQUAD_BRAVERY -> Emotes.HouseBravery
                User.UserFlag.HYPESQUAD_BRILLIANCE -> Emotes.HouseBrilliance
                User.UserFlag.HYPESQUAD_BALANCE -> Emotes.HouseBalance
                User.UserFlag.EARLY_SUPPORTER -> Emotes.EarlySupporter
                // I don't know how we could represent this
                // UserFlag.TeamUser -> ???
                User.UserFlag.VERIFIED_DEVELOPER -> Emotes.VerifiedBotDeveloper
                User.UserFlag.CERTIFIED_MODERATOR -> Emotes.LoriCoffee
                else -> null
            }
        }

        // Discord's System User (643945264868098049) does not have the "System" flag, so we will add a special handling for it
        val isSystemUser = user.isSystem

        // Get application information
        val applicationInfo = if (user.isBot && !isSystemUser) {
            // It looks like system user do not have an application bound to it, so we will just ignore it
            ApplicationInfoUtils.getApplicationInfo(context.loritta.http, user.idLong)
        } else null

        val roles = if (member != null && !member.isDetached) {
            member.roles
        } else null

        val topRole = roles?.maxByOrNull { it.position }
        // Did you know that you can't have a fully black role on Discord? The color "0" is used for "not set"!
        val topRoleForColor = roles?.filter { it.color != null }?.maxByOrNull { it.position }

        // We only get shared servers for non-bot accounts, because bot accounts may be in a lot of shared servers with Loritta, which may cause a OutOfMemoryError
        val sharedServers = if (!user.isBot && (context.channel.idLong == 358774895850815488L || context.channel.idLong == 547119872568459284L)) {
            val sharedServersResults = context.loritta.lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id)
            val sharedServers = sharedServersResults.sortedByDescending {
                it["memberCount"].int
            }

            sharedServers
        } else null

        return {
            embed {
                author(context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InfoAboutTheUser))
                title = buildString {
                    if (user.isBot) {
                        append(
                            when {
                                isSystemUser -> Emotes.VerifiedSystemTag
                                User.UserFlag.VERIFIED_BOT in flags -> Emotes.VerifiedBotTag
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

                    append(user.globalName ?: user.name)
                }
                url = "https://discord.com/users/${user.id}"

                field(
                    "${Emotes.LoriId} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.DiscordId)}",
                    "`${user.id}`",
                    true
                )
                field(
                    "${Emotes.LoriLabel} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.DiscordTag)}",
                    "`${user.asPomeloOrLegacyTag}`",
                    true
                )
                field(
                    "${Emotes.LoriCalendar} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.User.AccountCreationDate)}",
                    DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(user.timeCreated),
                    true
                )

                thumbnail = user.effectiveAvatar.url
                color = LorittaColors.DiscordBlurple.rgb
            }

            if (member != null) {
                val communicationDisabledUntil = member.timeOutEnd

                embed {
                    author(context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InfoAboutTheMember))
                    title = member.nickname ?: user.globalName ?: user.name

                    field(
                        "${Emotes.LoriCalendar} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.AccountJoinDate)}",
                        DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(member.timeJoined),
                        true
                    )

                    if (communicationDisabledUntil != null) {
                        field(
                            "${Emotes.LoriBonk} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.TimedOutUntil)}",
                            buildString {
                                append(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(communicationDisabledUntil))

                                if (now > communicationDisabledUntil.toInstant().toKotlinInstant()) {
                                    append("\n")
                                    append("*(${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.TimeoutTip)})*")
                                }
                            },
                            true
                        )
                    }

                    val premiumSince = member.timeBoosted

                    if (premiumSince != null) {
                        field(
                            "${Emotes.LoriWow} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.BoostingSince)}",
                            DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(premiumSince),
                            true
                        )
                    }

                    if (topRole != null) {
                        field(
                            "${Emotes.LoriSunglasses} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.HighestRole)}",
                            "<@&${topRole.id}>",
                            true
                        )

                        color = topRoleForColor?.color?.rgb
                    }

                    field(
                        "${Emotes.LoriZap} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InterestingTidbits)}",
                        """${fancify(!member.isPending)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.CompletedMembershipScreening)}
                                |${fancify(communicationDisabledUntil != null && communicationDisabledUntil.toInstant().toKotlinInstant() >= now)} ${
                            context.i18nContext.get(
                                UserCommand.I18N_PREFIX.Info.Member.IsTimedOut
                            )
                        }
                            """.trimMargin(),
                        false
                    )

                    thumbnail = (member.avatar ?: user.effectiveAvatar).url
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

                        // Convert the long to a binary string
                        val applicationFlags = applicationInfo.flags.toInt()

                        val hasGatewayPresence = applicationFlags and (1 shl 12) != 0
                        val hasGatewayPresenceLimited = applicationFlags and (1 shl 13) != 0
                        val hasGuildMembers = applicationFlags and (1 shl 14) != 0
                        val hasGuildMembersLimited = applicationFlags and (1 shl 15) != 0
                        // val verificationPendingGuildLimit = applicationFlags and (1 shl 16) != 0
                        val hasGatewayMessageContent = applicationFlags and (1 shl 18) != 0
                        val hasGatewayMessageContentLimited = applicationFlags and (1 shl 19) != 0

                        // While it would be nice to show the "Bot is actually using the intent but it is in less than 100 servers", it is not that reliable.
                        // Loritta has the "hasGuildMembersLimited" intent, however she is in more than 100 guilds
                        field(
                            "${Emotes.LoriZap} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.InterestingTidbits)}",
                            """${fancify(applicationInfo.botPublic)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.Public)}
                                |${fancify(applicationInfo.botRequireCodeGrant)} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.RequiresOAuth2CodeGrant)}
                                |${fancify(User.UserFlag.BOT_HTTP_INTERACTIONS in flags)} ${context.i18nContext.get(
                                UserCommand.I18N_PREFIX.Info.Application.UsesInteractionsOverHttp)}
                                |${fancify(hasGatewayPresence || hasGatewayPresenceLimited)} ${
                                context.i18nContext.get(
                                    UserCommand.I18N_PREFIX.Info.Application.IntentGatewayPresences
                                )
                            }
                                |${fancify(hasGuildMembers || hasGuildMembersLimited)} ${
                                context.i18nContext.get(
                                    UserCommand.I18N_PREFIX.Info.Application.IntentGuildMembers
                                )
                            }
                                |${fancify(hasGatewayMessageContent || hasGatewayMessageContentLimited)} ${
                                context.i18nContext.get(
                                    UserCommand.I18N_PREFIX.Info.Application.IntentMessageContent
                                )
                            }
                            """.trimMargin(),
                            false
                        )

                        field(
                            "\uD83D\uDCBB ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.PublicKey)}",
                            "`${applicationInfo.verifyKey}`",
                            false
                        )

                        if (applicationInfo.icon != null)
                            thumbnail = "https://cdn.discordapp.com/app-icons/${applicationInfo.id}/${applicationInfo.icon}.png"

                        color = LorittaColors.DiscordOldBlurple.rgb
                    } else {
                        title = "${Emotes.Error} Whoops"
                        description = context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.NoMatchingApplicationFound)
                    }
                }
            }

            if (sharedServers != null) {
                embed {
                    var sharedServersFieldTitle = context.locale["commands.command.userinfo.sharedServers"]
                    sharedServersFieldTitle = "\uD83C\uDF0E $sharedServersFieldTitle (${sharedServers.size})"

                    title = sharedServersFieldTitle

                    description = sharedServers.joinToString(separator = ", ", transform = { "`${it["name"].string}` (${it["id"].string})" }).substringIfNeeded(0 until 4096)
                }
            }

            // ===[ VIEW AVATAR BUTTONS ]===
            run {
                val components = mutableListOf<ItemComponent>()

                components.add(
                    context.loritta.interactivityManager
                        .buttonForUser(
                            context.user.idLong,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGlobalAvatar)
                        ) {
                            it.reply(true) {
                                apply(
                                    UserAvatarExecutor.createAvatarMessage(
                                        context,
                                        userAndMember,
                                        UserAvatarExecutor.AvatarTarget.GLOBAL_AVATAR
                                    )
                                )
                            }
                        }
                )

                if (member?.avatarUrl != null) {
                    components.add(
                        context.loritta.interactivityManager
                            .buttonForUser(
                                context.user.idLong,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGuildProfileAvatar)
                            ) {
                                it.reply(true) {
                                    apply(
                                        UserAvatarExecutor.createAvatarMessage(
                                            context,
                                            userAndMember,
                                            UserAvatarExecutor.AvatarTarget.GUILD_AVATAR
                                        )
                                    )
                                }
                            }
                    )
                }

                actionRow(components)
            }

            if (member != null && roles != null) {
                actionRow(
                    context.loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.MemberPermissions)
                    ) {
                        it.reply(true) {
                            embed {
                                val permissionList = member.permissions.joinToString(
                                    ", "
                                ) { it.getLocalizedName(context.i18nContext) }

                                // We put the permission list on the description because it is VERY big and doesn't fit on a field
                                description = "${Emotes.LoriAngel} **${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.Permissions)}:** $permissionList"

                                field(
                                    "${Emotes.LoriSunglasses} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.Roles)}",
                                    roles.joinToStringLimitedByCharacters(", ", DiscordResourceLimits.Embed.Field.Value) { it.asMention }.ifBlank { "\u200b" },
                                    false
                                )

                                color = member.color?.rgb
                            }
                        }
                    }
                )
            }

            run {
                val inviteUrl = applicationInfo?.customInstallUrl ?: if (applicationInfo?.installParams != null) {
                    "https://discord.com/api/oauth2/authorize?client_id=${applicationInfo.id}&scope=${
                        applicationInfo.installParams.scopes.joinToString(
                            "+"
                        )
                    }&permissions=${applicationInfo.installParams.permissions}"
                } else null

                val termsOfServiceUrl = applicationInfo?.termsOfServiceUrl
                val privacyPolicyUrl = applicationInfo?.privacyPolicyUrl

                if (applicationInfo?.botPublic == true && (inviteUrl != null || termsOfServiceUrl != null || privacyPolicyUrl != null)) {
                    val components = mutableListOf<ItemComponent>()

                    if (inviteUrl != null) {
                        components.add(
                            Button.link(
                                inviteUrl,
                                context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.AddToServer)
                            )
                        )
                    }

                    if (termsOfServiceUrl != null) {
                        components.add(
                            Button.link(
                                termsOfServiceUrl,
                                context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.TermsOfService)
                            )
                        )
                    }

                    if (privacyPolicyUrl != null) {
                        components.add(
                            Button.link(
                                privacyPolicyUrl,
                                context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Application.PrivacyPolicy)
                            )
                        )
                    }

                    if (components.isNotEmpty()) {
                        actionRow(*components.toTypedArray())
                    }
                }
            }
        }
    }

    private fun fancify(bool: Boolean) =
        if (bool)
            Emotes.CheckMark
        else
            Emotes.Error
}