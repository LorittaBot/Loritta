package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.compositionLocalOf
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

val LocalI18nContext = compositionLocalOf<I18nContext> { error("i18nContext isn't loaded yet!") }
val LocalUserIdentification = compositionLocalOf<GetUserIdentificationResponse> { error("User Identification isn't loaded yet!") }
val LocalSpicyInfo = compositionLocalOf<LorittaDashboardRPCResponse.GetSpicyInfoResponse.Success> { error("SpicyInfo isn't loaded yet!") }