package com.batyuk.dmytro.nfcreader

import androidx.appcompat.app.AppCompatActivity
import com.batyuk.dmytro.nfcreader.impl.NfcReader

object Factory {
    fun createReader(activity: AppCompatActivity): INfcReader {
        return NfcReader(activity)
    }
}