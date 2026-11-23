package dev.towhid.newsreader.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Single source of truth for stories.
 *
 * - Fetches items in parallel (the HN API returns one item per request).
 * - Keeps an in-memory cache so the detail screen and the Saved tab open instantly.
 * - Holds bookmarks as a [StateFlow] that every screen observes.
 */
class StoryRepository(
    private val api: HackerNewsApi,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    private val cache = mutableMapOf<Long, Story>()
    private val cacheLock = Mutex()

    private val _bookmarks = MutableStateFlow<List<Story>>(emptyList())
    val bookmarks: StateFlow<List<Story>> = _bookmarks.asStateFlow()

    suspend fun feed(type: FeedType): List<Story> = coroutineScope {
        val ids = api.feedIds(type).take(pageSize)
        val stories = ids
            .map { id -> async { runCatching { api.item(id) }.getOrNull()?.toStoryOrNull() } }
            .awaitAll()
            .filterNotNull()
        cacheLock.withLock { stories.forEach { cache[it.id] = it } }
        stories
    }

    suspend fun story(id: Long): Story? {
        cacheLock.withLock { cache[id] }?.let { return it }
        val story = api.item(id)?.toStoryOrNull() ?: return null
        cacheLock.withLock { cache[id] = story }
        return story
    }

    suspend fun topComments(story: Story, limit: Int = DEFAULT_COMMENT_LIMIT): List<Comment> = coroutineScope {
        story.commentIds
            .take(limit)
            .map { id -> async { runCatching { api.item(id) }.getOrNull()?.toCommentOrNull() } }
            .awaitAll()
            .filterNotNull()
    }

    fun isBookmarked(id: Long): Boolean = _bookmarks.value.any { it.id == id }

    fun toggleBookmark(story: Story) {
        _bookmarks.update { current ->
            if (current.any { it.id == story.id }) current.filterNot { it.id == story.id }
            else listOf(story) + current
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
        const val DEFAULT_COMMENT_LIMIT = 20
    }
}
