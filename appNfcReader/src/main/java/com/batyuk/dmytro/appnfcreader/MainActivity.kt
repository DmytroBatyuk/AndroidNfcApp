package com.batyuk.dmytro.appnfcreader

import android.nfc.TagLostException
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.batyuk.dmytro.librarynfc.*
import com.batyuk.dmytro.nfcreader.Factory
import com.batyuk.dmytro.nfcreader.INfcReader
import com.batyuk.dmytro.nfcreader.NfcBridge
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit

private val TAG = MainActivity::class.simpleName

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val compositeDisposable = CompositeDisposable()
    private val reader: INfcReader by lazy {
        Factory.createReader(this)
    }

    private val client = object: NfcBridge.INfcClient {
        override fun onTagConnected(tag: ITag) {
            testDataExchange(tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NfcBridge.client = client
    }

    override fun onResume() {
        super.onResume()
        reader.enable()
    }

    override fun onPause() {
        super.onPause()
        reader.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        NfcBridge.client = null
        compositeDisposable.clear()
    }


    private fun testDataExchange(tag: ITag) {
        Observable.fromCallable {
            val send = Data("Test message")
            try {
                do {
                    Log.i(TAG, "send: $send")
                    var recv = tag.send(send)
                    Log.i(TAG, "recv: $recv")
                    while (recv is NotReady) {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(recv.delaySeconds))
                        Log.i(TAG, "send: $send")
                        recv = tag.send(send)
                        Log.i(TAG, "recv: $recv")
                    }
                    if (recv is ExchangeCompleted) {
                        tag.disconnect()
                    }

                } while (recv is Data)
            } catch (tle: TagLostException) {
                Log.e(TAG, "tag has been lost")
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe({}, { Log.e(TAG, "failed=${it.message}", it) })
            .run(compositeDisposable::add)
    }

}