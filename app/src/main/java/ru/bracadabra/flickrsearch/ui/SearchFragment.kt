package ru.bracadabra.flickrsearch.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.bracadabra.flickrsearch.BaseFragment
import ru.bracadabra.flickrsearch.Observable
import ru.bracadabra.flickrsearch.R
import ru.bracadabra.flickrsearch.ui.dependencies.DependenciesProvider

private const val SPAN_COUNT = 3
private const val KEY_SAVED_STATE = "key_saved_state"

class SearchFragment : BaseFragment(), SearchView {

    private lateinit var searchView: EditText
    private lateinit var searchResults: RecyclerView
    private lateinit var progressView: View

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var resultsAdapter: SearchResultsAdapter

    private lateinit var presenter: SearchPresenter

    private val nextPageObservable = Observable<Unit>()
    private val textChangesObservable: TextChangesObservable = TextChangesObservable()

    override fun onAttach(context: Context) {
        presenter = DependenciesProvider.with(this).flickrSearchPresenter()
        super.onAttach(context)

        gridLayoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        val photoSize = context.resources.displayMetrics.widthPixels / SPAN_COUNT
        resultsAdapter = SearchResultsAdapter(context, photoSize)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photos_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.photos_search)
        searchResults = view.findViewById(R.id.photos_search_results)
        progressView = view.findViewById(R.id.photo_search_progress)

        searchResults.apply {
            layoutManager = gridLayoutManager
            adapter = resultsAdapter
            addOnScrollListener(PagingListener(nextPageObservable))
        }

        presenter.bind(this)
        savedInstanceState?.let {
            presenter.state = savedInstanceState.getParcelable(KEY_SAVED_STATE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_SAVED_STATE, presenter.state)
    }

    override fun onDestroyView() {
        presenter.unbind()
        super.onDestroyView()
    }

    override fun queryChanges(): Observable<CharSequence> {
        return searchView.observeTextChanges()
    }

    override fun requestNextPage(): Observable<Unit> {
        return nextPageObservable
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun render(state: SearchResultsState) {
        with(state) {
            errorMessage?.let {
                showError(it)
                return
            }

            if (state.query.toString() != searchView.text.toString()) {
                textChangesObservable.eatChanges = true
                searchView.setText(state.query)
                textChangesObservable.eatChanges = false
            }

            progressView.visibility = if (state.inProgress) View.VISIBLE else View.GONE

            resultsAdapter.items = state.photos
        }
    }

    private fun EditText.observeTextChanges(): Observable<CharSequence> {
        addTextChangedListener(textChangesObservable)
        return textChangesObservable.observable
    }

    inner class TextChangesObservable : TextWatcher {

        val observable: Observable<CharSequence> = Observable()
        var eatChanges = false

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!eatChanges) {
                observable.notifyChange(s)
            }
        }

    }

    inner class PagingListener(private val observable: Observable<Unit>) : RecyclerView.OnScrollListener() {
        private var lastLastIndex: Int = 0

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (resultsAdapter.items.isEmpty()) {
                return
            }

            val lastVisiblePosition = gridLayoutManager.findLastVisibleItemPosition()
            val lastIndex = requireNotNull(recyclerView.adapter).itemCount - 1
            if (lastLastIndex != lastIndex && lastIndex >= lastVisiblePosition) {
                observable.notifyChange(Unit)
                lastLastIndex = lastIndex
            }
        }
    }
}