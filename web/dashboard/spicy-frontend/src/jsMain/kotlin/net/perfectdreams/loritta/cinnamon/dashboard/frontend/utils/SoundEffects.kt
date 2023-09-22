package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import kotlinx.browser.window
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

class SoundEffects(val m: LorittaDashboardFrontend) {
    val configSaved = LazySoundEffect(m, "${window.location.origin}/assets/snd/config-saved.ogg")
    val configError = LazySoundEffect(m, "${window.location.origin}/assets/snd/config-error.ogg")
    val toastNotificationWhoosh = LazySoundEffect(m, "${window.location.origin}/assets/snd/toast-notification-whoosh.ogg")
    val whoosh = LazySoundEffect(m, "${window.location.origin}/assets/snd/whoosh.ogg")
    val error = LazySoundEffect(m, "${window.location.origin}/assets/snd/error.ogg")
}