package dev.towhid.newsreader.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.towhid.newsreader.data.FeedType
import dev.towhid.newsreader.data.Story
import dev.towhid.newsreader.data.StoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FeedTab { TOP, NEW, BEST, SAVED }

data class FeedUiState(
    val tab: FeedTab = FeedTab.TOP,
    val stories: List<Story> = emptyList(),
    val bookmarkedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
)

/** MVI-style view model: the screen renders [state] and sends intents through public functions. */
class FeedViewModel(private val repository: StoryRepository) : ViewModel() {

    private val feedState = MutableStateFlow(FeedUiState(isLoading = true))
    private var loadJob: Job? = null

    val state: StateFlow<FeedUiState> =
        combine(feedState, repository.bookmarks) { feed, bookmarks ->
            val saved = feed.tab == FeedTab.SAVED
            feed.copy(
                stories = if (saved) bookmarks else feed.stories,
                bookmarkedIds = bookmarks.map { it.id }.toSet(),
                isLoading = !saved && feed.isLoading,
                hasError = !saved && feed.hasError,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState(isLoading = true))

    init {
        load(FeedTab.TOP, refreshing = false)
    }

    fun selectTab(tab: FeedTab) {
        if (tab == feedState.value.tab) return
        feedState.update { it.copy(tab = tab, stories = emptyList()) }
        if (tab != FeedTab.SAVED) load(tab, refreshing = false)
    }

    fun refresh() {
        val tab = feedState.value.tab
        if (tab != FeedTab.SAVED) load(tab, refreshing = true)
    }

    fun toggleBookmark(story: Story) = repository.toggleBookmark(story)

    private fun load(tab: FeedTab, refreshing: Boolean) {
        val feedType = when (tab) {
            FeedTab.TOP -> FeedType.TOP
            FeedTab.NEW -> FeedType.NEW
            FeedTab.BEST -> FeedType.BEST
            FeedTab.SAVED -> return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            feedState.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, hasError = false) }
            try {
                val stories = repository.feed(feedType)
                feedState.update { it.copy(stories = stories, isLoading = false, isRefreshing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                feedState.update { it.copy(isLoading = false, isRefreshing = false, hasError = true) }
            }
        }
    }
}
