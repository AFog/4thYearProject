package com.aaronfogarty.huh;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aaronfogartyfogarty on 19/04/2017.
 */
public class PersonalityInsightsActivityTest {
    @Test
    public void jsonObjectBuild() throws Exception {

        PersonalityInsightsActivity p = new PersonalityInsightsActivity();
        String actualValue = p.jsonObjectBuild("test").toString();
        String expectedValue = "{\"contentItems\":[{\"content\":\"test\"}]}";
        assertEquals(expectedValue, actualValue);

    }

    @Test
    public void getInsightBracketscore() throws Exception {

        PersonalityInsightsActivity p = new PersonalityInsightsActivity();
        double inputValue1 = 0.0;
        double inputValue2 = 0.5;
        double inputValue3 = 1.0;

        String expectedValue1 = "You are not likely to prefer:";
        String expectedValue2 = "You are likely to prefer:";
        String expectedValue3 = "You are very likely to prefer:";

        String actualValue1 = p.getInsightBracketscore(inputValue1);
        assertEquals(expectedValue1, actualValue1);

        String actualValue2 = p.getInsightBracketscore(inputValue2);
        assertEquals(expectedValue2, actualValue2);

        String actualValue3 = p.getInsightBracketscore(inputValue3);
        assertEquals(expectedValue3, actualValue3);
    }

}