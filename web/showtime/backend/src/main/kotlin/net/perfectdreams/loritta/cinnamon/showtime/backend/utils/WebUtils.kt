package net.perfectdreams.loritta.cinnamon.showtime.backend.utils

import kotlinx.html.DIV
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.IMG
import kotlinx.html.div
import kotlinx.html.fieldSet
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.legend
import kotlinx.html.style
import net.perfectdreams.etherealgambi.data.DefaultImageVariantPreset
import net.perfectdreams.etherealgambi.data.ScaleDownToWidthImageVariantPreset
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend

fun FlowOrInteractiveOrPhrasingContent.imgSrcSetFromEtherealGambi(m: ShowtimeBackend, preloadedImageInfo: EtherealGambiImages.PreloadedImageInfo, extension: String, sizes: String, block: IMG.() -> Unit = {}) {
    val variantInfo = preloadedImageInfo.imageInfo

    val defaultVariant = variantInfo.variants.first { it.preset is DefaultImageVariantPreset }
    val scaleDownVariants = variantInfo.variants.filter { it.preset is ScaleDownToWidthImageVariantPreset }

    val imageUrls = (
            scaleDownVariants.map {
                "${m.etherealGambiClient.baseUrl}/${it.urlWithoutExtension}.$extension ${(it.preset as ScaleDownToWidthImageVariantPreset).width}w"
            } + "${m.etherealGambiClient.baseUrl}/${defaultVariant.urlWithoutExtension}.$extension ${variantInfo.imageInfo.width}w"
            ).joinToString(", ")

    imgSrcSet(
        "${m.etherealGambiClient.baseUrl}/${defaultVariant.urlWithoutExtension.removePrefix("/")}.$extension",
        sizes,
        imageUrls
    ) {
        block()
        style += "aspect-ratio: ${variantInfo.imageInfo.width}/${variantInfo.imageInfo.height}"
    }
}


fun FlowOrInteractiveOrPhrasingContent.imgSrcSetFromEtherealGambi(m: ShowtimeBackend, variantInfo: ImageVariantsResponse, extension: String, sizes: String, block: IMG.() -> Unit = {}) {
    val defaultVariant = variantInfo.variants.first { it.preset is DefaultImageVariantPreset }
    val scaleDownVariants = variantInfo.variants.filter { it.preset is ScaleDownToWidthImageVariantPreset }

    val imageUrls = (
            scaleDownVariants.map {
                "${m.etherealGambiClient.baseUrl}/${it.urlWithoutExtension}.$extension ${(it.preset as ScaleDownToWidthImageVariantPreset).width}w"
            } + "${m.etherealGambiClient.baseUrl}/${defaultVariant.urlWithoutExtension}.$extension ${variantInfo.imageInfo.width}w"
            ).joinToString(", ")

    imgSrcSet(
        "${m.etherealGambiClient.baseUrl}/${defaultVariant.urlWithoutExtension}.$extension",
        sizes,
        imageUrls
    ) {
        block()
        style += "aspect-ratio: ${variantInfo.imageInfo.width}/${variantInfo.imageInfo.height}"
    }
}

// Generates Image Sets
fun DIV.imgSrcSet(path: String, fileName: String, sizes: String, max: Int, min: Int, stepInt: Int, block : IMG.() -> Unit = {}) {
    val srcsets = mutableListOf<String>()
    val split = fileName.split(".")
    val ext = split.last()

    for (i in (max - stepInt) downTo min step stepInt) {
        // "${websiteUrl}$versionPrefix/assets/img/home/lori_gabi.png 1178w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_1078w.png 1078w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_978w.png 978w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_878w.png 878w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_778w.png 778w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_678w.png 678w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_578w.png 578w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_478w.png 478w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_378w.png 378w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_278w.png 278w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_178w.png 178w"
        srcsets.add("$path${split.first()}_${i}w.$ext ${i}w")
    }

    srcsets.add("$path$fileName ${max}w")

    imgSrcSet(
        "$path$fileName",
        sizes,
        srcsets.joinToString(", "),
        block
    )
}

fun FlowOrInteractiveOrPhrasingContent.imgSrcSet(filePath: String, sizes: String, srcset: String, block : IMG.() -> Unit = {})  {
    img(src = filePath) {
        style = "width: auto;"
        attributes["sizes"] = sizes
        attributes["srcset"] = srcset

        this.apply(block)
    }
}

