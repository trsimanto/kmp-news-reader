package dev.towhid.newsreader.di

import dev.towhid.newsreader.data.HackerNewsApi
import dev.towhid.newsreader.data.KtorHackerNewsApi
import dev.towhid.newsreader.data.StoryRepository
import dev.towhid.newsreader.ui.detail.DetailViewModel
import dev.towhid.newsreader.ui.feed.FeedViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
            }
        }
    }
    single<HackerNewsApi> { KtorHackerNewsApi(get()) }
    single { StoryRepository(get()) }
}

val viewModelModule = module {
    factoryOf(::FeedViewModel)
    factoryOf(::DetailViewModel)
}

fun initKoin() {
    startKoin {
        modules(dataModule, viewModelModule)
    }
}
