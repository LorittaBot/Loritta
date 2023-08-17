package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

@Composable
fun DiscordUserInput(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    screen: Screen,
    attrsScope: InputAttrsScope<String>.() -> (Unit),
    queriedUserResult: (CachedUserInfo?) -> (Unit)
) {
    Div {
        var state by remember { mutableStateOf<DiscordUserInputState>(DiscordUserInputState.Loading) }
        var parseResult by remember { mutableStateOf<DiscordUserInputResult>(DiscordUserInputResult.Empty) }
        var job by remember { mutableStateOf<Job?>(null) }

        Input(
            InputType.Text
        ) {
            attrsScope.invoke(this)

            placeholder(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.Tip))

            onInput {
                // Validate input
                val value = it.value

                val result = DiscordUserInputResult.parse(value)
                parseResult = result

                queriedUserResult.invoke(null)

                if (result is DiscordUserInputResult.DiscordParseSuccess) {
                    // Cancel previous job
                    job?.cancel()
                    // Start a new one!
                    job = screen.launch {
                        state = DiscordUserInputState.Loading

                        // Wait one second before querying user, to avoid spamming the API
                        delay(1_000)

                        // Query the database
                        val response = m.http.get("${window.location.origin}/api/v1/users/search") {
                            when (result) {
                                is DiscordUserInputResult.DiscordIdInput -> parameter(
                                    "id",
                                    result.userId.value.toString()
                                )

                                is DiscordUserInputResult.DiscordTagInput -> parameter("tag", result.tag)
                                is DiscordUserInputResult.DiscordPomeloInput -> parameter("pomelo", result.name)
                            }
                        }

                        if (response.status == HttpStatusCode.NotFound) {
                            queriedUserResult.invoke(null)
                            state = DiscordUserInputState.UnknownUser
                        } else {
                            val foundUser = Json.decodeFromString<CachedUserInfo>(response.bodyAsText())

                            val success = DiscordUserInputState.Success(foundUser)
                            state = success
                            queriedUserResult.invoke(success.user)
                        }
                    }
                }
            }
        }

        if (parseResult is DiscordUserInputResult.DiscordParseSuccess) {
            when (val state = state) {
                DiscordUserInputState.Loading -> {
                    ValidationMessage(ValidationMessageStatus.NEUTRAL) {
                        InlineLoadingSection(i18nContext)
                    }
                }

                is DiscordUserInputState.Success -> {
                    ValidationMessage(ValidationMessageStatus.SUCCESS) {
                        Div {
                            InlineUserDisplay(state.user)
                        }
                    }
                }

                DiscordUserInputState.UnknownUser -> {
                    ValidationMessageWithIcon(
                        ValidationMessageStatus.ERROR,
                        SVGIconManager.exclamationTriangle,
                        I18nKeysData.Website.Dashboard.DiscordUserInput.UnknownUser
                    )
                }
            }
        } else {
            ValidationMessageWithIcon(
                ValidationMessageStatus.ERROR,
                SVGIconManager.exclamationTriangle,
                when (parseResult) {
                    is DiscordUserInputResult.DiscordIdInput -> error("This should never happen!")
                    is DiscordUserInputResult.DiscordTagInput -> error("This should never happen!")
                    is DiscordUserInputResult.DiscordPomeloInput -> error("This should never happen!")
                    DiscordUserInputResult.Empty -> I18nKeysData.Website.Dashboard.DiscordUserInput.Tip
                    DiscordUserInputResult.InvalidDiscriminator -> I18nKeysData.Website.Dashboard.DiscordUserInput.InvalidDiscriminator
                    DiscordUserInputResult.MissingDiscriminator -> I18nKeysData.Website.Dashboard.DiscordUserInput.MissingDiscriminator
                }
            )
        }
    }
}

sealed class DiscordUserInputResult {
    companion object {
        fun parse(input: String): DiscordUserInputResult {
            if (input.isBlank())
                return Empty

            val trimmedInput = input.trim() // The user may have copied the text with spaces, so let's remove it
                .replace("@", "") // Usernames cannot have @, so let's remove it

            val valueAsLong = trimmedInput.toLongOrNull()
            if (valueAsLong == null) {
                val split = trimmedInput.split("#")
                if (split.size != 2)
                    return DiscordPomeloInput(
                        split
                            .first()
                            .substringBefore(" ")
                            .lowercase() // All pomelo names are in lowercase
                    )

                val (name, discriminator) = split
                val trimmedName = name.trim()
                val trimmedDiscriminator = discriminator.trim()

                if (trimmedDiscriminator.isBlank()) {
                    return MissingDiscriminator
                }

                if (trimmedDiscriminator.length != 4)
                    return InvalidDiscriminator

                val discriminatorAsInt = trimmedDiscriminator.toIntOrNull()
                if (discriminatorAsInt == null || discriminatorAsInt !in 1..9999)
                    return InvalidDiscriminator

                return DiscordTagInput("${trimmedName}#${trimmedDiscriminator}")
            }

            return DiscordIdInput(UserId(valueAsLong))
        }
    }

    object Empty : DiscordUserInputResult()
    object MissingDiscriminator : DiscordUserInputResult()
    object InvalidDiscriminator : DiscordUserInputResult()
    sealed class DiscordParseSuccess : DiscordUserInputResult()
    class DiscordTagInput(val tag: String) : DiscordParseSuccess()
    class DiscordPomeloInput(val name: String) : DiscordParseSuccess()
    class DiscordIdInput(val userId: UserId) : DiscordParseSuccess()
}

sealed class DiscordUserInputState {
    object Loading : DiscordUserInputState()
    class Success(val user: CachedUserInfo) : DiscordUserInputState()
    object UnknownUser : DiscordUserInputState()
}