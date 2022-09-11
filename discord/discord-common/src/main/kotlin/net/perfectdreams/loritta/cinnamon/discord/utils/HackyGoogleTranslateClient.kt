package net.perfectdreams.loritta.cinnamon.discord.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * A Google Translate Client that depends on undocumented endpoints, hacky!
 *
 * https://github.com/ssut/py-googletrans/issues/268
 */
class HackyGoogleTranslateClient {
    val http = HttpClient(CIO)

    suspend fun translate(from: Language, to: Language, input: String) = translate(
        from.code,
        to.code,
        input
    )

    private suspend fun translate(from: String, to: String, input: String): GoogleTranslateResponse? {
        val a = http.get("https://translate.googleapis.com/translate_a/single") {
            parameter("client", "gtx")
            parameter("sl", from)
            parameter("tl", to)
            parameter("q", input)
            parameter("dt", "t")
            parameter("ie", "UTF-8")
            parameter("oe", "UTF-8")
            userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0")
        }.bodyAsText()

        // Example: [[["Olá Mundo! ","Hello World!",null,null,1],["Como você está?","How are you?",null,null,10]],null,"en",null,null,null,null,[]]
        val response = Json.parseToJsonElement(a)
            .jsonArray

        val firstElementOnTheArray = response[0]
        // Nothing was translated: Example, if you input ""
        if (firstElementOnTheArray is JsonNull)
            return null

        val detectedLanguage = Language.fromLanguageCode(response[2].jsonPrimitive.content)

        val output = StringBuilder()
        firstElementOnTheArray.jsonArray.forEach {
            val innerArray = it.jsonArray

            val translated = innerArray[0].jsonPrimitive.content
            val source = innerArray[1].jsonPrimitive.content

            output.append(translated)
        }

        return GoogleTranslateResponse(
            output.toString(),
            detectedLanguage
        )
    }

    data class GoogleTranslateResponse(
        val output: String,
        val sourceLanguage: Language
    )

    enum class Language(val code: String) {
        AUTO_DETECT("auto"), // Not a "real language"
        AFRIKAANS("af"),
        ALBANIAN("sq"),
        AMHARIC("am"),
        ARABIC("ar"),
        ARMENIAN("hy"),
        AZERBAIJANI("az"),
        BASQUE("eu"),
        BELARUSIAN("be"),
        BENGALI("bn"),
        BOSNIAN("bs"),
        BULGARIAN("bg"),
        CATALAN("ca"),
        CEBUANO("ceb"),
        SIMPLIFIED_CHINESE("zh-CN"),
        TRADITIONAL_CHINESE("zh-TW"),
        CORSICAN("co"),
        CROATIAN("hr"),
        CZECH("cs"),
        DANISH("da"),
        DUTCH("nl"),
        ENGLISH("en"),
        ESPERANTO("eo"),
        ESTONIAN("et"),
        FINNISH("fi"),
        FRENCH("fr"),
        FRISIAN("fy"),
        GALICIAN("gl"),
        GEORGIAN("ka"),
        GERMAN("de"),
        GREEK("el"),
        GUJARATI("gu"),
        HAITIAN_CREOLE("ht"),
        HAUSA("ha"),
        HAWAIIAN("haw"),
        HEBREW("he"),
        HINDI("hi"),
        HMONG("hmn"),
        HUNGARIAN("hu"),
        ICELANDIC("is"),
        IGBO("ig"),
        INDONESIAN("id"),
        IRISH("ga"),
        ITALIAN("it"),
        JAPANESE("ja"),
        JAVANESE("jv"),
        KANNADA("kn"),
        KAZAKH("kk"),
        KHMER("km"),
        KINYARWANDA("rw"),
        KOREAN("ko"),
        KURDISH("ku"),
        KYRGYZ("ky"),
        LAO("lo"),
        LATIN("la"),
        LATVIAN("lv"),
        LITHUANIAN("lt"),
        LUXEMBOURGISH("lb"),
        MACEDONIAN("mk"),
        MALAGASY("mg"),
        MALAY("ms"),
        MALAYALAM("ml"),
        MALTESE("mt"),
        MAORI("mi"),
        MARATHI("mr"),
        MONGOLIAN("mn"),
        MYANMAR("my"),
        NEPALI("ne"),
        NORWEGIAN("no"),
        NYANJA("ny"),
        ODIA("or"),
        PASHTO("ps"),
        PERSIAN("fa"),
        POLISH("pl"),
        PORTUGUESE("pt"),
        PUNJABI("pa"),
        ROMANIAN("ro"),
        RUSSIAN("ru"),
        SAMOAN("sm"),
        SCOTS_GAELIC("gd"),
        SERBIAN("sr"),
        SESOTHO("st"),
        SHONA("sn"),
        SINDHI("sd"),
        SINHALA("si"),
        SLOVAK("sk"),
        SLOVENIAN("sl"),
        SOMALI("so"),
        SPANISH("es"),
        SUNDANESE("su"),
        SWAHILI("sw"),
        SWEDISH("sv"),
        TAGALOG("tl"),
        TAJIK("tg"),
        TAMIL("ta"),
        TATAR("tt"),
        TELUGU("te"),
        THAI("th"),
        TURKISH("tr"),
        TURKMEN("tk"),
        UKRAINIAN("uk"),
        URDU("ur"),
        UYGHUR("ug"),
        UZBEK("uz"),
        VIETNAMESE("vi"),
        WELSH("cy"),
        XHOSA("xh"),
        YIDDISH("yi"),
        YORUBA("yo"),
        ZULU("zu");

        companion object {
            fun fromLanguageCode(code: String) = Language.values().first { it.code == code }
        }
    }
}