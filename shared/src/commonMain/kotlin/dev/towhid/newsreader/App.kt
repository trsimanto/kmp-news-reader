package dev.towhid.newsreader

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.towhid.newsreader.ui.detail.DetailScreen
import dev.towhid.newsreader.ui.feed.FeedScreen
import kotlinx.serialization.Serializable

@Serializable
object FeedDestination

@Serializable
data class DetailDestination(val storyId: Long)

private val Orange = Color(0xFFFF6600)

@Composable
fun App() {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFFFF8A3D))
    } else {
        lightColorScheme(primary = Orange)
    }
    MaterialTheme(colorScheme = colors) {
        Surface {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = FeedDestination) {
                composable<FeedDestination> {
                    FeedScreen(onStoryClick = { id -> navController.navigate(DetailDestination(id)) })
                }
                composable<DetailDestination> { entry ->
                    DetailScreen(
                        storyId = entry.toRoute<DetailDestination>().storyId,
                        navigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
