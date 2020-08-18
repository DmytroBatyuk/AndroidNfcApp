package com.batyuk.dmytro.nfcreader.impl

import android.nfc.NfcAdapter
import android.nfc.TagLostException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.batyuk.dmytro.librarynfc.*
import com.batyuk.dmytro.nfcreader.INfcReader
import com.batyuk.dmytro.nfcreader.NfcBridge
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.lang.NullPointerException
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit

private var READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
private val TAG = NfcReader::class.simpleName

/**
 * [INfcReader] implementation responsible for:
 *  - connecting and sending APDU command to [ITag]
 *  - notifying [NfcBridge.INfcClient] about available for communication [ITag]
 *  - enables\disables [NfcAdapter] read mode
 */
internal class NfcReader(private val activity: AppCompatActivity) : INfcReader {
    private val adapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(activity) }
    private val nfcCallback =
        NfcReaderCallback()

    init {
        this.nfcCallback.onTagDiscovered = { tag ->
            if (connect(tag) && selectApdu(tag)) {
                NfcBridge.client?.onTagConnected(tag)
            }
        }
    }


    override fun enable() {
        adapter?.enableReaderMode(
            activity, nfcCallback,
            READER_FLAGS, null
        )
    }

    override fun disable() {
        adapter?.disableReaderMode(activity)
    }


    private fun connect(tag: ITag): Boolean {
        return try {
            tag.connect()
            Log.i(TAG, "tag connected")
            true
        } catch (ioe: IOException) {
            Log.e(TAG, "tag connection failed: $ioe")
            false
        } catch (npe: NullPointerException) {
            Log.e(TAG, "tag connection failed: $npe")
            false
        }
    }

    private fun selectApdu(tag: ITag): Boolean {
        return when (val result = tag.send(SelectApdu)) {
            is ApduSelectedOk -> {
                Log.i(TAG, "apdu command sent")
                true
            }
            is Error -> {
                Log.e(TAG, "apdu command sending failed: ${result.t}")
                false
            }
            else -> false
        }
    }
}