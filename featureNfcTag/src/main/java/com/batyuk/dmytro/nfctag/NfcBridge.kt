package com.batyuk.dmytro.nfctag

import com.batyuk.dmytro.librarynfc.Entity
import com.batyuk.dmytro.librarynfc.Command

object NfcBridge {
    var client: INfcClient? = null

    interface INfcClient{
        fun onApdu()
        fun isApduSelectReady(): Boolean
        fun onCommand(recv: Entity): Command
    }
}
