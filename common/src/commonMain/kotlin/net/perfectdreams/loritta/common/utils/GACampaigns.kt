package net.perfectdreams.loritta.common.utils

import io.ktor.http.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData

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
        return "${lorittaWebsiteUrl}dashboard/sonhos-shop?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }

    fun premiumUpsellDiscordMessageUrl(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return "<${premiumUpsellDiscordCampaignUrl(lorittaWebsiteUrl, medium, campaignContent)}>"
    }

    fun premiumUpsellDiscordCampaignUrl(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return premiumUrl(lorittaWebsiteUrl, "discord", medium, "premium-upsell", campaignContent)
    }

    fun premiumUrl(
        lorittaWebsiteUrl: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ): String {
        return "${lorittaWebsiteUrl}donate?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }

    fun dailyWebRewardDiscordCampaignUrl(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return dailyUrl(lorittaWebsiteUrl, "discord", medium, "daily-web-reward", campaignContent)
    }

    fun dailyUrl(
        lorittaWebsiteUrl: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ): String {
        return "${lorittaWebsiteUrl}daily?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }

    fun patchNotesUrl(
        lorittaWebsiteUrl: String,
        websiteLocaleId: String,
        path: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ): String {
        return "${lorittaWebsiteUrl}$websiteLocaleId$path?utm_source=$source&utm_medium=$medium&utm_campaign=$campaignName&utm_content=$campaignContent"
    }

    fun createUrlWithCampaign(
        url: String,
        source: String,
        medium: String,
        campaignName: String,
        campaignContent: String
    ) = URLBuilder(url)
        .apply {
            parameters.append("utm_source", source)
            parameters.append("utm_medium", medium)
            parameters.append("utm_campaign", campaignName)
            parameters.append("utm_content", campaignContent)
        }.build()
}