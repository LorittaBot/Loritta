package net.perfectdreams.loritta.cinnamon.platform.modals.components

import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.TextInputBuilder

fun ActionRowBuilder.textInput(
    option: ModalComponent<String>,
    style: TextInputStyle,
    label: String,
    builder: TextInputBuilder.() -> (Unit)
) {
   this.textInput(style, option.name, label, builder)
}