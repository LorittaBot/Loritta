package net.perfectdreams.loritta.discordchatmarkdownparser

sealed class MarkdownNode

sealed class CompositeMarkdownNode(val children: List<MarkdownNode>) : MarkdownNode()
class ChatRootNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class BoldNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class ItalicsNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class StrikethroughNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class CodeBlockNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class InlineCodeNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class HeaderNode(val level: Int, children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class SubTextNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class BlockQuoteNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class MaskedLinkNode(val url: String, children: List<MarkdownNode>) : CompositeMarkdownNode(children)
class DiscordTextNode(children: List<MarkdownNode>) : CompositeMarkdownNode(children)

sealed class LeafMarkdownNode : MarkdownNode()
class TextNode(val text: String) : LeafMarkdownNode()
class CodeTextNode(val text: String) : LeafMarkdownNode()
class LinkNode(val url: String) : LeafMarkdownNode()
class DiscordEmojiEntityNode(
    val id: Long,
    val name: String,
    val animated: Boolean
) : LeafMarkdownNode()
class DiscordCommandEntityNode(
    val id: Long,
    val path: String
) : LeafMarkdownNode()
class DiscordUserMentionEntityNode(
    val id: Long
) : LeafMarkdownNode()
data object DiscordEveryoneMentionEntityNode : LeafMarkdownNode()
data object DiscordHereMentionEntityNode : LeafMarkdownNode()