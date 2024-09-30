package net.perfectdreams.loritta.lorituber.server

import java.security.Provider
import java.security.Security


fun main() {
    // Get all providers
    for (provider: Provider in Security.getProviders()) {
        // Get all entries for each provider
        for (service: Provider.Service in provider.getServices()) {
            // Check if the service type is SecureRandom
            if ("SecureRandom" == service.getType()) {
                System.out.println(
                    ("Algorithm: " + service.getAlgorithm()).toString() +
                            ", Provider: " + provider.getName()
                )
            }
        }
    }
}