package com.example.carrotmarketapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carrotmarketapplication.databinding.ItemLayoutBinding
import java.text.DecimalFormat

class CustomAdapter(private val dataList: MutableList<Item>):RecyclerView.Adapter<CustomAdapter.Holder>() {

    val decimal = DecimalFormat("#,###")

    interface ItemClick {
        fun onItemClick(view: View, position:Int)
        fun onItemLongClick(view:View, position:Int)
    }

    var itemClick: ItemClick? = null

    // 뷰 홀더 생성
    inner class Holder(binding:ItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {

        val icon = binding.itemIvImage
        val title = binding.itemTvTitle
        val address = binding.itemTvAddress
        val price = binding.itemTvPrice
        val chatNum = binding.itemTvChatNum

        val heartNum = binding.itemTvLikeNum
        val heartStatus = binding.itemIvHeart

        // 좋아요 기능 추가
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(it,position)
        }
        holder.itemView.setOnLongClickListener {
            itemClick?.onItemLongClick(it,position)
            true
        }

        holder.icon.setImageResource(dataList[position].icon)
        holder.title.text = dataList[position].title
        holder.address.text = dataList[position].address
        holder.price.text = "${decimal.format(dataList[position].price)}원"
        holder.chatNum.text=dataList[position].chatNum.toString()
        holder.heartNum.text=dataList[position].likeNum.toString()

        if(dataList[position].heartStatus) {
            // true인 경우
            holder.heartStatus.setImageResource(R.drawable.ic_fullheart2)
        } else {
            // false인 경우
            holder.heartStatus.setImageResource(R.drawable.ic_heart2)
        }
    }
}