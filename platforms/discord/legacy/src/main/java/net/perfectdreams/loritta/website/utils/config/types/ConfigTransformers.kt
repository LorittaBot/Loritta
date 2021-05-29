package net.perfectdreams.loritta.website.utils.config.types

import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin

object ConfigTransformers {
    val ALL_TRANSFORMERS: List<ConfigTransformer>
        get() = loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.configTransformers } + DEFAULT_TRANSFORMERS

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