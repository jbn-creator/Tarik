package com.example.tarik.data
import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient

object HistoryImageModule {

    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "TarikHistoryApp/1.0 (student-contact)") //user-agent header necessary for wikipedia api calls
                            .build()
                        chain.proceed(request)
                    }
                    .dispatcher(okhttp3.Dispatcher().also { it.maxRequestsPerHost = 2 })
                    .build()
            }
            .build()
    }
}