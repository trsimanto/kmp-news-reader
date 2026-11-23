package dev.towhid.newsreader.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun nowEpochSeconds(): Long = Clock.System.now().epochSeconds

/** "just now", "5m", "3h", "2d" — compact relative time used across the feed. */
fun timeAgo(epochSeconds: Long, now: Long = nowEpochSeconds()): String {
    val diff = (now - epochSeconds).coerceAtLeast(0)
    return when {
        diff < 60 -> "just now"
        diff < 3_600 -> "${diff / 60}m"
        diff < 86_400 -> "${diff / 3_600}h"
        diff < 2_592_000 -> "${diff / 86_400}d"
        diff < 31_536_000 -> "${diff / 2_592_000}mo"
        else -> "${diff / 31_536_000}y"
    }
}
