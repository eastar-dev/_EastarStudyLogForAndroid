package smart.base

import smart.auth.Session
import smart.net.UserAgent

abstract class BApplication : BDApplication() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess) {
            UserAgent.create(this)
            Session.create(this)
        }
    }
}