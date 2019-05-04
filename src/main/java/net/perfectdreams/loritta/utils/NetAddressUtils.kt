package net.perfectdreams.loritta.utils

object NetAddressUtils {
    fun getWithPortIfMissing(address: String, defaultPort: Int): String {
        if (address.contains(":"))
            return address

        return "$address:$defaultPort"
    }
}