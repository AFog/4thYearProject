package com.aaronfogarty.huh;

/**
 * Created by aaronfogartyfogarty on 21/04/2017.
 */

import android.util.Base64;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class AES {

    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret)
    {
        try
        {
            setKey(secret);

            Cipher ecipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // encode the string into a sequence of bytes using the named charset

            // storing the result into a new byte array.

            byte[] utf8 = strToEncrypt.getBytes("UTF8");

            byte[] enc = ecipher.doFinal(utf8);



// encode to base64

            enc = BASE64EncoderStream.encode(enc);

            return  secret + " " + new String(enc);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }

    public static String decrypt(String strToDecrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher dcipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            dcipher.init(Cipher.DECRYPT_MODE, secretKey);
            // decode with base64 to get bytes

            byte[] dec = BASE64DecoderStream.decode(strToDecrypt.getBytes());

            byte[] utf8 = dcipher.doFinal(dec);

// create new string based on the specified charset

            return new String(utf8, "UTF8");

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }
}
