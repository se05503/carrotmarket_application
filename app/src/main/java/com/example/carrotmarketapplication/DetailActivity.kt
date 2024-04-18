package com.example.carrotmarketapplication

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carrotmarketapplication.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
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

        // 밑줄 만들기
        binding.textView4.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        // MainActivity 에서 넘어온 데이터 받기
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

        binding.apply {
            detailIbBack.setOnClickListener{
                finish()
            }
            detailIvBlankheart.setOnClickListener {
                if(detailIvFullheart.visibility == View.VISIBLE) {
                    detailIvFullheart.visibility = View.INVISIBLE
                    Log.d("heart","false")
                }
                else {
                    detailIvFullheart.visibility = View.VISIBLE
                    var snackbar = Snackbar.make(main, "관심 목록에 추가되었습니다",Snackbar.LENGTH_LONG) // 첫번째 인자 main 맞나?
                    snackbar.show()
                    Log.d("heart","true")
                }
            }
            detailIvFullheart.setOnClickListener {
                if(detailIvFullheart.visibility == View.VISIBLE) {
                    detailIvFullheart.visibility = View.INVISIBLE
                }
                else {
                    detailIvFullheart.visibility = View.VISIBLE
                    var snackbar = Snackbar.make(main, "관심 목록에 추가되었습니다",Snackbar.LENGTH_LONG) // 첫번째 인자 main 맞나?
                    snackbar.show()
                }
            }
        }

    }
}