package ru.bracadabra.flickrsearch.imageloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.collection.LruCache
import java.io.IOException
import java.io.InterruptedIOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


private const val CORE_POOL_SIZE = 8
private const val KEEP_ALIVE_TIME = 1L

object ImageLoader {

    private val maxMemory = Runtime.getRuntime().maxMemory()
    private val cacheSize = maxMemory / 2
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize.toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount
        }
    }
    private val tasksMap = WeakHashMap<ImageView, Task>()
    private val tasksQueue = LinkedBlockingQueue<Runnable>()
    private val tasksExecutor =
        ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, tasksQueue)
    private val handler = Handler(Looper.getMainLooper())

    fun load(imageView: ImageView, url: String) {
        val cachedBitmap = memoryCache[url]
        if (cachedBitmap == null) {
            val task = tasksMap[imageView] ?: Task()
            task.loadImage(url, imageView, tasksExecutor)
        } else {
            imageView.setImageBitmap(cachedBitmap)
        }
    }

    private class Task : Runnable {

        @Volatile
        private lateinit var url: String
        private var future: Future<*>? = null
        private lateinit var imageViewRef: WeakReference<ImageView>

        override fun run() {
            try {
                val currentUrl = url
                val connection = URL(currentUrl).openConnection() as HttpURLConnection
                checkInterruption()

                if (connection.responseCode != 200) {
                    throw IOException()
                }

                val bytes = connection.inputStream.use {
                    checkInterruption()
                    it.readBytes()
                }
                checkInterruption()

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                checkInterruption()

                memoryCache.put(currentUrl, bitmap)
                doOnComplete {
                    if (url == currentUrl) {
                        it.setImageBitmap(bitmap)
                    }
                }
            } catch (exception: Exception) {
                when (exception) {
                    is InterruptedIOException, is InterruptedException -> {
                        // Skip
                    }
                    is IOException -> {
                        doOnComplete { it.setImageResource(ru.bracadabra.flickrsearch.R.drawable.ic_download_error) }
                    }
                    else -> throw exception
                }

            } finally {
                // Clear interrupted flag
                Thread.interrupted()
            }
        }

        @MainThread
        fun loadImage(url: String, imageView: ImageView, executor: ThreadPoolExecutor) {
            tasksMap[imageView] = this
            imageViewRef = WeakReference(imageView)
            imageView.setImageResource(ru.bracadabra.flickrsearch.R.drawable.item_placeholder)

            this.url = url
            executor.remove(this)
            future?.cancel(true)
            future = executor.submit(this)
        }

        private fun doOnComplete(@MainThread action: (ImageView) -> Unit) {
            imageViewRef.get()?.let { imageVew ->
                handler.post {
                    tasksMap.remove(imageVew)
                    action(imageVew)
                }
            }
        }

        private fun checkInterruption() {
            if (Thread.interrupted()) {
                throw InterruptedException()
            }
        }
    }

}
