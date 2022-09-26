package kotlinx.serialization.json

object JSON {
	// Workaround because I'm too lazy to fix old code
	val nonstrict = Json {
		ignoreUnknownKeys = true
		isLenient = true
	}
}