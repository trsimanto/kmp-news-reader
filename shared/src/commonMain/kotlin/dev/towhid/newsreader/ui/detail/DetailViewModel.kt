package dev.towhid.newsreader.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.towhid.newsreader.data.Comment
import dev.towhid.newsreader.data.Story
import dev.towhid.newsreader.data.StoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val story: Story? = null,
    val comments: List<Comment> = emptyList(),
    val isBookmarked: Boolean = false,
    val isLoadingComments: Boolean = true,
    val hasError: Boolean = false,
)

class DetailViewModel(private val repository: StoryRepository) : ViewModel() {

    private val detailState = MutableStateFlow(DetailUiState())

    val state: StateFlow<DetailUiState> =
        combine(detailState, repository.bookmarks) { detail, bookmarks ->
            detail.copy(isBookmarked = bookmarks.any { it.id == detail.story?.id })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun load(storyId: Long) {
        if (detailState.value.story?.id == storyId) return
        viewModelScope.launch {
            try {
                val story = repository.story(storyId)
                if (story == null) {
                    detailState.update { it.copy(hasError = true, isLoadingComments = false) }
                    return@launch
                }
                detailState.update { it.copy(story = story) }
                val comments = repository.topComments(story)
                detailState.update { it.copy(comments = comments, isLoadingComments = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                detailState.update { it.copy(hasError = true, isLoadingComments = false) }
            }
        }
    }

    fun toggleBookmark() {
        detailState.value.story?.let(repository::toggleBookmark)
    }
}
