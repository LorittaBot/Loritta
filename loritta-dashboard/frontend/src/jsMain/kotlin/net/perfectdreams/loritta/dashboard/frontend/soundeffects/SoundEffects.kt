package net.perfectdreams.loritta.dashboard.frontend.soundeffects

import kotlinx.browser.window
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend

class SoundEffects(val m: LorittaDashboardFrontend) {
    val configSaved = LazySoundEffect(m, "${window.location.origin}/assets/sounds/config-saved.ogg")
    val toastNotificationWhoosh = LazySoundEffect(m, "${window.location.origin}/assets/sounds/toast-notification-whoosh.ogg")
}