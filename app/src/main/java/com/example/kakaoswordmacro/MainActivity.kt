package com.example.kakaoswordmacro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinnerLevel = findViewById<Spinner>(R.id.spinner_target_level)
        val etRoomName = findViewById<EditText>(R.id.et_room_name)
        val etUserName = findViewById<EditText>(R.id.et_user_name)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnSettings = findViewById<Button>(R.id.btn_settings)
        val switchAutoSell = findViewById<SwitchCompat>(R.id.switch_auto_sell)

        // 1. Spinner에 1~20까지 숫자 데이터 넣기
        val levels = (1..20).toList() // [1, 2, 3, ..., 20] 리스트 생성
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLevel.adapter = adapter
        // 기본값 설정 (예: 12강)
        spinnerLevel.setSelection(11)

        btnSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // 2. 시작 버튼 클릭 시 처리
        btnStart.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            val userName = etUserName.text.toString().trim()

            // Spinner에서 선택된 숫자 가져오기
            val selectedLevel = spinnerLevel.selectedItem as Int

            if (roomName.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "방 이름과 닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // 서비스 전역 변수에 값 전달
                MyMacroService.targetRoomName = roomName
                MyMacroService.myNickname = userName
                MyMacroService.targetLevel = selectedLevel
                MyMacroService.isMacroRunning = true

                Toast.makeText(this, "${userName}님, +${selectedLevel}강 목표로 시작!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // 자동 판매 여부
        btnStart.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            val userName = etUserName.text.toString().trim()
            val selectedLevel = spinnerLevel.selectedItem as Int
            val isAutoSellEnabled = switchAutoSell.isChecked // 스위치 상태 가져오기

            if (roomName.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "설정을 모두 입력해주세요!", Toast.LENGTH_SHORT).show()} else {
                MyMacroService.targetRoomName = roomName
                MyMacroService.myNickname = userName
                MyMacroService.targetLevel = selectedLevel
                MyMacroService.isAutoSellEnabled = isAutoSellEnabled // 서비스에 전달
                MyMacroService.isMacroRunning =true

                val sellMsg = if(isAutoSellEnabled) "자동판매 포함" else "자동판매 미사용"
                Toast.makeText(this, "+${selectedLevel}강 목표 시작 ($sellMsg)", Toast.LENGTH_SHORT).show()
            }
        }

        btnStop.setOnClickListener {
            MyMacroService.isMacroRunning = false
            Log.d("Macro", "btn click : 매크로 정지 버튼 클릭")
            Toast.makeText(this, "매크로 정지", Toast.LENGTH_SHORT).show()
        }
    }
}