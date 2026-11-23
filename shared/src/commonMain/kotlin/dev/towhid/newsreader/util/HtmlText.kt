package dev.towhid.newsreader.util

private val tagRegex = Regex("<[^>]+>")
private val entities = mapOf(
    "&quot;" to "\"",
    "&#x27;" to "'",
    "&#39;" to "'",
    "&#x2F;" to "/",
    "&gt;" to ">",
    "&lt;" to "<",
    "&nbsp;" to " ",
    "&amp;" to "&",
)

/** HN returns comment bodies as HTML. Convert the common subset to plain text. */
fun htmlToPlainText(html: String): String {
    var text = html
        .replace("<p>", "\n\n")
        .replace("<br>", "\n")
        .replace(tagRegex, "")
    entities.forEach { (entity, value) -> text = text.replace(entity, value) }
    return text.trim()
}
