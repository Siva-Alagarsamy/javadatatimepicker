package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** CalendarPanel displays a given calendar. User can select a date. 
 *  Unavailable dates are disabled for user selection. 
 */
public class CalendarPanel extends JPanel
{
    private String monthNames[] = null;
    private String dayNames[] = null;

    private Vector actionListeners = new Vector();

    private JPanel monthPanel= null;
    private CardLayout monthPanelLayout = null;
    private JComboBox monthCB = null;
    private JLabel monthL = null;

    private JPanel yearPanel = null;
    private CardLayout yearPanelLayout = null;
    private JSpinner yearS = null;
    private JLabel yearL = null; 

    private JPanel dayGridPanel = null;
    private JPanel headerPanel = null;
    private Color selectedDayColor = new Color(130, 160, 255);
    private Color unavailableDayColor = new Color(220, 150, 150);
    private Color selectedButtonOriginalColor = null;
    private JButton currentSelectedButton = null;
    private DateUnavailabilityModel dateUnavailabilityModel = null;
    private PropertyChangeSupport propertySupport = null;

    private GregorianCalendar calendar = null;
    private GregorianCalendar oldCalendar = null;

    private GregorianCalendar minSelectableDate = null;
    private GregorianCalendar maxSelectableDate = null;

    private boolean enableListeners = true;
    private boolean editable = true;

	public static boolean macOSX = false;


	static
	{
		try
		{
			if(System.getProperty("mrj.version") != null)
				macOSX = true;
		}
		catch(Exception ex)
		{
		}
	}

    /** Constructs a CalendarPanel with the current date time. 
     */
    public CalendarPanel()
    {
        this(new GregorianCalendar(), Locale.getDefault());
    }

    /** Constructs a CalendarPanel with the given GregorianCalendar.
     *  @param cal  The GregorianCalendar that needs to be edited. 
     */
    public CalendarPanel(GregorianCalendar cal)
    {
        this(cal, Locale.getDefault());
    }

    /** Constructs a CalendarPanel with the given GregorianCalendar and locale.
     *  @param cal  The GregorianCalendar that needs to be edited. 
     *  @param locale  The Locale that will be used for month and day names. 
     */
    public CalendarPanel(GregorianCalendar cal, Locale locale)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        calendar = (GregorianCalendar)cal.clone();
        oldCalendar = (GregorianCalendar)cal.clone();

        DateFormatSymbols dateSymbols = new DateFormatSymbols(locale);
        String[] mntList = dateSymbols.getMonths();
        int mntStartIdx = cal.getActualMinimum(Calendar.MONTH);
        int mntEndIdx = cal.getActualMaximum(Calendar.MONTH);
        monthNames = new String[mntEndIdx - mntStartIdx +1 ];
        int idx = 0;
        for(int i=mntStartIdx; i<=mntEndIdx; i++)
            monthNames[idx++] = mntList[i];

        dayNames = dateSymbols.getShortWeekdays();

        setLayout(new BorderLayout());
        headerPanel = new JPanel(new FlowLayout());
        headerPanel.setBorder(new EtchedBorder());
        add(headerPanel, BorderLayout.NORTH);

