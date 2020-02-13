package net.perfectdreams.loritta.website.blog

import java.text.SimpleDateFormat

class Post(val slug: String, val metadata: Map<String, Any>) {
	val title: String
		get() = metadata["title"] as String

	val author: String?
		get() = metadata["author"] as String?

	val content: String
		get() = metadata["content"] as String

	val isPublic: Boolean
		get() = metadata["public"] as Boolean? ?: false

	val date by lazy {
		val date = metadata["date"] as String

		val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")

		sdf.parse(date)
	}
}