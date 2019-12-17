package nl.bezorgdirect.mijnbd.helpers

import android.content.Context
import nl.bezorgdirect.mijnbd.encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.encryption.KeyStoreWrapper

fun getDecryptedToken(context: Context): String{
    val sharedPrefs = context.getSharedPreferences("mybd", Context.MODE_PRIVATE)
    val encryptedToken = sharedPrefs.getString("T", "")

    val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
    val keyStoreWrapper = KeyStoreWrapper(context, "mybd")
    val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")
    val decryptedToken = cipherWrapper.decrypt(encryptedToken!!, Key?.private)
    return decryptedToken
}