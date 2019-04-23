package com.lavantech.gui.comp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;

/** LocaleSpecificResources class provides language specific
 *  string and other preferences for the Date Time Picker components. If 
 *  string used for "OK", "Cancel", "Hour", "Min"...  needs to be changed to 
 *  a language other than english, this is the class that needs to be customized.
 *  The class also sets the preferences for displaying hour in 12 hour or 24 hour format.
 *  This hour preference is used in Time Panel.
 */
public class LocaleSpecificResources
{
    /** Value of Hour format indicating 12 hour format. */
    public static final int HOUR_FORMAT_12 = 1;

    /** Value of Hour format indicating 24 hour format. */
    public static final int HOUR_FORMAT_24 = 2;

    private static Hashtable labelStrings = new Hashtable();
    private static int hourFormat = HOUR_FORMAT_12;
    
    static
    {
        labelStrings.put("okay", "OK");
        labelStrings.put("cancel", "Cancel");
        labelStrings.put("hour", "Hour");
        labelStrings.put("minute", "Min");
        labelStrings.put("seconds", "Sec");
        labelStrings.put("today", "Today");
        labelStrings.put("now", "Now");
        labelStrings.put("clear", "Clear");
    }

    /** Sets the language specific string for given key string. If there is no
     *  match for the key string, a new key is added to the resource list. 
     *  The following key are predefined and set with english string. 
     *  <table>
     *  <tr><th>Key String</th><th>Predefined value</th></tr>
     *  <tr><td>okay</td><td>OK</td></tr>
     *  <tr><td>cancel</td><td>Cancel</td></tr>
     *  <tr><td>hour</td><td>Hour</td></tr>
     *  <tr><td>minute</td><td>Min</td></tr>
     *  <tr><td>seconds</td><td>Sec</td></tr>
     *  <tr><td>today</td><td>Today</td></tr>
     *  <tr><td>now</td><td>Now</td></tr>
     *  <tr><td>clear</td><td>Clear</td></tr>
     *  </table>
     */
    public static void setLabelString(String key, String value)
    {
        labelStrings.put(key, value);
    }

    /** Returns the language specific string for a given key string. See setLabelString
     *  method for a list of predefined keys. 
     *  @param key Key string for which the value will be returned.
     *  @return Language specific string for the given key string, or null if key is not defined.
     */
    public static String getLabelString(String key)
    {
        return (String)labelStrings.get(key);
    }

    /** Sets the hour format preference. The hour format can either be 
     *  HOUR_FORMAT_12 or HOUR_FORMAT_24. This format is used in TimePanel. 
     */
    public static void setHourFormat(int format)
    {
        hourFormat = format;
    }

    /** Returns the hour format preference. This format is used in TimePanel.
     */
    public static int getHourFormat()
    {
        return hourFormat;
    }
}
