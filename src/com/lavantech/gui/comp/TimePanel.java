package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

/** TimePanel displays the given time in a digital and analog format
 *  for a user to change the given time. The time that needs to be
 *  displayed/edited is passed as a GregorianCalendar object to the constructor.
 *  The edited time is returned by getCalendar() method. 
 */
public class TimePanel extends JPanel implements ActionListener, ChangeListener
{
    private PropertyChangeSupport propertySupport = null;
    private Vector actionListeners = new Vector();

    private GregorianCalendar calendar = null;
    private GregorianCalendar oldCalendar = null;

    private JPanel northPanel = null;
    private JPanel digitalCardPanel = null;
    private CardLayout digitalCardLayout= null;
    private JPanel editableDigitalPanel = null;
    private JPanel nonEditableDigitalPanel = null;

    private String[] ampmStrs;

	private JPanel hourPanel1 = new JPanel(new GridLayout(0,1));
	private JPanel minPanel1 = new JPanel(new GridLayout(0,1));
	private JPanel secPanel1 = new JPanel(new GridLayout(0,1));
	private JPanel ampmPanel1 = new JPanel(new GridLayout(0,1));

	private JPanel hourPanel2 = new JPanel(new GridLayout(0,1));
	private JPanel minPanel2 = new JPanel(new GridLayout(0,1));
	private JPanel secPanel2 = new JPanel(new GridLayout(0,1));
	private JPanel ampmPanel2 = new JPanel(new GridLayout(0,1));

    private JLabel hourStrL1 = null;
    private JLabel minStrL1 = null;
    private JLabel secStrL1 = null;
    private JLabel ampmStrL1 = null;
    private JLabel hourStrL2 = null;
    private JLabel minStrL2 = null;
    private JLabel secStrL2 = null;
    private JLabel ampmStrL2 = null;

    //make this package private for DateTimePopup to setFocus
    CustomJSpinner hourS = null;
    private CustomJSpinner minS = null;
    private CustomJSpinner secS = null;
    private JComboBox ampmCB = null;
    private JLabel hourL = null;
    private JLabel minL = null;
    private JLabel secL = null;
    private JLabel ampmL = null;

    private ClockPanel clockPanel = null;
    private int hourFormat;

    private boolean displayAnalog = true;
    private boolean displayDigital = true;

    private GregorianCalendar minSelectableTime = null;
    private GregorianCalendar maxSelectableTime = null;

    // Use flags to ignore events caused by programmatically setting time
    private boolean enableListeners = true;

    private boolean editable = true;
    private boolean hourDisplayed = true;
    private boolean minDisplayed = true;
    private boolean secDisplayed = true;
	private boolean hourMinSecLabelDisplayed = true;

    /** Constructs a TimePanel with the current Date Time.
     */
    public TimePanel()
    {
        this(new GregorianCalendar(), Locale.getDefault());
    }

    /** Constructs a TimePanel with the given GregorianCalendar.
     *  @param cal   The GregorianCalendar that needs to be displayed/edited
     */
    public TimePanel(GregorianCalendar cal)
    {
        this(cal, Locale.getDefault());
    }

    /** Constructs a TimePanel with the given GregorianCalendar.
     *  @param cal   The GregorianCalendar that needs to be displayed/edited
     *  @param locale  The locale that will be used to display "AM/PM" string.
     */
    public TimePanel(GregorianCalendar cal, Locale locale)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        calendar = (GregorianCalendar)cal.clone();
        oldCalendar = (GregorianCalendar)calendar.clone();
        setLayout(new BorderLayout());

        clockPanel = new ClockPanel(calendar);
        clockPanel.addActionListener(this);
        add(clockPanel, BorderLayout.CENTER);

        northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(northPanel, BorderLayout.NORTH);

        digitalCardLayout = new CardLayout(0,0);
        digitalCardPanel = new JPanel(digitalCardLayout);
        northPanel.add(digitalCardPanel);

        editableDigitalPanel = new JPanel(new GridLayout(1,0));
        digitalCardPanel.add(editableDigitalPanel,"Editable");

        nonEditableDigitalPanel = new JPanel(new GridLayout(1,0));
        digitalCardPanel.add(nonEditableDigitalPanel,"NonEditable");
        digitalCardLayout.show(digitalCardPanel, "Editable");

        hourStrL1 = new JLabel(LocaleSpecificResources.getLabelString("hour"),
            SwingConstants.CENTER);

