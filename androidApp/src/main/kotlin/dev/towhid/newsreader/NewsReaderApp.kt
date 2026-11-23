package dev.towhid.newsreader

import android.app.Application
import dev.towhid.newsreader.di.initKoin

class NewsReaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
