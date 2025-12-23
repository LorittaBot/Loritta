package net.perfectdreams.dora.utils

import com.ibm.icu.text.MessagePatternUtil

object ICU4JUtils {
    fun contextualTransform(message: String, transform: (String) -> (Unit)): String {
        val messageNode = MessagePatternUtil.buildMessageNode(message)
        return processMessageNode(messageNode, transform)
    }

    fun processMessageNode(messageNode: MessagePatternUtil.MessageNode, transform: (String) -> (Unit)): String {
        return buildString {
            for (node in messageNode.contents) {
                append(processNode(node, transform))
            }
        }
    }

    fun processNode(node: MessagePatternUtil.Node, transform: (String) -> (Unit)): String {
        return when (node) {
            is MessagePatternUtil.TextNode -> node.text.uppercase()
            is MessagePatternUtil.ArgNode -> processArgNode(node, transform)
            is MessagePatternUtil.MessageContentsNode -> {
                // Handle the # placeholder for plural/select
                if (node.type == MessagePatternUtil.MessageContentsNode.Type.REPLACE_NUMBER) {
                    "#"
                } else {
                    ""
                }
            }
            else -> ""
        }
    }

    fun processArgNode(argNode: MessagePatternUtil.ArgNode, transform: (String) -> (Unit)): String {
        return buildString {
            append("{")
            append(argNode.name) // Keep placeholder name as-is

            val typeName = argNode.typeName
            if (typeName != null) {
                append(",")
                append(typeName)

                val complexStyle = argNode.complexStyle
                val simpleStyle = argNode.simpleStyle

                when {
                    complexStyle != null -> {
                        append(",")
                        for (variant in complexStyle.variants) {
                            append(" ")
                            append(variant.selector) // Keep selector as-is (e.g., "=0", "one", "other")
                            append(" {")
                            append(processMessageNode(variant.message, transform))
                            append("}")
                        }
                    }
                    simpleStyle != null -> {
                        append(",")
                        append(simpleStyle)
                    }
                }
            }
            append("}")
        }
    }
}