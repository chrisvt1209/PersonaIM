package dev.compose.messenger.core.network

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import dev.compose.messenger.core.datastore.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient

/**
 * Coil loader for avatar images: `GET /users/{id}/avatar-image` is JWT-authenticated like every
 * other users route, so Coil's HTTP client needs the same bearer token KtorClient's `AuthPlugin`
 * attaches to the Ktor client.
 *
 * Every user's avatar lives at the same URL for their whole account lifetime (`/users/{id}/avatar-image`),
 * and that URL is Coil's cache key - re-uploading a new photo changes the response body but not the URL,
 * so a caching loader would keep showing the old bitmap after a successful re-upload. Both cache layers
 * are disabled so every render fetches the current file; avatars are small dithered PNGs so this is cheap.
 */
fun createAvatarImageLoader(context: Context, preferencesManager: PreferencesManager): ImageLoader {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = runBlocking { preferencesManager.authToken.first() }
            val request = if (token != null) {
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        .build()

    return ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()
}

val LocalAvatarImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided - wrap the composable tree in CompositionLocalProvider(LocalAvatarImageLoader provides ...)")
}
