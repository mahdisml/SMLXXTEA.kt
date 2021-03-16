package ir.mahdisml.smlxxtea
class SMLXXTEA (private val k:String) {
    private var initialized = false
    private var ks:IntArray? = null
    private val d = -0x61c88647
    private val hexArrays = "0123456789ABCDEF".toCharArray()

    fun encrypt(data: String): ByteArray? {
        return try {
            if (!initialize()){
                return null
            }
            encrypt(data.encodeToByteArray())!!
        } catch (e: Exception) {
            null
        }
    }
    fun decrypt(data: ByteArray): String? {
        return try {
            if (!initialize()){
                return null
            }
            val bytes = decryptByte(data) ?: return null
            var result = ""
            bytes.map {
                result += it
            }
            return result
        } catch (ex: Exception) {
            null
        }
    }

    fun encryptString(data: String): String? {
        return try {
            bytesToHex(encrypt(data)!!)
        } catch (e: Exception) {
            null
        }
    }
    fun decryptString(data: String): String? {
        return try {
            decrypt(hexToBytes(data))
        } catch (e: Exception) {
            null
        }
    }
    private fun initialize () : Boolean {
        return if (!initialized) {
            try {
                ks = toIntArray(fixK(k.encodeToByteArray()), false)
                initialized = true
                true
            } catch (e: Exception) {
                false
            }
        }else {
            true
        }
    }
    private fun fixK(ky: ByteArray): ByteArray {
        if (ky.size == 16) return ky
        return if (ky.size < 16) {
            ky.copyOf(ky.size)
        } else {
            ky.copyOf(16)
        }
    }
    private fun encrypt(data: ByteArray): ByteArray? {
        return if (data.isEmpty()) {
            data
        } else encodeToByteArray(
            encrypt(toIntArray(data, true), ks!!), false
        )
    }
    private fun encrypt(v: IntArray, k: IntArray): IntArray {
        val n = v.size - 1
        if (n < 1) {
            return v
        }
        var p: Int
        var q = 6 + 52 / (n + 1)
        var z = v[n]
        var y: Int
        var sum = 0
        var e: Int
        while (q-- > 0) {
            sum += d
            e = sum ushr 2 and 3
            p = 0
            while (p < n) {
                y = v[p + 1]
                v[p] += mx(sum, y, z, p, e, k)
                z = v[p]
                p++
            }
            y = v[0]
            v[n] += mx(sum, y, z, p, e, k)
            z = v[n]
        }
        return v
    }
    private fun decryptByte(data: ByteArray): ByteArray? {
        return try {
            encodeToByteArray(decrypt(toIntArray(data,false),ks!!),true)
        } catch (e: Exception) {
            null
        }
    }
    private fun decrypt(v: IntArray, k: IntArray): IntArray {
        val n = v.size - 1
        if (n < 1) {
            return v
        }
        var p: Int
        val q = 6 + 52 / (n + 1)
        var z: Int
        var y = v[0]
        var sum = q * d
        var e: Int
        while (sum != 0) {
            e = sum ushr 2 and 3
            p = n
            while (p > 0) {
                z = v[p - 1]
                v[p] -= mx(sum, y, z, p, e, k)
                y = v[p]
                p--
            }
            z = v[n]
            v[0] -= mx(sum, y, z, p, e, k)
            y = v[0]
            sum -= d
        }
        return v
    }
    private fun toIntArray(data: ByteArray, includeLength: Boolean): IntArray {
        var n = if (data.size and 3 == 0) data.size ushr 2 else (data.size ushr 2) + 1
        val result: IntArray
        if (includeLength) {
            result = IntArray(n + 1)
            result[n] = data.size
        } else {
            result = IntArray(n)
        }
        n = data.size
        for (i in 0 until n) {
            result[i ushr 2] = result[i ushr 2] or (0x000000ff and data[i].toInt() shl (i and 3 shl 3))
        }
        return result
    }
    private fun encodeToByteArray(data: IntArray, includeLength: Boolean): ByteArray? {
        var n = data.size shl 2
        if (includeLength) {
            val m = data[data.size - 1]
            n -= 4
            if (m < n - 3 || m > n) {
                return null
            }
            n = m
        }
        val result = ByteArray(n)
        for (i in 0 until n) {
            result[i] = (data[i ushr 2] ushr (i and 3 shl 3)).toByte()
        }
        return result
    }
    private fun mx(sum: Int, y: Int, z: Int, p: Int, e: Int, k: IntArray): Int {
        return (z ushr 5 xor y shl 2) + (y ushr 3 xor z shl 4) xor (sum xor y) + (k[p and 3 xor e] xor z)
    }
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArrays[v ushr 4]
            hexChars[j * 2 + 1] = hexArrays[v and 0x0F]
        }
        return String(hexChars)
    }
    private fun hexToBytes(s: String): ByteArray {
        val data = ByteArray(s.length / 2)
        var i = 0
        var j = 0
        while (i < s.length && j < data.size) {
            data[j] = s.substring(i, i + 2).toInt(16).toByte()
            i += 2
            j++
        }
        return data
    }
}