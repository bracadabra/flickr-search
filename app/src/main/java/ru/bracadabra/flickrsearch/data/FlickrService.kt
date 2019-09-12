package ru.bracadabra.flickrsearch.data

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.net.ssl.HttpsURLConnection

class FlickrService(private val apiKey: String, private val baseUrl: String) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private val photosSearchParser = PhotosSearchParser()

    private var future: Future<*>? = null

    @MainThread
    fun searchPhotos(searchQuery: CharSequence, page: Int = 1, responseListener: (SearchResponse) -> Unit) {
        future?.cancel(true)
        future = executor.submit(Callable {
            var connection: HttpsURLConnection? = null
            try {
                val url =
                    URL("$baseUrl/services/rest/?method=flickr.photos.search&api_key=$apiKey&format=json&nojsoncallback=1&text=$searchQuery&page=$page")
                checkInterruption()
                connection = url.openConnection() as HttpsURLConnection

                val response = connection.inputStream.toResponse()
                checkInterruption()

                handler.post {
                    future = null
                    responseListener(response)
                }
            } catch (exception: InterruptedException) {
                // Skip
            } catch (exception: InterruptedIOException) {
                // Skip
            } finally {
                Thread.interrupted()
                connection?.disconnect()
            }
        })
    }

    fun cancelRequest() {
        future?.cancel(true)
    }

    private fun checkInterruption() {
        if (Thread.interrupted()) {
            throw InterruptedException()
        }
    }

    private fun InputStream.toResponse() = photosSearchParser.parse(this)

}