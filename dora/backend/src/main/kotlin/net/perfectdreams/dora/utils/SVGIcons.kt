package net.perfectdreams.dora.utils

import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIconManager

object SVGIcons : SVGIconManager(SVGIcons::class) {
    val Key = register("key", "/svg_icons/phosphor/fill/key.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Robot = register("robot", "/svg_icons/phosphor/fill/robot.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val NotePencil = register("note-pencil", "/svg_icons/phosphor/fill/note-pencil.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val List = register("list", "/svg_icons/phosphor/bold/list.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretLeft = register("caret-left", "/svg_icons/phosphor/bold/caret-left.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretRight = register("caret-right", "/svg_icons/phosphor/bold/caret-right.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CursorText = register("cursor-text", "/svg_icons/phosphor/bold/cursor-text.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
}