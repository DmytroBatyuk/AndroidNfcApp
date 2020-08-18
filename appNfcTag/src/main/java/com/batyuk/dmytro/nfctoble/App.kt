package com.batyuk.dmytro.nfctoble

import android.app.Application
import android.util.Log
import com.batyuk.dmytro.librarynfc.*
import com.batyuk.dmytro.nfctag.NfcBridge
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private val TAG = App::class.simpleName

class App: Application() {
    val compositeDisposable = CompositeDisposable()
    val isDataReady = AtomicBoolean(false)
    val isExchangeCompleted = AtomicBoolean(false)


    private val client = object: NfcBridge.INfcClient {
        override fun onApdu() {
            testPrepareData()
        }
        override fun onCommand(recv: Entity): Command {
            return testDataExchange(recv)
        }
    }

    override fun onCreate() {
        super.onCreate()




        NfcBridge.client = client
    }

    override fun onTerminate() {
        super.onTerminate()
        compositeDisposable.clear()

        NfcBridge.client = null
    }

    private fun testPrepareData() {
        //For testing -- start
        isDataReady.set(false)
        isExchangeCompleted.set(false)
        Completable.timer(4, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                isDataReady.set(true)
            }
            .subscribe().run(compositeDisposable::add)

        Completable.timer(6, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                isExchangeCompleted.set(true)
            }
            .subscribe().run(compositeDisposable::add)
        //For testing -- end
    }

    private fun testDataExchange(recv: Entity): Command {
        Log.i(TAG, "recv: $recv")
        val send = when (recv) {
            is Data -> {
                if (isExchangeCompleted.get()) {
                    ExchangeCompleted
                } else if (!isDataReady.get()) {
                    NotReady(1)
                } else {
                    Data("from tag")
                }
            }
            is NotReady -> {
                Data("Waiting...")
            }
            ExchangeCompleted -> {
                Unknown
            }
            Unknown -> {
                Unknown
            }
            is Error -> {
                Unknown
            }
            SelectApdu,
            ApduSelectedOk -> throw UnsupportedOperationException("not supported package type: $recv")
        }

        Log.i(TAG, "send: $send")
        return send
    }
}