        monthPanelLayout = new CardLayout(0,0);
        monthPanel = new JPanel(monthPanelLayout);
        headerPanel.add(monthPanel);
        monthCB = new JComboBox(monthNames);
        monthPanel.add(monthCB, "ComboBox");
        monthCB.addItemListener(
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent itemevent)
                {
                    if(!enableListeners)
                        return;
                    
                    String monthName = (String)monthCB.getSelectedItem();
                    for(int i=0; i<monthNames.length; i++)
                    {
                        if(monthNames[i].equals(monthName))
                        {
                            setCalendarMonth(i);
                            return;
                        }
                    }
                }
            });
        monthL = new JLabel("",SwingConstants.RIGHT);
        monthPanel.add(monthL, "Label");
        monthPanelLayout.show(monthPanel, "ComboBox");

        yearPanelLayout = new CardLayout(0,0);
        yearPanel = new JPanel(yearPanelLayout);
        headerPanel.add(yearPanel);
        yearS = new JSpinner(new SpinnerNumberModel());
        yearS.setEditor(new javax.swing.JSpinner.NumberEditor(yearS, "0000"));
        yearPanel.add(yearS, "Spinner");
        yearS.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeevent)
                {
                    if(!enableListeners)
                        return;
                    int year = ((Integer)yearS.getValue()).intValue();
                    setCalendarYear(year);
                }
            });
        yearL = new JLabel("",SwingConstants.LEFT);
        yearPanel.add(yearL, "Label");
        yearPanelLayout.show(yearPanel, "Spinner");

        dayGridPanel = new JPanel(new CompactGridLayout(0, 7));
        add(dayGridPanel, BorderLayout.CENTER);

        updateGUI();

    }

    /** Returns the minimum date that can be selected in the calendar. 
     *  If there is no minimum date limit, null is returned.
     *  @return GregorianCalendar Minimum date that can be selected or null if 
     *  no minimum limit.
     */
    public GregorianCalendar getMinSelectableDate()
    {
        return minSelectableDate;
    }

    /** Set the minimum date that can be selected with the calendar. If the current
     *  selected date is less than the minimum date, the current selected date is
     *  set to the minimum date. 
     *  @param minCal Minimum date that can be selected in CalendarPanel. null
     *                 can be passed to remove minimum limit. 
     *  @exception IllegalArgumentException If minCal is greater than maxSelectableDate
     *  @see CalendarPanel#setDateUnavailabilityModel
     */
    public void setMinSelectableDate(GregorianCalendar minCal)
    {
        if(minCal == null)
            minSelectableDate = null;
        else
        {
            if((maxSelectableDate != null) && 
                (minCal.getTime().after(maxSelectableDate.getTime())))
                throw new IllegalArgumentException(
                    "Min Selectable Date is greater than Max Selectable Date");

            minSelectableDate = (GregorianCalendar)minCal.clone();
            minSelectableDate.set(Calendar.HOUR_OF_DAY,0);
            minSelectableDate.set(Calendar.MINUTE,0);
            minSelectableDate.set(Calendar.SECOND,0);

            if(calendar.getTime().before(minSelectableDate.getTime()))
            {
                GregorianCalendar newCal = (GregorianCalendar)minSelectableDate.clone();
                newCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                newCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                newCal.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
                setCalendar(newCal);
            }
        }
        SpinnerNumberModel model = (SpinnerNumberModel)yearS.getModel();
        if(minSelectableDate == null)
            model.setMinimum(null);
        else
            model.setMinimum(new Integer(minSelectableDate.get(Calendar.YEAR)));

        updateGUI();
    }

    /** Returns the maximum date that can be selected in the calendar. 
     *  If there is no maximum date limit, null is returned.
     *  @return GregorianCalendar Maximum date that can be selected or null if 
     *  no maximum limit.
     */
    public GregorianCalendar getMaxSelectableDate()
    {
        return maxSelectableDate;
    }

    /** Set the maximum date that can be selected with the calendar. If the current
     *  selected date is less than the maximum date, the current selected date is
     *  set to the maximum date. 
     *  @param maxCal Maximum date that can be selected in CalendarPanel. null
     *                 can be passed to remove maximum limit. 
     *  @exception IllegalArgumentException If maxCal is less than minSelectableTime
     *  @see CalendarPanel#setDateUnavailabilityModel
     */
    public void setMaxSelectableDate(GregorianCalendar maxCal)
    {
        if(maxCal == null)
            maxSelectableDate = null;
        else
        {
            if((minSelectableDate != null) && 
                (maxCal.getTime().before(minSelectableDate.getTime())))
                throw new IllegalArgumentException(
                    "Max Selectable Date is less than Min Selectable Date");

            maxSelectableDate = (GregorianCalendar)maxCal.clone();
            maxSelectableDate.set(Calendar.HOUR_OF_DAY,23);
            maxSelectableDate.set(Calendar.MINUTE,59);
            maxSelectableDate.set(Calendar.SECOND,59);

            if(calendar.getTime().after(maxSelectableDate.getTime()))
            {
                GregorianCalendar newCal = (GregorianCalendar)maxSelectableDate.clone();
                newCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                newCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                newCal.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
                setCalendar(newCal);
            }
        }
        SpinnerNumberModel model = (SpinnerNumberModel)yearS.getModel();
        if(maxSelectableDate == null)
            model.setMaximum(null);
        else
            model.setMaximum(new Integer(maxSelectableDate.get(Calendar.YEAR)));
        updateGUI();
    }

    /** Returns whether the CalendarPanel is enabled or not. */
    public boolean isEnabled()
    {
        return super.isEnabled();
    }

    /** Set the CalendarPanel to be enabled or not. */
    public void setEnabled(boolean enable)
    {
        if(isEnabled() == enable)
            return;
        for(int idx = 0; idx < getComponentCount(); idx++)
            setChildrenEnabled(getComponent(idx), enable);
        super.setEnabled(enable);
        if(enable)
            updateGUI();
        repaint();
        propertySupport.firePropertyChange("enabled", !enable, enable);
    }

    /** Returns whether the calendar date can be changed by the user or not. */
    public boolean isEditable()
    {
        return editable;
    }

    /** Sets whether the calendar date can be changed by the user or not. */
    public void setEditable(boolean edit)
    {
        editable = edit;
        if(editable)
        {
            monthPanelLayout.show(monthPanel,"ComboBox");
            yearPanelLayout.show(yearPanel,"Spinner");
        }
        else
        {
            monthPanelLayout.show(monthPanel,"Label");
            yearPanelLayout.show(yearPanel,"Label");
        }
    }

    /** Utility function to recursively set enable for children. */
    private static void setChildrenEnabled(Component comp, boolean enable)
    {
        comp.setEnabled(enable);
        if(comp instanceof Container)
        {
            Container container = (Container)comp;
            for(int idx = 0; idx < container.getComponentCount(); idx++)
            {
                setChildrenEnabled(container.getComponent(idx), enable);
            }
        }
    }

    /** Returns the DateUnavailabilityModel for this class.
     *  If no DateUnavailabilityModel is set before, the return value will be null. 
     */
    public DateUnavailabilityModel getDateUnavailabilityModel()
    {
        return dateUnavailabilityModel;
    }

    /** Sets the DateUnavailabilityModel for the CalendarPanel. */
    public void setDateUnavailabilityModel(DateUnavailabilityModel model)
    {
        dateUnavailabilityModel = model;
        updateGUI();
    }

    /** Returns the current selected date.
     */
    public GregorianCalendar getCalendar()
    {
        return (GregorianCalendar)calendar.clone();
    }


    /**  Changes the selected date. If minSelectableDate or maxSelectableDate was
     *   set before and the passed date is beyond the limit, the date limited to the min or max.
     */
    public void setCalendar(GregorianCalendar cal)
    {
        calendar.setTime(cal.getTime());
        calendar.setTimeZone(cal.getTimeZone());
        checkMinMaxLimits();
        updateGUI();
        propertySupport.firePropertyChange("calendar", oldCalendar, calendar.clone());
        notifyListeners();
        revalidate();
        repaint();
        oldCalendar.setTime(calendar.getTime());
        oldCalendar.setTimeZone(calendar.getTimeZone());
    }

    boolean isCurrentSelectedDayUnavailable()
    {
        if(!currentSelectedButton.isEnabled())
            return true;
        else
            return false;
    }

    private void checkMinMaxLimits()
    {
        if((minSelectableDate != null) &&
            (calendar.getTime().before(minSelectableDate.getTime())))
        {
            calendar.set(Calendar.YEAR,minSelectableDate.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH,minSelectableDate.get(Calendar.MONTH));
            calendar.set(Calendar.DATE,minSelectableDate.get(Calendar.DATE));
        }
        if((maxSelectableDate != null) &&
            (calendar.getTime().after(maxSelectableDate.getTime())))
        {
            calendar.set(Calendar.YEAR,maxSelectableDate.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH,maxSelectableDate.get(Calendar.MONTH));
            calendar.set(Calendar.DATE,maxSelectableDate.get(Calendar.DATE));
        }
    }

    private void setCalendarDay(int day)
    {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        checkMinMaxLimits();
        //updateGUI(); selectDayButton() takes care of updating.
        propertySupport.firePropertyChange("calendar", oldCalendar, calendar.clone());
        notifyListeners();
        oldCalendar.setTime(calendar.getTime());
    }

    private void setCalendarMonth(int month)
    {
        calendar.set(Calendar.MONTH, month);
        checkMinMaxLimits();
        updateGUI();
        propertySupport.firePropertyChange("calendar", oldCalendar, calendar.clone());
        notifyListeners();
        oldCalendar.setTime(calendar.getTime());
    }

    private void setCalendarYear(int year)
    {
        calendar.set(Calendar.YEAR, year);
        checkMinMaxLimits();
        updateGUI();
        propertySupport.firePropertyChange("calendar", oldCalendar, calendar.clone());
        notifyListeners();
        oldCalendar.setTime(calendar.getTime());
    }

    private void updateGUI()
    {
        if(monthCB == null)  // called by super class counstructor through setForeground. 
            return;

        enableListeners = false;
        yearS.setValue(new Integer(calendar.get(Calendar.YEAR)));
        yearL.setText(new Integer(calendar.get(Calendar.YEAR)).toString());

        int minMonth = 0, maxMonth = monthNames.length-1;
        if((minSelectableDate != null) &&
            (minSelectableDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)))
            minMonth = minSelectableDate.get(Calendar.MONTH);
        if((maxSelectableDate != null) &&
            (maxSelectableDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)))
            maxMonth = maxSelectableDate.get(Calendar.MONTH);

        String[] availMonths = new String[maxMonth-minMonth+1];
        int idx=0;
        for(int i=minMonth; i<=maxMonth; i++)
            availMonths[idx++] = monthNames[i];

        monthCB.setModel(new DefaultComboBoxModel(availMonths));
        monthCB.setSelectedItem(monthNames[calendar.get(Calendar.MONTH)]);
        monthL.setText(monthNames[calendar.get(Calendar.MONTH)]);

        dayGridPanel.removeAll();

        int[] weekDayArray = { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, 
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };

        int weekDayArrayIdx = 0;
        while(weekDayArray[weekDayArrayIdx] != calendar.getFirstDayOfWeek()) weekDayArrayIdx++;

        for(int i = 0; i < 7; i++)
        {
            JLabel label = new JLabel(dayNames[weekDayArray[weekDayArrayIdx]], SwingConstants.CENTER);
            label.setFont(getFont());
            label.setForeground(getForeground());
            label.setBackground(getBackground());
            dayGridPanel.add(label);
            weekDayArrayIdx++;
            weekDayArrayIdx = weekDayArrayIdx % 7;
        }

        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeekOnFirst = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_MONTH, curDay);

        int curIdx = 0;
        while(weekDayArray[curIdx] != calendar.getFirstDayOfWeek()) curIdx++;

        while(weekDayArray[curIdx] != dayOfWeekOnFirst)
        {
            JLabel label = new JLabel("");
			if(macOSX)
				label.putClientProperty("JComponent.sizeVariant", "mini"); //Mac OS X
            label.setFont(getFont());
            label.setForeground(getForeground());
            label.setBackground(getBackground());
            dayGridPanel.add(label);
            curIdx++;
            curIdx = curIdx % 7;
        }

        int unavailDays[] = new int[0];
        if(dateUnavailabilityModel != null)
            unavailDays = dateUnavailabilityModel.getUnavailableDaysInAMonth(
                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        GregorianCalendar checkCal = (GregorianCalendar)calendar.clone();

        Dimension commonButtonSize = new Dimension(0,0);
        for(int i = 1; i <= maxDays; i++)
        {
            JButton dayB = new JButton((new Integer(i)).toString());
            dayB.setOpaque(true);
            dayB.setFont(getFont());
            dayB.setForeground(getForeground());
            dayB.setBackground(getBackground());
            dayB.setMargin(new Insets(3, 4, 3, 4));
            dayB.setHorizontalAlignment(SwingConstants.RIGHT);
            dayB.setActionCommand((new Integer(i)).toString());

			//Apple look and feel property
			if(macOSX)
				dayB.putClientProperty("JButton.buttonType", "bevel"); //Mac OS X
            dayB.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        if(!editable || !enableListeners)
                            return;
                        JButton clickB = (JButton)evt.getSource();
                        if(clickB == currentSelectedButton)
                            return;
                        int day;
                        try
                        {
                            day = Integer.parseInt(clickB.getText());
                        }
                        catch(Exception exception){ return; }
                        setCalendarDay(day);
                        selectDayButton(clickB);
                    }
                });
            Dimension buttonSize = dayB.getPreferredSize();
            if(buttonSize.width > commonButtonSize.width)
                commonButtonSize.width = buttonSize.width;
            if(buttonSize.height > commonButtonSize.height)
                commonButtonSize.height = buttonSize.height;

            for(int j = 0; j < unavailDays.length; j++)
            {
                if(unavailDays[j] == i)
                {
                    dayB.setBackground(unavailableDayColor);
                    dayB.setEnabled(false);
                }
            }
            checkCal.set(Calendar.DATE, i);
            checkCal.set(Calendar.HOUR_OF_DAY, 23);
            checkCal.set(Calendar.MINUTE, 59);
            checkCal.set(Calendar.SECOND, 59);
            if((minSelectableDate != null) && 
                checkCal.getTime().before(minSelectableDate.getTime()))
            {
                dayB.setBackground(unavailableDayColor);
                dayB.setEnabled(false);
            }
            checkCal.set(Calendar.HOUR_OF_DAY, 0);
            checkCal.set(Calendar.MINUTE, 0);
            checkCal.set(Calendar.SECOND, 0);
            if((maxSelectableDate != null) && 
                checkCal.getTime().after(maxSelectableDate.getTime()))
            {
                dayB.setBackground(unavailableDayColor);
                dayB.setEnabled(false);
            }
            
            if(i == curDay)
                selectDayButton(dayB);

            dayGridPanel.add(dayB);
        }
        int numComps = dayGridPanel.getComponentCount();

        for(int i=0; i<numComps; i++)
        {
            Component comp = dayGridPanel.getComponent(i);
            if(comp instanceof JButton)
            {
                JButton button = (JButton)comp;
                button.setMinimumSize(commonButtonSize);
                button.setPreferredSize(commonButtonSize);
            }
        }

        //Add dummy labels to create 7 columns 7 row layout
        if(numComps <= 42)
        {
            int numLabels = 43 - numComps;
            for(int i=0; i<numLabels; i++)
            {
                JLabel label = new JLabel("");
				if(macOSX)
					label.putClientProperty("JComponent.sizeVariant", "mini"); //Mac OS X
                label.setFont(getFont());
                label.setForeground(getForeground());
                label.setBackground(getBackground());
				label.setMinimumSize(commonButtonSize);
				label.setPreferredSize(commonButtonSize);
                dayGridPanel.add(label);
            }
        }
        enableListeners = true;

        dayGridPanel.revalidate();
        dayGridPanel.repaint();
    }

    /** Set the font for this Calendar Panel.
     *  @param font the desired Font for this component
     */
    public void setFont(Font font)
    {
        Font oldFont = getFont();
        if(monthCB != null)
            monthCB.setFont(font);
        if(monthL != null)
            monthL.setFont(font);
        if(yearS != null)
            ((javax.swing.JSpinner.NumberEditor)yearS.getEditor()).getTextField().setFont(font);
        if(yearL != null)
            yearL.setFont(font);
        if(dayGridPanel != null)
        {
            dayGridPanel.setFont(font);
            Component[] comps = dayGridPanel.getComponents();
            for(int i=0; i<comps.length; i++)
                if(comps[i] instanceof JButton)
                    ((JButton)comps[i]).setFont(font);
        }
        if(headerPanel != null)
            headerPanel.setFont(font);
        super.setFont(font);
        updateGUI();
        revalidate();
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("font", oldFont, font);
    }

    /** Set the foreground color of this component.
     *  @param fg  the desired foreground Color.
     */
    public void setForeground(Color fg)
    {
        Color oldColor = getForeground();
        if(monthCB != null)
            monthCB.setForeground(fg);
        if(monthL != null)
            monthL.setForeground(fg);
        if(yearL != null)
            yearL.setForeground(fg);
        if(yearS != null)
            ((javax.swing.JSpinner.NumberEditor)yearS.getEditor()).getTextField().setForeground(fg);
        if(dayGridPanel != null)
        {
            dayGridPanel.setForeground(fg);
            Component[] comps = dayGridPanel.getComponents();
            for(int i=0; i<comps.length; i++)
                if(comps[i] instanceof JButton)
                    ((JButton)comps[i]).setForeground(fg);
        }
        if(headerPanel != null)
            headerPanel.setForeground(fg);
        super.setForeground(fg);
        updateGUI();
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("foreground", oldColor, fg);
    }

    /** Set the background color of this component.
     *  @param bg  the desired background Color.
     */
    public void setBackground(Color bg)
    {
        Color oldColor = getBackground();
        if(monthCB != null)
            monthCB.setBackground(bg);
        if(monthL != null)
            monthL.setBackground(bg);
        if(yearL != null)
            yearL.setBackground(bg);
        if(yearS != null)
            ((javax.swing.JSpinner.NumberEditor)yearS.getEditor()).getTextField().setBackground(bg);
        if(dayGridPanel != null)
        {
            dayGridPanel.setBackground(bg);
            Component[] comps = dayGridPanel.getComponents();
            for(int i=0; i<comps.length; i++)
                if(comps[i] instanceof JButton)
                    ((JButton)comps[i]).setBackground(bg);
        }
        if(headerPanel != null)
            headerPanel.setBackground(bg);
        super.setBackground(bg);
        updateGUI();
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("background", oldColor, bg);
    }
    
    /** Set the ToolTip Text for this component.
     *  @param text  the desired ToolTip. If the text is null, the tooltip is turned off.
     */
    public void setToolTipText(String text)
    {
        String oldText = getToolTipText();
        if(monthCB != null)
            monthCB.setToolTipText(text);
        if(monthL != null)
            monthL.setToolTipText(text);
        if(yearL != null)
            yearL.setToolTipText(text);
        if(yearS != null)
            ((javax.swing.JSpinner.NumberEditor)yearS.getEditor()).getTextField().setToolTipText(text);
        if(dayGridPanel != null)
        {
            dayGridPanel.setToolTipText(text);
            Component[] comps = dayGridPanel.getComponents();
            for(int i=0; i<comps.length; i++)
                if(comps[i] instanceof JButton)
                    ((JButton)comps[i]).setToolTipText(text);
        }
        if(headerPanel != null)
            headerPanel.setToolTipText(text);
        super.setToolTipText(text);
        if(propertySupport != null)
            propertySupport.firePropertyChange("toolTipText", oldText, text);
    }

    /** Returns the color used to indicate unavailable days in the calendar panel. */
    public Color getUnavailableDayColor()
    {
        return unavailableDayColor;
    }

    /** Set the color for the unavailable days in the Calendar panel. */
    public void setUnavailableDayColor(Color color)
    {
        Color oldColor = getUnavailableDayColor();
        unavailableDayColor = color;
        updateGUI();
        if(propertySupport != null)
            propertySupport.firePropertyChange("unavailableDayColor", oldColor, color);
    }

    /** Returns the color used to indicate the selected day in the calendar panel. */
    public Color getSelectedDayColor()
    {
        return selectedDayColor;
    }

    /** Set the color for the selected day box in the Calendar panel. */
    public void setSelectedDayColor(Color color)
    {
        Color oldColor = getSelectedDayColor();
        selectedDayColor = color;
        updateGUI();
        if(propertySupport != null)
            propertySupport.firePropertyChange("selectedDayColor", oldColor, color);
    }



    /** Add an action listener that will be notified when the selected date is changed. */
    public void addActionListener(ActionListener lis)
    {
        if(!actionListeners.contains(lis))
            actionListeners.add(lis);
    }

    /** Remove an action listener. */
    public void removeActionListener(ActionListener lis)
    {
        actionListeners.remove(lis);
    }

    private void notifyListeners()
    {
        Vector vector;
        synchronized(this)
        {
            vector = (Vector)actionListeners.clone();
        }
        ActionEvent actionevent = new ActionEvent(this, 1, null);
        for(int i = 0; i < vector.size(); i++)
            ((ActionListener)vector.elementAt(i)).actionPerformed(actionevent);

    }

    private void selectDayButton(JButton dayB)
    {
        if(dayB == currentSelectedButton)
            return;
        if(currentSelectedButton != null)
        {
            currentSelectedButton.setBackground(selectedButtonOriginalColor);
        }
        currentSelectedButton = dayB;
        selectedButtonOriginalColor = currentSelectedButton.getBackground();
        currentSelectedButton.setBackground(selectedDayColor);
    }

    /** Add a PropertyChangeListener. */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove a PropertyChangeListener. */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.removePropertyChangeListener(l);
    }
}
