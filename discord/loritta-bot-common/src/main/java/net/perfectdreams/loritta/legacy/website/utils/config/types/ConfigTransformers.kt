package net.perfectdreams.loritta.legacy.website.utils.config.types

object ConfigTransformers {
    val ALL_TRANSFORMERS: List<ConfigTransformer>
        get() = DEFAULT_TRANSFORMERS

    val DEFAULT_TRANSFORMERS = listOf(
            YouTubeConfigTransformer,
            TwitchConfigTransformer,
            TwitterConfigTransformer,
            TextChannelsTransformer,
            UserDonationKeysTransformer,
            ActiveDonationKeysTransformer,
            GuildInfoTransformer,
            GeneralConfigTransformer,
            LevelUpConfigTransformer,
            RolesTransformer,
            DonationConfigTransformer,
            AutoroleConfigTransformer,
            WelcomerConfigTransformer,
            MemberCountersTransformer,
            ModerationConfigTransformer,
            CustomCommandsConfigTransformer
    )
}