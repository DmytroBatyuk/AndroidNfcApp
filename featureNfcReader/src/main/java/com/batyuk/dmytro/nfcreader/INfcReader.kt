package com.batyuk.dmytro.nfcreader

interface INfcReader {
    /**
     * Enables NFC reader
     */
    fun enable()

    /**
     * Disables NFC reader
     */
    fun disable()
}