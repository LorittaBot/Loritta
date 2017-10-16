package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup

class APIGetCommunityIconView : AbstractView() {
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

		val document = Jsoup.parse(httpRequest.body())

		val title = document.getElementsByAttribute("property").first { it.attr("property") == "og:title" }
		val description = document.getElementsByAttribute("property").first { it.attr("property") == "og:description" }
		val iconUrl = document.getElementsByAttribute("property").first { it.attr("property") == "og:image" }
		val communityId = document.getElementsByClass("deeplink-holder").first()
		json["title"] = title.attr("content")
		json["description"] = description.attr("content")
		json["iconUrl"] = iconUrl.attr("content")
		json["communityId"] = communityId.attr("data-link").split("/")[2]
		return json.toString()
	}
}