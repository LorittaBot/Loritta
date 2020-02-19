package net.perfectdreams.loritta.website.utils.config.types

object ConfigTransformers {
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
            DonationConfigTransformer
    )
}