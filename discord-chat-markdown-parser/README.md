# Discord Chat Markdown Parser

Parses Discord Chat Markdown + Entities to an AST (Abstract Syntax Tree).

We decided to reinvent the wheel instead of using yet another library because none of the markdown libraries fit our needs. We just want to parse Discord Chat markdown dangit! We don't need things wrapped in `<p>`, we don't want new lines to be collapsed, so on and so forth...

Because it is parsed to an AST, you can do it whatever whenever with it after it is being parsed! Visit all the nodes, transform them into something else like an HTML tree! Heck, you can even convert it back to markdown if your heart desires. Fun!

## Examples

### Cleaning up Markdown output

This will append only `TextNode`s to the output, providing a markdown-free text output.

```kotlin
fun main() {
    val parser = DiscordChatMarkdownParser()
    val node = parser.parse("hello **world**!")

    val builder = StringBuilder()
    recursiveCleanPrint(node, builder)

    println(builder.toString())
}

fun recursiveCleanPrint(node: MarkdownNode, builder: StringBuilder) {
    when (node) {
        is CompositeMarkdownNode -> {
            for (children in node.children) {
                recursiveCleanPrint(children, builder)
            }
        }
        is LeafMarkdownNode -> {
            when (node) {
                is TextNode -> {
                    builder.append(node.text)
                }

                else -> {}
            }
        }
    }
}
```

Result:

```
hello world!
```

More samples can be found on the `:discord-chat-markdown-parser` module.