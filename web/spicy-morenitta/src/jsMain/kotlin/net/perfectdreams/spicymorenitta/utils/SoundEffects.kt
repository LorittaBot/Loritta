package net.perfectdreams.spicymorenitta.utils

import kotlinx.browser.window
import net.perfectdreams.spicymorenitta.SpicyMorenitta

class SoundEffects(val m: SpicyMorenitta) {
    val configSaved = LazySoundEffect(m, "${window.location.origin}/v3/assets/v3/snd/config-saved.ogg")
    val configError = LazySoundEffect(m, "${window.location.origin}/v3/assets/snd/config-error.ogg")
    val toastNotificationWhoosh = LazySoundEffect(m, "${window.location.origin}/v3/assets/snd/toast-notification-whoosh.ogg")
}