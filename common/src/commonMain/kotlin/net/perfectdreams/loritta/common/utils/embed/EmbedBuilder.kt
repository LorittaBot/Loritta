package net.perfectdreams.loritta.common.utils.embed

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class EmbedBuilder {
    var author: Author? = null
    var body: Body? = null
    var footer: Footer? = null
    var images: Images? = null

    val fields = mutableListOf<Field>()

    /**
     * This is the method we can use
     * for defining the embed's author
     *
     * @param callback Declaration of the author's data
     */
    fun author(name: String, callback: Author.() -> Unit) {
        this.author = Author(name).apply(callback)
    }

    /**
     * This is the method we can use
     * for defining the embed's body data (title, description, color).
     *
     * @param callback Declaration of the body's data
     */
    fun body(callback: Body.() -> Unit) {
        this.body = Body().apply(callback)
    }

    fun field(name: String, value: String, callback: Field.() -> Unit = {}) {
        fields.add(Field(name, value).apply(callback))
    }

    /**
     * This is the method we can use
     * for defining the embed's images (thumbnail, image).
     *
     * @param callback Declaration of the images
     */
    fun images(callback: Images.() -> Unit) {
        this.images = Images().apply(callback)
    }

    /**
     * This is the method we can use
     * for defining the embed's footer (also including the timestamp).
     *
     * @param callback Declaration of the footer's data
     */
    fun footer(text: String, callback: Footer.() -> Unit) {
        this.footer = Footer(text).apply(callback)
    }

    fun build(): LorittaEmbed {
        return LorittaEmbed(
            title = body?.title,
            description = body?.description,
            image = images?.image,
            thumbnail = images?.thumbnail,
            color = body?.color,
            footer = footer?.let {LorittaEmbed.Footer(it.text, it.icon)},
            author = author?.let {LorittaEmbed.Author(it.name, it.icon, it.url)},
            timestamp = footer?.timestamp,
            fields = fields.map {LorittaEmbed.Field(it.name, it.value, it.inline)}
        )
    }

    class Author(val name: String) {
        var url: String? = null
        var icon: String? = null
    }

    class Body {
        var title: String? = null
        var description: String? = null
        var color: LorittaColor? = null
    }

    class Field(val name: String, val value: String) {
        var inline: Boolean = false
    }

    class Images {
        var image: String? = null
        var thumbnail: String? = null
    }

    class Footer(val text: String) {
        var icon: String? = null
        var timestamp: Instant? = null
    }

}

fun embed(embed: EmbedBuilder.() -> Unit): EmbedBuilder = EmbedBuilder().apply(embed)