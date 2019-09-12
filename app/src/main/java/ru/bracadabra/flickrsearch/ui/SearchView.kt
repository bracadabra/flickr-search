package ru.bracadabra.flickrsearch.ui

import ru.bracadabra.flickrsearch.Observable

interface SearchView {

    fun queryChanges(): Observable<CharSequence>

    fun requestNextPage(): Observable<Unit>

    fun render(state: SearchResultsState)

}