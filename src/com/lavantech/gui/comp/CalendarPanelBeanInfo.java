package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/** BeanInfo class for the CalendarPanel. */
public class CalendarPanelBeanInfo extends SimpleBeanInfo
{
    /** Return an icon for the bean. */
    public Image getIcon(int kind)
    {
        switch(kind)
        {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("calpanel16.gif");

            case BeanInfo.ICON_COLOR_32x32:
            case BeanInfo.ICON_MONO_32x32:
            default:
                return loadImage("calpanel32.gif");
        }
    }

    /** Return a descriptor for the bean. */
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(CalendarPanel.class);
    }

    private PropertyDescriptor createPropertyDescriptor(String propName, String description)
        throws Exception
    {
        PropertyDescriptor pd = new PropertyDescriptor(propName, CalendarPanel.class);
        pd.setShortDescription(description);
        pd.setPreferred(true);
        return pd;
    }

    /** Returns an array of PropertyDescriptor objects that specify
    *  information about the properties supported by the CalendarPanel.
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        try
        {
            PropertyDescriptor[] props =
            {
                createPropertyDescriptor("calendar", "Selected Date Time"),
                createPropertyDescriptor("unavailableDayColor", "Color of unavailable days"),
                createPropertyDescriptor("selectedDayColor", "Color of selected day"),
                createPropertyDescriptor("editable", "Date editable by user"),
            };

            //We need to set a custom property editor for Calendar
            props[0].setPropertyEditorClass(CalendarPropertyEditor.class);

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
        Class superclass = CalendarPanel.class.getSuperclass();
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
