package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import kotlinx.html.IMG
import kotlinx.html.classes
import kotlinx.html.img

object EtherealGambiUtils {
    val scaleDownToWidthVariantsPresets = listOf(
        ScaleDownToWidthImageVariantPreset("32w", 32),
        ScaleDownToWidthImageVariantPreset("64w", 64),
        ScaleDownToWidthImageVariantPreset("160w", 160),
        ScaleDownToWidthImageVariantPreset("320w", 320),
        ScaleDownToWidthImageVariantPreset("640w", 640),
        ScaleDownToWidthImageVariantPreset("960w", 960),
        ScaleDownToWidthImageVariantPreset("1280w", 1280),
        ScaleDownToWidthImageVariantPreset("1920w", 1920),
        ScaleDownToWidthImageVariantPreset("2560w", 2560)
    )

    fun FlowContent.etherealGambiImg(src: String, classes: String? = null, alt: String = "", sizes: String, block: IMG.() -> (Unit)) = img {
        if (classes != null)
            this.classes = classes.split(" ").toSet()

        // This is not "correct", as in: The right way is to load the data by requesting EtherealGambi what image variations are present
        // But whatever, this ain't a big deal
        val srcsets = mutableListOf<String>()
        val splitted = src.split(".")
        val extension = splitted.last()
        val everythingBeforeExtension = splitted.dropLast(1)
        val everythingBeforeExtensionAsPath = everythingBeforeExtension.joinToString(".")

        for (variant in scaleDownToWidthVariantsPresets) {
            // "${websiteUrl}$versionPrefix/assets/img/home/lori_gabi.png 1178w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_1078w.png 1078w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_978w.png 978w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_878w.png 878w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_778w.png 778w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_678w.png 678w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_578w.png 578w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_478w.png 478w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_378w.png 378w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_278w.png 278w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_178w.png 178w"
            srcsets.add("${everythingBeforeExtensionAsPath}@${variant.name}.$extension ${variant.width}w")
        }

        // srcsets.add("$path$fileName ${max}w")

        this.src = src
        this.alt = alt

        attributes["sizes"] = sizes
        attributes["srcset"] = srcsets.joinToString(", ")

        block.invoke(this)
    }

    data class ScaleDownToWidthImageVariantPreset(
        val name: String,
        val width: Int
    ) {
        fun variantWithPrefix() = "@$name"
    }
}