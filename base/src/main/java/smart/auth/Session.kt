package smart.auth

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.log.Log
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.work.*
import dev.eastar.ktx.startMain
import smart.base.NN
import smart.net.BEnty
import smart.net.Net
import java.util.concurrent.TimeUnit

fun Application.session() = registerActivityLifecycleCallbacks(Session.create(this))

object Session : Application.ActivityLifecycleCallbacks {
    fun create(application: Application): Session {
        this.application = application
        return this
    }

    private lateinit var application: Application
    private const val MAX_SESSION_MINUTES: Long = 10L
    private const val TIMEOUT_WORKER = "timeoutWorker"

    private var mLastActivity: AppCompatActivity? = null
    private var mAliveRefCount: Int = 0

    private val timeoutWarnWorker by lazy {
        OneTimeWorkRequestBuilder<TimeoutWarnWorker>()
                .setInitialDelay(MAX_SESSION_MINUTES - 1, TimeUnit.MINUTES)
                .build()
    }
    private val timeoutWorker by lazy {
        OneTimeWorkRequestBuilder<TimeoutWorker>()
                .setInitialDelay(MAX_SESSION_MINUTES, TimeUnit.MINUTES)
                .build()
    }

    @JvmStatic
    fun startSessionTimeout() {
        Log.e("SS<<", "startSessionTimeout ")
        if (AA.isLogin()) WorkManager.getInstance(application).enqueueUniqueWork(TIMEOUT_WORKER, ExistingWorkPolicy.REPLACE, listOf(timeoutWarnWorker, timeoutWorker))
    }

    @JvmStatic
    fun stopSessionTimeout() {
        Log.w("SS<<", "stopSessionTimeout ")
        WorkManager.getInstance(application).cancelUniqueWork(TIMEOUT_WORKER)
    }

    @JvmStatic
    fun updateSession() {
        Log.pn(Log.INFO, 1, "SS>>", "updateSession ")
        stopSessionTimeout()
//        if (AA.isLogin()) Net.asyncKt(SessionUpdate(), { startSessionTimeout() })
    }

    @Suppress("unused")
    fun addRef() {
        mAliveRefCount++
        Log.e("SS", "addRef ", mAliveRefCount)
    }

    @Suppress("unused")
    fun removeRef() {
        mAliveRefCount--
        Log.w("SS", "removeRef ", mAliveRefCount)
        if (mAliveRefCount < 0)
            mAliveRefCount = 0
    }

    //@formatter:off
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) = updateSession()
    override fun onActivityResumed(activity: Activity?) { mLastActivity = activity as? AppCompatActivity }
    override fun onActivityDestroyed(activity: Activity?) {}
    override fun onActivityStarted(activity: Activity?) {}
    override fun onActivityStopped(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
    override fun onActivityPaused(activity: Activity?) {}
    //@formatter:on

    private var warnDlg: AlertDialog? = null

    class TimeoutWarnWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
        override fun doWork(): Result {
            Log.w("SS<<", "TimeoutWarnWorker", id)
            if (!AA.isLogin()) {
                Log.e("SS>>", "로그아웃이 되어 있어서 통과")
                return Result.success()
            }

            if (mAliveRefCount > 0) {
                Log.e("SS>>", "참조 카운트가 있어서 자동업데이트 ", mAliveRefCount)
                updateSession()
                return Result.success()
            }

            mLastActivity?.run {
                if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    Log.i("SS<<!", "마지막 activity가 참조 DESTROYED 종료됨")
                    return Result.success()
                }
                runOnUiThread {
                    Log.i("SS<<", javaClass.simpleName, lifecycle.currentState, "1분 후 로그아웃 됩니다. 연장하시겠습니까?")
                    warnDlg = AlertDialog.Builder(this)
                            .setMessage("1분 후 로그아웃 됩니다. 연장하시겠습니까?")
                            .setPositiveButton("연장") { _, _ -> updateSession() }
                            .setNegativeButton("취소", null)
                            .setCancelable(false)
                            .show()
                }

            }
            return Result.success()
        }
    }

    class TimeoutWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
        override fun doWork(): Result {
            Log.w("SS<<", "TimeoutWarnWorker", id)
            AA.logout()
            warnDlg?.dismiss()
            mLastActivity?.run {
                if (lifecycle.currentState == Lifecycle.State.DESTROYED)
                    return Result.success()

                runOnUiThread {
                    Log.i("SS<<", javaClass.simpleName, lifecycle.currentState, "10분이상 사용하지 않아 로그아웃 합니다.")
                    AlertDialog.Builder(this).setMessage("10분이상 사용하지 않아 로그아웃 합니다.")
                            .setPositiveButton("확인") { _, _ -> mLastActivity?.startMain() }
                            .setCancelable(false)
                            .show()
                }
            }
            return Result.success()
        }
    }
}

class SessionUpdate : BEnty() {
    @Suppress("unused")
    private val data: Data = Data()

    init {}

    class Data
}