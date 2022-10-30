package net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class CorreiosResponse(
    val versao: String,
    val quantidade: Int,
    val pesquisa: String,
    val resultado: String,
    val objeto: List<CorreiosObjeto>
)

@Serializable(CorreiosObjeto.Serializer::class)
sealed class CorreiosObjeto {
    abstract val numero: String

    object Serializer : JsonContentPolymorphicSerializer<CorreiosObjeto>(CorreiosObjeto::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            "sigla" !in element.jsonObject -> CorreiosUnknownObjeto.serializer()
            else -> CorreiosFoundObjeto.serializer()
        }
    }
}

@Serializable
data class CorreiosFoundObjeto(
    override val numero: String,
    val sigla: String,
    val nome: String,
    val categoria: String,
    @SerialName("evento")
    val events: List<CorreiosEvento>
) : CorreiosObjeto()

@Serializable
data class CorreiosUnknownObjeto(
    override val numero: String,
    val categoria: String
) : CorreiosObjeto()

@Serializable
data class CorreiosEvento(
    @Serializable(EventType.Serializer::class)
    @SerialName("tipo")
    val type: EventType,
    val status: String,
    @Serializable(LocalDateSerializer::class)
    val data: LocalDate,
    @Serializable(LocalTimeSerializer::class)
    val hora: LocalTime,
    @Serializable(CorreiosCreationDateSerializer::class)
    val criacao: LocalDateTime,
    val descricao: String,
    val detalhe: String? = null,
    val recebedor: JsonObject? = null,
    val unidade: CorreiosUnidade,
    val postagem: CorreiosPostagem? = null,
    val destino: List<CorreiosDestino>? = null,
    val cepDestino: Int? = null,
    val prazoGuarda: Int? = null,
    val diasUteis: Int? = null,
    val dataPostagem: String? = null,
    val detalheOEC: CorreiosDetalheOEC? = null
)

@Serializable
data class CorreiosDetalheOEC(
    val carteiro: String,
    val distrito: String,
    val lista: String,
    val unidade: String
)

@Serializable(CorreiosUnidade.Serializer::class)
sealed class CorreiosUnidade {
    object Serializer : JsonContentPolymorphicSerializer<CorreiosUnidade>(CorreiosUnidade::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element.jsonObject["tipounidade"]!!.jsonPrimitive.content == "País" -> CorreiosUnidadeExterior.serializer()
            else -> CorreiosUnidadeBrasil.serializer()
        }
    }

    abstract val local: String
    abstract val codigo: String
    abstract val sto: String
    abstract val tipounidade: String
}

@Serializable
data class CorreiosUnidadeBrasil(
    override val local: String,
    override val codigo: String,
    override val sto: String,
    override val tipounidade: String,
    val cidade: String? = null,
    val uf: String,
    val endereco: CorreiosEnderecoBrasil
) : CorreiosUnidade()

@Serializable
data class CorreiosUnidadeExterior(
    override val local: String,
    override val codigo: String,
    override val sto: String,
    override val tipounidade: String,
    val endereco: CorreiosEnderecoExterior
) : CorreiosUnidade()

@Serializable
data class CorreiosDestino(
    val local: String,
    val codigo: String,
    val cidade: String? = null,
    val uf: String,
    val bairro: String? = null,
    val endereco: CorreiosEnderecoBrasil
)

