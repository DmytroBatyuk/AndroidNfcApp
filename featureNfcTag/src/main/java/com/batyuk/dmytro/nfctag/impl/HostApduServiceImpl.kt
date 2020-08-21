package com.batyuk.dmytro.nfctag.impl

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.batyuk.dmytro.librarynfc.*
import com.batyuk.dmytro.nfctag.NfcBridge

private val TAG = HostApduServiceImpl::class.java.simpleName

/**
 * [HostApduService] implementation responsible for:
 *  - detecting APDU command and sending back APDU seleck OK response
 *  - mapping [ByteArray] to [Entity] and vise vesta
 *  - notifying about successful APDU via [NfcBridge.INfcClient.onApdu]
 *  - notifying about new command via [NfcBridge.INfcClient.onCommand] (in case
 *  [NfcBridge.INfcClient] is [null] returning [NotReady] command
 */
class HostApduServiceImpl : HostApduService() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "service: created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "service: destroyed")
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        val send = if (SelectApdu.let(Utils::toByteArray).contentEquals(commandApdu)) {
            Log.i(TAG, "recv: apdu: ${Utils.byteArrayToHexString(commandApdu)}")
            NfcBridge.client?.onApdu()
            if (NfcBridge.client?.isApduSelectReady() != true) {
                return null
            }
            ApduSelectedOk
        } else if (null != NfcBridge.client) {

            commandApdu
                .let(Utils::toEntity)
                .let {
                    Log.i(TAG, "recv: $it")
                    it
                }
                .let(NfcBridge.client!!::onCommand)
        } else {
            NotReady(1)
        }

        Log.i(TAG, "service: send: $send")
        return Utils.toByteArray(send)
    }

    override fun onDeactivated(reason: Int) {
        //TODO: show when connection completed and explain the reason
    }
}