package com.batyuk.dmytro.nfcreader

import com.batyuk.dmytro.librarynfc.ITag

object NfcBridge {
    var client: INfcClient? = null

    interface INfcClient {
        fun onTagConnected(tag: ITag)
    }
}
