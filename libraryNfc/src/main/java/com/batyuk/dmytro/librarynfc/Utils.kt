package com.batyuk.dmytro.librarynfc

import java.lang.UnsupportedOperationException
import java.nio.charset.Charset

object Utils {
    // AID for our loyalty card service.
    private const val AID = "F202008110"

    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private const val SELECT_APDU_HEADER = "00A40400"

    // "OK" status word sent in response to SELECT AID command (0x9000)
    val SELECT_APDU_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
    val RESPONSE_DATA_NOT_READY: Byte = 0x00
    val RESPONSE_DATA_OK: Byte = 0x01
    val RESPONSE_EXCHANGE_COMPLETE: Byte = 0x02

    val COMMAND_DATA: Byte = 0x00
    val COMMAND_NOT_READY: Byte = 0x01
    val COMMAND_EXCHANGE_COMPLETED: Byte = 0x02
    val COMMAND_UNKNOWN = 0xFF.toByte()

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    fun buildSelectApdu(): ByteArray {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return hexStringToByteArray(
            SELECT_APDU_HEADER + String.format(
                "%02X",
                AID.length / 2
            ) + AID
        )
    }

    fun toEntity(bytes: ByteArray): Entity {
        return when (bytes.firstOrNull()) {
            COMMAND_DATA -> Data(String(bytes.copyOfRange(1, bytes.size), Charset.forName("UTF-8")))
            COMMAND_NOT_READY -> NotReady(bytes.getOrNull(1)?.toLong() ?: 0)
            COMMAND_EXCHANGE_COMPLETED -> ExchangeCompleted
            COMMAND_UNKNOWN -> Unknown
            else -> {
                if (buildSelectApdu().contentEquals(bytes)) {
                    return SelectApdu
                } else if (bytes.size > 1 && SELECT_APDU_OK.contentEquals(
                        byteArrayOf(bytes[0], bytes[1])
                    )
                ) {
                    ApduSelectedOk
                } else {
                    Error(
                        UnsupportedOperationException(
                            "command = ${byteArrayToHexString(
                                if (bytes.isEmpty()) {
                                    byteArrayOf()
                                } else {
                                    byteArrayOf(bytes.first())
                                }
                            )}"
                        )
                    )
                }
            }
        }
    }

    fun toByteArray(command: Command): ByteArray {
        return when (command) {
            is SelectApdu -> buildSelectApdu()
            is ApduSelectedOk -> SELECT_APDU_OK
            is Data -> byteArrayOf(COMMAND_DATA, *command.text.toByteArray())
            is NotReady -> byteArrayOf(COMMAND_NOT_READY, command.delaySeconds.toByte())
            ExchangeCompleted -> byteArrayOf(COMMAND_EXCHANGE_COMPLETED)
            Unknown -> byteArrayOf(COMMAND_UNKNOWN)
        }
    }

    /**
     * Utility class to convert a hexadecimal string to a byte string.
     *
     *
     * Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     */
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16).shl(4))
                    + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    /**
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    fun byteArrayToHexString(bytes: ByteArray): String? {
        val hexArray = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            hexChars[i * 2] = hexArray[bytes[i].toInt().and(0xF0).ushr(4)]
            hexChars[i * 2 + 1] = hexArray[bytes[i].toInt().and(0x0F)]
        }
        return String(hexChars)
    }
}