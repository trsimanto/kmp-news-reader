package dev.towhid.newsreader.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeAgoTest {
    private val now = 1_000_000_000L

    @Test
    fun formatsEachRange() {
        assertEquals("just now", timeAgo(now - 30, now))
        assertEquals("5m", timeAgo(now - 5 * 60, now))
        assertEquals("3h", timeAgo(now - 3 * 3_600, now))
        assertEquals("2d", timeAgo(now - 2 * 86_400, now))
        assertEquals("1y", timeAgo(now - 31_536_000, now))
    }

    @Test
    fun futureTimestampsAreTreatedAsNow() {
        assertEquals("just now", timeAgo(now + 100, now))
    }
}
