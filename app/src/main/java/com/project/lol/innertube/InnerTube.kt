package com.project.lol.innertube

import com.project.lol.innertube.models.Context
import com.project.lol.innertube.models.YouTubeClient
import com.project.lol.innertube.models.YouTubeLocale
import com.project.lol.innertube.models.body.BrowseBody
import com.project.lol.innertube.models.body.NextBody
import com.project.lol.innertube.models.body.PlayerBody
import com.project.lol.innertube.models.body.SearchBody
import com.project.lol.innertube.utils.parseCookieString
import com.project.lol.innertube.utils.sha1
import com.project.lol.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.Proxy
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Provide access to InnerTube endpoints.
 * For making HTTP requests, not parsing response.
 */
@OptIn(ExperimentalEncodingApi::class)
class InnerTube {
    @Volatile
    private var cacheMaxBytes = DEFAULT_CACHE_BYTES

    @Volatile
    private var httpClient = createClient()

    private var cacheDir: File? = null

    var locale = YouTubeLocale(
        gl = Locale.getDefault().country,
        hl = Locale.getDefault().toLanguageTag()
    )
    var visitorData: String? = null
    var dataSyncId: String? = null
    var cookie: String? = null
        set(value) {
            field = value
            cookieMap = if (value == null) emptyMap() else parseCookieString(value)
        }
    private var cookieMap = emptyMap<String, String>()

    var proxy: Proxy? = null
        set(value) {
            field = value
            httpClient.close()
            httpClient = createClient()
        }
    
    var proxyAuth: String? = null

    var useLoginForBrowse: Boolean = false

    @Suppress("unused")
    fun setCacheDir(dir: File?) = setCacheDir(dir, cacheMaxBytes)

    fun setCacheDir(dir: File?, maxBytes: Long) {
        if (dir == cacheDir && maxBytes == cacheMaxBytes) return
        cacheDir = dir
        cacheMaxBytes = maxBytes.coerceAtLeast(1L)
        httpClient.close()
        httpClient = createClient()
        Logger.d(TAG, "http cache dir set to ${dir?.absolutePath ?: "none"}")
    }

    fun setCacheLimit(maxBytes: Long) {
        val bounded = maxBytes.coerceAtLeast(1L)
        if (bounded == cacheMaxBytes) return
        cacheMaxBytes = bounded
        if (cacheDir == null) {
            Logger.d(TAG, "http cache limit set to $bounded bytes (no cache dir yet)")
            return
        }
        httpClient.close()
        httpClient = createClient()
        Logger.d(TAG, "http cache limit set to $bounded bytes")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createClient() = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            gzip(0.9F)
            deflate(0.8F)
        }

        // Enhanced network configuration for better performance
        engine {
            config {
                // Connection pool settings for better connection reuse
                connectionPool(
                    okhttp3.ConnectionPool(
                        10, // maxIdleConnections
                        5, // keepAliveDuration
                        java.util.concurrent.TimeUnit.MINUTES
                    )
                )
                
                // Timeout configurations
                connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                
                // Enable HTTP/2 for better performance
                protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
                
                // Retry on connection failure
                retryOnConnectionFailure(true)
                
                // Cache configuration for better performance
                cacheDir?.let { dir ->
                    runCatching {
                        dir.mkdirs()
                        cache(
                            okhttp3.Cache(
                                directory = dir,
                                maxSize = cacheMaxBytes
                            )
                        )
                    }.onFailure { error ->
                        Logger.w(
                            TAG,
                            "okhttp cache unavailable at ${dir.absolutePath}: " +
                                "${error::class.java.name}: ${error.message ?: "(no message)"}",
                            error,
                        )
                    }
                }
                
                // Apply proxy configuration
                this@InnerTube.proxy?.let { proxyConfig ->
                    proxy(proxyConfig)
                }
                
                // Apply proxy authentication
                this@InnerTube.proxyAuth?.let { auth ->
                    proxyAuthenticator { _, response ->
                        response.request.newBuilder()
                            .header("Proxy-Authorization", auth)
                            .build()
                    }
                }
            }
        }

        // Request timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }

