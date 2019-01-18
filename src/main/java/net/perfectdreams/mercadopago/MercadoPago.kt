package net.perfectdreams.mercadopago

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import net.perfectdreams.mercadopago.entities.Payment
import net.perfectdreams.mercadopago.entities.PaymentSettings
import net.perfectdreams.mercadopago.response.CheckoutOAuth2Response
import net.perfectdreams.mercadopago.response.PaymentSearch
import net.perfectdreams.mercadopago.response.PaymentSettingsResponse


class MercadoPago(val clientId: String? = null, val clientSecret: String? = null, val accessToken: String? = null) {
    companion object {
        private val gson = Gson()
    }

    private var checkoutAccessToken: String? = null
    private var checkoutLastRenewal = 0L
    private var expiresIn = 0

    fun getCheckoutOAuth2Response(): CheckoutOAuth2Response {
        val json = HttpRequest.post(Endpoints.API_URL + "/oauth/token")
                .part("grant_type", "client_credentials")
                .part("client_id", clientId)
                .part("client_secret", clientSecret)
                .body()

        println(json)

        return gson.fromJson(json)
    }

    fun getCheckoutToken(): String {
        if (checkoutAccessToken != null && System.currentTimeMillis() > ((checkoutLastRenewal + expiresIn) - 60_000)) { // 60 segundos para evitar que a gente faça um request bem na hora que vá expirar
            return checkoutAccessToken!!
        }

        val response = getCheckoutOAuth2Response()

        this.checkoutLastRenewal = System.currentTimeMillis()
        this.expiresIn = response.expiresIn
        this.checkoutAccessToken = response.accessToken
        return checkoutAccessToken!!
    }

    fun getPaymentInfoById(paymentId: String): Payment {
        return getPaymentInfoById(paymentId.toLong())
    }

    fun getPaymentInfoById(paymentId: Long): Payment {
        val body = HttpRequest.get("${Endpoints.API_V1_URL}/payments/$paymentId?access_token=${getCheckoutToken()}")
                .contentType("application/json")
                .acceptJson()
                .body()

        println(body)

        return gson.fromJson(body)
    }

    fun searchPayments(limit: Int = 30, offset: Int = 0, filters: Map<String, String> = mapOf()): PaymentSearch {
        val body = HttpRequest.get("${Endpoints.API_V1_URL}/payments/search?access_token=${getCheckoutToken()}&limit=$limit&offset=$offset&${filters.entries.joinToString("&", transform = { it.key + "=" + it.value })}")
                .contentType("application/json")
                .acceptJson()
                .body()

        println(body)

        return gson.fromJson(body)
    }

    fun createPayment(preference: PaymentSettings): PaymentSettingsResponse {
        val response = HttpRequest.post(Endpoints.API_URL + "/checkout/preferences?access_token=" + getCheckoutToken())
                .acceptJson()
                .contentType("application/json")
                .send(gson.toJson(preference))
                .body()

        println(response)

        return gson.fromJson(response)
    }
}