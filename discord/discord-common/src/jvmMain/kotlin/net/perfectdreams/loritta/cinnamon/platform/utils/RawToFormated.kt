package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.GuildFeature
import dev.kord.common.entity.Permission
import dev.kord.common.entity.UserFlag
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeys

object RawToFormated {
    @JvmStatic
    val flagsRepository = hashMapOf(
        UserFlag.DiscordEmployee to Emotes.DiscordEmployee,
        UserFlag.DiscordPartner to Emotes.DiscordPartner,
        UserFlag.HypeSquad to Emotes.HypeSquad,
        UserFlag.BugHunterLevel1 to Emotes.BugHunterLevel1,
        UserFlag.HouseBravery to Emotes.HouseBravery,
        UserFlag.HouseBrilliance to Emotes.HouseBrilliance,
        UserFlag.HouseBalance to Emotes.HouseBalance,
        UserFlag.EarlySupporter to Emotes.EarlySupporter,
        UserFlag.TeamUser to Emotes.MissingEmote,
        UserFlag.System to Emotes.MissingEmote,
        UserFlag.BugHunterLevel2 to Emotes.BugHunterLevel2,
        UserFlag.VerifiedBot to Emotes.MissingEmote,
        UserFlag.VerifiedBotDeveloper to Emotes.VerifiedBotDeveloper
    ).toMutableMap()

    @JvmStatic
    val permissionsRepository = hashMapOf(
        Permission.CreateInstantInvite to I18nKeys.Permissions.CreateInstantInvite,
        Permission.KickMembers to I18nKeys.Permissions.KickMembers,
        Permission.BanMembers to I18nKeys.Permissions.BanMembers,
        Permission.Administrator to I18nKeys.Permissions.Administrator,
        Permission.ManageChannels to I18nKeys.Permissions.ManageChannels,
        Permission.ManageGuild to I18nKeys.Permissions.ManageGuild,
        Permission.AddReactions to I18nKeys.Permissions.AddReactions,
        Permission.ViewAuditLog to I18nKeys.Permissions.ViewAuditLog,
        Permission.ViewChannel to I18nKeys.Permissions.ViewChannel,
        Permission.SendMessages to I18nKeys.Permissions.SendMessages,
        Permission.SendTTSMessages to I18nKeys.Permissions.SendTTSMessages,
        Permission.ManageMessages to I18nKeys.Permissions.ManageMessages,
        Permission.EmbedLinks to I18nKeys.Permissions.EmbedLinks,
        Permission.AttachFiles to I18nKeys.Permissions.AttachFiles,
        Permission.ReadMessageHistory to I18nKeys.Permissions.ReadMessageHistory,
        Permission.MentionEveryone to I18nKeys.Permissions.MentionEveryone,
        Permission.UseExternalEmojis to I18nKeys.Permissions.UseExternalEmojis,
        Permission.ViewGuildInsights to I18nKeys.Permissions.ViewGuildInsights,
        Permission.Connect to I18nKeys.Permissions.Connect,
        Permission.Speak to I18nKeys.Permissions.Speak,
        Permission.MuteMembers to I18nKeys.Permissions.MuteMembers,
        Permission.DeafenMembers to I18nKeys.Permissions.DeafenMembers,
        Permission.MoveMembers to I18nKeys.Permissions.MoveMembers,
        Permission.UseVAD to I18nKeys.Permissions.UseVAD,
        Permission.PrioritySpeaker to I18nKeys.Permissions.PrioritySpeaker,
        Permission.ChangeNickname to I18nKeys.Permissions.ChangeNickname,
        Permission.ManageNicknames to I18nKeys.Permissions.ManageNicknames,
        Permission.ManageRoles to I18nKeys.Permissions.ManageRoles,
        Permission.ManageWebhooks to I18nKeys.Permissions.ManageWebhooks,
        Permission.ManageEmojis to I18nKeys.Permissions.ManageEmojis,
        Permission.UseSlashCommands to I18nKeys.Permissions.UseSlashCommands,
        Permission.RequestToSpeak to I18nKeys.Permissions.RequestToSpeak,
        Permission.ManageThreads to I18nKeys.Permissions.ManageThreads,
        Permission.CreatePublicThreads to I18nKeys.Permissions.CreatePublicThreads,
        Permission.CreatePrivateThreads to I18nKeys.Permissions.CreatePrivateThreads,
        Permission.SendMessagesInThreads to I18nKeys.Permissions.SendMessagesInThreads
    ).toMutableMap()

    @JvmStatic
    val featuresRepository = hashMapOf(
        GuildFeature.InviteSplash to I18nKeys.GuildFeatures.InviteSplash,
        GuildFeature.VIPRegions to I18nKeys.GuildFeatures.VipRegions,
        GuildFeature.VanityUrl to I18nKeys.GuildFeatures.VanityUrl,
        GuildFeature.Verified to I18nKeys.GuildFeatures.Verified,
        GuildFeature.Partnered to I18nKeys.GuildFeatures.Partnered,
        GuildFeature.Community to I18nKeys.GuildFeatures.Community,
        GuildFeature.Commerce to I18nKeys.GuildFeatures.Commerce,
        GuildFeature.News to I18nKeys.GuildFeatures.News,
        GuildFeature.Discoverable to I18nKeys.GuildFeatures.Discoverable,
        GuildFeature.Featurable to I18nKeys.GuildFeatures.Featurable,
        GuildFeature.AnimatedIcon to I18nKeys.GuildFeatures.AnimatedIcon,
        GuildFeature.Banner to I18nKeys.GuildFeatures.Banner,
        GuildFeature.WelcomeScreenEnabled to I18nKeys.GuildFeatures.WelcomeScreenEnabled,
        GuildFeature.TicketedEventsEnabled to I18nKeys.GuildFeatures.TicketedEventsEnabled,
        GuildFeature.MonetizationEnabled to I18nKeys.GuildFeatures.MonetizationEnabled,
        GuildFeature.MoreStickers to I18nKeys.GuildFeatures.MoreStickers,
        GuildFeature.ThreeDayThreadArchive to I18nKeys.GuildFeatures.ThreeDayThreadArchive,
        GuildFeature.SevenDayThreadArchive to I18nKeys.GuildFeatures.SevenDayThreadArchive,
        GuildFeature.PrivateThreads to I18nKeys.GuildFeatures.PrivateThreads
    ).toMutableMap()

    fun List<UserFlag>.toEmotes() = mapNotNull {
        flagsRepository[it]?.asMention
    }.ifEmpty { null }

    fun Set<Permission>.toLocalized() = mapNotNull {
        permissionsRepository[it]
    }.ifEmpty { null }

    fun List<GuildFeature>.toLocalized() = mapNotNull {
        featuresRepository[it]
    }.ifEmpty { null }

    fun Boolean.toLocalized() =
        if (this) I18nKeys.Common.FancyBoolean.True
        else I18nKeys.Common.FancyBoolean.False
}