package net.perfectdreams.loritta.common.builder

/**
 * Builder Factory provides builder instances of common classes
 *
 * This is used to allow platforms to override builders to replace methods.
 */
interface BuilderFactory {
    fun createReplyBuilder(): LorittaReplyBuilder
    fun createMultiReplyBuilder(): LorittaMultiReplyBuilder

    /**
     * Default implementation of the BuilderFactory with the default builders
     */
    object DefaultBuilderFactory : BuilderFactory {
        override fun createReplyBuilder() = LorittaReplyBuilder()
        override fun createMultiReplyBuilder() = LorittaMultiReplyBuilder(this)
    }
}