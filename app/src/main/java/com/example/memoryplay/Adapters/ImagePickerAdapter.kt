package com.example.memoryplay.Adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.R
import com.example.memoryplay.modles.BoardSize
import kotlin.math.min


class ImagePickerAdapter(
    private val context: Context,
    private val imageURIs: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListner: ImageClickListner

) : RecyclerView.Adapter<ImagePickerAdapter.NewViewHolder>() {
    interface ImageClickListner {
        fun onPlaceholderClicker()
    }

    companion object {
        private const val TAG = "ImagePickerAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.card_custom_images, parent, false)
//        val cardWidth = parent.width / boardSize.getColums()
//        val cardHeight = parent.height / boardSize.getRows()
//        val cardSideLength = min(cardWidth, cardHeight)
//        Log.i(TAG, "onCreateViewHolder $cardSideLength, $cardWidth, $cardHeight")
//        val layoutParams = view.findViewById<ImageView>(R.id.customImage).layoutParams
//        layoutParams.width = cardSideLength
//        layoutParams.height = cardSideLength
        return NewViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) {
        if (position < imageURIs.size) {
            holder.bind(imageURIs[position])
        } else {
            holder.bind()

        }
    }

    override fun getItemCount() = boardSize.numOfPairs()

    inner class NewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customImagePicker = itemView.findViewById<ImageView>(R.id.customImage)
        fun bind(uri: Uri) {
            customImagePicker.setImageURI(uri)
            customImagePicker.setOnClickListener(null)
        }

        fun bind() {
            customImagePicker.setOnClickListener {
                Log.i(TAG, "bind: u can tap on card to select images")
                imageClickListner.onPlaceholderClicker()
            }
        }

    }
}
