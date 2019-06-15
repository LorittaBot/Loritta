package net.perfectdreams.loritta.website.ws

fun main() {
    while (true) {
        fun matches(path: String, input: String): Boolean {
            val sourceSplit = path.removeSuffix("/").split("/")
            val inputSplit = input.removeSuffix("/").split("/")

            var inputSplitLength = 0

            for (index in 0 until Math.max(sourceSplit.size, inputSplit.size)) {
                val sInput = sourceSplit.getOrNull(index)
                val iInput = inputSplit.getOrNull(index)

                // Check if it is a group match
                if (sInput != null && sInput.startsWith("{") && sInput.endsWith("}")) {
                    if (iInput == null && sInput.endsWith("?}")) {
                        inputSplitLength++
                        continue
                    }

                    inputSplitLength++
                    continue
                }

                if (iInput == null)
                    return false

                if (iInput != sInput) // Input does not match
                    return false

                inputSplitLength++
            }

            return true
        }

        fun getPathParameters(path: String, input: String): Map<String, String> {
            val parameters = mutableMapOf<String, String>()

            val sourceSplit = path.removeSuffix("/").split("/")
            val inputSplit = input.removeSuffix("/").split("/")

            var inputSplitLength = 0

            for (index in 0 until Math.max(sourceSplit.size, inputSplit.size)) {
                val sInput = sourceSplit.getOrNull(index)
                val iInput = inputSplit.getOrNull(index)

                // Check if it is a group match
                if (sInput != null && sInput.startsWith("{") && sInput.endsWith("}")) {
                    if (iInput == null && sInput.endsWith("?}")) {
                        inputSplitLength++
                        continue
                    }

                    parameters[sInput.removePrefix("{").removeSuffix("?}").removeSuffix("}")] = iInput ?: "?"

                    inputSplitLength++
                    continue
                }

                if (iInput == null)
                    return parameters

                if (iInput != sInput) // Input does not match
                    return parameters

                inputSplitLength++
            }

            return parameters
        }

        val path = readLine()!!
        println("Result is ${matches("/fanarts/{artist?}", path)}")
        println("Params: ${getPathParameters("/fanarts/{artist?}", path)}")
    }
}