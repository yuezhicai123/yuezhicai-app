package cn.xiaizizi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 处理通知相关的广播
        val action = intent.action
        if (action == "cn.xiaizizi.NOTIFICATION_ACTION") {
            // 获取通知内容
            val title = intent.getStringExtra("title") ?: "新消息"
            val content = intent.getStringExtra("content") ?: "您有一条新通知"
            val notificationId = intent.getIntExtra("notificationId", 0)
            
            // 创建NotificationService实例并显示通知
            val notificationService = NotificationService()
            notificationService.createNotificationChannel(context)
            notificationService.showNotification(context, title, content, notificationId)
        }
    }
}