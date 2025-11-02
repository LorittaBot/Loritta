package net.perfectdreams.loritta.dashboard.frontend.utils

import web.window.window

fun isUserUsingUBlockOrigin() = window.asDynamic().isUserUsingUBlockOrigin != false
fun isUserUsingEasyListPortuguese() = window.asDynamic().isUserUsingEasyListPortuguese != false
fun isUserUsingAdGuardSpanishPortuguese() = window.asDynamic().isUserUsingAdGuardSpanishPortuguese != false
fun isUserUsingBraveShields() = window.asDynamic().isUserUsingBraveShields != false

fun isUserUsingAdBlock() = isUserUsingUBlockOrigin() || isUserUsingEasyListPortuguese() || isUserUsingAdGuardSpanishPortuguese() || isUserUsingBraveShields()