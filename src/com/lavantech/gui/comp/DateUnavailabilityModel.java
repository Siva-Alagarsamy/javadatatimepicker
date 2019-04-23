package com.lavantech.gui.comp;

/** DateUnavailabilityModel Interface is used by DateTimePicker and CalendarPanel
 *  to get non selectable dates. 
 */
public interface DateUnavailabilityModel
{

    /** Return an array of day numbers(1-31) unavailable in a month. 
     *  @param month  The month number (0-11)
     *  @param year   The year including the century
     */
    public int[] getUnavailableDaysInAMonth(int month, int year);
}