        hourStrL2 = new JLabel(LocaleSpecificResources.getLabelString("hour"),
            SwingConstants.CENTER);

        minStrL1 = new JLabel(LocaleSpecificResources.getLabelString("minute"),
            SwingConstants.CENTER);
        minStrL2 = new JLabel(LocaleSpecificResources.getLabelString("minute"),
            SwingConstants.CENTER);

        secStrL1 = new JLabel(LocaleSpecificResources.getLabelString("seconds"),
            SwingConstants.CENTER);
        secStrL2 = new JLabel(LocaleSpecificResources.getLabelString("seconds"),
            SwingConstants.CENTER);

        ampmStrL1 = new JLabel("");
        ampmStrL2 = new JLabel("");

        hourS = new CustomJSpinner(new SpinnerNumberModel(0,-1,24,1));
        hourL = new JLabel("",SwingConstants.CENTER);

        minS = new CustomJSpinner(new SpinnerNumberModel(0,-1,60,1));
		minS.setTwoDigitFormat();
        minL = new JLabel("",SwingConstants.CENTER);

        secS = new CustomJSpinner(new SpinnerNumberModel(0,-1,60,1));
		secS.setTwoDigitFormat();
        secL = new JLabel("",SwingConstants.CENTER);

		DateFormatSymbols dateSymbols = new DateFormatSymbols(locale);
		ampmStrs = dateSymbols.getAmPmStrings();
		ampmCB = new JComboBox(ampmStrs);
		ampmL = new JLabel("",SwingConstants.CENTER);

		updateHourMinSecPanels();

		editableDigitalPanel.add(hourPanel1);
		editableDigitalPanel.add(minPanel1);
		editableDigitalPanel.add(secPanel1);

		nonEditableDigitalPanel.add(hourPanel2);
		nonEditableDigitalPanel.add(minPanel2);
		nonEditableDigitalPanel.add(secPanel2);

        hourFormat = LocaleSpecificResources.getHourFormat();
        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
        {
			editableDigitalPanel.add(ampmPanel1);
			nonEditableDigitalPanel.add(ampmPanel2);
        }

        updateGUI();

