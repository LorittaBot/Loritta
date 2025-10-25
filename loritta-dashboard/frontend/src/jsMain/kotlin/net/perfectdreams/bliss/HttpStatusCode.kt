package net.perfectdreams.bliss

// Based on Ktor's HttpStatusCode class
data class HttpStatusCode(val value: Int, val description: String) {
    companion object {
        val OK: HttpStatusCode = HttpStatusCode(200, "OK")

        fun fromValue(value: Int) = HttpStatusCode(value, "Unknown Status Code")
    }
}