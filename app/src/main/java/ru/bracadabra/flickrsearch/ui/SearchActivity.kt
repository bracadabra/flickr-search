package ru.bracadabra.flickrsearch.ui

import android.os.Bundle
import ru.bracadabra.flickrsearch.BaseActivity
import ru.bracadabra.flickrsearch.R

class SearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_search)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, SearchFragment())
                    .commit()
        }
    }
}
