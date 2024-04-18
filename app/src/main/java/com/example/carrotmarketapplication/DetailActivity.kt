package com.example.carrotmarketapplication

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carrotmarketapplication.databinding.ActivityDetailBinding
import java.text.DecimalFormat

class DetailActivity : AppCompatActivity() {

    val decimal = DecimalFormat("#,###")

    companion object {
        const val PARCEL_KEY:String = "parcel_key"
//        const val BUNDLE_KEY:String = "bundle_key"
    }

    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.textView4.paintFlags = Paint.UNDERLINE_TEXT_FLAG // 밑줄 만들기
//        val data = intent.getBundleExtra(BUNDLE_KEY) // Intent 에서 bundle 꺼내오기
//        val item = data?.getParcelable(PARCEL_KEY,Item::class.java) // bundle 에서 data class 값 꺼내오기
        val item = intent.getParcelableExtra(PARCEL_KEY,Item::class.java)
        if (item != null) {
            binding.apply {
                detailIvImage.setImageResource(item.icon)
                detailTvSeller.text=item.seller
                detailTvAddress.text=item.address
                detailTvTitle.text=item.title
                detailTvDescription.text=item.description
                detailTvPrice.text = "${decimal.format(item.price)}원"
            }
        }
        binding.detailIbBack.setOnClickListener {
            finish()
        }
    }
}