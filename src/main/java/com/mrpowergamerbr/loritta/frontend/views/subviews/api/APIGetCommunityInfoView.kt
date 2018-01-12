package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup

class APIGetCommunityInfoView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().matches(Regex("^/api/v1/get_community_info"))
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val json = JsonObject()

		if (!req.param("aminoInviteLink").isSet) {
			json["error"] = "Missing aminoInviteLink param"
			return json.toString()
		}

		val aminoInviteLink = req.param("aminoInviteLink").value()


		val httpRequest = HttpRequest.get(aminoInviteLink)

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown Amino community"
			return json.toString()
		}

		val body = httpRequest.body()
		val document = Jsoup.parse(body)

		val title = document.getElementsByAttribute("property").first { it.attr("property") == "og:site_name" }
		val description = document.getElementsByAttribute("property").first { it.attr("property") == "og:description" }
		val iconUrl = document.getElementsByAttribute("property").first { it.attr("property") == "og:image" }

		json["title"] = title.attr("content").replace(" | aminoapps.com", "")
		json["description"] = description.attr("content")
		json["iconUrl"] = iconUrl.attr("content")

		val pattern = "window\\.ServerData\\.deeplink = \"narviiapp:\\/\\/(x[0-9]+)\\/.+\";".toPattern().matcher(body).apply { find() }
		json["communityId"] = pattern.group(1)
		return json.toString()
	}
}