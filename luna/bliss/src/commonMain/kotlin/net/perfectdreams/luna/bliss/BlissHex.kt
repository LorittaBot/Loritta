package net.perfectdreams.luna.bliss

// "Why would you need to do this???"
// Well, embedding JSON in HTML *seems* easy, but it is actually very painful:
// 1. On <script> tags, if the JSON contains a </script> tag in a JSON String, the <script> tags ends and everything brekas
// 2. On attribute tags, it seems that kotlinx.html escapes it and if you have nested JSON within a HTML within a JSON, the code also breaks
// So, to make everything work well, we encode it as a hex string
// We use hex string instead of Base64 because we WANT it to be compression friendly, because Base64 has high entropy and is not a good fit for us
// We could use percent encoding, but I wanted to reinvent the wheel heh :3
object BlissHex {
    private val format = HexFormat.Default

    fun encodeToHexString(input: String): String {
        return input.encodeToByteArray().toHexString(format)
    }

    fun decodeFromHexString(input: String): String {
        return input.hexToByteArray(format).decodeToString()
    }
}