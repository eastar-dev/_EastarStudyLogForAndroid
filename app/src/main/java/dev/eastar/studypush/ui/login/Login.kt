package dev.eastar.studypush.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding2.widget.RxTextView
import dev.eastar.ktx.toast
import dev.eastar.operaxinterceptor.event.OperaXEvent
import dev.eastar.operaxinterceptor.event.OperaXEventObservable
import dev.eastar.operaxinterceptor.event.OperaXEventObserver
import dev.eastar.operaxinterceptor.event.OperaXEvents
import dev.eastar.studypush.R
import dev.eastar.studypush.databinding.LoginBinding
import smart.base.BActivity

class Login : BActivity() {

    private lateinit var bb: LoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bb = LoginBinding.inflate(layoutInflater)
        setContentView(bb.root)
    }

    override fun onLoadOnce() {
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory()).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@Login, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            bb.login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                bb.username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                bb.password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@Login, Observer {
            val loginResult = it ?: return@Observer

            bb.loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        RxTextView.afterTextChangeEvents(bb.username)
            .subscribe {
                loginViewModel.loginDataChanged(
                    bb.username.text.toString(),
                    bb.password.text.toString()
                )
            }.autoDispose()

        RxTextView.afterTextChangeEvents(bb.password)
            .subscribe {
                loginViewModel.loginDataChanged(
                    bb.username.text.toString(),
                    bb.password.text.toString()
                )
            }.autoDispose()

        bb.password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> login()

            }
            false
        }

        bb.login.setOnClickListener { login() }
    }

    override fun login() {
        bb.loading.visibility = View.VISIBLE
        loginViewModel.login(bb.studyGroup.text.toString(), bb.username.text.toString(), bb.password.text.toString())
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        toast("$welcome $displayName")
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        toast(errorString)
        OperaXEventObservable.notify(OperaXEvents.Exited)
    }

    override fun onBackPressedEx(): Boolean {
        if (super.onBackPressedEx())
            return true
        OperaXEventObservable.notify(OperaXEvents.Exited)
        return true
    }
}
