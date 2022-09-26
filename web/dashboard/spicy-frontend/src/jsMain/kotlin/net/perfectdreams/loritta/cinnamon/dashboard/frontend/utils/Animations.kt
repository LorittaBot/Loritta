package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

// I know, this is a super extremely hacky hack hack hack!
// It would be better if we just used Web Animations API from Kotlin/JS, but because Kotlin/JS is not that fast when recompiling
// (and incremental IR compilation is still super buggy)
// I think it is better to just load from the html file directly, and this has the advantage that it is easier to do animation prototypes
// because reloading the current animation is easy
//
// And besides, the animations are so simple that it wouldn't be that hard to recreate them in Kotlin/JS using WAAPI down the road :)
object Animations {
    val lorittaSonhos = parseAnimationFromHTML(htmlLorittaSonhos)

    private fun parseAnimationFromHTML(str: String): HackyAnimation {
        // This is a horrible, stupid hack
        val codeToBeEvaluated = str.substringAfter("<script>")
            .substringBefore("</script>")
        val codeToBeInserted = str.substringBefore("<script>")

        return HackyAnimation(
            codeToBeInserted,
            codeToBeEvaluated
        )
    }

    data class HackyAnimation(
        val codeToBeInserted: String,
        val codeToBeEvaluated: String
    )
}

@JsModule("./animations/loritta-sonhos.html")
@JsNonModule
external val htmlLorittaSonhos: dynamic