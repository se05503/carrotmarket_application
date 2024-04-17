package com.example.carrotmarketapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carrotmarketapplication.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

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
//        val data = intent.getBundleExtra(BUNDLE_KEY) // Intent 에서 bundle 꺼내오기
//        val item = data?.getParcelable(PARCEL_KEY,Item::class.java) // bundle 에서 data class 값 꺼내오기
        val item = intent.getParcelableExtra(PARCEL_KEY,Item::class.java)
        if (item != null) {
            binding.detailIvImage.setImageResource(item.icon)
            binding.detailTvSeller.text=item.seller
            binding.detailTvAddress.text=item.address
            binding.detailTvTitle.text=item.title
            binding.detailTvDescription.text=item.description
            binding.detailTvPrice.text=item.price.toString()
        }
        binding.detailIbBack.setOnClickListener {
            finish()
        }
    }
}