@Serializable
data class CorreiosEnderecoBrasil(
    val codigo: String,
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val numero: String? = null,
    val localidade: String? = null,
    val uf: String,
    val bairro: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CorreiosEnderecoExterior(
    val codigo: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CorreiosPostagem(
    val cepdestino: Int?,
    val ar: String,
    val mp: String,
    val dh: String,
    val peso: Double,
    val volume: Double,
    val dataprogramada: String,
    val datapostagem: String,
    val prazotratamento: Int
)

@Serializable(with = EventType.Serializer::class)
public sealed class EventType(public val value: String) {
    public class Unknown(value: String) : EventType(value) {
        override fun getStatusById(status: Int) = StatusType.Unknown(status)
    }

    public object PackagePosted : EventType("PO") {
        object PackagePosted : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackagePosted.status -> PackagePosted
            else -> StatusType.Unknown(status)
        }
    }

    // Not really sure the difference between both
    public object PackageInTransitFromTreatmentUnitToDistributionUnit : EventType("RO") {
        // Objeto em trânsito - por favor aguarde
        object PackageInTransit : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInTransit.status -> PackageInTransit
            else -> StatusType.Unknown(status)
        }
    }

    public object PackageInTransitToTreatmentUnit : EventType("DO") {
        // Objeto em trânsito - por favor aguarde
        object PackageInTransit : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInTransit.status -> PackageInTransit
            else -> StatusType.Unknown(status)
        }
    }

    // Objeto saiu para entrega ao destinatário
    public object PackageInDeliveryRouteToRecipient : EventType("OEC") {
        // Objeto saiu para entrega ao destinatário
        object PackageInDeliveryRouteToRecipient : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInDeliveryRouteToRecipient.status -> PackageInDeliveryRouteToRecipient
            else -> StatusType.Unknown(status)
        }
    }

    public object PackageDeliveredToRecipient : EventType("BDE") {
        // Objeto entregue ao destinatário
        object PackageDeliveredToRecipient : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageDeliveredToRecipient.status -> PackageDeliveredToRecipient
            else -> StatusType.Unknown(status)
        }
    }

    // Objeto ainda não chegou à unidade
    public object ObjectHasNotYetArrivedAtTheUnit : EventType("BDE") {
        override fun getStatusById(status: Int) = StatusType.Unknown(status)
    }

    // This can be two things:
    // O endereço indicado para entrega contém inconsistências. Poderá ocorrer atraso ou devolução ao remetente
    // Corrigimos um equívoco no encaminhamento do seu objeto. Por favor aguarde
    public object IssuesInPackageDelivery : EventType("FC") {
        // Objeto em correção de rota
        object PackageInRouteCorrection : StatusType(3)

        // Objeto não entregue - endereço incorreto
        object PackageNotDeliveredIncorrectAddress : StatusType(4)

        override fun getStatusById(status: Int) = when (status) {
            PackageInRouteCorrection.status -> PackageInRouteCorrection
            PackageNotDeliveredIncorrectAddress.status -> PackageNotDeliveredIncorrectAddress
            else -> StatusType.Unknown(status)
        }
    }

    // This can be two things:
    // Fiscalização aduaneira finalizada
    // Objeto recebido pelos Correios do Brasil
    // Objeto recebido na unidade de exportação no país de origem
    public object ExternalPackageUpdate : EventType("PAR") {
        // Objeto recebido na unidade de exportação no país de origem
        object PackageReceivedInTheExportFacility : StatusType(18)

        // Objeto recebido pelos Correios do Brasil
        object PackageReceivedByCorreiosBrasil : StatusType(16)

        // Encaminhado para fiscalização aduaneira
        object ForwardedForCustomsInspection : StatusType(21)

        // Aguardando pagamento
        object WaitingForPayment : StatusType(17)

        // Pagamento confirmado
        object PaymentConfirmed : StatusType(31)

        // Fiscalização aduaneira finalizada
        object CustomsInspectionFinished : StatusType(10)

        override fun getStatusById(status: Int) = when (status) {
            PackageReceivedInTheExportFacility.status -> PackageReceivedInTheExportFacility
            PackageReceivedByCorreiosBrasil.status -> PackageReceivedByCorreiosBrasil
            ForwardedForCustomsInspection.status -> ForwardedForCustomsInspection
            WaitingForPayment.status -> WaitingForPayment
            PaymentConfirmed.status -> PaymentConfirmed
            CustomsInspectionFinished.status -> CustomsInspectionFinished
            else -> StatusType.Unknown(status)
        }
    }

    abstract fun getStatusById(status: Int): StatusType

    internal object Serializer : KSerializer<EventType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): EventType = when (val value = decoder.decodeString()) {
            PackagePosted.value -> PackagePosted
            PackageDeliveredToRecipient.value -> PackageDeliveredToRecipient
            PackageInTransitFromTreatmentUnitToDistributionUnit.value -> PackageInTransitFromTreatmentUnitToDistributionUnit
            PackageInTransitToTreatmentUnit.value -> PackageInTransitToTreatmentUnit
            PackageInDeliveryRouteToRecipient.value -> PackageInDeliveryRouteToRecipient
            IssuesInPackageDelivery.value -> IssuesInPackageDelivery
            ExternalPackageUpdate.value -> ExternalPackageUpdate

            else -> Unknown(value)
        }

        override fun serialize(encoder: Encoder, value: EventType) {
            encoder.encodeString(value.value)
        }
    }
}

public sealed class StatusType(public val status: Int) {
    public class Unknown(value: Int) : StatusType(value)
}

private object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(
            "${value.dayOfMonth.toString().padStart(2, '0')}/${
                value.monthNumber.toString().padStart(2, '0')
            }/${value.year}"
        )
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val (day, month, year) = decoder.decodeString().split("/")
        return LocalDate(year.toInt(), month.toInt(), day.toInt())
    }
}

private object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString("${value.hour.toString().padStart(2, '0')}:${value.minute.toString().padStart(2, '0')}")
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        val (hour, minute) = decoder.decodeString().split(":")
        return LocalTime(hour.toInt(), minute.toInt())
    }
}

private object CorreiosCreationDateSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CorreiosCreationDateSerializer", PrimitiveKind.STRING)

    // Example: 24012022124519

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(
            "${value.dayOfMonth.toString().padStart(2, '0')}${
                value.monthNumber.toString().padStart(2, '0')
            }${value.year}${value.hour.toString().padStart(2, '0')}${
                value.minute.toString().padStart(2, '0')
            }${value.second.toString().padStart(2, '0')}"
        )
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val input = decoder.decodeString()

        val day = input.substring(0..1).toInt()
        val month = input.substring(2..3).toInt()
        val year = input.substring(4..7).toInt()
        val hour = input.substring(8..9).toInt()
        val minute = input.substring(10..11).toInt()
        val second = input.substring(12..13).toInt()

        return LocalDateTime(year, month, day, hour, minute, second)
    }
}

// kotlinx.datetime doesn't have LocalTime... yet
data class LocalTime(val hour: Int, val minute: Int)

val CorreiosEvento.eventTypeWithStatus
    get() = EventTypeWithStatus(
        this.type,
        this.type.getStatusById(this.status.toInt())
    )

data class EventTypeWithStatus(val event: EventType, val status: StatusType)