        defaultRequest {
            url(YouTubeClient.API_URL_YOUTUBE_MUSIC)
            header("Accept", "application/json")
            // Use the user's locale instead of hardcoding en-US so region-specific
            // catalogs and language-matched recommendations are returned.
            header("Accept-Language", "${locale.hl},${locale.gl};q=0.9,en;q=0.8")
            header("Cache-Control", "no-cache")
        }
    }

    private fun HttpRequestBuilder.ytClient(client: YouTubeClient, setLogin: Boolean = false) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", client.clientId /* Not a typo. The Client-Name header does contain the client id. */)
            append("X-YouTube-Client-Version", client.clientVersion)
            append("X-Origin", YouTubeClient.ORIGIN_YOUTUBE_MUSIC)
            append("Referer", YouTubeClient.REFERER_YOUTUBE_MUSIC)
            visitorData?.let { append("X-Goog-Visitor-Id", it) }
            if (setLogin && client.loginSupported) {
                cookie?.let { cookie ->
                    append("cookie", cookie)
                    if ("SAPISID" !in cookieMap) return@let
                    val currentTime = System.currentTimeMillis() / 1000
                    val sapisidHash = sha1("$currentTime ${cookieMap["SAPISID"]} ${YouTubeClient.ORIGIN_YOUTUBE_MUSIC}")
                    append("Authorization", "SAPISIDHASH ${currentTime}_${sapisidHash}")
                }
            }
        }
        userAgent(client.userAgent)
        parameter("prettyPrint", false)
    }

    /**
     * Simple retry wrapper for transient IO errors (socket aborts, timeouts).
     * Retries the given block up to [maxAttempts] times with exponential backoff.
     * Cancellation is respected since [delay] will throw if the coroutine is cancelled.
     */
    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 500L,
        factor: Double = 2.0,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: IOException) {
                attempt++
                if (attempt >= maxAttempts) {
                    Logger.w(
                        TAG,
                        "request failed after $attempt attempt(s): ${e::class.java.name}: ${e.message}",
                        e,
                    )
                    throw e
                }
                Logger.w(TAG, "request retry $attempt/$maxAttempts after ${e::class.java.name}: ${e.message}")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
    }

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
    ) = withRetry {
        httpClient.post("search") {
            ytClient(client, setLogin = useLoginForBrowse)
            setBody(
                SearchBody(
                    context = client.toContext(
                        locale,
                        visitorData,
                        if (useLoginForBrowse) dataSyncId else null
                    ),
                    query = query,
                    params = params
                )
            )
            parameter("continuation", continuation)
            parameter("ctoken", continuation)
        }
    }

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String?,
        signatureTimestamp: Int?,
        poToken: String? = null,
    ) = withRetry {
        httpClient.post("player") {
            ytClient(client, setLogin = true)
            setBody(
                PlayerBody(
                    context = client.toContext(locale, visitorData, dataSyncId).let {
                        if (client.isEmbedded) {
                            it.copy(
                                thirdParty = Context.ThirdParty(
                                    embedUrl = "https://www.youtube.com/watch?v=${videoId}"
                                )
                            )
                        } else it
                    },
                    videoId = videoId,
                    playlistId = playlistId,
                    playbackContext = if (client.useSignatureTimestamp && signatureTimestamp != null) {
                        PlayerBody.PlaybackContext(
                            PlayerBody.PlaybackContext.ContentPlaybackContext(
                                signatureTimestamp
                            )
                        )
                    } else null,
                    serviceIntegrityDimensions = if (client.useWebPoTokens && poToken != null) {
                        PlayerBody.ServiceIntegrityDimensions(poToken)
                    } else null,
                )
            )
        }
    }

    suspend fun getSwJsData() = withRetry { httpClient.get("https://music.youtube.com/sw.js_data") }

    /**
     * Watch-next payload. Used to locate the Lyrics tab, whose browseId cannot be derived
     * from the videoId.
     *
     * Never sends the account cookie: the tab list we need is public, and there is no reason
     * to attach a session to a lyrics lookup.
     */
    suspend fun next(
        client: YouTubeClient,
        videoId: String,
    ) = withRetry {
        httpClient.post("next") {
            ytClient(client, setLogin = false)
            setBody(
                NextBody(
                    context = client.toContext(locale, visitorData, null),
                    videoId = videoId,
                )
            )
        }
    }

    /** Generic browse. Lyrics live behind an opaque browseId obtained from [next]. */
    suspend fun browse(
        client: YouTubeClient,
        browseId: String,
        params: String? = null,
    ) = withRetry {
        httpClient.post("browse") {
            ytClient(client, setLogin = false)
            setBody(
                BrowseBody(
                    context = client.toContext(locale, visitorData, null),
                    browseId = browseId,
                    params = params,
                )
            )
        }
    }

    
    private suspend fun returnYouTubeDislike(videoId: String) = withRetry {
        httpClient.get("https://returnyoutubedislikeapi.com/Votes?videoId=$videoId") {
            contentType(ContentType.Application.Json)
        }
    }

    companion object {
        private const val TAG = "innertube"
        const val DEFAULT_CACHE_BYTES = 50L * 1024L * 1024L
    }

}
