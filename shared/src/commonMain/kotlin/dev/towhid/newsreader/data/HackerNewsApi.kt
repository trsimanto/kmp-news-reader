package dev.towhid.newsreader.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface HackerNewsApi {
    suspend fun feedIds(feed: FeedType): List<Long>
    suspend fun item(id: Long): HnItem?
}

class KtorHackerNewsApi(
    private val client: HttpClient,
    private val baseUrl: String = BASE_URL,
) : HackerNewsApi {

    override suspend fun feedIds(feed: FeedType): List<Long> =
        client.get("$baseUrl/${feed.path}.json").body()

    override suspend fun item(id: Long): HnItem? =
        client.get("$baseUrl/item/$id.json").body()

    companion object {
        const val BASE_URL = "https://hacker-news.firebaseio.com/v0"
    }
}
