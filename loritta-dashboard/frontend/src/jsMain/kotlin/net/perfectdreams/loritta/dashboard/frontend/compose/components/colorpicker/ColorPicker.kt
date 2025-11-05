package net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButtonType
import net.perfectdreams.loritta.dashboard.frontend.compose.components.RawHtml
import net.perfectdreams.loritta.dashboard.frontend.compose.components.SVGIcon
import net.perfectdreams.loritta.dashboard.frontend.modals.Modal
import net.perfectdreams.loritta.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import web.cssom.ClassName
import web.html.HTMLDivElement
import kotlin.random.Random

@Composable
fun ColorPicker(
    m: LorittaDashboardFrontend,
    checkmarkIconRawHtml: SVGIconManager.SVGIcon,
    eyeDropperIconRawHtml: SVGIconManager.SVGIcon,
    currentColor: Color?,
    onColorSelection: (Color?) -> (Unit)
) {
    val colorToBeUsedAsABase = currentColor ?: ColorUtils.LorittaAqua
    var colorAsHSB by mutableStateOf(createHSBColorFromArray(ColorUtils.RGBtoHSB(colorToBeUsedAsABase.red, colorToBeUsedAsABase.green, colorToBeUsedAsABase.blue, null)))

    val isCustomColor = currentColor != null && currentColor !in ColorUtils.defaultColors

    Div(attrs = {
        classes("discord-color-selector")
    }) {
        Button(attrs = {
            type(ButtonType.Button)
            classes("big-color-swatch")

            onClick {
                onColorSelection.invoke(null)
            }
        }) {
            if (currentColor == null)
                SVGIcon(checkmarkIconRawHtml) {
                    classList.add(ClassName("selected-icon"))
                }
        }

        Button(attrs = {
            type(ButtonType.Button)
            classes("big-color-swatch")
            style {
                if (currentColor != null) {
                    // Even tho this may not be a custom color (it may be a color in the default color list), I think it looks nicer if the background was at least colorized
                    backgroundColor(rgb(currentColor.red, currentColor.green, currentColor.blue))
                    val brightness = ColorUtils.calculateBrightness(currentColor)

                    // This is CRAZY and GENIUS
                    // (Change the eye dropper/checkmark color depending on the color brightness)
                    // (discord also does that lmao so it is not really that genius)
                    if (brightness >= 0.65f) {
                        color(rgb(0, 0, 0))
                    } else {
                        color(rgb(255, 255, 255))
                    }
                }
            }

            onClick {
                var colorAsHSB by mutableStateOf(createHSBColorFromArray(ColorUtils.RGBtoHSB(colorToBeUsedAsABase.red, colorToBeUsedAsABase.green, colorToBeUsedAsABase.blue, null)))

                m.modalManager.openModalWithCloseButton(
                    "Selecionar Cor",
                    Modal.Size.MEDIUM,
                    {
                        // This needs to be here to avoid the color being "remembered" after closing the color
                        val colorAsRGB = Color(
                            ColorUtils.HSBtoRGB(
                                colorAsHSB.hue,
                                colorAsHSB.saturation,
                                colorAsHSB.brightness
                            )
                        )

                        Div(attrs = {
                            classes("color-picker-wrapper")
                        }) {
                            Div(attrs = {
                                classes("color-picker-elements")
                            }) {
                                Div(attrs = {
                                    classes("color-picker-box-and-hue")
                                }) {
                                    ColorBoxAndHuePicker(colorAsHSB) {
                                        colorAsHSB = it
                                    }

                                    Div(attrs = {
                                        classes("quick-color-palette")
                                    }) {
                                        for ((colorUp, colorDown) in ColorUtils.defaultColorsCombo) {
                                            Div(attrs = {
                                                classes("quick-color-swatch-combo")
                                            }) {
                                                ColorSwatch(checkmarkIconRawHtml, colorAsRGB, colorUp) {
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            it.red,
                                                            it.green,
                                                            it.blue,
                                                            null
                                                        )
                                                    )
                                                }
                                                ColorSwatch(checkmarkIconRawHtml, colorAsRGB, colorDown) {
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            it.red,
                                                            it.green,
                                                            it.blue,
                                                            null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Div(attrs = {
                                    classes("color-picker-values-grid")
                                }) {
                                    ColorPickerValueInput(
                                        "R:",
                                        block = {
                                            NumberInput(
                                                colorAsRGB.red,
                                            ) {
                                                onInput {
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            it.value!!.toInt().coerceIn(0..255),
                                                            colorAsRGB.green,
                                                            colorAsRGB.blue,
                                                            null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "G:",
                                        block = {
                                            NumberInput(colorAsRGB.green) {
                                                onInput {
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            colorAsRGB.red,
                                                            it.value!!.toInt().coerceIn(0..255),
                                                            colorAsRGB.green,
                                                            null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "B:",
                                        block = {
                                            NumberInput(colorAsRGB.blue) {
                                                onInput {
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            colorAsRGB.red,
                                                            colorAsRGB.green,
                                                            it.value!!.toInt().coerceIn(0..255),
                                                            null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "Matiz:",
                                        endLabel = "°",
                                        block = {
                                            NumberInput((colorAsHSB.hue * 360 + 0.5).toInt()) {
                                                onInput {
                                                    println("Value of hue float: ${(it.value!!.toFloat() / 360)}")
                                                    colorAsHSB = colorAsHSB.copy(
                                                        hue = (it.value!!.toFloat() / 360).coerceIn(0f..1f)
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "Saturação:",
                                        endLabel = "%",
                                        block = {
                                            NumberInput((colorAsHSB.saturation * 100 + 0.5).toInt()) {
                                                onInput {
                                                    println("Value of saturation float: ${(it.value!!.toFloat() / 100)}")
                                                    colorAsHSB = colorAsHSB.copy(
                                                        saturation = (it.value!!.toFloat() / 100).coerceIn(0f..1f)
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "Brilho:",
                                        endLabel = "%",
                                        block = {
                                            NumberInput((colorAsHSB.brightness * 100 + 0.5).toInt()) {
                                                onInput {
                                                    colorAsHSB = colorAsHSB.copy(
                                                        brightness = (it.value!!.toFloat() / 100).coerceIn(0f..1f)
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    var hexString by remember(colorAsRGB) {
                                        mutableStateOf(
                                            ColorUtils.convertFromColorToHex(
                                                colorAsRGB.rgb
                                            )
                                        )
                                    }

                                    ColorPickerValueInput(
                                        "Hexadecimal:",
                                        block = {
                                            TextInput(hexString) {
                                                onInput {
                                                    // Small validation to avoid changing the color while we are writing
                                                    val valid = it.value.startsWith("#") && it.value.length == 7
                                                    hexString = it.value
                                                    if (!valid)
                                                        return@onInput

                                                    val newColor = ColorUtils.convertFromHexToColor(it.value.removePrefix("#"))
                                                    colorAsHSB = createHSBColorFromArray(
                                                        ColorUtils.RGBtoHSB(
                                                            newColor.red,
                                                            newColor.green,
                                                            newColor.blue,
                                                            null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    ColorPickerValueInput(
                                        "Decimal:",
                                        block = {
                                            TextInput(colorAsRGB.rgb.toString()) {
                                                onInput {
                                                    // Small validation to avoid changing the color while we are writing
                                                    val valid = it.value.toIntOrNull() != null
                                                    if (!valid)
                                                        return@onInput

                                                    val newColor = Color(it.value.toInt())
                                                    colorAsHSB = ColorUtils.RGBtoHSB(
                                                        newColor.red,
                                                        newColor.green,
                                                        newColor.blue,
                                                        null
                                                    ).let {
                                                        HSBColor(it[0], it[1], it[2])
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                GabrielaColorPreview(colorAsHSB)
                            }

                            Div(attrs = {
                                classes("color-picker-random-buttons")
                            }) {
                                Div {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        {
                                            onClick {
                                                colorAsHSB = HSBColor(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
                                            }
                                        }
                                    ) {
                                        Text("Cor Aleatória")
                                    }
                                }

                                Div {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        {
                                            onClick {
                                                val newColor = ColorUtils.defaultColors.random()
                                                colorAsHSB = ColorUtils.RGBtoHSB(
                                                    newColor.red,
                                                    newColor.green,
                                                    newColor.blue,
                                                    null
                                                ).let {
                                                    HSBColor(it[0], it[1], it[2])
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Cor Padrão Aleatória")
                                    }
                                }

                                Div {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        {
                                            onClick {
                                                colorAsHSB = HSBColor(Random.nextFloat(), 1f, 1f)
                                            }
                                        }
                                    ) {
                                        Text("Cor Vibrante Aleatória")
                                    }
                                }

                                Div {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        {
                                            onClick {
                                                val newColor = ColorUtils.pastelColors.random()

                                                colorAsHSB = ColorUtils.RGBtoHSB(
                                                    newColor.red,
                                                    newColor.green,
                                                    newColor.blue,
                                                    null
                                                ).let {
                                                    HSBColor(it[0], it[1], it[2])
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Cor Pastel Aleatória")
                                    }
                                }
                            }
                        }
                    },
                    { modal ->
                        DiscordButton(
                            DiscordButtonType.PRIMARY,
                            attrs = {
                                onClick {
                                    modal.close()
                                    val rgb = ColorUtils.HSBtoRGB(colorAsHSB.hue, colorAsHSB.saturation, colorAsHSB.brightness)
                                    onColorSelection.invoke(Color(rgb))
                                }
                            }
                        ) {
                            Text("Aplicar")
                        }
                    }
                )
            }
        }) {
            SVGIcon(eyeDropperIconRawHtml) {
                classList.add(ClassName("eye-dropper-icon"))
            }

            if (isCustomColor) {
                SVGIcon(checkmarkIconRawHtml) {
                    classList.add(ClassName("selected-icon"))
                }
            }
        }

        Div(attrs = {
            classes("quick-color-palette")
        }) {
            for ((colorUp, colorDown) in ColorUtils.defaultColorsCombo) {
                Div(attrs = {
                    classes("quick-color-swatch-combo")
                }) {
                    ColorSwatch(checkmarkIconRawHtml, currentColor, colorUp, onColorSelection)
                    ColorSwatch(checkmarkIconRawHtml, currentColor, colorDown, onColorSelection)
                }
            }
        }
    }
}

@Composable
fun ColorSwatch(checkmarkIconRawHtml: SVGIconManager.SVGIcon, currentColor: Color?, color: Color, onColorSelection: (Color) -> Unit) {
    Button(attrs = {
        type(ButtonType.Button)
        classes("quick-color-swatch")
        style {
            backgroundColor(rgb(color.red, color.green, color.blue))
        }

        onClick {
            onColorSelection.invoke(color)
        }
    }) {
        if (currentColor == color) {
            SVGIcon(checkmarkIconRawHtml) {
                classList.add(ClassName("selected-icon"))
            }
        }
    }
}

@Composable
fun ColorBoxAndHuePicker(currentColor: HSBColor, onColorSelection: (HSBColor) -> Unit) {
    var selectedHue by mutableStateOf(currentColor.hue)
    var selectedSaturation by mutableStateOf(currentColor.saturation)
    var selectedBrightness by mutableStateOf(currentColor.brightness)

    // This NEEDS to be remembered, if not the move function doesn't work!
    var isHolding by remember { mutableStateOf(false) }

    fun updateSelectedColorIfPossible() {
        val currentHue = selectedHue
        val currentSaturation = selectedSaturation
        val currentBrightness = selectedBrightness

        println("Invoking onColorSelection...")
        onColorSelection.invoke(HSBColor(currentHue, currentSaturation, currentBrightness))
    }

    Div {
        Div(attrs = {
            attr("style", "display: flex; flex-direction: column; gap: 0.25em;")
        }) {
            Div(attrs = {
                val colorAsHSL = ColorUtils.HSBtoHSL(currentColor.hue, currentColor.saturation, currentColor.brightness)
                classes("color-picker-color-preview")

                style {
                    backgroundColor(hsl((colorAsHSL.first * 360).deg, (colorAsHSL.second * 100), (colorAsHSL.third * 100)))
                }
            })

            Div(attrs = {
                classes("color-picker-box-wrapper")
            }) {
                Div(attrs = {
                    classes("color-picker-box")
                    style {
                        backgroundColor(hsl((selectedHue * 360).deg, 100, 50))
                    }

                    // TODO: Bind these events to document, for better UX
                    onMouseDown {
                        println("[ColorPicker] onMouseDown")
                        isHolding = true
                    }

                    onMouseUp {
                        println("[ColorPicker] onMouseUp")
                        isHolding = false

                        updateSelectedColorIfPossible()
                    }

                    onMouseLeave {
                        if (!isHolding)
                            return@onMouseLeave

                        println("[ColorPicker] onMouseLeave")
                        isHolding = false

                        updateSelectedColorIfPossible()
                    }

                    onMouseMove {
                        if (!isHolding)
                            return@onMouseMove

                        println("[ColorPicker] onMouseMove")

                        val rect = (it.currentTarget as HTMLDivElement).getBoundingClientRect()
                        val x = it.clientX - rect.left // x position within the element.
                        val y = it.clientY - rect.top  // y position within the element.

                        println("clicked $x; $y")

                        val saturation = (x / rect.width)
                        val brightness = 1.0 - (y / rect.height)

                        println("Saturation: $saturation")
                        println("Brightness: $brightness")

                        // Instead of relying on onMouseLeave, we will coerce until the user unholds their mouse
                        selectedSaturation = saturation.toFloat().coerceIn(0.0f..1.0f)
                        selectedBrightness = brightness.toFloat().coerceIn(0.0f..1.0f)

                        updateSelectedColorIfPossible()
                    }

                    onClick {
                        val rect = (it.currentTarget as HTMLDivElement).getBoundingClientRect()
                        val x = it.clientX - rect.left // x position within the element.
                        val y = it.clientY - rect.top  // y position within the element.

                        println("clicked $x; $y")

                        val saturation = (x / rect.width)
                        val brightness = 1.0 - (y / rect.height)

                        println("Saturation: $saturation")
                        println("Brightness: $brightness")

                        selectedSaturation = saturation.toFloat()
                        selectedBrightness = brightness.toFloat()

                        updateSelectedColorIfPossible()
                    }
                }) {
                    Div(attrs = {
                        attr(
                            "style",
                            "background: linear-gradient(to right, #fff, rgba(255,255,255,0)); width: 100%; height: 100%; position: absolute; top: 0; left: 0;"
                        )
                    })

                    Div(attrs = {
                        attr(
                            "style",
                            "background: linear-gradient(to top, #000, rgba(0,0,0,0)); width: 100%; height: 100%; position: absolute; top: 0; left: 0;"
                        )
                    })

                    // Because we know the saturation/brightness values, we can know where the cursor should be!
                    Div(attrs = {
                        classes("color-picker-box-handle")
                        style {
                            bottom((selectedBrightness * 100).percent)
                            left((selectedSaturation * 100).percent)
                        }
                    })
                }

                ColorHuePicker(selectedHue) {
                    println("New hue is $it")
                    selectedHue = it

                    updateSelectedColorIfPossible()
                }
            }
        }
    }
}

@Composable
fun ColorHuePicker(selectedHue: Float, onHueChange: (Float) -> (Unit)) {
    println("RECOMPOSING HUE PICKER")

    var isHolding by remember { mutableStateOf(false) }

    println("[ColorHuePicker] isHolding? $isHolding")

    Div(attrs = {
        classes("color-picker-hue-slider")

        onMouseDown {
            println("[ColorHuePicker] onMouseDown")
            isHolding = true
        }

        onMouseUp {
            println("[ColorHuePicker] onMouseUp")
            isHolding = false
        }

        onMouseLeave {
            println("[ColorHuePicker] onMouseLeave")
            isHolding = false
        }

        onMouseMove {
            if (!isHolding)
                return@onMouseMove

            println("[ColorHuePicker] onMouseMove")

            val rect = (it.currentTarget as HTMLDivElement).getBoundingClientRect()
            val y = it.clientY - rect.top // y position within the element.

            onHueChange.invoke(1f - (y / rect.height).toFloat().coerceIn(0.0f..1.0f))
        }

        onClick {
            println("[ColorHuePicker] onClick")

            val rect = (it.currentTarget as HTMLDivElement).getBoundingClientRect()
            val y = it.clientY - rect.top // y position within the element.

            onHueChange.invoke(1f - (y / rect.height).toFloat().coerceIn(0.0f..1.0f))
        }
    }) {
        Div(attrs = {
            style {
                bottom((selectedHue * 100).percent)
            }
            classes("color-picker-hue-slider-handle")
        })
    }
}

@Composable
fun ColorPickerValueInput(label: String, endLabel: String? = null, block: @Composable () -> (Unit)) {
    Div(attrs = {
        classes("color-picker-value-label")
    }) {
        Text(label)
    }

    Div {
        block.invoke()
    }

    Div(attrs = {
        classes("color-picker-value-end-label")
    }) {
        if (endLabel != null)
            Text(endLabel)
    }
}

@Composable
@NoLiveLiterals
fun GabrielaColorPreview(currentColor: HSBColor) {
    println("RECOMPOSING GabrielaColorPreviewer $currentColor")

    // Create the Gabriela Color Preview Canvas, this NEEDS to be cached across recompositions!
    val gabiCanvas by remember { mutableStateOf(GabrielaColorPreviewCanvas(currentColor)) }

    SideEffect {
        GlobalScope.launch {
            gabiCanvas.updateColor(currentColor)
        }
    }

    Div(attrs = {
        classes("gabriela-color-preview")
    }) {
        // Yes, a canvas is the best way to do this
        // I know, you can hue-rotate in vanilla CSS, but the results aren't great + too many issues + doesn't work well
        // Yes, I know that you can use an SVG filter, but it flickers when changing the color
        Canvas(
            attrs = {
                attr("style", "position: absolute; top: 0; left: 0; width: 100%; height: 100%;")
                ref { canvas ->
                    gabiCanvas.mountIn(canvas)

                    onDispose {
                        gabiCanvas.unmount()
                    }
                }
            }
        )
    }
}

/**
 * Creates a [HSBColor] from a [FloatArray], where:
 * * 0: Hue
 * * 1: Saturation
 * * 2: Brightness
 */
private fun createHSBColorFromArray(array: FloatArray) = HSBColor(array[0], array[1], array[2])

data class HSBColor(
    val hue: Float,
    val saturation: Float,
    val brightness: Float
)