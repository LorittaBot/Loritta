package net.perfectdreams.loritta.common.locale

import kotlinx.serialization.Serializable

// The reason everything is in the same class is because LocaleDataType is sealed
// So because "sealed" also means "not public", we can't move the other classes (like LocaleKeyData) to other files!
//
// (It does make sense, because if we could use different files, kotlinx.serialization wouldn't be able to create the polymorphism *magically*)
@Serializable
sealed class LocaleDataType

/**
 * LocaleKeyData is used to store a locale [key] and [arguments].
 *
 * The difference between using [LocaleKeyData] and a simple [String] is because [LocaleKeyData] allows you
 * to store the locale arguments too.
 *
 * And because the class is [Serializable], it is possible to use it inside of serializable objects, allowing
 * to share the locale key itself, with their arguments too.
 *
 * @param key       the locale string key
 * @param arguments the required arguments of the locale key
 */
@Serializable
data class LocaleKeyData(val key: String, val arguments: List<LocaleDataType>? = null) : LocaleDataType()

/**
 * LocaleStringData is used to store a [text] related to a locale key.
 *
 * @param text the text used in the locale
 */
@Serializable
data class LocaleStringData(val text: String) : LocaleDataType()