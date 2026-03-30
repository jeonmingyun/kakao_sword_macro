package com.example.kakaoswordmacro

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.random.Random

class MyMacroService : AccessibilityService() {

    companion object {
        var isMacroRunning = false
        var targetRoomName = ""
        var myNickname = ""
        var targetLevel = 0
        var isAutoSellEnabled = false
        private var lastSendTime = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isMacroRunning || targetRoomName.isEmpty() || myNickname.isEmpty()) return

        // 1. 메시지 텍스트 추출 (리스트를 문자열로 합침)
        val receivedText = event.text.joinToString("")
        if (receivedText.isEmpty()) return

        val rootNode = rootInActiveWindow ?: return

        // 2. 채팅방 이름 확인
        val roomTitleNodes = rootNode.findAccessibilityNodeInfosByViewId("com.kakao.talk:id/title")
        val currentRoomName =
            if (roomTitleNodes.isNotEmpty()) roomTitleNodes[0].text?.toString() ?: "" else ""
        if (!currentRoomName.contains(targetRoomName)) return

        // ---------------------------------------------------------
        // 3. [판매 완료 확인] -> 다시 강화 시작 (무한 루프의 핵심)
        // 조건: 사용자 이름 포함 AND "검 판매" 포함 AND "획득 골드" 포함
        // ---------------------------------------------------------
        val isSaleComplete = receivedText.contains(myNickname) &&
                receivedText.contains("검 판매") &&
                receivedText.contains("획득 골드")

        if (isSaleComplete && isAutoSellEnabled) {
            Log.d("Macro", "판매 완료 확인됨. 다시 강화를 시작합니다.")
            executeWithDelay(rootNode, "/강화 ")
            return // 판매 확인 로직이 실행되면 아래 로직은 건너뜀
        }// 4. [목표 도달 확인] -> 판매 명령어 전송
        val goalText = "[+$targetLevel]"
        if (receivedText.contains(myNickname) && receivedText.contains(goalText)) {
            if (isAutoSellEnabled) {
                // 목표 도달 시 /판매 전송
                executeWithDelay(rootNode, "/판매")
                Log.d("Macro", "목표 도달: /판매 전송")
            } else {
                isMacroRunning = false
                Log.d("Macro", "목표 도달! 매크로를 종료합니다")
            }
            return
        }

        // 5. [일반 강화 결과 확인] (성공/유지/파괴) -> 다음 강화 진행
        val isMyResult = receivedText.contains(myNickname)
        val isGameResult = receivedText.contains("강화 성공") || receivedText.contains("강화 유지") ||
                receivedText.contains("강화 파괴")

        if (isMyResult && isGameResult) {
            executeWithDelay(rootNode, "/강화 ")
        }
    }

    // 딜레이와 쿨타임을 포함한 실행 함수
    private fun executeWithDelay(rootNode: AccessibilityNodeInfo, command: String) {
        val currentTime = System.currentTimeMillis()
        // 방어 로직: 최소 5초 간격 유지 (카톡 차단 방지)
        if (currentTime - lastSendTime > 5000) {
            lastSendTime = currentTime
            Handler(Looper.getMainLooper()).postDelayed({
                if (isMacroRunning) {
                    sendSpecificCommand(rootNode, command)
                }
            }, Random.nextLong(1500, 3000)) // 1.5~3초 사이 랜덤 지연
        }
    }

    // 실제 전송 로직
    private fun sendSpecificCommand(rootNode: AccessibilityNodeInfo, command: String) {
        Log.d("Macro", "전송 명령어: $command")
        val editTexts =
            rootNode.findAccessibilityNodeInfosByViewId("com.kakao.talk:id/message_edit_text")
        if (editTexts.isNotEmpty()) {
            val inputNode = editTexts[0]
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                command
            )
            inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            val sendButtons =
                rootNode.findAccessibilityNodeInfosByViewId("com.kakao.talk:id/send_button")
            if (sendButtons.isNotEmpty()) {
                sendButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    override fun onInterrupt() {}
}