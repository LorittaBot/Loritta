package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import mu.KotlinLogging
import kotlin.reflect.KClass

fun KotlinLogging.loggerClassName(clazz: KClass<*>) = KotlinLogging.logger(clazz.simpleName ?: "Unknown")