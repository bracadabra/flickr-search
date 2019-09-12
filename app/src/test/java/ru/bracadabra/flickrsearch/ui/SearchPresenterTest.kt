package ru.bracadabra.flickrsearch.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import ru.bracadabra.flickrsearch.Observable
import ru.bracadabra.flickrsearch.data.FlickrService
import ru.bracadabra.flickrsearch.data.Photo
import ru.bracadabra.flickrsearch.data.PhotoUrlResolver
import ru.bracadabra.flickrsearch.data.SearchResponse

class SearchPresenterTest {

    private lateinit var presenter: SearchPresenter
    private lateinit var service: FlickrService
    private lateinit var urlResolver: PhotoUrlResolver
    private lateinit var view: SearchView

    @Before
    fun setUp() {
        service = mock(FlickrService::class.java)
        urlResolver = mock(PhotoUrlResolver::class.java)
        view = mock(SearchView::class.java)
        `when`(view.queryChanges()).thenReturn(Observable.empty())
        `when`(view.requestNextPage()).thenReturn(Observable.empty())
        presenter = SearchPresenter(service, urlResolver)
    }

    @Test
    fun bind() {
        presenter.bind(view)

        val actual = presenter.state
        val expected = SearchResultsState()
        assertEquals(actual, expected)
    }

    @Test
    fun unbind() {
        presenter.bind(view)
        presenter.unbind()

        assertNull(presenter.view)
    }

    @Test
    fun queryChanges_lessThanMin() {
        val queryChangersObservable = Observable<CharSequence>()
        `when`(view.queryChanges()).thenReturn(queryChangersObservable)

        presenter.bind(view)
        queryChangersObservable.notifyChange("ki")

        val actual = presenter.state
        val expected = SearchResultsState(query = "ki")
        assertEquals(actual, expected)
    }

    @Test
    fun queryChanges_greaterThanMin() {
        val queryChangersObservable = Observable<CharSequence>()
        `when`(view.queryChanges()).thenReturn(queryChangersObservable)
        `when`(urlResolver.resolve(anyString(), anyInt(), anyString(), anyString())).thenReturn("url")

        // It's possible to implement this with interface, but for me it was interesting to do it in such way
        `when`(service.searchPhotos(anyString(), anyInt(), any())).then { answer ->
            @Suppress("UNCHECKED_CAST")
            val listener = (answer.arguments.last() as (SearchResponse) -> Unit)
            val photo = Photo("1", 1, "1", "1")
            listener(SearchResponse.Success(1, 1, listOf(photo)))
        }

        presenter.bind(view)
        queryChangersObservable.notifyChange("kit")

        val actual = presenter.state
        val expected = SearchResultsState(1, 1, listOf(ResolvedPhoto("url")), "kit", false, null)
        assertEquals(actual, expected)
    }

    private fun <T> any(): T = Mockito.any<T>()
}