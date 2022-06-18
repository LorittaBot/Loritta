package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Ads
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import kotlin.random.Random

// just... ad
@Composable
fun Ad(ad: Ads.AdInfo) {
    val userInfo = LocalUserIdentification.current

    // Bye have a great time!
    if (!userInfo.displayAds)
        return

    val abTest = Random.nextBoolean()

    // If true, we show NitroPay ads
    // If false, we show Google AdSense ads
    if (abTest) {
        NitroPayAd(ad.nitroPayId, ad.size.width, ad.size.height)
    } else {
        GoogleAdSenseAd(ad.nitroPayId, ad.size.width, ad.size.height)
    }
}