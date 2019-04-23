package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/** BeanInfo class for the TimePanel. */
public class TimePanelBeanInfo extends SimpleBeanInfo
{
    /** Return an icon for the bean. */
    public Image getIcon(int kind)
    {
        switch(kind)
        {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("timepanel16.gif");

            case BeanInfo.ICON_COLOR_32x32:
            case BeanInfo.ICON_MONO_32x32:
            default:
                return loadImage("timepanel32.gif");
        }
    }

    /** Return a descriptor for the bean. */
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(TimePanel.class);
    }

    private PropertyDescriptor createPropertyDescriptor(String propName, String description)
        throws Exception
    {
        PropertyDescriptor pd = new PropertyDescriptor(propName, TimePanel.class);
        pd.setShortDescription(description);
        pd.setPreferred(true);
        return pd;
    }

    /** Returns an array of PropertyDescriptor objects that specify
    *  information about the properties supported by the TimePanel.
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        try
        {
            PropertyDescriptor[] props =
            {
                createPropertyDescriptor("calendar", "Selected Date"),
                createPropertyDescriptor("displayDigital", "Whether to display time in Digital form"),
                createPropertyDescriptor("displayAnalog", "Whether to display time in Digital form"),
                createPropertyDescriptor("faceColor", "Color of analog clock face"),
                createPropertyDescriptor("hourNeedleColor", "Hour needle color of analog clock"),
                createPropertyDescriptor("minNeedleColor", "Minute needle color of analog clock"),
                createPropertyDescriptor("secNeedleColor", "Second needle color of analog clock"),
                createPropertyDescriptor("editable", "Time editable by user"),
                createPropertyDescriptor("minDisplayed", "Minute displayed or not"),
                createPropertyDescriptor("secDisplayed", "Second displayed or not"),
            };

            //We need to set a custom property editor for Time
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
        Class superclass = TimePanel.class.getSuperclass();
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
