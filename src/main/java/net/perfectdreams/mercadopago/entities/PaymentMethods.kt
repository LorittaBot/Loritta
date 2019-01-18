package net.perfectdreams.mercadopago.entities

import com.google.gson.annotations.SerializedName

data class PaymentMethods(
        @SerializedName("default_installments")
        val defaultInstallments: Any,
        @SerializedName("default_payment_method_id")
        val defaultPaymentMethodId: Any,
        @SerializedName("excluded_payment_methods")
        val excludedPaymentMethods: List<ExcludedPaymentMethod>,
        @SerializedName("excluded_payment_types")
        val excludedPaymentTypes: List<ExcludedPaymentType>,
        @SerializedName("installments")
        val installments: Int
)