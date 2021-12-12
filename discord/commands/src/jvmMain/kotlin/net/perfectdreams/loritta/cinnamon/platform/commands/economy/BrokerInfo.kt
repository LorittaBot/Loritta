package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author

object BrokerInfo {
    // TODO: Refactor
    val trackedTickerCodes = mapOf(
        "GOLL4"  to "Gol",
        "AZUL4"  to "Azul",
        "PETR4"  to "Petrobrás",
        "MGLU3"  to "Magazine Luiza",
        "VIIA3"  to "Via Varejo",
        "LAME4"  to "Lojas Americanas",
        "ITUB4"  to "Itaú Unibanco",
        "VALE3"  to "Vale S.A.",
        "BBDC4"  to "Bradesco S.A.",
        "IRBR3"  to "IRB Brasil S.A",
        "BBAS3"  to "Banco do Brasil S.A",
        "CRFB3"  to "Atacadão",
        "CMIG4"  to "Companhia Energética de Minas Gerais",
        "IGTA3"  to "Iguatemi Empresa de Shopping Centers",
        "OIBR3"  to "Oi",
        "ABEV3"  to "AMBEV",
        "TSLA34" to "Tesla",
        "B3SA3"  to "B3",
        "SBSP3"  to "SABESP",
        "LWSA3"  to "Locaweb",
        "CIEL3"  to "Cielo"
    )

    val validStocksCodes = trackedTickerCodes.keys

    fun MessageBuilder.brokerBaseEmbed(block: dev.kord.rest.builder.message.EmbedBuilder.() -> kotlin.Unit) = embed {
        author("Loritta's Home Broker")
        // TODO: Color
        // .setColor(BROKER_COLOR)
        // TODO: Image
        // .setThumbnail("${(loritta as LorittaDiscord).instanceConfig.loritta.website.url}assets/img/loritta_stonks.png")
        block()
    }

    /**
     * Converts reais (from TradingView) to sonhos
     *
     * *Currently this does nothing to the input, it just returns the current value*
     *
     * @param input the input in reais
     * @return      the value in sonhos
     */
    fun convertReaisToSonhos(input: Long) = input

    fun convertToBuyingPrice(input: Long) = input + 1
    fun convertToSellingPrice(input: Long) = input - 1
}