        hourS.addChangeListener(this);
        minS.addChangeListener(this);
        secS.addChangeListener(this);

        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
            ampmCB.addActionListener(this);
    }

    private void repopulateDigitalPanel()
    {
        editableDigitalPanel.removeAll();
        nonEditableDigitalPanel.removeAll();

        if(hourDisplayed)
        {
            editableDigitalPanel.add(hourPanel1);
            nonEditableDigitalPanel.add(hourPanel2);
        }

        if(minDisplayed)
        {
            editableDigitalPanel.add(minPanel1);
            nonEditableDigitalPanel.add(minPanel2);
        }

        if(secDisplayed)
        {
            editableDigitalPanel.add(secPanel1);
            nonEditableDigitalPanel.add(secPanel2);
        }

        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12
        	&& hourDisplayed)
        {
            editableDigitalPanel.add(ampmPanel1);
            nonEditableDigitalPanel.add(ampmPanel2);
        }
    }

	private void updateHourMinSecPanels()
	{
		hourPanel1.removeAll();
		hourPanel2.removeAll();
		minPanel1.removeAll();
		minPanel2.removeAll();
		secPanel1.removeAll();
		secPanel2.removeAll();
		ampmPanel1.removeAll();
		ampmPanel2.removeAll();

		if(hourMinSecLabelDisplayed)
		{
			hourPanel1.add(hourStrL1);
			hourPanel2.add(hourStrL2);
			minPanel1.add(minStrL1);
			minPanel2.add(minStrL2);
			secPanel1.add(secStrL1);
			secPanel2.add(secStrL2);
			ampmPanel1.add(ampmStrL1);
			ampmPanel2.add(ampmStrL2);
		}
        hourPanel1.add(hourS);
        hourPanel2.add(hourL);
        minPanel1.add(minS);
        minPanel2.add(minL);
        secPanel1.add(secS);
        secPanel2.add(secL);
		ampmPanel1.add(ampmCB);
		ampmPanel2.add(ampmL);
	}

    /** Returns whether minute is displayed or not. */
    public boolean isMinDisplayed()
    {
        return minDisplayed;
    }

    /** Sets whether the minute is displayed or not. */
    public void setMinDisplayed(boolean val)
    {
        if(val == minDisplayed)
            return;
        minDisplayed = val;
        repopulateDigitalPanel();
        clockPanel.setMinDisplayed(minDisplayed);
        if(propertySupport != null)
            propertySupport.firePropertyChange("minDisplayed", !minDisplayed, minDisplayed);
    }

    /** Returns whether second is displayed or not. */
    public boolean isSecDisplayed()
    {
        return secDisplayed;
    }

    /** Sets whether the second is displayed or not. */
    public void setSecDisplayed(boolean val)
    {
        if(val == secDisplayed)
            return;
        secDisplayed = val;
        repopulateDigitalPanel();
        clockPanel.setSecDisplayed(secDisplayed);
        if(propertySupport != null)
            propertySupport.firePropertyChange("secDisplayed", !secDisplayed, secDisplayed);
    }

	/** Sets whether the hour, min and sec label above the digital time is displayed or not. */
	public void setHourMinSecLabelDisplayed(boolean disp)
	{
		if(hourMinSecLabelDisplayed == disp)
			return;

		hourMinSecLabelDisplayed = disp;
		updateHourMinSecPanels();
		clockPanel.recalculateSize();
		revalidate();
		repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("hourMinSecLabelDisplayed",!disp, disp);
	}

	/** Returns whether the hour, min and sec label above the digital time is displayed or not. */
	public boolean isHourMinSecLabelDisplayed()
	{
		return hourMinSecLabelDisplayed;
	}

   /** Returns the minimum time that can be selected. 
     *  If there is no minimum time limit, null is returned.
     *  @return GregorianCalendar Minimum time that can be selected or null if 
     *  no minimum time limit.
     */
    public GregorianCalendar getMinSelectableTime()
    {
        return minSelectableTime;
    }

    /** Set the minimum time that can be selected. If the current
     *  selected time is less than the minimum time, the current selected time is
     *  set to the minimum time. 
     *  @param minTime Minimum time that can be selected in DateTimePicker. null
     *                 can be passed to remove minimum limit. 
     *  @exception IllegalArgumentException If minTime is greater than maxSelectableTime
     */
    public void setMinSelectableTime(GregorianCalendar minTime) throws IllegalArgumentException
    {
        if(minTime == null)
            minSelectableTime = null;
        else
        {
            if((maxSelectableTime != null) && 
                (minTime.getTime().after(maxSelectableTime.getTime())))
                throw new IllegalArgumentException(
                    "Min Selectable Time is greater than Max Selectable Time");
            minSelectableTime = (GregorianCalendar)minTime.clone();
            if(calendar.getTime().before(minSelectableTime.getTime()))
                setCalendar(minSelectableTime);
        }
        clockPanel.setMinSelectableTime(minSelectableTime);
    }

    /** Returns the maximum time that can be selected. 
     *  If there is no maximum time limit, null is returned.
     *  @return GregorianCalendar Maximum time that can be selected or null if 
     *  no maximum time limit.
     */
    public GregorianCalendar getMaxSelectableTime()
    {
        return maxSelectableTime;
    }

    /** Set the maximum time that can be selected. If the current
     *  selected time is greater than the maximum time, the current selected time is
     *  set to the maximum time. 
     *  @param maxTime Maximum time that can be selected in DateTimePicker. null
     *                 can be passed to remove maximum limit. 
     *  @exception IllegalArgumentException If maxTime is less than minSelectableTime
     */
    public void setMaxSelectableTime(GregorianCalendar maxTime) throws IllegalArgumentException
    {
        if(maxTime == null)
            maxSelectableTime = null;
        else
        {
            if((minSelectableTime != null) && 
                (maxTime.getTime().before(minSelectableTime.getTime())))
                throw new IllegalArgumentException(
                    "Max Selectable Time is less than Min Selectable Time");

            maxSelectableTime = (GregorianCalendar)maxTime.clone();
            if(calendar.getTime().after(maxSelectableTime.getTime()))
                setCalendar(maxSelectableTime);
        }
        clockPanel.setMaxSelectableTime(maxSelectableTime);
    }

    /** Returns whether the TimePanel is enabled or not. */
    public boolean isEnabled()
    {
        return super.isEnabled();
    }

    /** Sets whether the TimePanel is enabled or not. */
    public void setEnabled(boolean enable)
    {
        if(enable == isEnabled())
            return;
        super.setEnabled(enable);
        for(int i=0; i<editableDigitalPanel.getComponentCount(); i++)
            editableDigitalPanel.getComponent(i).setEnabled(enable);
        clockPanel.setEnabled(enable);
        if(propertySupport != null)
            propertySupport.firePropertyChange("enabled", !enable, enable);
    }

    /** Returns whether the time in the TimePanel can be changed by the user. */
    public boolean isEditable()
    {
        return editable;
    }

    /** Sets whether the time in the TimePanel can be changed by the user. */
    public void setEditable(boolean edit)
    {
        if(editable == edit)
            return;

        editable = edit;
        clockPanel.setEditable(editable);
        if(editable)
            digitalCardLayout.show(digitalCardPanel,"Editable");
        else
            digitalCardLayout.show(digitalCardPanel,"NonEditable");
        
        if(propertySupport != null)
            propertySupport.firePropertyChange("editable", !editable, editable);
    }

    /** Returns whether the time in digital form will be displayed. */
    public boolean getDisplayDigital()
    {
        return displayDigital;
    }

    /** Sets whether Digital time will be displayed in the Time Panel.
     *  Both DisplayDigital and DisplayAnalog cannot be false.
     *  If DisplayAnalog is already false and this method is called
     *  to set DisplayDigital to false, then DisplayAnalog will be
     *  set to true.
     */
    public void setDisplayDigital(boolean disp)
    {
        if(disp == getDisplayDigital())
            return;

        boolean fireAnalogChange = false;
        displayDigital = disp;
        if(!displayDigital && !displayAnalog)
        {
            fireAnalogChange = true;
            displayAnalog = true;
        }

        updateDisplayTypes();
        if(propertySupport != null)
            propertySupport.firePropertyChange("displayDigital",!displayDigital, displayDigital);
        if(fireAnalogChange && propertySupport != null)
            propertySupport.firePropertyChange("displayAnalog",!displayAnalog, displayAnalog);
    }

    /** Returns whether the time in analog clock will be displayed. */
    public boolean getDisplayAnalog()
    {
        return displayAnalog;
    }

    /** Sets whether Analog Clock will be displayed in the Time Panel.
     *  Both DisplayDigital and DisplayAnalog cannot be false.
     *  If DisplayDigital is already false and this method is called
     *  to set DisplayAnalog to false, then DisplayDigital will be
     *  set to true.
     */
    public void setDisplayAnalog(boolean disp)
    {
        if(disp == displayAnalog)
            return;

        boolean fireDigitalChange = false;
        displayAnalog = disp;
        if(!displayDigital && !displayAnalog)
        {
            displayDigital = true;
            fireDigitalChange = true;
        }

        updateDisplayTypes();
        if(propertySupport != null)
            propertySupport.firePropertyChange("displayAnalog",!displayAnalog, displayAnalog);
        if(fireDigitalChange && propertySupport != null)
            propertySupport.firePropertyChange("displayDigital",!displayDigital, displayDigital);
    }

    private void updateDisplayTypes()
    {
        remove(northPanel);
        remove(clockPanel);
        if(!displayDigital && !displayAnalog)
            displayAnalog = true;

        if(displayDigital)
            add(northPanel, BorderLayout.NORTH);
        if(displayAnalog)
            add(clockPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }


    /** Returns the selected Time.  */
    public GregorianCalendar getCalendar()
    {
        return (GregorianCalendar)calendar.clone();
    }

    /** Sets the selected time. If a minSelectableTime was set and the given time is
     *  less than the minSelectableTime, the time is set to the minSelectableTime.
     *  Similar check is done for maxSelectableTime also.
     */
    public void setCalendar(GregorianCalendar cal)
    {
        calendar.setTime(cal.getTime());
        calendar.setTimeZone(cal.getTimeZone());
        if((minSelectableTime != null) &&
                (calendar.getTime().before(minSelectableTime.getTime())))
        {
            calendar.setTimeInMillis(minSelectableTime.getTimeInMillis());
        }
        if((maxSelectableTime != null) &&
                (calendar.getTime().after(maxSelectableTime.getTime())))
        {
            calendar.setTimeInMillis(maxSelectableTime.getTimeInMillis());
        }
        updateGUI();
        enableListeners = false;
        clockPanel.setCalendar(calendar);
        enableListeners = true;

        if(propertySupport != null)
            propertySupport.firePropertyChange("calendar",oldCalendar, calendar.clone());
        notifyListeners();
        oldCalendar.setTimeInMillis(calendar.getTimeInMillis());
    }

    private void updateTime(int field, int newValue)
    {
		GregorianCalendar cal = (GregorianCalendar)calendar;
		cal.set(field, newValue);
        calendar.setTimeInMillis(cal.getTimeInMillis());
        if((minSelectableTime != null) &&
                (calendar.getTime().before(minSelectableTime.getTime())))
        {
            calendar.setTimeInMillis(minSelectableTime.getTimeInMillis());
        }
        if((maxSelectableTime != null) &&
                (calendar.getTime().after(maxSelectableTime.getTime())))
        {
            calendar.setTimeInMillis(maxSelectableTime.getTimeInMillis());
        }

        updateGUI();
        enableListeners = false;
        clockPanel.setCalendar(calendar);
        enableListeners = true;

        if(propertySupport != null)
            propertySupport.firePropertyChange("calendar",oldCalendar, calendar.clone());
        notifyListeners();
        oldCalendar.setTimeInMillis(calendar.getTimeInMillis());
    }

    /** Set the font for this component.
     *  @param font the desired Font for this component
     */
    public void setFont(Font font)
    {
        Font oldFont = getFont();
        if(hourStrL1 != null)
            hourStrL1.setFont(font);
        if(minStrL1 != null)
            minStrL1.setFont(font);
        if(secStrL1 != null)
            secStrL1.setFont(font);
        if(ampmStrL1 != null)
            ampmStrL1.setFont(font);
        if(hourStrL2 != null)
            hourStrL2.setFont(font);
        if(minStrL2 != null)
            minStrL2.setFont(font);
        if(secStrL2 != null)
            secStrL2.setFont(font);
        if(ampmStrL2 != null)
            ampmStrL2.setFont(font);
        if(hourS != null)
            ((JSpinner.NumberEditor)hourS.getEditor()).getTextField().setFont(font);
        if(hourL != null)
            hourL.setFont(font);
        if(minS != null)
            ((JSpinner.NumberEditor)minS.getEditor()).getTextField().setFont(font);
        if(minL != null)
            minL.setFont(font);
        if(secS != null)
            ((JSpinner.NumberEditor)secS.getEditor()).getTextField().setFont(font);
        if(secL != null)
            secL.setFont(font);
        if(ampmCB != null)
            ampmCB.setFont(font);
        if(ampmL != null)
            ampmL.setFont(font);
        if(northPanel != null)
            northPanel.setFont(font);
        if(editableDigitalPanel != null)
            editableDigitalPanel.setFont(font);
        super.setFont(font);
        revalidate();
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("font",oldFont, font);
    }

    /** Set the foreground color of this component.
     *  @param fg  the desired foreground Color.
     */
    public void setForeground(Color fg)
    {
        Color oldCol = getForeground();
        if(hourStrL1 != null)
            hourStrL1.setForeground(fg);
        if(minStrL1 != null)
            minStrL1.setForeground(fg);
        if(secStrL1 != null)
            secStrL1.setForeground(fg);
        if(ampmStrL1 != null)
            ampmStrL1.setForeground(fg);
        if(hourStrL2 != null)
            hourStrL2.setForeground(fg);
        if(minStrL2 != null)
            minStrL2.setForeground(fg);
        if(secStrL2 != null)
            secStrL2.setForeground(fg);
        if(ampmStrL2 != null)
            ampmStrL2.setForeground(fg);
        if(hourS != null)
            ((JSpinner.NumberEditor)hourS.getEditor()).getTextField().setForeground(fg);
        if(hourL != null)
            hourL.setForeground(fg);
        if(minS != null)
            ((JSpinner.NumberEditor)minS.getEditor()).getTextField().setForeground(fg);
        if(minL != null)
            minL.setForeground(fg);
        if(secS != null)
            ((JSpinner.NumberEditor)secS.getEditor()).getTextField().setForeground(fg);
        if(secL != null)
            secL.setForeground(fg);
        if(ampmCB != null)
            ampmCB.setForeground(fg);
        if(ampmL != null)
            ampmL.setForeground(fg);
        if(northPanel != null)
            northPanel.setForeground(fg);
        if(editableDigitalPanel != null)
            editableDigitalPanel.setForeground(fg);
        super.setForeground(fg);
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("foreground",oldCol, fg);
    }

    /** Set the background color of this component.
     *  @param bg  the desired background Color.
     */
    public void setBackground(Color bg)
    {
        Color oldCol = getBackground();
        if(hourStrL1 != null)
            hourStrL1.setBackground(bg);
        if(minStrL1 != null)
            minStrL1.setBackground(bg);
        if(secStrL1 != null)
            secStrL1.setBackground(bg);
        if(ampmStrL1 != null)
            ampmStrL1.setBackground(bg);
        if(hourStrL2 != null)
            hourStrL2.setBackground(bg);
        if(minStrL2 != null)
            minStrL2.setBackground(bg);
        if(secStrL2 != null)
            secStrL2.setBackground(bg);
        if(ampmStrL2 != null)
            ampmStrL2.setBackground(bg);
        if(hourS != null)
            ((JSpinner.NumberEditor)hourS.getEditor()).getTextField().setBackground(bg);
        if(hourL != null)
            hourL.setBackground(bg);
        if(minS != null)
            ((JSpinner.NumberEditor)minS.getEditor()).getTextField().setBackground(bg);
        if(minL != null)
            minL.setBackground(bg);
        if(secS != null)
            ((JSpinner.NumberEditor)secS.getEditor()).getTextField().setBackground(bg);
        if(secL != null)
            secL.setBackground(bg);
        if(ampmCB != null)
            ampmCB.setBackground(bg);
        if(ampmL != null)
            ampmL.setBackground(bg);
        if(northPanel != null)
            northPanel.setBackground(bg);
        if(editableDigitalPanel != null)
            editableDigitalPanel.setBackground(bg);
        super.setBackground(bg);
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("background",oldCol, bg);
    }

    /** Set the ToolTip Text for this component.
     *  @param text  the desired tooltip text. If text is null, tooltip is turned off.
     */
    public void setToolTipText(String text)
    {
        String oldText = getToolTipText();
        if(hourStrL1 != null)
            hourStrL1.setToolTipText(text);
        if(minStrL1 != null)
            minStrL1.setToolTipText(text);
        if(secStrL1 != null)
            secStrL1.setToolTipText(text);
        if(ampmStrL1 != null)
            ampmStrL1.setToolTipText(text);
        if(hourStrL2 != null)
            hourStrL2.setToolTipText(text);
        if(minStrL2 != null)
            minStrL2.setToolTipText(text);
        if(secStrL2 != null)
            secStrL2.setToolTipText(text);
        if(ampmStrL2 != null)
            ampmStrL2.setToolTipText(text);
        if(hourS != null)
            ((JSpinner.NumberEditor)hourS.getEditor()).getTextField().setToolTipText(text);
        if(hourL != null)
            hourL.setToolTipText(text);
        if(minS != null)
            ((JSpinner.NumberEditor)minS.getEditor()).getTextField().setToolTipText(text);
        if(minL != null)
            minL.setToolTipText(text);
        if(secS != null)
            ((JSpinner.NumberEditor)secS.getEditor()).getTextField().setToolTipText(text);
        if(secL != null)
            secL.setToolTipText(text);
        if(ampmCB != null)
            ampmCB.setToolTipText(text);
        if(ampmL != null)
            ampmL.setToolTipText(text);
        if(northPanel != null)
            northPanel.setToolTipText(text);
        if(editableDigitalPanel != null)
            editableDigitalPanel.setToolTipText(text);
        super.setToolTipText(text);
        if(propertySupport != null)
            propertySupport.firePropertyChange("toolTipText",oldText, text);
    }

    
    /** Updates the GUI with the calendar time. */
    private void updateGUI()
    {
        enableListeners = false;

        //Reset limits
        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
            ampmCB.setModel(new DefaultComboBoxModel(ampmStrs));

        //Check and set the min limit.
        if((minSelectableTime != null))
        {
            if( (calendar.get(Calendar.YEAR) == minSelectableTime.get(Calendar.YEAR)) &&
                (calendar.get(Calendar.MONTH) == minSelectableTime.get(Calendar.MONTH)) &&
                (calendar.get(Calendar.DATE) == minSelectableTime.get(Calendar.DATE)))
            {
                if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
                {
                    if(minSelectableTime.get(Calendar.AM_PM) == Calendar.PM)
                    {
                        String[] pmStr = new String[1];
                        pmStr[0] = ampmStrs[1];
                        ampmCB.setModel(new DefaultComboBoxModel(pmStr));
                    }
                }
            }
        }

        //Check for max limit
        if((maxSelectableTime != null))
        {
            if( (calendar.get(Calendar.YEAR) == maxSelectableTime.get(Calendar.YEAR)) &&
                (calendar.get(Calendar.MONTH) == maxSelectableTime.get(Calendar.MONTH)) &&
                (calendar.get(Calendar.DATE) == maxSelectableTime.get(Calendar.DATE)))
            {
                if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
                {
                    if(maxSelectableTime.get(Calendar.AM_PM) == Calendar.AM)
                    {
                        String[] amStr = new String[1];
                        amStr[0] = ampmStrs[0];
                        ampmCB.setModel(new DefaultComboBoxModel(amStr));
                    }
                }
            }
        }

        // Set the spinner values to reflect the current time.
        minS.setValue(new Integer(calendar.get(Calendar.MINUTE)));
        minL.setText(new Integer(calendar.get(Calendar.MINUTE)).toString());
        secS.setValue(new Integer(calendar.get(Calendar.SECOND)));
        secL.setText(new Integer(calendar.get(Calendar.SECOND)).toString());
        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
        {
            if(calendar.get(Calendar.HOUR) == 0)
            {
                hourS.setValue(new Integer(12));
                hourL.setText("12");
            }
            else
            {
                hourS.setValue(new Integer(calendar.get(Calendar.HOUR)));
                hourL.setText(new Integer(calendar.get(Calendar.HOUR)).toString());
            }
            if(calendar.get(Calendar.AM_PM) == Calendar.AM)
            {
                ampmCB.setSelectedItem(ampmStrs[0]);
                ampmL.setText(ampmStrs[0]);
            }
            else
            {
                ampmCB.setSelectedItem(ampmStrs[1]);
                ampmL.setText(ampmStrs[1]);
            }
        }
        else
        {
            hourS.setValue((new Integer(calendar.get(Calendar.HOUR_OF_DAY))));
            hourL.setText((new Integer(calendar.get(Calendar.HOUR_OF_DAY))).toString());
        }
        
        enableListeners = true;
        revalidate();
        repaint();
    }

    /** Returns the clockPanel inside the Time panel */
    public ClockPanel getClockPanel()
    {
        return clockPanel;
    }

    /** Adds an ActionListener. The listeners are notified when the time is changed in this panel. */
    public void addActionListener(ActionListener ls)
    {
        if(!actionListeners.contains(ls))
            actionListeners.add(ls);
    }

    /** Removes an ActionListener */
    public void removeActionListener(ActionListener ls)
    {
        actionListeners.remove(ls);
    }

    /** Add a propertyChangeListener. */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove a propertyChangeListner. */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.removePropertyChangeListener(l);
    }

    /** Returns the clock's face color. */
    public Color getFaceColor()
    {
        return clockPanel.getFaceColor();
    }

    /** Sets the clock's face color. */
    public void setFaceColor(Color color)
    {
        Color oldCol = getFaceColor();
        clockPanel.setFaceColor(color);
        if(propertySupport != null)
            propertySupport.firePropertyChange("faceColor",oldCol, color);
    }

    /** Returns the clock hour needle color */
    public Color getHourNeedleColor()
    {
        return clockPanel.getHourNeedleColor();
    }

    /** Sets the clock hour needle color */
    public void setHourNeedleColor(Color color)
    {
        Color oldCol = getHourNeedleColor();
        clockPanel.setHourNeedleColor(color);
        if(propertySupport != null)
            propertySupport.firePropertyChange("hourNeedleColor",oldCol, color);
    }

    /** Gets the clock minute needle color */
    public Color getMinNeedleColor()
    {
        return clockPanel.getMinNeedleColor();
    }

    /** Sets the clock minute needle color */
    public void setMinNeedleColor(Color color)
    {
        Color oldCol = getMinNeedleColor();
        clockPanel.setMinNeedleColor(color);
        if(propertySupport != null)
            propertySupport.firePropertyChange("minNeedleColor",oldCol, color);
    }

    /** Gets the clock second needle color */
    public Color getSecNeedleColor()
    {
        return clockPanel.getSecNeedleColor();
    }

    /** Sets the clock second needle color */
    public void setSecNeedleColor(Color color)
    {
        Color oldCol = getSecNeedleColor();
        clockPanel.setSecNeedleColor(color);
        if(propertySupport != null)
            propertySupport.firePropertyChange("secNeedleColor",oldCol, color);
    }

    /** Implementation side effect */
    public void stateChanged(ChangeEvent evt)
    {
        if(!enableListeners)
            return;
        try
        {
            if(evt.getSource() == hourS)
            {
                int hour = ((Integer)hourS.getValue()).intValue();
                if(hour < 0 || hour > 23)
                    hour = 0;
                if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12 && hour < 13)
					updateTime(Calendar.HOUR, hour);
                else
					updateTime(Calendar.HOUR_OF_DAY, hour);
            }
            else if(evt.getSource() == minS)
            {
                int min = ((Integer)minS.getValue()).intValue();
                if(min < 0 || min > 59)
                    min = 0;
                updateTime(Calendar.MINUTE, min);
            }
            else if(evt.getSource() == secS)
            {
                int sec = ((Integer)secS.getValue()).intValue();
				if(sec < 0 || sec > 59)
					sec = 0;
				updateTime(Calendar.SECOND, sec);
            }
        }
        catch(Exception exp)
        {
            exp.printStackTrace();
        }
    }

    /** Implementation side effect */
    public void actionPerformed(ActionEvent evt)
    {
        if(!enableListeners)
            return;
        try
        {
            if(evt.getSource() == ampmCB)
            {
                if(hourFormat != LocaleSpecificResources.HOUR_FORMAT_12)
                    return;

                GregorianCalendar newCal = getCalendar();

                int hour = ((Integer)hourS.getValue()).intValue();
                if(ampmCB.getSelectedIndex() == Calendar.AM)
                {
                    if(hour == 12)
                        newCal.set(Calendar.HOUR_OF_DAY, 0);
                    else
                        newCal.set(Calendar.HOUR_OF_DAY, hour);
                }
                else
                {
                    if(hour == 12)
                        newCal.set(Calendar.HOUR_OF_DAY, 12);
                    else
                        newCal.set(Calendar.HOUR_OF_DAY, hour + 12);
                }
                setCalendar(newCal);
            }

            if(evt.getSource() == clockPanel)
            {
                setCalendar(clockPanel.getCalendar());
            }
        }
        catch(Exception exp)
        {
            exp.printStackTrace();
        }
    }

    private void notifyListeners()
    {
        Vector vector;
        synchronized(this)
        {
            vector = (Vector)actionListeners.clone();
        }
        ActionEvent evt = new ActionEvent(this, 1, null);
        for(int i = 0; i < vector.size(); i++)
            ((ActionListener)vector.elementAt(i)).actionPerformed(evt);
    }

    class CustomJSpinner extends JSpinner
    {
        JFormattedTextField editorTF;

        public CustomJSpinner(SpinnerModel model)
        {
            super(model);
            editorTF = ((JSpinner.DefaultEditor)getEditor()).getTextField();
            editorTF.addFocusListener(
                new FocusAdapter()
                {
                    public void focusGained(FocusEvent evt)
                    {
                        //editorTF.selectAll doesn't work directly. May be a bug in java.
                        // This is a workaround for it.
                        SwingUtilities.invokeLater(
                            new Runnable()
                            {
                                public void run()
                                {
                                    editorTF.selectAll();
                                }
                            });
                    }
                });
        }

        public JFormattedTextField getEditorTextField()
        {
            return editorTF;
        }

		public void setTwoDigitFormat()
		{
			editorTF.setFormatterFactory(new TwoDigitFormatterFactory());
		}

		class TwoDigitFormatterFactory extends JFormattedTextField.AbstractFormatterFactory
		{
			public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf)
			{
				return new TwoDigitFormatter();
			}
		}

		class TwoDigitFormatter extends JFormattedTextField.AbstractFormatter
		{
			DecimalFormat formatter = new DecimalFormat("00");
			public Object stringToValue(String text) throws ParseException
			{
				try
				{
					if(text == null || text.trim().equals(""))
						return new Integer(0);
					return new Integer(text);
				}
				catch(NumberFormatException ex)
				{
					throw new ParseException(text, 0);
				}
			}

			public String valueToString(Object value) throws ParseException
			{
				try
				{
					if(value == null)
						return "";
					return formatter.format(((Integer)value).longValue());
				}
				catch(Exception ex)
				{
					throw new ParseException("", 0);
				}
			}
		}
    }
}
