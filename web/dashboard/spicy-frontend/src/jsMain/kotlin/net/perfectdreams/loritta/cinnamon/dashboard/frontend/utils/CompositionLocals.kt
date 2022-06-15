package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.compositionLocalOf
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSpicyInfoResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse

val LocalI18nContext = compositionLocalOf<I18nContext> { error("i18nContext isn't loaded yet!") }
val LocalUserIdentification = compositionLocalOf<GetUserIdentificationResponse> { error("User Identification isn't loaded yet!") }
val LocalSpicyInfo = compositionLocalOf<GetSpicyInfoResponse> { error("SpicyInfo isn't loaded yet!") }