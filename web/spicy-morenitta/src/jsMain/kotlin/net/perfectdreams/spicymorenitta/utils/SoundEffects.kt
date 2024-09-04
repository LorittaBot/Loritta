package net.perfectdreams.spicymorenitta.utils

import kotlinx.browser.window
import net.perfectdreams.spicymorenitta.SpicyMorenitta

class SoundEffects(val m: SpicyMorenitta) {
    val configSaved = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/config-saved.ogg")
    val configError = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/config-error.ogg")
    val error = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/error.ogg")
    val toastNotificationWhoosh = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/toast-notification-whoosh.ogg")
    val recycleBin = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/windows-xp-recycle-bin.ogg")
    val xarolaRatinho = LazySoundEffect(m, "${window.location.origin}/lori-slippy/assets/snd/xarola-ratinho.ogg")
}