fun DIV.adWrapper(svgIconManager: SVGIconManager, callback: DIV.() -> Unit) {
    // Wraps the div in a nice wrapper
    div {
        style = "text-align: center;"
        fieldSet {
            style = "display: inline;\n" +
                    "border: 2px solid rgba(0,0,0,.05);\n" +
                    "border-radius: 7px;\n" +
                    "color: rgba(0,0,0,.3);"

            legend {
                style = "margin-left: auto;"
                svgIconManager.ad.apply(this)
            }

            div {
                callback.invoke(this)
            }
        }
    }
}

fun DIV.mediaWithContentWrapper(
    mediaOnTheRightSide: Boolean,
    mediaFigure: DIV.() -> (Unit),
    mediaBody: DIV.() -> (Unit),
) {
    div(classes = "media") {
        if (mediaOnTheRightSide) {
            div(classes = "media-body") {
                mediaBody.invoke(this)
            }

            div(classes = "media-figure") {
                mediaFigure.invoke(this)
            }
        } else {
            div(classes = "media-figure") {
                mediaFigure.invoke(this)
            }

            div(classes = "media-body") {
                mediaBody.invoke(this)
            }
        }
    }
}

fun DIV.innerContent(block: DIV.() -> (Unit)) = div {
    id = "inner-content"

    div(classes = "background-overlay") {}

    block.invoke(this)
}

