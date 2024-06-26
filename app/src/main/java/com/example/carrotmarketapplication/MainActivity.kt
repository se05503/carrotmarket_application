package com.example.carrotmarketapplication

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.graphics.drawable.ClipDrawable.VERTICAL
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carrotmarketapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    companion object {
        val dataList = mutableListOf<Item>()
    }

    // 좋아요 기능 디테일에서 메인으로 업데이트
    override fun onRestart() {
        super.onRestart()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 사용자 권한 요청(알림)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // 알림 권한이 없는 경우 → 사용자에게 권한 뇨청
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }

        dataAddToList() // 데이터 초기화

        // recyclerView
        val marketAdapter = CustomAdapter(dataList)

        // recycler item 각 항목 사이사이에 구분선 추가
        val decoration =
            DividerItemDecoration(applicationContext, HORIZONTAL) // 처음에 자료보고 VERTICAL 로 지정했었는데 에러뜸

        binding.recyclerView.apply {
            addItemDecoration(decoration) // decoration(구분선) 지정은 어댑터 연결전에 해줘야 한다!
            adapter = marketAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true) // 일단 넣어봄
        }

        // floating button 구현 부분
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }
        var isTop = true

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!binding.recyclerView.canScrollVertically(-1)
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    // 버튼이 점점 사라지게 만든다
                    // 리스트가 최상단에 있는 경우
                    binding.mainFloatingBtn.startAnimation(fadeOut)
                    binding.mainFloatingBtn.visibility = View.GONE
                    isTop = true
                    Log.d("floating", "Top")
                } else {
                    if (isTop) {
                        binding.mainFloatingBtn.visibility = View.VISIBLE
                        binding.mainFloatingBtn.startAnimation(fadeIn)
                        isTop = false
                        Log.d("floating", "Not Top")
                    }
                }
            }
        })

        binding.mainFloatingBtn.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }

