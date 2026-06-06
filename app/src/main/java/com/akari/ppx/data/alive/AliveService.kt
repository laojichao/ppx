package com.akari.ppx.data.alive

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.*
import kotlin.concurrent.schedule

/**
 * 保活 Service，通过短暂的前台存在提升进程优先级。
 * 创建后延迟 5 秒自动停止，仅用于触发系统对进程的保活机制。
 */
class AliveService : Service() {
    override fun onCreate() {
        Timer().schedule(5000) {
            stopSelf()
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null
}