/* fun DIV.generateNitroPayAdOrSponsor(sponsorId: Int, adSlot: String, adName: String? = null, callback: (NitroPayAdDisplay) -> (Boolean)) {
    // TODO: Fix
    val sponsors = listOf<Sponsor>() // loritta.sponsors
    val sponsor = sponsors.getOrNull(sponsorId)

    if (sponsor != null) {
        generateSponsor(sponsor)
    } else {
        generateNitroPayAdOrSponsor(sponsorId, "$adSlot-desktop", NitroPayAdDisplay.DESKTOP, "Loritta Daily Reward", callback.invoke(
            NitroPayAdDisplay.DESKTOP))
        generateNitroPayAdOrSponsor(sponsorId, "$adSlot-phone", NitroPayAdDisplay.PHONE, "Loritta Daily Reward", callback.invoke(
            NitroPayAdDisplay.PHONE))
        generateNitroPayAdOrSponsor(sponsorId, "$adSlot-tablet", NitroPayAdDisplay.TABLET, "Loritta Daily Reward", callback.invoke(
            NitroPayAdDisplay.TABLET))
    }
}

fun DIV.generateNitroPayAd(adSlot: String, adName: String? = null) {
    generateNitroPayAd("$adSlot-desktop", NitroPayAdDisplay.DESKTOP, "Loritta Daily Reward")
    generateNitroPayAd("$adSlot-phone", NitroPayAdDisplay.PHONE, "Loritta Daily Reward")
    generateNitroPayAd("$adSlot-tablet", NitroPayAdDisplay.TABLET, "Loritta Daily Reward")
}

// TODO: Fix
fun DIV.generateNitroPayAdOrSponsor(sponsorId: Int, adSlot: String, displayType: NitroPayAdDisplay, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true)
        = generateNitroPayAdOrSponsor(listOf<Sponsor>(), sponsorId, adSlot, displayType, adName, showIfSponsorIsMissing)

fun DIV.generateNitroPayAdOrSponsor(sponsors: List<Sponsor>, sponsorId: Int, adSlot: String, displayType: NitroPayAdDisplay, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true) {
    val sponsor = sponsors.getOrNull(sponsorId)

    if (sponsor != null) {
        generateSponsor(sponsor)
    } else if (showIfSponsorIsMissing) {
        generateNitroPayAd(adSlot, displayType, adName)
    }
}

// TODO: Fix
fun DIV.generateNitroPayVideoAdOrSponsor(sponsorId: Int, adSlot: String, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true)
        = generateNitroPayVideoAdOrSponsor(listOf(), sponsorId, adSlot, adName, showIfSponsorIsMissing)

fun DIV.generateNitroPayVideoAdOrSponsor(sponsors: List<Sponsor>, sponsorId: Int, adSlot: String, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true) {
    val sponsor = sponsors.getOrNull(sponsorId)

    if (sponsor != null) {
        generateSponsor(sponsor)
    } else if (showIfSponsorIsMissing) {
        generateNitroPayVideoAd(adSlot, adName)
    }
}

fun DIV.generateAdOrSponsor(sponsorId: Int, adSlot: String, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true)
        = generateAdOrSponsor(listOf(), sponsorId, adSlot, adName, showIfSponsorIsMissing)

fun DIV.generateAdOrSponsor(sponsors: List<Sponsor>, sponsorId: Int, adSlot: String, adName: String? = null, showIfSponsorIsMissing: Boolean = true, showOnMobile: Boolean = true) {
    val sponsor = sponsors.getOrNull(sponsorId)

    if (sponsor != null) {
        generateSponsor(sponsor)
    } else if (showIfSponsorIsMissing) {
        generateAd(adSlot, adName, showOnMobile)
    }
}

fun DIV.generateSponsor(sponsor: Sponsor) {
    div(classes = "media") {
        generateSponsorNoWrap(sponsor)
    }
}

fun DIV.generateSponsorNoWrap(sponsor: Sponsor) {
    a(href = "/sponsor/${sponsor.slug}", classes = "sponsor-wrapper", target = "_blank") {
        div(classes = "sponsor-pc-image") {
            img(src = sponsor.getRectangularBannerUrl(), classes = "sponsor-banner")
        }
        div(classes = "sponsor-mobile-image") {
            img(src = sponsor.getSquareBannerUrl(), classes = "sponsor-banner")
        }
    }
}

fun DIV.generateHowToSponsorButton(locale: BaseLocale) {
    div(classes = "media") {
        style = "justify-content: end;"
        div {
            style = "font-size: 0.8em; margin: 8px;"
            + (locale["website.sponsors.wantYourServerHere"] + " ")
            a(href = "/sponsors") {
                span(classes = "sponsor-button") {
                    + "Premium Slots"
                }
            }
        }
    }
}

fun DIV.generateAd(adSlot: String, adName: String? = null, showOnMobile: Boolean = true) {
    // O "adName" não é utilizado para nada, só está aí para que fique mais fácil de analisar aonde está cada ad (caso seja necessário)
    // TODO: Random ID
    val adGen = "ad-$adSlot-"

    div(classes = "centralized-ad") {
        ins(classes = "adsbygoogle") {
            style = "display: block;"

            if (!showOnMobile)
                attributes["id"] = adGen

            attributes["data-ad-client"] = "ca-pub-9989170954243288"
            attributes["data-ad-slot"] = adSlot
            attributes["data-ad-format"] = "auto"
            attributes["data-full-width-responsive"] = "true"
        }
    }

    if (!showOnMobile) {
        script(type = ScriptType.textJavaScript) {
            unsafe {
                raw("if (document.body.clientWidth >= 1366) { (adsbygoogle = window.adsbygoogle || []).push({}); } else { console.log(\"Not displaying ad: Browser width is too smol!\"); document.querySelector(\"#$adGen\").remove(); }")
            }
        }
    } else {
        script(type = ScriptType.textJavaScript) {
            unsafe {
                raw("(adsbygoogle = window.adsbygoogle || []).push({});")
            }
        }
    }
}

fun DIV.generateNitroPayAd(adId: String, displayType: NitroPayAdDisplay, adName: String? = null) {
    // O "adName" não é utilizado para nada, só está aí para que fique mais fácil de analisar aonde está cada ad (caso seja necessário)
    div(classes = "centralized-ad") {
        div(classes = "nitropay-ad") {
            id = adId
            attributes["data-nitropay-ad-type"] = NitroPayAdType.STANDARD_BANNER.name.lowercase()
            attributes["data-nitropay-ad-display"] = displayType.name.lowercase()
        }
    }
}

fun DIV.generateNitroPayVideoAd(adId: String, adName: String? = null) {
    // O "adName" não é utilizado para nada, só está aí para que fique mais fácil de analisar aonde está cada ad (caso seja necessário)
    div(classes = "centralized-ad") {
        div(classes = "nitropay-ad") {
            id = adId
            attributes["data-nitropay-ad-type"] = NitroPayAdType.VIDEO_PLAYER.name.lowercase()
            attributes["data-nitropay-ad-display"] = NitroPayAdDisplay.RESPONSIVE.name.lowercase()
        }
    }
} */