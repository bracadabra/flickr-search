package ru.bracadabra.flickrsearch.data

sealed class SearchResponse {

    data class Success(
            val page: Int,
            val totalPages: Int,
            val photos: List<Photo>
    ) : SearchResponse()

    data class Failed(
            val code: Int,
            val message: String
    ) : SearchResponse()

}

data class Photo(
        val id: String,
        val farm: Int,
        val server: String,
        val secret: String
)
