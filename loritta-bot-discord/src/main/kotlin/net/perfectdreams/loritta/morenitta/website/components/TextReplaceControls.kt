package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey

object TextReplaceControls {
    fun FlowContent.appendAsFormattedText(i18nContext: I18nContext, map: Map<String, Any?>): (String) -> (Unit) = {
        text(
            i18nContext.formatter.format(
                it,
                map
            )
        )
    }

    fun FlowContent.handleI18nString(
        i18nContext: I18nContext,
        key: StringI18nKey,
        onText: (String) -> (Unit),
        onControl: (String) -> (ControlResult)
    ) {
        handleI18nString(
            i18nContext.language.textBundle.strings[key.key]!!,
            onText,
            onControl
        )
    }

    fun FlowContent.handleI18nString(
        text: String,
        onText: (String) -> (Unit),
        onControl: (String) -> (ControlResult)
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
                            is ComposableFunctionResult -> result.block.invoke(this, controlStr)
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

    sealed class ControlResult
    class ComposableFunctionResult(val block: FlowContent.(String) -> (Unit)) : ControlResult()
    object DoNothingResult : ControlResult()
    object AppendControlAsIsResult : ControlResult()
}