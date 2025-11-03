package net.perfectdreams.loritta.morenitta.website.utils

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.util.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import org.jetbrains.exposed.sql.ResultRow

object WebsiteUtils {
	val handledStatusBefore = AttributeKey<Boolean>("handledStatusBefore")

	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(loritta: LorittaBot, code: LoriWebCode, message: String? = null, data: ((JsonObject) -> Unit)? = null): JsonObject {
		val result = jsonObject("error" to createErrorObject(loritta, code, message))
		data?.invoke(result)
		return result
	}

	/**
	 * Creates an JSON object containing the code error
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object with the error
	 */
	fun createErrorObject(loritta: LorittaBot, code: LoriWebCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
			"code" to code.errorId,
			"reason" to code.fancyName,
			"help" to "${loritta.config.loritta.website.url}docs/api"
		)

		if (message != null) {
			jsonObject["message"] = message
		}

		return jsonObject
	}

	fun transformToJson(user: CachedUserInfo): JsonObject {
		return jsonObject(
			"id" to user.id,
			"name" to user.name,
			"discriminator" to user.discriminator,
			"effectiveAvatarUrl" to user.effectiveAvatarUrl
		)
	}

    fun checkIfAccountHasMFAEnabled(loritta: LorittaBot, userIdentification: DiscordOAuth2UserIdentification): Boolean {
        // This is a security measure, to avoid "high risk" purchases.
        // We will require that users need to verify their account + have MFA enabled.
        if (!userIdentification.verified)
            throw WebsiteAPIException(
                HttpStatusCode.Forbidden,
                createErrorPayload(
                    loritta,
                    LoriWebCode.UNVERIFIED_ACCOUNT
                )
            )

        if (userIdentification.mfaEnabled == false)
            throw WebsiteAPIException(
                HttpStatusCode.Forbidden,
                createErrorPayload(
                    loritta,
                    LoriWebCode.MFA_DISABLED
                )
            )

        return true
    }

	suspend fun toSerializable(loritta: LorittaBot, profileDesign: ProfileDesign) = loritta.pudding.transaction { fromProfileDesignToSerializable(loritta, profileDesign.readValues) }
	suspend fun fromProfileDesignToSerializable(loritta: LorittaBot, profileDesign: ResultRow) = loritta.pudding.transaction { ProfileDesign.wrapRow(profileDesign).toSerializable() }

	fun buildAsHtml(originalString: String, onControlChar: (String) -> (Unit), onStringBuild: (String) -> (Unit)) {
		var isControl = false

		val genericStringBuilder = StringBuilder()
		val controlStringBuilder = StringBuilder()

		for (ch in originalString) {
			if (isControl) {
				if (ch == '}') {
					onControlChar.invoke(controlStringBuilder.toString())
					isControl = false
					controlStringBuilder.clear()
					continue
				}

				controlStringBuilder.append(ch)
				continue
			}

			if (ch == '{') {
				onStringBuild.invoke(genericStringBuilder.toString())
				genericStringBuilder.clear()
				isControl = true
				continue
			}

			genericStringBuilder.append(ch)
		}

		onStringBuild.invoke(genericStringBuilder.toString())
	}
}