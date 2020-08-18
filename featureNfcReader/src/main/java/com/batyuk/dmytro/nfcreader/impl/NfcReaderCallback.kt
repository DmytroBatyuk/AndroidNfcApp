package com.batyuk.dmytro.nfcreader.impl

import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.util.Log
import com.batyuk.dmytro.librarynfc.ITag

private val TAG = NfcReaderCallback::class.java.simpleName

/**
 * [NfcAdapter.ReaderCallback] implementation responsible for:
 * - hiding [IsoDep] tag implementation under [ITag] interface
 * - notifying through [onTagDiscovered] about discovered tag
 */
internal class NfcReaderCallback : NfcAdapter.ReaderCallback {

    var onTagDiscovered: ((ITag) -> Unit)? = null

    override fun onTagDiscovered(nfcTag: android.nfc.Tag) {
        Log.i(TAG, "(${Thread.currentThread().name}) onTagDiscovered")
        IsoDep.get(nfcTag)?.let {
            onTagDiscovered?.invoke(Tag(it))
        }
    }
}