package ru.bracadabra.flickrsearch.ui

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import kotlinx.android.parcel.Parcelize
import ru.bracadabra.flickrsearch.Subscription
import ru.bracadabra.flickrsearch.data.FlickrService
import ru.bracadabra.flickrsearch.data.PhotoUrlResolver
import ru.bracadabra.flickrsearch.data.SearchResponse
import ru.bracadabra.flickrsearch.subscribe
import kotlin.properties.Delegates

private const val MIN_QUERY_LENGTH = 3

@Parcelize
data class ResolvedPhoto(val url: String) : Parcelable

@Parcelize
data class SearchResultsState(
    val page: Int = 1,
    val totalPages: Int? = null,
    val photos: List<ResolvedPhoto> = emptyList(),
    val query: CharSequence = "",
    val inProgress: Boolean = false,
    val errorMessage: String? = null
) : Parcelable

class SearchPresenter(
    private val flickrService: FlickrService,
    private val urlResolver: PhotoUrlResolver
) {

    @VisibleForTesting
    internal var view: SearchView? = null
    internal var state: SearchResultsState by Delegates.observable(SearchResultsState()) { _, _, new ->
        view?.render(new)
    }

    private val subscriptions: MutableList<Subscription> = mutableListOf()

    fun bind(view: SearchView) {
        this.view = view

        subscriptions.add(
            view.queryChanges().subscribe { query ->
                if (query.length >= MIN_QUERY_LENGTH) {
                    state = state.copy(
                        query = query,
                        inProgress = true
                    )
                    flickrService.searchPhotos(query.toString()) { response -> handleResponse(response) }
                } else {
                    state = state.copy(
                        query = query,
                        page = 1,
                        totalPages = null,
                        photos = emptyList(),
                        errorMessage = null,
                        inProgress = false
                    )
                    flickrService.cancelRequest()
                }
            }
        )
        subscriptions.add(
            view.requestNextPage().subscribe {
                val totalPages = state.totalPages
                if (totalPages != null && state.page < totalPages) {
                    val nextPage = state.page + 1
                    flickrService.searchPhotos(state.query, nextPage) { response -> handleResponse(response) }
                }
            }
        )
    }

    fun unbind() {
        view = null
        subscriptions.forEach { it.unsubscribe() }
        subscriptions.clear()
    }

    private fun handleResponse(response: SearchResponse) {
        state = when (response) {
            is SearchResponse.Success -> {
                val newPhotos =
                    response.photos.map { ResolvedPhoto(urlResolver.resolve(it.id, it.farm, it.server, it.secret)) }
                state.copy(
                    page = response.page,
                    totalPages = response.totalPages,
                    photos = state.photos + newPhotos,
                    errorMessage = null,
                    inProgress = false
                )
            }
            is SearchResponse.Failed -> {
                state.copy(
                    inProgress = false,
                    errorMessage = response.message
                )
            }
        }
    }
}