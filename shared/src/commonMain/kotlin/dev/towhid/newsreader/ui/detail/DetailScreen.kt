package dev.towhid.newsreader.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.towhid.newsreader.data.Comment
import dev.towhid.newsreader.data.Story
import dev.towhid.newsreader.resources.Res
import dev.towhid.newsreader.resources.back
import dev.towhid.newsreader.resources.comments
import dev.towhid.newsreader.resources.error_generic
import dev.towhid.newsreader.resources.no_comments
import dev.towhid.newsreader.resources.open_article
import dev.towhid.newsreader.ui.components.LoadingView
import dev.towhid.newsreader.ui.components.MessageView
import dev.towhid.newsreader.util.htmlToPlainText
import dev.towhid.newsreader.util.timeAgo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    storyId: Long,
    navigateBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    LaunchedEffect(storyId) { viewModel.load(storyId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (state.story != null) {
                        IconButton(onClick = viewModel::toggleBookmark) {
                            Icon(
                                imageVector = if (state.isBookmarked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (state.isBookmarked) "Remove from saved" else "Save story",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val story = state.story
        when {
            state.hasError && story == null ->
                MessageView(stringResource(Res.string.error_generic), Modifier.padding(padding))
            story == null -> LoadingView(Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { StoryHeader(story) }
                item {
                    Text(
                        "${stringResource(Res.string.comments)} (${story.commentCount})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                when {
                    state.isLoadingComments -> item { CircularProgressIndicator() }
                    state.comments.isEmpty() -> item { Text(stringResource(Res.string.no_comments)) }
                    else -> items(state.comments, key = { it.id }) { CommentItem(it) }
                }
            }
        }
    }
}

@Composable
private fun StoryHeader(story: Story) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(story.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "▲ ${story.score} · ${story.author} · ${timeAgo(story.postedAtEpochSeconds)}" +
                (story.domain?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        story.text?.let { Text(htmlToPlainText(it), style = MaterialTheme.typography.bodyLarge) }
        story.url?.let { url ->
            Button(onClick = { uriHandler.openUri(url) }) { Text(stringResource(Res.string.open_article)) }
        }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "${comment.author} · ${timeAgo(comment.postedAtEpochSeconds)}" +
                if (comment.replyCount > 0) " · ${comment.replyCount} replies" else "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(htmlToPlainText(comment.text), style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}
