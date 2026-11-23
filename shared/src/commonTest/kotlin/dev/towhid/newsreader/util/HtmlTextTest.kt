package dev.towhid.newsreader.util

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTextTest {
    @Test
    fun stripsTagsAndDecodesEntities() {
        val html = "Hello <i>world</i> &amp; friends<p>It&#x27;s &quot;great&quot; &gt; ok"
        assertEquals("Hello world & friends\n\nIt's \"great\" > ok", htmlToPlainText(html))
    }

    @Test
    fun removesLinkMarkupButKeepsText() {
        assertEquals("see github.com", htmlToPlainText("see <a href=\"https://github.com\">github.com</a>"))
    }
}
