package smart.base

import android.log.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BViewModel : ViewModel() {
    val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    val _toast = MutableLiveData<String>()
    val toast: LiveData<String>
        get() = _toast

    val _alert = MutableLiveData<String>()
    val alert: LiveData<String>
        get() = _alert

    val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val disposables = CompositeDisposable()
    fun Disposable.autoDisposable() {
        disposables.add(this)
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    @Suppress("UnstableApiUsage")
    fun <T> Single<T>.progress(): Single<T> =
        doOnSubscribe {
            Log.e("doOnSubscribe")
            _isProgress.postValue(true)
        }.doOnError {
            Log.e("doOnError")
            _isProgress.postValue(false)
        }.doOnSuccess {
            Log.e("doOnSuccess")
            _isProgress.postValue(false)
        }.doOnTerminate {
            Log.e("doOnTerminate")
            _isProgress.postValue(false)
        }
}