package com.lavantech.gui.comp;

import java.beans.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** This class is used by IDE to edit calendar property.  */
public class CalendarPropertyEditor extends PropertyEditorSupport implements ActionListener
{
    private static final String FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";
    private SimpleDateFormat formatter = null;
    private JPanel panel = null;
    private DateTimePicker picker = null;

    public CalendarPropertyEditor()
    {
        formatter = new SimpleDateFormat(FORMAT_STRING);
        panel = new JPanel(new FlowLayout());
        picker = new DateTimePicker(new Date(), FORMAT_STRING);
        panel.add(picker);
        picker.addActionListener(this);
    }

    public String getAsText()
    {
        formatter.setCalendar(picker.getCalendar());
        return formatter.format(picker.getCalendar().getTime());
    }

    public Component getCustomEditor()
    {
        return panel;
    }

    public String getJavaInitializationString()
    {
        GregorianCalendar calendar = picker.getCalendar();
        return  "new java.util.GregorianCalendar("+
            calendar.get(Calendar.YEAR)+", "+
            calendar.get(Calendar.MONTH)+", "+
            calendar.get(Calendar.DATE)+", "+
            calendar.get(Calendar.HOUR_OF_DAY)+", "+
            calendar.get(Calendar.MINUTE)+", "+
            calendar.get(Calendar.SECOND)+")";
    }

    public Object getValue()
    {
        return picker.getCalendar();
    }

    public void setValue(Object obj)
    {
        picker.removeActionListener(this);
        picker.setCalendar((GregorianCalendar)obj);
        picker.addActionListener(this);
    }

    public boolean supportsCustomEditor()
    {
        return true;
    }

    public void actionPerformed(ActionEvent evt)
    {
        firePropertyChange();
    }
}
