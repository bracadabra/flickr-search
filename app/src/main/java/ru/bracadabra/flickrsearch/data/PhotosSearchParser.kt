package ru.bracadabra.flickrsearch.data

import android.util.JsonReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class PhotosSearchParser {

    fun parse(stream: InputStream): SearchResponse {
        val isr = InputStreamReader(stream, "UTF-8")
        return JsonReader(BufferedReader(isr)).use { reader ->
            reader.readResponse()
        }
    }

    private fun JsonReader.readResponse(): SearchResponse {
        var status: String? = null
        var code: Int? = null
        var message: String? = null
        var page: Int? = null
        var totalPages: Int? = null
        var photos: List<Photo>? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "stat" -> status = nextString()
                "code" -> code = nextInt()
                "message" -> message = nextString()
                "photos" -> {
                    beginObject()
                    while (hasNext()) {
                        when (nextName()) {
                            "page" -> page = nextInt()
                            "pages" -> totalPages = nextInt()
                            "photo" -> photos = readPhotos()
                            else -> skipValue()
                        }
                    }
                    endObject()
                }
                else -> skipValue()
            }
        }
        endObject()

        return when (requireNotNull(status)) {
            "ok" -> SearchResponse.Success(
                    page = requireNotNull(page),
                    totalPages = requireNotNull(totalPages),
                    photos = requireNotNull(photos)
            )
            "fail" -> SearchResponse.Failed(
                    code = requireNotNull(code),
                    message = requireNotNull(message)
            )
            else -> throw IllegalStateException()
        }
    }

    private fun JsonReader.readPhotos(): List<Photo> {
        val photos = mutableListOf<Photo>()
        beginArray()
        while (hasNext()) {
            photos.add(readPhoto())
        }
        endArray()

        return photos
    }

    private fun JsonReader.readPhoto(): Photo {
        var id: String? = null
        var farm: Int? = null
        var server: String? = null
        var secret: String? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "id" -> id = nextString()
                "farm" -> farm = nextInt()
                "server" -> server = nextString()
                "secret" -> secret = nextString()
                else -> skipValue()
            }
        }
        endObject()

        return Photo(
                id = requireNotNull(id),
                farm = requireNotNull(farm),
                server = requireNotNull(server),
                secret = requireNotNull(secret)
        )
    }

}