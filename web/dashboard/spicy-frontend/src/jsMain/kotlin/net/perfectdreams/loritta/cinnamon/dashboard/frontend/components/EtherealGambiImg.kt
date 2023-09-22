package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.EtherealGambiUtils
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.HTMLImageElement

@Composable
fun EtherealGambiImg(src: String, alt: String = "", sizes: String, attrs: AttrBuilderContext<HTMLImageElement>? = null) = Img(
    src,
    alt
) {
    // This is not "correct", as in: The right way is to load the data by requesting EtherealGambi what image variations are present
    // But whatever, this ain't a big deal
    val srcsets = mutableListOf<String>()
    val splitted = src.split(".")
    val extension = splitted.last()
    val everythingBeforeExtension = splitted.dropLast(1)
    val everythingBeforeExtensionAsPath = everythingBeforeExtension.joinToString(".")

    for (variant in EtherealGambiUtils.scaleDownToWidthVariantsPresets) {
        // "${websiteUrl}$versionPrefix/assets/img/home/lori_gabi.png 1178w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_1078w.png 1078w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_978w.png 978w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_878w.png 878w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_778w.png 778w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_678w.png 678w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_578w.png 578w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_478w.png 478w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_378w.png 378w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_278w.png 278w, ${websiteUrl}$versionPrefix/assets/img/home/lori_gabi_178w.png 178w"
        srcsets.add("${everythingBeforeExtensionAsPath}@${variant.name}.$extension ${variant.width}w")
    }

    // srcsets.add("$path$fileName ${max}w")

    attr("sizes", sizes)
    attr("srcset", srcsets.joinToString(", "))

    attrs?.invoke(this)
}