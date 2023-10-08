package net.perfectdreams.loritta.cinnamon.discord.utils.correios.exceptions

import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient

class InvalidTrackingIdException(val packageId: String) : IllegalArgumentException() {
    override val message = "Package \"$packageId\" is not a valid tracking ID because it doesn't match the \"${CorreiosClient.CORREIOS_PACKAGE_REGEX}\" RegRx"
}