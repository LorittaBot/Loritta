package net.perfectdreams.loritta.dashboard.frontend.utils

import web.window.window

fun isUserUsingAdblock() = window.asDynamic().isUserUsingAdblock != false