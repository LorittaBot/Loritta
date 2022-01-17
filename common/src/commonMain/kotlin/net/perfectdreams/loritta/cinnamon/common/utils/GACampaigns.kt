package net.perfectdreams.loritta.cinnamon.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

/**
 * Google Analytics' campaigns to track where users are coming from
 */
object GACampaigns {
    fun sonhosBundlesUpsellDiscordMessage(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): StringI18nData {
        return I18nKeysData.Commands.WantingMoreSonhosBundlesUpsell(sonhosBundlesUpsellDiscordMessageUrl(lorittaWebsiteUrl, medium, campaignContent))
    }

    fun sonhosBundlesUpsellDiscordMessageUrl(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return "<${sonhosBundlesUpsellUrl(lorittaWebsiteUrl, "discord", medium, "sonhos-bundles-upsell", campaignContent)}>"
    }

    fun sonhosBundlesUpsellUrl(
        lorittaWebsiteUrl: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ): String {
        return "${lorittaWebsiteUrl}user/@me/dashboard/bundles?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }

    fun premiumUpsellDiscordMessageUrl(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return "<${sonhosBundlesUpsellUrl(lorittaWebsiteUrl, "discord", medium, "premium-upsell", campaignContent)}>"
    }

    fun premiumUpsellUrl(
        lorittaWebsiteUrl: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ): String {
        return "${lorittaWebsiteUrl}premium?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }
}