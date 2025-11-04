package net.perfectdreams.bliss

// Based on Ktor's HttpStatusCode class
data class HttpStatusCode(val value: Int, val description: String) {
    companion object {
        val OK: HttpStatusCode = HttpStatusCode(200, "OK")

        val statusCodes = listOf(
            OK
        )

        val codeToStatus = statusCodes.associate { it.value to it }

        fun fromValue(value: Int) = codeToStatus[value] ?: HttpStatusCode(value, "Unknown Status Code")
    }
}