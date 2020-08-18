package com.batyuk.dmytro.nfcreader.impl

import android.nfc.tech.IsoDep
import com.batyuk.dmytro.librarynfc.*
import java.io.IOException

/**
 * [ITag] implementation which encapsulates [IsoDep] tag. Responsible for:
 *  - mapping [ByteArray] into [Entity] and vice versa
 *  - sending and receiving [ByteArray] via [IsoDep] tag
 *  - notifying [ITag] state changes via [callback]
 */
internal class Tag(isoDep: IsoDep) : ITag {
    private var isoDep: IsoDep? = isoDep

    override var callback: ITag.Callback? = null


    override fun connect() {
        return isoDep?.connect() ?: throw NullPointerException("isoDep is null")
    }

    override fun send(command: Command): Entity {
        return isoDep?.let {
            try {
                command
                    .let(Utils::toByteArray)
                    .let(it::transceive)
                    .let(Utils::toEntity)
            } catch (ioe: IOException) {
                Error(ioe)
            }
        } ?: Error(NullPointerException("isoDep is null"))
    }

    override fun disconnect() {
        isoDep?.close()
        isoDep = null
        callback?.onDisconnected()
    }

    fun onFailed(t: Throwable) {
        isoDep = null
        callback?.onFailed(t)
    }
}