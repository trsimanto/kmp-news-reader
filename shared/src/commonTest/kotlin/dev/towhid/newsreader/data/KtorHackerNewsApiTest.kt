package dev.towhid.newsreader.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KtorHackerNewsApiTest {

    private val requestedPaths = mutableListOf<String>()

    private fun apiReturning(body: String): KtorHackerNewsApi {
        val engine = MockEngine { request ->
            requestedPaths += request.url.encodedPath
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return KtorHackerNewsApi(client)
    }

    @Test
    fun feedIdsHitsFeedEndpointAndParsesIds() = runTest {
        val api = apiReturning("[3, 2, 1]")

        val ids = api.feedIds(FeedType.TOP)

        assertEquals(listOf(3L, 2L, 1L), ids)
        assertEquals("/v0/topstories.json", requestedPaths.single())
    }

    @Test
    fun itemParsesStoryAndIgnoresUnknownKeys() = runTest {
        val api = apiReturning(
            """{"id":42,"type":"story","by":"pg","time":1175714200,"title":"Hello","url":"https://example.com",
               "score":111,"descendants":7,"kids":[1,2],"unknownField":"ignored"}""",
        )

        val item = assertNotNull(api.item(42L))

        assertEquals("/v0/item/42.json", requestedPaths.single())
        assertEquals("Hello", item.title)
        assertEquals(111, item.score)
        assertEquals(listOf(1L, 2L), item.kids)
    }
}
