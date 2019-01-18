package net.perfectdreams.mercadopago.dsl

import com.google.gson.annotations.SerializedName
import net.perfectdreams.mercadopago.entities.*

fun paymentSettings(block: PaymentSettingsBuilder.() -> Unit): PaymentSettings = PaymentSettingsBuilder().apply(block).build()

class PaymentSettingsBuilder {
    val items = mutableListOf<Item>()
    var payer: PaymentSettings.Payer? = null
    var externalReference: String? = null
    var notificationUrl: String? = null


    fun item(block: ItemBuilder.() -> Unit) = items.add(ItemBuilder().apply(block).build())

    fun payer(block: PaymentSettingsPayerBuilder.() -> Unit) {
        payer = PaymentSettingsPayerBuilder().apply(block).build()
    }

    fun build() = PaymentSettings(
            items = items,
            payer = payer,
            externalReference = externalReference,
            notificationUrl = notificationUrl
    )
}

class ItemBuilder {
    @SerializedName("title")
    var title: String? = null
    @SerializedName("quantity")
    var quantity: Int? = null
    @SerializedName("currency_id")
    var currencyId: String? = null
    @SerializedName("unit_price")
    var unitPrice: Float? = null

    @SerializedName("category_id")
    var categoryId: String? = null
    @SerializedName("description")
    var description: String? = null
    @SerializedName("id")
    var id: String? = null
    @SerializedName("picture_url")
    var pictureUrl: String? = null

    fun build() = Item(
            title!!,
            quantity!!,
            currencyId!!,
            unitPrice!!,
            categoryId,
            description,
            id,
            pictureUrl
    )
}

class PaymentSettingsPayerBuilder {
    @SerializedName("address")
    var address: Address? = null
    @SerializedName("date_created")
    var dateCreated: String? = null
    @SerializedName("email")
    var email: String? = null
    @SerializedName("identification")
    var identification: Identification? = null
    @SerializedName("name")
    var firstName: String? = null
    @SerializedName("phone")
    var phone: Phone? = null
    @SerializedName("surname")
    var lastName: String? = null

    fun build() = PaymentSettings.Payer(
            address = address,
            dateCreated = dateCreated,
            email = email,
            identification = identification,
            firstName = firstName,
            phone = phone,
            lastName = lastName
    )
}