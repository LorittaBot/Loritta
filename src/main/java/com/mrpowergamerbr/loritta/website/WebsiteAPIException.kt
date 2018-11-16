package com.mrpowergamerbr.loritta.website

import com.google.gson.JsonElement
import org.jooby.Status

class WebsiteAPIException(val status: Status, val payload: JsonElement) : RuntimeException()