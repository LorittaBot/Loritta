package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import org.jetbrains.compose.web.dom.*

@Composable
fun LocalizedText(i18nContext: I18nContext, keyData: StringI18nData) {
    Text(i18nContext.get(keyData))
}

@Composable
fun LocalizedText(globalState: GlobalState, keyData: StringI18nData) {
    val state = globalState.i18nContext
    when (state) {
        is Resource.Failure -> Text(keyData.key.key)
        is Resource.Loading -> Text("...")
        is Resource.Success -> LocalizedText(state.value, keyData)
    }
}

@Composable
fun LocalizedFieldLabel(i18nContext: I18nContext, keyData: StringI18nData, forId: String) {
    Div(attrs = { classes("field-title") }) {
        Label(forId) {
            Text(i18nContext.get(keyData))
        }
    }
}

@Composable
fun TextReplaceControls(
    i18nContext: I18nContext,
    key: StringI18nKey,
    onText: @Composable (String) -> (Unit),
    onControl: @Composable (String) -> (ControlResult)
) {
    TextReplaceControls(
        i18nContext.language.textBundle.strings[key.key]!!,
        onText,
        onControl
    )
}

@Composable
fun TextReplaceControls(
    text: String,
    onText: @Composable (String) -> (Unit),
    onControl: @Composable (String) -> (ControlResult)
) {
    val builder = StringBuilder()
    val control = StringBuilder()
    var controlCharCount = 0

    for (ch in text) {
        if (controlCharCount != 0) {
            if (ch == '{') {
                controlCharCount++
            }

            if (ch == '}') {
                controlCharCount--

                if (controlCharCount == 0) {
                    val controlStr = control.toString()
                    when (val result = onControl.invoke(controlStr)) {
                        is ComposableFunctionResult -> result.block.invoke(controlStr)
                        AppendControlAsIsResult -> builder.append("{$control}")
                        DoNothingResult -> {}
                    }
                    control.clear()
                    continue
                }
            }

            control.append(ch)
            continue
        }

        if (ch == '{') {
            onText.invoke(builder.toString())
            builder.clear()
            controlCharCount++
            continue
        }

        builder.append(ch)
    }

    onText.invoke(builder.toString())
}

@Composable
fun appendAsFormattedText(i18nContext: I18nContext, map: Map<String, Any?>): @Composable (String) -> (Unit) = {
    Text(
        i18nContext.formatter.format(
            it,
            map
        )
    )
}

sealed class ControlResult
class ComposableFunctionResult(val block: @Composable (String) -> (Unit)) : ControlResult()
object DoNothingResult : ControlResult()
object AppendControlAsIsResult : ControlResult()

@Composable
fun LocalizedH1(i18nContext: I18nContext, keyData: StringI18nData) {
    H1 {
        LocalizedText(i18nContext, keyData)
    }
}

@Composable
fun LocalizedH2(i18nContext: I18nContext, keyData: StringI18nData) {
    H2 {
        LocalizedText(i18nContext, keyData)
    }
}