package dev.towhid.newsreader.data

import kotlinx.serialization.Serializable

/** A Hacker News item (story or comment), as returned by the public Firebase API. */
@Serializable
data class HnItem(
    val id: Long,
    val type: String? = null,
    val by: String? = null,
    val time: Long = 0,
    val title: String? = null,
    val url: String? = null,
    val text: String? = null,
    val score: Int = 0,
    val descendants: Int = 0,
    val kids: List<Long> = emptyList(),
    val deleted: Boolean = false,
    val dead: Boolean = false,
)

/** Domain model used by the UI. */
data class Story(
    val id: Long,
    val title: String,
    val author: String,
    val url: String?,
    val text: String?,
    val score: Int,
    val commentCount: Int,
    val postedAtEpochSeconds: Long,
    val commentIds: List<Long>,
) {
    /** Host of the linked article, e.g. "github.com", or null for Ask/Show HN text posts. */
    val domain: String?
        get() = url
            ?.substringAfter("://")
            ?.substringBefore("/")
            ?.removePrefix("www.")
            ?.takeIf { it.isNotBlank() }
}

data class Comment(
    val id: Long,
    val author: String,
    val text: String,
    val postedAtEpochSeconds: Long,
    val replyCount: Int,
)

enum class FeedType(val path: String) {
    TOP("topstories"),
    NEW("newstories"),
    BEST("beststories"),
}

fun HnItem.toStoryOrNull(): Story? {
    if (deleted || dead || title.isNullOrBlank()) return null
    return Story(
        id = id,
        title = title,
        author = by ?: "unknown",
        url = url,
        text = text,
        score = score,
        commentCount = descendants,
        postedAtEpochSeconds = time,
        commentIds = kids,
    )
}

fun HnItem.toCommentOrNull(): Comment? {
    if (deleted || dead || text.isNullOrBlank()) return null
    return Comment(
        id = id,
        author = by ?: "unknown",
        text = text,
        postedAtEpochSeconds = time,
        replyCount = kids.size,
    )
}
