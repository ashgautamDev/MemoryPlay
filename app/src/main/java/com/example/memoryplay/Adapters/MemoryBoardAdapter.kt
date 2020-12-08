package com.example.memoryplay.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.R
import com.example.memoryplay.modles.BoardSize
import com.example.memoryplay.modles.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards : List<MemoryCard>,
    private val cardclickListner: CardclickListner
) :
    RecyclerView.Adapter<MemoryBoardAdapter.BoardViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 10

    }
    interface CardclickListner{
        fun OncardClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val cardwidth = parent.width / boardSize.getColums() - (MARGIN_SIZE*2)
        val cardheight = parent.height /boardSize.getRows() - (MARGIN_SIZE*2)
        val cardsidelength = min(cardwidth, cardheight)

        val view = LayoutInflater.from(context).inflate(R.layout.item_piece, parent, false)
        val layoutParameters = view.findViewById<CardView>(R.id.DummyCardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParameters.width=cardsidelength
        layoutParameters.height=cardsidelength
        layoutParameters.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)


        return BoardViewHolder(view)

    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = boardSize.numOfCards

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageButton= itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            val memoryCard = cards[position]

            imageButton.setImageResource(if (memoryCard.isFaceUp ) memoryCard.identifier
                    else R.drawable.ic_launcher_background )

            imageButton.alpha = if (memoryCard.isMatched) .4f else 1.0f
            val colorStateChange = if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.gray) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStateChange)

            imageButton.setOnClickListener {
                cardclickListner.OncardClickListener(position)

            }


        }

    }
}

