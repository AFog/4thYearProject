package com.aaronfogarty.huh;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.aaronfogarty.huh.RegistrationActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by aaronfogartyfogarty on 19/04/2017.
 */

@RunWith(AndroidJUnit4.class)
public class RegistrationActivityTest {

    @Test
    public void checkShaGenerator() throws Exception {

        RegistrationActivity r = new RegistrationActivity();
        String testvalue = r.get_SHA_512_SecurePassword("12345", "hello");

        assertEquals("6B237C9376425021B4AE4296DC75D892460D97D12BC2FC5B8D07AC31EF0624F1749EFB0EA25FD4BFE19A3A01BFF6BA95334065112966FCFBFC158E936CD39888", testvalue);
    }
}
