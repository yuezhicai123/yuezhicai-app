package cn.xiaizizi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationService : JobService() {
    companion object {
        // 通知渠道ID
        private const val CHANNEL_ID = "default_channel"
        // 通知渠道名称
        private const val CHANNEL_NAME = "默认通知"
        // 通知渠道描述
        private const val CHANNEL_DESCRIPTION = "应用的默认通知渠道"
    }

    override fun onStartJob(params: JobParameters): Boolean {
        // 这里可以执行后台任务来获取通知内容，然后显示通知
        // 为了演示，直接显示一条示例通知
        showNotification(this, "新消息", "您有一条新的论坛消息", 1)
        return false // 任务已完成
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true // 如果任务被中断，是否需要重新调度
    }

    /**
     * 创建通知渠道（Android 8.0+ 必需）
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            // 注册通知渠道
            val notificationManager: NotificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 显示通知
     */
    fun showNotification(context: Context, title: String, content: String, notificationId: Int) {
        // 创建通知点击后的意图
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 显示通知
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}