package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/** BeanInfo class for the DateTimePicker. */
public class DateTimePickerBeanInfo extends SimpleBeanInfo
{
    /** Return an icon for the bean. */
    public Image getIcon(int kind)
    {
        switch(kind)
        {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("dtpicker16.gif");

            case BeanInfo.ICON_COLOR_32x32:
            case BeanInfo.ICON_MONO_32x32:
            default:
                return loadImage("dtpicker32.gif");
        }
    }

    /** Return a descriptor for the bean. */
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(DateTimePicker.class);
    }

    private PropertyDescriptor createPropertyDescriptor(String propName, String description)
        throws Exception
    {
        PropertyDescriptor pd = new PropertyDescriptor(propName, DateTimePicker.class);
        pd.setShortDescription(description);
        pd.setPreferred(true);
		pd.setBound(true);
        return pd;
    }

    /** Returns an array of PropertyDescriptor objects that specify
    *  information about the properties supported by the DateTimePicker.
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        try
        {
            PropertyDescriptor[] props =
            {
                createPropertyDescriptor("calendar", "Selected Date Time"),
                createPropertyDescriptor("pattern",
                    "Pattern to display Date/Time. "+
                    "The pattern should be in the same syntax as in "+
                    "java.text.SimpleDateFormat"),
                createPropertyDescriptor("editable", "Whether the Date Time Picker is editable by keyboard"),
                createPropertyDescriptor("displayCalendar", "Whether to display Calendar for Date Modification"),
                createPropertyDescriptor("displayClock", "Whether to display Clock for Time Modification"),
                createPropertyDescriptor("dateRollOverByClockEnabled",
                    "Whether changing Clock can affect Date"),
                createPropertyDescriptor("dateTimePopupAutoCancelEnabled",
                    "Whether mouse clicks on other components can automaticallly cancel Date Time Popup"),
                createPropertyDescriptor("date",
                    "Selected Date Time"),
            };

            //We need to set a custom property editor for Calendar
            props[0].setPropertyEditorClass(CalendarPropertyEditor.class);
            props[5].setPreferred(false);
            props[6].setPreferred(false);

            return props;
        }
        catch (Exception e)
        {
            return super.getPropertyDescriptors();
        }
    }

    /** Returns more BeanInfo from super class. */
    public BeanInfo[] getAdditionalBeanInfo()
    {
        Class superclass = DateTimePicker.class.getSuperclass();
        try
        {
            BeanInfo superBeanInfo = Introspector.getBeanInfo(superclass); 
            return new BeanInfo[] { superBeanInfo };
        }
        catch (IntrospectionException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    /** The calendar property is most often customized; make it the default. */
    public int getDefaultPropertyIndex()
    {
        return 0;
    }
}
