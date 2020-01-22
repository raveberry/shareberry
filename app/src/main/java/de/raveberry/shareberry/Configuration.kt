package de.raveberry.shareberry

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.woxthebox.draglistview.DragItemAdapter
import com.woxthebox.draglistview.DragListView
import java.util.*


class Configuration : Activity() {

    private lateinit var mAdapter: ItemAdapter
    private lateinit var mObserver: RecyclerView.AdapterDataObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val urls = Storage.getUrls(this)
        val urlPairs = LinkedList<Pair<Long, String>>()
        for ((i, url) in urls.withIndex()) {
            urlPairs.add(Pair(i.toLong(), url))
        }

        when (intent?.action) {
            Intent.ACTION_MAIN -> {
                setContentView(R.layout.activity_main)

                val urlsView = findViewById<DragListView>(R.id.urlsView)
                mAdapter = ItemAdapter(urlPairs, R.layout.list_item, R.id.image)
                urlsView.setAdapter(mAdapter, false)
                urlsView.setCanDragHorizontally(false)
                urlsView.setLayoutManager(LinearLayoutManager(this))

                mObserver = object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()

                        Storage.storeUrls(this@Configuration, mAdapter.itemList.map { it.second })
                    }
                }
                mAdapter.registerAdapterDataObserver(mObserver)

            }
            else -> {
                // default
            }
        }
    }

    // thanks to https://github.com/woxblom/DragListView
    private inner class ItemAdapter(
        list: List<Pair<Long, String>>,
        val mLayoutId: Int,
        val mGrabHandleId: Int
    ) : DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>() {
        init {
            super.setItemList(list)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
            return this.ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            val text = mItemList[position].second
            holder.mText.text = text
            holder.itemView.tag = mItemList[position]
        }

        override fun getUniqueItemId(position: Int): Long {
            return mItemList[position].first
        }

        private inner class ViewHolder(itemView: View) :
            DragItemAdapter.ViewHolder(itemView, mGrabHandleId, false) {
            val mText: TextView = itemView.findViewById(R.id.text)

            override fun onItemClicked(view: View) {
                // create dialog to let the user change or delete the entry
                val builder = AlertDialog.Builder(view.context)
                builder.setTitle("Change Entry")
                val input = EditText(view.context)
                input.setText(mText.text)
                input.setOnFocusChangeListener { _, _ ->
                    input.post {
                        val inputMethodManager: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
                input.requestFocus()
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)
                builder.setPositiveButton("OK") { _, _ ->
                    mAdapter.itemList[layoutPosition] =
                        Pair(mAdapter.itemList[layoutPosition].first, input.text.toString())
                    mAdapter.notifyDataSetChanged()
                }
                builder.setNeutralButton("Delete") { _, _ ->
                    mAdapter.removeItem(layoutPosition)
                    mAdapter.notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                builder.show()
            }

        }

    }

    fun addUrl(view: View) {
        val taken = HashSet<Long>()
        for (item in mAdapter.itemList) {
            taken += item.first
        }
        var id: Long
        do {
            id = Random().nextLong()
        } while (taken.contains(id))

        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("Change Entry")
        val input = EditText(view.context)
        input.setText("http://example.com")
        input.setOnFocusChangeListener { _, _ ->
            input.post {
                val inputMethodManager: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        input.requestFocus()
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            mAdapter.addItem(mAdapter.itemCount, Pair(id, input.text.toString()))
            mAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }
}