//        // 예: 나는 1번째 아이템에 대하여 디테일 페이지에서 하트를 누르고 메인 페이지로 넘어왔다.
//        val bundle = intent.getBundleExtra("bundle")
//        val heartStatus = bundle?.getBoolean("heartStatus") // true
//        val position = bundle?.getInt("position") // 0


        marketAdapter.itemClick = object : CustomAdapter.ItemClick {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                val bundle = Bundle().apply { // bundle 에다가 data class 패킹해서 넣기
                    putParcelable(DetailActivity.PARCEL_KEY, dataList[position])
                }
                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun onItemLongClick(view: View, position: Int) {
                // 기본 다이얼로그 적용 → 뒤로가기 버튼에서도 동일한 다이얼로그 적용했으니 시간되면 함수로 빼기
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("상품 삭제")
                builder.setMessage("상품을 정말로 삭제하시겠습니까?")
                builder.setIcon(R.drawable.ic_chat2)

                // 버튼 클릭시 발생하는 이벤트를 listener 객체에 정의
                val listener = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                deleteItem(position)
                            }

                            DialogInterface.BUTTON_NEGATIVE -> {
                                // 뒤로가기
                                null
                            }
                        }
                    }
                }

                builder.setPositiveButton("확인", listener)
                builder.setNegativeButton("취소", listener)
                builder.show()
            }
        }

        // notification
        binding.ivNotification.setOnClickListener {
            // 알림 발생
            notification()
        }
    }

    // 리스트에서 아이템을 삭제하는 함수
    fun deleteItem(position: Int) {
        dataList.removeAt(position)
        // 아이템이 변경되고 UI가 바뀐걸 업데이트 해주는 코드
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun notification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // android 8.0 이상인 경우
            // 채널 생성
            val channelId = "android-channel"
            val channelName = "Android Channel!"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                // channel 에 대한 다양한 정보 설정
                description = "Carrot Market notification!"
                setShowBadge(true)
                val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(uri, audioAttributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            builder = NotificationCompat.Builder(this, channelId)
        } else {
            // android 8.0 이하인 경우
            builder = NotificationCompat.Builder(this)
        }
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_carrot)
        builder.run {
            setSmallIcon(R.drawable.ic_carrot)
            setWhen(System.currentTimeMillis())
            setContentTitle("키워드 알림")
            setContentText("설정한 키워드에 대한 알림이 도착했습니다!!")
        }
        manager.notify(11, builder.build())
    }

    override fun onBackPressed() {
        // 기본 다이얼로그 사용
        // 뭐지.. deprecated 됐네.. 몰라 일단 로직 작성하고 나중에 바꾸자.
        // neutral button은 따로 지정하지 않으면 화면에서도 안나타나는구나!
        val builder = AlertDialog.Builder(this)
        builder.setTitle("종료")
        builder.setMessage("정말 종료하시겠습니까?")
        builder.setIcon(R.drawable.ic_chat)

        val listener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    super.onBackPressed() // 이거 맞나?
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    null
                }
            }
        }

        builder.setPositiveButton("확인", listener)
        builder.setNegativeButton("취소", listener)
        builder.show()
    }

    private fun dataAddToList() {
        dataList.add(
            Item(
                0,
                R.drawable.sample1,
                "산지 한달된 선풍기 팝니다",
                "이사가서 필요가 없어졌어요 급하게 내놓습니다",
                "대현동",
                1000,
                "서울 서대문구 창천동",
                13,
                25,
                false
            )
        )
        dataList.add(
            Item(
                1,
                R.drawable.sample2,
                "김치냉장고",
                "이사로인해 내놔요",
                "안마담",
                20000,
                "인천 계양구 귤현동",
                8,
                28,
                false
            )
        )
        dataList.add(
            Item(
                2,
                R.drawable.sample3,
                "샤넬 카드지갑",
                "고퀄지갑이구요\n사용감이 있어서 싸게 내어둡니다",
                "코코유",
                10000,
                "수성구 범어동",
                23,
                5,
                false
            )
        )
        dataList.add(
            Item(
                3,
                R.drawable.sample4,
                "금고",
                "금고\n떼서 가져가야함\n대우월드마크센텀\n미국이주관계로 싸게 팝니다",
                "Nicole",
                10000,
                "해운대구 우제2동",
                14,
                17,
                false
            )
        )
        dataList.add(
            Item(
                4,
                R.drawable.sample5,
                "갤럭시Z플립3 팝니다",
                "갤럭시 Z플립3 그린 팝니다\n항시 케이스 씌워서 썻고 필름 한장챙겨드립니다\n화면에 살짝 스크래치난거 말고 크게 이상은없습니다!",
                "절명",
                150000,
                "연제구 연산제8동",
                22,
                9,
                false
            )
        )
        dataList.add(
            Item(
                5,
                R.drawable.sample6,
                "프라다 복조리백",
                "까임 오염없고 상태 깨끗합니다\n정품여부모름",
                "미니멀하게",
                50000,
                "수원시 영통구 원천동",
                25,
                16,
                false
            )
        )
        dataList.add(
            Item(
                6,
                R.drawable.sample7,
                "울산 동해오션뷰 60평 복층 펜트하우스 1일 숙박권 펜션 힐링 숙소 별장",
                "울산 동해바다뷰 60평 복층 펜트하우스 1일 숙박권\n(에어컨이 없기에 낮은 가격으로 " +
                        "변경했으며 8월 초 가장 더운날 다녀가신 분 경우 시원했다고 잘 지내다 가셨습니다)\n1. " +
                        "인원: 6명 기준입니다. 1인 10,000원 추가요금\n2. 장소: 북구 블루마시티, 32-33층\n3. " +
                        "취사도구, 침구류, 세면도구, 드라이기 2개, 선풍기 4대 구비\n4. 예약방법: " +
                        "예약금 50,000원 하시면 저희는 명함을 드리며 입실 오전 잔금 입금하시면 저희는 " +
                        "동.호수를 알려드리며 고객님은 예약자분 신분증 앞면 주민번호 뒷자리 가리시거나 지우시고 " +
                        "문자로 보내주시면 저희는 카드키를 우편함에 놓아 둡니다.\n5. 33층 옥상 야외 테라스 있음, " +
                        "가스버너 있음\n6. 고기 굽기 가능\n7. 입실 오후 3시, 오전 11시 퇴실, 정리, 정돈 , 밸브" +
                        "잠금 부탁드립니다.\n8. 층간소음 주의 부탁드립니다.\n9. 방3개, 화장실3개, 비데 3개\n10." +
                        "저희 집안이 쓰는 별장입니다.",
                "굿리치",
                150000,
                "남구 옥동",
                142,
                54,
                false
            )
        )
        dataList.add(
            Item(
                7,
                R.drawable.sample8,
                "샤넬 탑핸들 가방",
                "샤넬 트랜디 CC 탑핸들 스몰 램스킨 블랙 금장 플랩백 !\n\"색상\" : " +
                        "블랙\n\"사이즈\" : 25.5cm * 17.5cm * 8cm\n\"구성\" : " +
                        "본품더스트\n급하게 돈이 필요해서 팝니다 ㅠ ㅠ",
                "난쉽",
                180000,
                "동래구 온천제2동",
                31,
                7,
                false
            )
        )
        dataList.add(
            Item(
                8,
                R.drawable.sample9,
                "4행정 엔진분무기 판매합니다.",
                "3년전에 사서 한번 사용하고 그대로 둔 상태입니다. 요즘 사용은 안해봤습니다. " +
                        "그래서 저렴하게 내 놓습니다. 중고라 반품은 어렵습니다.\n",
                "알뜰한",
                30000,
                "원주시 명륜2동",
                7,
                28,
                false
            )
        )
        dataList.add(
            Item(
                9,
                R.drawable.sample10,
                "셀린느 버킷 가방",
                "22년 신세계 대전 구매입니당\n셀린느 버킷백\n구매해서 몇번사용했어요\n" +
                        "까짐 스크래치 없습니다.\n타지역에서 보내는거라 택배로 진행합니당!",
                "똑태현",
                190000,
                "중구 동화동",
                40,
                6,
                false
            )
        )
    }
}