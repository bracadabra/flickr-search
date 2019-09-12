package ru.bracadabra.flickrsearch.ui.dependencies

import ru.bracadabra.flickrsearch.data.FlickrService
import ru.bracadabra.flickrsearch.data.PhotoUrlResolver
import ru.bracadabra.flickrsearch.ui.SearchPresenter

private const val API_KEY = "3e7cc266ae2b0e0d78e279ce8e361736"
private const val BASE_URL = "https://api.flickr.com"

class FlickrSearchDependencies {

    fun apiKey(): String {
        return API_KEY
    }

    fun baseUrl(): String {
        return BASE_URL
    }

    fun flickrService(): FlickrService {
        return FlickrService(
                apiKey = apiKey(),
                baseUrl = baseUrl()
        )
    }

    fun photoUrlResolver(): PhotoUrlResolver {
        return PhotoUrlResolver()
    }

    fun flickrSearchPresenter(): SearchPresenter {
        return SearchPresenter(
            flickrService = flickrService(),
            urlResolver = photoUrlResolver()
        )
    }

}