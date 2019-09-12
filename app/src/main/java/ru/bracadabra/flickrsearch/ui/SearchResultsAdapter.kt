package ru.bracadabra.flickrsearch.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.bracadabra.flickrsearch.R
import ru.bracadabra.flickrsearch.imageloader.ImageLoader

class SearchResultsAdapter(context: Context, private val photoSize: Int) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    var items: List<ResolvedPhoto> = emptyList()
        set(value) {
            DiffUtil.calculateDiff(SearchResultsDiffCallback(field, value)).dispatchUpdatesTo(this)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_photos_search_result, parent, false)).apply {
            itemView.layoutParams.width = photoSize
            itemView.layoutParams.height = photoSize
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val photoView = itemView.findViewById<ImageView>(R.id.search_result_photo)

        fun bind(photo: ResolvedPhoto) {
            ImageLoader.load(photoView, photo.url)
        }
    }

}

class SearchResultsDiffCallback(private val oldList: List<ResolvedPhoto>, private val newList: List<ResolvedPhoto>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].url == newList[newItemPosition].url
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].url == newList[newItemPosition].url
    }

}