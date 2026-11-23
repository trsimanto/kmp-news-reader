package dev.towhid.newsreader.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeHackerNewsApi(
    private val feeds: Map<FeedType, List<Long>> = emptyMap(),
    private val items: Map<Long, HnItem> = emptyMap(),
) : HackerNewsApi {
    var itemCalls = 0
        private set

    override suspend fun feedIds(feed: FeedType): List<Long> = feeds[feed].orEmpty()

    override suspend fun item(id: Long): HnItem? {
        itemCalls++
        return items[id]
    }
}

private fun story(id: Long, title: String? = "Story $id", deleted: Boolean = false, dead: Boolean = false) =
    HnItem(id = id, type = "story", by = "user$id", time = 1_000L, title = title, score = id.toInt(), deleted = deleted, dead = dead)

class StoryRepositoryTest {

    @Test
    fun feedFiltersDeletedDeadAndUntitledItems() = runTest {
        val api = FakeHackerNewsApi(
            feeds = mapOf(FeedType.TOP to listOf(1L, 2L, 3L, 4L, 5L)),
            items = mapOf(
                1L to story(1),
                2L to story(2, deleted = true),
                3L to story(3, dead = true),
                4L to story(4, title = null),
                5L to story(5),
            ),
        )
        val stories = StoryRepository(api).feed(FeedType.TOP)

        assertEquals(listOf(1L, 5L), stories.map { it.id })
        assertEquals("Story 1", stories.first().title)
        assertEquals("user1", stories.first().author)
    }

    @Test
    fun feedRespectsPageSize() = runTest {
        val ids = (1L..10L).toList()
        val api = FakeHackerNewsApi(
            feeds = mapOf(FeedType.NEW to ids),
            items = ids.associateWith { story(it) },
        )
        val stories = StoryRepository(api, pageSize = 3).feed(FeedType.NEW)

        assertEquals(3, stories.size)
        assertEquals(3, api.itemCalls)
    }

    @Test
    fun storyIsServedFromCacheAfterFeedLoad() = runTest {
        val api = FakeHackerNewsApi(
            feeds = mapOf(FeedType.BEST to listOf(7L)),
            items = mapOf(7L to story(7)),
        )
        val repository = StoryRepository(api)
        repository.feed(FeedType.BEST)
        val callsAfterFeed = api.itemCalls

        val cached = repository.story(7L)

        assertNotNull(cached)
        assertEquals(callsAfterFeed, api.itemCalls)
    }

    @Test
    fun storyReturnsNullForUnknownId() = runTest {
        val repository = StoryRepository(FakeHackerNewsApi())
        assertNull(repository.story(99L))
    }

    @Test
    fun toggleBookmarkAddsAndRemoves() = runTest {
        val api = FakeHackerNewsApi(items = mapOf(1L to story(1)))
        val repository = StoryRepository(api)
        val loaded = assertNotNull(repository.story(1L))

        repository.toggleBookmark(loaded)
        assertTrue(repository.isBookmarked(1L))
        assertEquals(listOf(1L), repository.bookmarks.value.map { it.id })

        repository.toggleBookmark(loaded)
        assertFalse(repository.isBookmarked(1L))
        assertTrue(repository.bookmarks.value.isEmpty())
    }

    @Test
    fun topCommentsSkipsDeletedComments() = runTest {
        val api = FakeHackerNewsApi(
            items = mapOf(
                1L to story(1).copy(kids = listOf(10L, 11L, 12L)),
                10L to HnItem(id = 10L, type = "comment", by = "a", text = "Hello"),
                11L to HnItem(id = 11L, type = "comment", deleted = true),
                12L to HnItem(id = 12L, type = "comment", by = "b", text = "World", kids = listOf(20L)),
            ),
        )
        val repository = StoryRepository(api)
        val loaded = assertNotNull(repository.story(1L))

        val comments = repository.topComments(loaded)

        assertEquals(listOf(10L, 12L), comments.map { it.id })
        assertEquals(1, comments.last().replyCount)
    }

    @Test
    fun domainIsExtractedFromUrl() {
        val s = story(1).copy(url = "https://www.github.com/trsimanto/repo").toStoryOrNull()
        assertEquals("github.com", s?.domain)
        assertNull(story(2).toStoryOrNull()?.domain)
    }
}
