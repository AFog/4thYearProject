package com.aaronfogarty.huh;

import android.preference.PreferenceManager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aaronfogartyfogarty on 19/04/2017.
 */



public class RegistrationActivityTest {

    @Test
    public void isPasswordValid() throws Exception {
        String password1 = "Password123";
        String password2 = "pass";
        String password3 = "Password";
        String password4 = "password123";


        RegistrationActivity r = new RegistrationActivity();
        boolean actualValue1 = r.isPasswordValid(password1);
        boolean actualValue2 = r.isPasswordValid(password2);
        boolean actualValue3 = r.isPasswordValid(password3);
        boolean actualValue4 = r.isPasswordValid(password3);

        boolean expectedValue1 = true;

        //check password is correct
        assertEquals(expectedValue1, actualValue1);
        //check password is too short
        assertNotEquals(expectedValue1, actualValue2);
        //check password contains a number
        assertNotEquals(expectedValue1, actualValue3);
        //check value contains a Uppercse letter
        assertNotEquals(expectedValue1,actualValue4);

    }

    @Test
    public void saveCredentials() throws Exception {


        RegistrationActivity r = new RegistrationActivity();
        String actualPassword = "12345";
        String actualUser = "NewUser";
        r.saveCredentials(actualUser,actualPassword);


    }


    @Test
    public void get_SHA_512_SecurePassword() throws Exception {

        RegistrationActivity r = new RegistrationActivity();
        String actualValue = r.get_SHA_512_SecurePassword("12345", "helloworld");
        String expectedValue = "6b237c9376425021b4ae4296dc75d892460d97d12bc2fc5b8d07ac31ef0624f1749efb0ea25fd4bfe19a3a01bff6ba95334065112966fcfbfc158e936cd39888";
        assertEquals(expectedValue, actualValue);

    }

}