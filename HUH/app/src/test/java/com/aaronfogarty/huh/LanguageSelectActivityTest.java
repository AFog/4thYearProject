package com.aaronfogarty.huh;

import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Created by aaronfogartyfogarty on 19/04/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class LanguageSelectActivityTest {

    @Mock
    SharedPreferences preferences;
    @Mock
    AccountManager accountManager;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    PreferenceManager preferenceManager;

    @Test
    public void saveLanguageSelect() throws Exception {

        LanguageSelectActivity l = new LanguageSelectActivity();

        //verify - preferences are updated
        verify(preferences, atLeastOnce()).edit();
        //verify - changed to preferences are committed
        verify(editor, atLeastOnce()).commit();
        //when - adding a new language
        l.saveLanguageSelect("fr");

    }

}