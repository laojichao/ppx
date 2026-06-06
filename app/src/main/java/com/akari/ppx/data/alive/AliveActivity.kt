package com.akari.ppx.data.alive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.akari.ppx.BuildConfig.APPLICATION_ID

/**
 * 保活 Activity，用于在模块启动时拉起 [AliveService] 以维持进程存活。
 * 启动 Service 后立即结束自身，用户无感知。
 * 通过 [companion object] 的 invoke 操作符从任意位置启动。
 */
class AliveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, AliveService::class.java).let(::startService)
        setResult(RESULT_OK)
        finish()
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    companion object {
        /**
         * 从给定 Context 启动保活 Activity。
         * @param context 上下文
         */
        operator fun invoke(context: Context) = Intent().also {
            ComponentName(APPLICATION_ID, this::class.java.name.split("$")[0]).let(it::setComponent)
        }.let { context.startActivity(it) }
    }
}