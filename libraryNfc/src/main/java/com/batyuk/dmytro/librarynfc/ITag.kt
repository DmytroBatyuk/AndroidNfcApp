package com.batyuk.dmytro.librarynfc

import java.io.IOException
import java.lang.NullPointerException

interface ITag {
    var callback: Callback?

    @Throws(IOException::class, NullPointerException::class)
    fun connect()
    fun send(command: Command): Entity
    fun disconnect()

    interface Callback {
        fun onDisconnected()
        fun onFailed(t: Throwable)
    }
}