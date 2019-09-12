package ru.bracadabra.flickrsearch.data

class PhotoUrlResolver {

    fun resolve(id: String, farm: Int, server: String, secret: String): String {
        return "https://farm$farm.static.flickr.com/$server/${id}_$secret.jpg"
    }

}