package net.perfectdreams.spicymorenitta.components.colorpicker

object EtherealGambiUtils {
    val scaleDownToWidthVariantsPresets = listOf(
        ScaleDownToWidthImageVariantPreset("32w", 32),
        ScaleDownToWidthImageVariantPreset("64w", 64),
        ScaleDownToWidthImageVariantPreset("160w", 160),
        ScaleDownToWidthImageVariantPreset("320w", 320),
        ScaleDownToWidthImageVariantPreset("640w", 640),
        ScaleDownToWidthImageVariantPreset("960w", 960),
        ScaleDownToWidthImageVariantPreset("1280w", 1280),
        ScaleDownToWidthImageVariantPreset("1920w", 1920),
        ScaleDownToWidthImageVariantPreset("2560w", 2560)
    )

    data class ScaleDownToWidthImageVariantPreset(
        val name: String,
        val width: Int
    ) {
        fun variantWithPrefix() = "@$name"
    }
}