package net.perfectdreams.loritta.website.blog

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File

class Blog {
	var posts = listOf<Post>()

	val options = MutableDataSet().apply {
		this.set(Parser.EXTENSIONS, listOf(TablesExtension.create(), StrikethroughExtension.create()))
		this.set(HtmlRenderer.SOFT_BREAK, "<br />\n")
    }
	val parser = Parser.builder(options).build()
	val renderer = HtmlRenderer.builder(options).build()

	fun loadAllBlogPosts(): List<Post> {
		val path = File("${LorittaWebsite.FOLDER}/blog/")

		val posts = mutableListOf<Post>()

		path.listFiles().forEach {
			if (it.extension == "md")
				posts.add(loadBlogPost(it))
		}

		// Hora de preencher corretamente todos os "related"
		/* for (post in posts) {
			val related = post.metadata["related"] as List<String>?

			if (related != null) {
				for (relatedPostSlug in related) {
					val relatedPost = posts.firstOrNull { it.slug == relatedPostSlug }
					val relatedPosts = relatedPost?.metadata?.get("related") as MutableList<String>?

					if (relatedPosts != null) {
						relatedPosts.add(post.slug)
						val relatedMutable = relatedPost?.metadata as MutableMap<String, Any>
						relatedMutable["related"] = relatedPosts.distinct()
					}
				}
			}
		} */

		return posts
	}

	fun loadBlogPost(file: File): Post {
		val txt = file.readText()

		val split = txt.split("<---------->")
		val header = split[0]
		val contents = split[1]

		val postStuff = Constants.YAML.load<Map<String, Any>>(header)
		val mutablePostStuff = postStuff.toMutableMap()

		// You can re-use parser and renderer instances
		val document = parser.parse(contents)
		mutablePostStuff["content"] = renderer.render(document)

		return Post(file.nameWithoutExtension, mutablePostStuff)
	}
}