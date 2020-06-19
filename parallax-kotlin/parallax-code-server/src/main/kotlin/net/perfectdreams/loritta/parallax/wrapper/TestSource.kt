package net.perfectdreams.loritta.parallax.wrapper

class TestSource {
	fun where(t: Any) {
		println(t::class)

		t as java.util.function.Function<Void?, Any?>

		t.apply(null)
	}
}