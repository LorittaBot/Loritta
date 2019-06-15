package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.utils.locale.BaseLocale
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AddLoriAminoRoute : LocalizedRoute("/add-lori-amino") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val base64UserParam = call.parameters["user"]

        // Nós iremos decodificar o base64, o base64 é um JSON assim:
        // {"id":"UUID"}
        // Que representa o ID do usuário no Amino, assim não é necessário pedir para o usuário logar na conta apenas para adicionar a Lori
        // (Mesmo que não precise de tudo isso, whoops)
    }
}

fun main() {
    val text = "Hello World"
    val key = "Bar12345Bar12345" // 128 bit key
    // Create key and cipher
    val aesKey = SecretKeySpec(key.toByteArray(), "AES")
    val fakeAesKey = SecretKeySpec("Bar12345Bar12346".toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES")
    // encrypt the text
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    val encrypted = cipher.doFinal(text.toByteArray())

    val asBase64 = Base64.getEncoder().encode(encrypted)
    val base64Str = asBase64.toString(Charsets.UTF_8)
    println(base64Str)

    val fromBase64 = Base64.getDecoder().decode(base64Str.toByteArray(Charsets.UTF_8))

    // decrypt the text
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    val decrypted = String(cipher.doFinal(fromBase64))
    System.err.println(decrypted)
}