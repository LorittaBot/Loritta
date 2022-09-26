package net.perfectdreams.loritta.legacy.utils

/**
 * Google Analytics' campaigns to track where users are coming from
 *
 * This is from Loritta's Cinnamon Branch!
 *
 * The messages are hardcoded because I'm too lazy to add new strings + translate them + etc, they are fully translated on Loritta's Cinnamon branch, however.
 */
object GACampaigns {
    fun sonhosBundlesUpsellDiscordMessage(
        lorittaWebsiteUrl: String,
        medium: String,
        campaignContent: String
    ): String {
        return "Psiu, está querendo mais sonhos? Então compre na minha lojinha! Nós aceitamos pagamentos via boleto, cartão de crédito e Pix e comprando por lá você me ajuda a ficar online enquanto você se diverte com mais sonhos! Mas não se preocupe, a escolha é sua e você pode continuar a usar a Loritta sem se preocupar em tirar dinheiro do seu bolso. Ficou interessado? Então acesse! ${sonhosBundlesUpsellDiscordMessageUrl(lorittaWebsiteUrl, medium, campaignContent)}"
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