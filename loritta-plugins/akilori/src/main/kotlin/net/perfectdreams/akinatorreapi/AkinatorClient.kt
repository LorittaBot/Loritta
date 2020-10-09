package net.perfectdreams.akinatorreapi

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.akinatorreapi.payload.CharacterGuess
import net.perfectdreams.akinatorreapi.payload.GameIdentification

class AkinatorClient(val region: Region, val childMode: Boolean = false) {
    companion object {
        internal val http = HttpClient {
            expectSuccess = false
        }
        private val logger = KotlinLogging.logger {}
        private val patternRegex = Regex("var uid_ext_session = '(.*)';\n.*var frontaddr = '(.*)';")
        private val defaultHttpParameters: HttpRequestBuilder.() -> (Unit) = {
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            header("Accept-Language", "en-US,en;q=0.9")
            header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) snap Chromium/81.0.4044.92 Chrome/81.0.4044.92 Safari/537.36")
            header("x-requested-with", "XMLHttpRequest")
        }
    }

    private val uri = region.uri
    private val urlApiWs = region.urlApiWs
    private val noSession = "Could not find the game session. Please make sure you have started the game!"
    var currentStep: AkinatorStep? = null
    private val callbackCode = "jQuery${System.currentTimeMillis()}"
    private lateinit var gameIdentification: GameIdentification
    private lateinit var session: SessionInformation

    /**
     * Starts a Akinator game
     */
    suspend fun start() {
        session = retrieveSession()

        val payload = http.get<HttpResponse>("https://${this.uri}/new_session") {
            defaultHttpParameters.invoke(this)

            parameter("callback", callbackCode)
            parameter("urlApiWs", "https://$urlApiWs/ws")
            parameter("partner", "1")
            parameter("player", "website-desktop")
            parameter("uid_ext_session", session.uid)
            parameter("frontaddr", session.frontaddr)
            if (childMode)
                parameter("childMod", "true")
            parameter("constraint", "ETAT<>'AV'")
        }

        val result = payload.readText()
            .removePrefix("$callbackCode(")
            .removeSuffix(")")

        val json = Json.parseToJsonElement(result).jsonObject
        val identification = json["parameters"]!!.jsonObject["identification"]!!.jsonObject
        val stepInformation = json["parameters"]!!.jsonObject["step_information"]!!.jsonObject

        gameIdentification = Json.decodeFromString(GameIdentification.serializer(), identification.toString())
        logger.trace { gameIdentification }

        currentStep = AkinatorStep(
            stepInformation["question"]!!.jsonPrimitive.content,
            stepInformation["answers"]!!.jsonArray.map { it.jsonObject["answer"]!!.jsonPrimitive.content },
            stepInformation["step"]!!.jsonPrimitive.int,
            stepInformation["progression"]!!.jsonPrimitive.double,
            stepInformation["questionid"]!!.jsonPrimitive.int,
            stepInformation["infogain"]!!.jsonPrimitive.double
        )
    }

    /**
     * Answers the current [currentStep]
     */
    suspend fun answerCurrentQuestion(answer: AkinatorAnswer) = answerCurrentQuestion(answer.id)
    suspend fun answerCurrentQuestion(answerId: Int) {
        val payload = http.get<HttpResponse>("https://${this.uri}/answer_api?callback=$callbackCode&urlApiWs=https://${this.urlApiWs}/ws&session=${this.gameIdentification.session}&signature=${this.gameIdentification.signature}&step=${this.currentStep?.step}&answer=${answerId}&frontaddr=${session.frontaddr}") {
            defaultHttpParameters.invoke(this)
        }

        val text = payload.readText()
            .removePrefix("$callbackCode(")
            .removeSuffix(")")
        logger.trace { text }

        val json = Json.parseToJsonElement(text).jsonObject
        val stepInformation = json["parameters"]!!.jsonObject

        try {
            currentStep = AkinatorStep(
                    stepInformation["question"]!!.jsonPrimitive.content,
                    stepInformation["answers"]!!.jsonArray.map { it.jsonObject["answer"]!!.jsonPrimitive.content },
                    stepInformation["step"]!!.jsonPrimitive.int,
                    stepInformation["progression"]!!.jsonPrimitive.double,
                    stepInformation["questionid"]!!.jsonPrimitive.int,
                    stepInformation["infogain"]!!.jsonPrimitive.double
            )
        } catch (e: Exception) {
            currentStep = null
        }
    }

    /**
     * Goes back one step
     */
    suspend fun back() {
        if (currentStep?.step == 0)
            throw RuntimeException("Can't go back because you are currently on step 0!")

        val payload = http.get<HttpResponse>("https://${this.urlApiWs}/ws/cancel_answer") {
            defaultHttpParameters.invoke(this)

            parameter("callback", callbackCode)
            parameter("session", gameIdentification.session)
            parameter("signature", gameIdentification.signature)
            parameter("step", currentStep?.step)
            parameter("answer", "-1")
        }

        val text = payload.readText()
            .removePrefix("$callbackCode(")
            .removeSuffix(")")
        logger.trace { text }

        val json = Json.parseToJsonElement(text).jsonObject
        val stepInformation = json["parameters"]!!.jsonObject

        currentStep = AkinatorStep(
            stepInformation["question"]!!.jsonPrimitive.content,
            stepInformation["answers"]!!.jsonArray.map { it.jsonObject["answer"]!!.jsonPrimitive.content },
            stepInformation["step"]!!.jsonPrimitive.int,
            stepInformation["progression"]!!.jsonPrimitive.double,
            stepInformation["questionid"]!!.jsonPrimitive.int,
            stepInformation["infogain"]!!.jsonPrimitive.double
        )
    }

    /**
     * Retrieves all Akinator guesses
     */
    suspend fun retrieveGuesses(): List<CharacterGuess> {
        val payload = http.get<HttpResponse>("https://${this.urlApiWs}/ws/list?callback=&signature=${this.gameIdentification.signature}&step=${this.currentStep?.step}&session=${gameIdentification.session}") {
            defaultHttpParameters.invoke(this)
        }

        val text = payload.readText()
            .removePrefix("$callbackCode(")
            .removeSuffix(")")
        logger.trace { text }

        val json = Json.parseToJsonElement(text).jsonObject
        return json["parameters"]!!.jsonObject["elements"]!!.jsonArray.map { it.jsonObject["element"]!!.jsonObject }.map { Json.decodeFromString(CharacterGuess.serializer(), it.toString()) }
    }

    /**
     * Retrieves the session
     */
    private suspend fun retrieveSession(): SessionInformation {
        val result = http.get<String>("https://en.akinator.com/game") { defaultHttpParameters.invoke(this) }

        logger.trace { result }

        val matchResult = patternRegex.find(result) ?: throw RuntimeException("Cannot find the uid and frontaddr. Please report!")

        if (matchResult.groupValues.size != 3)
            throw RuntimeException("Cannot find the uid and frontaddr. Please report!")

        return SessionInformation(
            matchResult.groupValues[1],
            matchResult.groupValues[2]
        )
    }

    private suspend fun readTextOrThrowIfInvalid(response: HttpResponse): String {
        if (response.status != HttpStatusCode.OK)
            throw RuntimeException("Bad status code! ${response.status}")

        return response.readText()
    }
}