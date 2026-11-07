package com.mylove.app.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 生理期数据上传前用口令派生的 AES-GCM 加密，服务器只能看到密文。
 * 两台设备需要设置同一个口令才能互相解密查看。
 */
object PeriodCrypto {

    private const val ITERATIONS = 12000
    private const val KEY_BITS = 256
    private const val TAG_BITS = 128
    private const val IV_SIZE = 12

    private fun deriveKey(password: String, salt: String): SecretKey {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(Charsets.UTF_8),
            ITERATIONS,
            KEY_BITS
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    fun encrypt(password: String, plain: String, salt: String): String? = runCatching {
        if (password.isBlank()) return@runCatching null
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            deriveKey(password, salt.ifBlank { "my_love_period" }),
            GCMParameterSpec(TAG_BITS, iv)
        )
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }.getOrNull()

    fun decrypt(password: String, payload: String, salt: String): String? = runCatching {
        if (password.isBlank() || payload.isBlank()) return@runCatching null
        val bytes = Base64.decode(payload, Base64.NO_WRAP)
        if (bytes.size <= IV_SIZE) return@runCatching null
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val data = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            deriveKey(password, salt.ifBlank { "my_love_period" }),
            GCMParameterSpec(TAG_BITS, iv)
        )
        String(cipher.doFinal(data), Charsets.UTF_8)
    }.getOrNull()
}