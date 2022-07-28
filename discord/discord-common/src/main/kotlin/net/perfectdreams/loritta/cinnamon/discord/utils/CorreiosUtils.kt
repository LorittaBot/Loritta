package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosEvento
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnidadeBrasil
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnidadeExterior
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.EventTypeWithStatus

object CorreiosUtils {
    fun getEmoji(eventTypeWithStatus: EventTypeWithStatus) = when (eventTypeWithStatus.event) {
        EventType.ExternalPackageUpdate -> {
            when (eventTypeWithStatus.status) {
                EventType.ExternalPackageUpdate.WaitingForPayment -> Emotes.LoriCard
                EventType.ExternalPackageUpdate.PaymentConfirmed -> Emotes.LoriRich
                else -> Emotes.BrasilTorcida
            }
        }
        EventType.IssuesInPackageDelivery -> Emotes.LoriBonk
        EventType.PackageDeliveredToRecipient -> Emotes.LoriSmile
        EventType.PackageInDeliveryRouteToRecipient -> Emotes.Dash
        EventType.PackageInTransitFromTreatmentUnitToDistributionUnit -> Emotes.DeliveryTruck
        EventType.PackageInTransitToTreatmentUnit -> Emotes.DeliveryTruck
        EventType.PackagePosted -> Emotes.Inbox
        else -> Emotes.Package
    }

    fun getImage(eventTypeWithStatus: EventTypeWithStatus) = when (eventTypeWithStatus.event) {
        EventType.ExternalPackageUpdate -> {
            when (eventTypeWithStatus.status) {
                EventType.ExternalPackageUpdate.WaitingForPayment -> "https://cdn.discordapp.com/attachments/393332226881880074/957313834832506930/waiting_payment.png"
                EventType.ExternalPackageUpdate.PaymentConfirmed -> "https://cdn.discordapp.com/attachments/393332226881880074/957314224986673182/payment_confirmed.png"
                else -> null
            }
        }
        EventType.IssuesInPackageDelivery -> null
        EventType.PackageDeliveredToRecipient -> "https://cdn.discordapp.com/attachments/890603988607701055/957312937964474388/lori_entregando_pacote.png"
        EventType.PackageInDeliveryRouteToRecipient -> "https://cdn.discordapp.com/attachments/890603988607701055/957311357416513536/devious_delivery.png"
        EventType.PackageInTransitFromTreatmentUnitToDistributionUnit, EventType.PackageInTransitToTreatmentUnit -> "https://cdn.discordapp.com/attachments/890603988607701055/957311961228521512/correios_to_correios_delivery.png"
        EventType.PackagePosted -> "https://cdn.discordapp.com/attachments/393332226881880074/957316232871288912/package_posted.png"
        else -> null
    }

    fun formatEvent(event: CorreiosEvento): String {
        val unidade = event.unidade

        val local = when (unidade) {
            is CorreiosUnidadeBrasil -> unidade.local
            is CorreiosUnidadeExterior -> unidade.local
        }

        val firstDestino = event.destino?.firstOrNull()

        return if (event.type == EventType.PackageDeliveredToRecipient) {
            buildString {
                if (event.detalhe != null) {
                    append("> **${event.detalhe}**")
                    append("\n")
                }

                append(
                    when (unidade) {
                        is CorreiosUnidadeBrasil -> "pela ${unidade.tipounidade}, ${unidade.local}, ${unidade.endereco.localidade} - ${unidade.endereco.uf}"
                        is CorreiosUnidadeExterior -> "pela ${unidade.tipounidade}, ${unidade.local}"
                    }
                )

                append("\n")

                append("<t:${event.criacao.toInstant(UtcOffset(-3)).epochSeconds}:F>")
            }
        } else if (firstDestino != null && unidade is CorreiosUnidadeBrasil) {
            buildString {
                if (event.detalhe != null) {
                    append("> **${event.detalhe}**")
                    append("\n")
                }

                append("de ${unidade.tipounidade}, ${unidade.local}, ${unidade.endereco.localidade} - ${unidade.endereco.uf}")
                append("\n")
                append("para ${firstDestino.local}, ${firstDestino.endereco.localidade} - ${unidade.endereco.uf}")
                append("\n")

                append("<t:${event.criacao.toInstant(UtcOffset(-3)).epochSeconds}:F>")
            }
        } else {
            val endereco = (unidade as? CorreiosUnidadeBrasil)
                ?.let {
                    "${it.endereco.localidade} - ${it.endereco.uf}"
                }

            buildString {
                if (event.detalhe != null) {
                    append("> **${event.detalhe}**")
                    append("\n")
                }

                append(local)
                append("\n")
                if (endereco != null) {
                    append(endereco)
                    append("\n")
                }

                append("<t:${event.criacao.toInstant(UtcOffset(-3)).epochSeconds}:F>")
            }
        }
    }
}