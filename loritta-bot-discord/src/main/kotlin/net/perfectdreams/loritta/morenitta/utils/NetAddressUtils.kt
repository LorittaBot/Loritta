package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot

object NetAddressUtils {
	fun getWithPortIfMissing(address: String, defaultPort: Int): String {
		if (address.contains(":"))
			return address

		return "$address:$defaultPort"
	}

	/**
	 * Changes the IP if the connecting IP is the same IP as the external machine IP
	 *
	 * The syntax is {255.255.255.255/10.0.0.0}:1234
	 *
	 * @param  address the above IP's syntax
	 * @return the fixed ip
	 */
	fun fixIp(loritta: LorittaBot, address: String): String {
		if (!address.startsWith("{"))
			return address

		val split = address.split(":")
		val addressKindOf = split.first()

		val theTwoIps = addressKindOf.removePrefix("{").removeSuffix("}")
				.split("/")

		val externalAddress = theTwoIps.first()
		val internalAddress = theTwoIps.last()

		// TODO: Fix this, afaik Loritta doesn't need this anymore
		val theRightIp = externalAddress /* if (externalAddress == loritta.instanceConfig.machineExternalIp) {
			internalAddress
		} else {
			externalAddress
		} */

		return if (split.size == 2) {
			getWithPortIfMissing(theRightIp, split.last().toInt())
		} else theRightIp
	}
}