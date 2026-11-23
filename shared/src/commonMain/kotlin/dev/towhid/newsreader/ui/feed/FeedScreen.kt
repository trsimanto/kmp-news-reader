package dev.towhid.newsreader.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.towhid.newsreader.data.Story
import dev.towhid.newsreader.resources.Res
import dev.towhid.newsreader.resources.app_title
import dev.towhid.newsreader.resources.empty_saved
import dev.towhid.newsreader.resources.error_generic
import dev.towhid.newsreader.resources.retry
import dev.towhid.newsreader.resources.tab_best
import dev.towhid.newsreader.resources.tab_new
import dev.towhid.newsreader.resources.tab_saved
import dev.towhid.newsreader.resources.tab_top
import dev.towhid.newsreader.ui.components.LoadingView
import dev.towhid.newsreader.ui.components.MessageView
import dev.towhid.newsreader.ui.components.StoryCard
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val tabs: List<Pair<FeedTab, StringResource>> = listOf(
    FeedTab.TOP to Res.string.tab_top,
    FeedTab.NEW to Res.string.tab_new,
    FeedTab.BEST to Res.string.tab_best,
    FeedTab.SAVED to Res.string.tab_saved,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onStoryClick: (Long) -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.app_title)) }) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tabs.indexOfFirst { it.first == state.tab }) {
                tabs.forEach { (tab, label) ->
                    Tab(
                        selected = state.tab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(stringResource(label)) },
                    )
                }
            }
            when {
                state.isLoading -> LoadingView()
                state.hasError -> MessageView(
                    message = stringResource(Res.string.error_generic),
                    actionLabel = stringResource(Res.string.retry),
                    onAction = viewModel::refresh,
                )
                state.tab == FeedTab.SAVED && state.stories.isEmpty() ->
                    MessageView(stringResource(Res.string.empty_saved))
                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    StoryList(
                        stories = state.stories,
                        bookmarkedIds = state.bookmarkedIds,
                        onStoryClick = onStoryClick,
                        onToggleBookmark = viewModel::toggleBookmark,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryList(
    stories: List<Story>,
    bookmarkedIds: Set<Long>,
    onStoryClick: (Long) -> Unit,
    onToggleBookmark: (Story) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(stories, key = { it.id }) { story ->
            StoryCard(
                story = story,
                isBookmarked = story.id in bookmarkedIds,
                onClick = { onStoryClick(story.id) },
                onToggleBookmark = { onToggleBookmark(story) },
            )
        }
    }
}
