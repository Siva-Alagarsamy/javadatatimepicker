package com.lavantech.gui.comp;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;

/** DateTimePicker allows a user to select a date and time. */
public class DateTimePicker extends JComponent
{
    private static final String EDITABLE = "EDITABLE";
    private static final String UNEDITABLE = "UNEDITABLE";

    private static final int EDIT_START = 1;
    private static final int EDIT_STOP = 2;
    private static final int EDIT_CANCEL = 3;

    private boolean enabled = true;
    private boolean editable = false;
    private boolean displayCalendar = true;
    private boolean displayClock = true;
    private boolean dateRollOverByClockEnabled = true;
    private boolean nullDate = false;

    private SimpleDateFormat dateFormatter = null;
    private GregorianCalendar calendar = null;
    private GregorianCalendar minSelectableTime = null;
    private GregorianCalendar maxSelectableTime = null;

    private Vector actionListeners = new Vector();
    private Vector editorListeners = new Vector();
    private JButton dropButton = null;
    private DateTimeRenderer dateTimeRenderer = null;
    private DateTimeEditor dateTimeEditor = null;
    private DateTimePopup popup = null;
    private JPanel cardPanel = null;
    private Locale locale;

    private PropertyChangeSupport propertySupport = null;

    /** Constructs a DateTimePicker component. */
    public DateTimePicker()
    {
        this(new Date(), new SimpleDateFormat().toPattern(), true, true, Locale.getDefault());
    }

    /** Constructs a DateTimePicker component. Use the constructor with Calendar to set TimeZone.
     *  @param date   The initial date that will be selected.
     *  @param format The format string for displaying the selected date/time.
     *                The format string is the one used in java.text.SimpleDateFormat.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(Date date, String format)
    {
        this(date, format, true, true, Locale.getDefault());
    }

    /** Constructs a DateTimePicker component. Use the constructor with Calendar to set TimeZone.
     *  @param date     The initial date that will be selected.
     *  @param format   The format string for displaying the selected date/time.
     *  @param dispCal  Boolean indicating whether to display Calendar for Date modification
     *                  if both dispCal and dispClock are false, then dispCal will be true.
     *  @param dispClock Boolean indicating whether to display Clock for Time modification.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(Date date, String format, boolean dispCal, boolean dispClock)
    {
        this(date, format, dispCal, dispClock, Locale.getDefault());
    }

    /** Constructs a DateTimePicker component. Use the constructor with Calendar to set TimeZone.
     *  @param date     The initial date that will be selected.
     *  @param format   The format string for displaying the selected date/time.
     *  @param dispCal  Boolean indicating whether to display Calendar for Date modification
     *                  if both dispCal and dispClock are false, then dispCal will be true.
     *  @param dispClock Boolean indicating whether to display Clock for Time modification.
     *  @param locale   Locale to use for displaying calendar month and weekday names.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(Date date, String format, boolean dispCal, boolean dispClock, Locale locale)
    {
        calendar = new GregorianCalendar(locale);
		if(date != null)
        	calendar.setTime(date);
		else
			nullDate = true;

        dateFormatter = new SimpleDateFormat(format);
        dateFormatter.setTimeZone(calendar.getTimeZone());
        this.locale = locale;
        this.displayCalendar = dispCal;
        this.displayClock = dispClock;
        if(!displayCalendar && !displayClock)
            displayCalendar = true;

        initializeGUI();
    }

    /** Constructs a DateTimePicker component. 
     *  @param cal    The Calendar that will be used to initialize the picker.
     *  @param format The format string for displaying the selected date/time.
     *                The format string is the one used in java.text.SimpleDateFormat.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(GregorianCalendar cal, String format)
    {
        this(cal, format, true, true, Locale.getDefault());
    }

    /** Constructs a DateTimePicker component.
     *  @param cal      The Calendar that will be used to initialize the picker.
     *  @param format   The format string for displaying the selected date/time.
     *  @param dispCal  Boolean indicating whether to display Calendar for Date modification
     *                  if both dispCal and dispClock are false, then dispCal will be true.
     *  @param dispClock Boolean indicating whether to display Clock for Time modification.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(GregorianCalendar cal, String format, boolean dispCal, boolean dispClock)
    {
        this(cal, format, dispCal, dispClock, Locale.getDefault());
    }

    /** Constructs a DateTimePicker component for the given locale.
     *  @param cal      The Calendar that will be used to initialize the picker.
     *  @param format   The format string for displaying the selected date/time.
     *  @param dispCal  Boolean indicating whether to display Calendar for Date modification
     *                  if both dispCal and dispClock are false, then dispCal will be true.
     *  @param dispClock Boolean indicating whether to display Clock for Time modification.
     *  @param locale   Locale to use for displaying calendar month and weekday names.
     *  @see          java.text.SimpleDateFormat
     */
    public DateTimePicker(GregorianCalendar cal, String format, boolean dispCal, boolean dispClock,
        Locale locale)
    {
		if(cal != null)
        	calendar = (GregorianCalendar)cal.clone();
		else
		{
			calendar = new GregorianCalendar(locale);
			nullDate = true;
		}

        dateFormatter = new SimpleDateFormat(format);
        dateFormatter.setTimeZone(calendar.getTimeZone());
        this.locale = locale;
        this.displayCalendar = dispCal;
        this.displayClock = dispClock;
        if(!displayCalendar && !displayClock)
            displayCalendar = true;

        initializeGUI();
    }

    private void initializeGUI()
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);

        setLayout(new BorderLayout(0,0));

        cardPanel = new JPanel(new CardLayout());
        add(cardPanel, BorderLayout.CENTER);


        dateTimeRenderer = new DateTimeRenderer();
        cardPanel.add(dateTimeRenderer, UNEDITABLE);

        dateTimeEditor = new DateTimeEditor(new DateTimeFormatter(dateFormatter, false));
        cardPanel.add(dateTimeEditor, EDITABLE);

        dropButton = createDropDownButton();
        dropButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    popup.togglePopup();
                }
            });
        add(dropButton, BorderLayout.EAST);

		switchCardPanel();

        popup = new DateTimePopup(calendar, displayCalendar, displayClock, this);
    }

	private void switchCardPanel()
	{
        if(editable)
		{
            ((CardLayout)cardPanel.getLayout()).show(cardPanel, EDITABLE);
			dateTimeEditor.setRequestFocusEnabled(true);
			dateTimeEditor.requestFocus();
		}
        else
		{
            ((CardLayout)cardPanel.getLayout()).show(cardPanel, UNEDITABLE);
			dateTimeRenderer.setRequestFocusEnabled(true);
			dateTimeRenderer.requestFocus();
		}
	}

    /** Returns a arrow button used to activate/close the Date Time Popup. 
     *  Override this method to have custom drop down buttons.
     *  @return JButton This method should return a JButton component.
     */
    protected JButton createDropDownButton()
    {
        return new DropDownButton();
    }

	/** Toggle the popup window.  */
	public void togglePopup()
	{
		popup.togglePopup();
	}

    /** Returns the minimum time that can be selected in the date time picker. 
     *  If there is no minimum time limit, null is returned.
     *  @return GregorianCalendar Minimum time that can be selected or null if 
     *  no minimum time limit.
     */
    public GregorianCalendar getMinSelectableTime()
    {
        return minSelectableTime;
    }

    /** Set the minimum date/time that can be selected with the picker. This method
     *  restricts the user from browsing the calendar beyond the given date/time.
     *  null should be passed to remove any previously set limit. If the current
     *  selected time is less than the minimum time, the current selected time is
     *  set to the minimum time. 
     *  @param minTime Minimum time that can be selected in DateTimePicker. null
     *                 can be passed to remove minimum limit. 
     *  @exception IllegalArgumentException If minTime is greater than maxSelectableTime
     *  @see DateTimePicker#setDateUnavailabilityModel
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
            {
                calendar.setTime(minSelectableTime.getTime());
                dateTimeChanged();
            }
        }
        popup.setMinSelectableTime(minSelectableTime);
    }

    /** Returns the maximum time that can be selected in the date time picker. 
     *  If there is no maximum time limit, null is returned.
     *  @return GregorianCalendar Maximum time that can be selected or null if 
     *  no maximum time limit.
     */
    public GregorianCalendar getMaxSelectableTime()
    {
        return maxSelectableTime;
    }

    /** Set the maximum time that can be selected with the picker. This method
     *  restricts the user from browsing the calendar beyond the give date/time.
     *  null should be passed to remove any previously set limit.
     *  If the current selected time is greater than the maximum time, the current
     *  selected time is set to the maximum time. 
     *  @param maxTime Maximum time that can be selected in DateTimePicker. null
     *                 can be passed to remove maximum limit. 
     *  @exception IllegalArgumentException If maxTime is less than minSelectableTime
     *  @see DateTimePicker#setDateUnavailabilityModel
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
            {
                calendar.setTime(maxSelectableTime.getTime());
                dateTimeChanged();
            }
        }
        popup.setMaxSelectableTime(maxSelectableTime);
    }

    /** Returns the pattern used for displaying in the selected date/time.
     *  @return The pattern string for displaying the selected date/time.
     *                The format string is in the syntax used in java.text.SimpleDateFormat.
     */
    public String getPattern()
    {
        return dateFormatter.toPattern();
    }

    /** Set the pattern for displaying in the selected date/time.
     *  @param pattern The pattern string for displaying the selected date/time.
     *                The pattern string should be in the syntax used in java.text.SimpleDateFormat.
     */
    public void setPattern(String pattern)
    {
        String oldPattern = getPattern();
        dateFormatter.applyPattern(pattern);
        dateTimeRenderer.updateDateTime();
        dateTimeEditor.updatePattern();
        dateTimeEditor.updateDateTime();
        revalidate();
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("pattern",oldPattern, pattern);
    }

    /** Set the font for this component.
     *  @param font the desired Font for this component
     */
    public void setFont(Font font)
    {
        Font oldFont = getFont();
        if(dateTimeRenderer != null)
            dateTimeRenderer.setFont(font);
        if(dateTimeEditor != null)
            dateTimeEditor.setFont(font);
        if(popup != null)
            popup.setFont(font);
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
        Color oldColor = getForeground();

        if(dateTimeRenderer != null)
            dateTimeRenderer.setForeground(fg);
        if(dateTimeEditor != null)
            dateTimeEditor.setForeground(fg);
        if(popup != null)
            popup.setForeground(fg);
        if(dropButton != null)
            dropButton.setForeground(fg);
        if(cardPanel != null)
            cardPanel.setForeground(fg);
        super.setForeground(fg);
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("foreground",oldColor, fg);
    }

    /** Set the tooltip text for this component. 
     *  @param text Tooltip text.
     */
    public void setToolTipText(String text)
    {
        String oldText = getToolTipText();

        if(dateTimeRenderer != null)
            dateTimeRenderer.setToolTipText(text);
        if(dateTimeEditor != null)
            dateTimeEditor.setToolTipText(text);
        if(dropButton != null)
            dropButton.setToolTipText(text);
        if(cardPanel != null)
            cardPanel.setToolTipText(text);
        super.setToolTipText(text);
        if(propertySupport != null)
            propertySupport.firePropertyChange("toolTipText",oldText, text);
    }

    /** Set the background color of this component.
     *  @param bg  the desired background Color.
     */
    public void setBackground(Color bg)
    {
        Color oldColor = getBackground();
        if(dateTimeRenderer != null)
            dateTimeRenderer.setBackground(bg);
        if(dateTimeEditor != null)
            dateTimeEditor.setBackground(bg);
        if(popup != null)
            popup.setBackground(bg);
        if(dropButton != null)
            dropButton.setBackground(bg);
        if(cardPanel != null)
            cardPanel.setBackground(bg);
        super.setBackground(bg);
        repaint();
        if(propertySupport != null)
            propertySupport.firePropertyChange("background",oldColor, bg);
    }

    /** Sets whether the DateTimePicker is editable through Keyboard */
    public void setEditable(boolean editable)
    {
        if(this.editable == editable)
            return;
        this.editable = editable;
		switchCardPanel();
        if(propertySupport != null)
            propertySupport.firePropertyChange("editable",!editable, editable);
    }

    /** Returns whether the DateTimePicker is editable through Keyboard */
    public boolean isEditable()
    {
        return editable;
    }

    /** Sets whether or not this picker is enabled. A DateTimePicker
     *  that is enabled responds to user input, while a DateTimePicker 
     *  that is not enabled doesn't respond to any user input.
     */
    public void setEnabled(boolean enabled)
    {
        if(this.enabled == enabled)
            return;
        this.enabled = enabled;
        if(dropButton != null)
            dropButton.setEnabled(enabled);
        if(dateTimeRenderer != null)
            dateTimeRenderer.setEnabled(enabled);
        if(dateTimeEditor != null)
            dateTimeEditor.setEnabled(enabled);
        if(cardPanel != null)
            cardPanel.setEnabled(enabled);
        super.setEnabled(enabled);
        if(propertySupport != null)
            propertySupport.firePropertyChange("enabled",!enabled, enabled);
    }

    /** Returns whether Calendar will be displayed for Date Modification. */
    public boolean getDisplayCalendar()
    {
        return displayCalendar;
    }

    /** Sets whether Calendar will be displayed for Date Modification.
     *  Both DisplayCalendar and DisplayClock cannot be false. 
     *  If DisplayClock is already false and this method is called
     *  to set DisplayCalendar also to false, then DisplayClock will be
     *  set to true.
     */
    public void setDisplayCalendar(boolean disp)
    {
        if(disp == displayCalendar)
            return;

        boolean fireClockChange = false;
        displayCalendar = disp;
        if(!displayCalendar && !displayClock)
        {
            fireClockChange = true;
            displayClock = true;
        }

        popup.setCalendarClockDisplay(displayCalendar, displayClock);
        if(propertySupport != null)
            propertySupport.firePropertyChange("displayCalendar",!displayCalendar, displayCalendar);
        if(fireClockChange && propertySupport != null)
            propertySupport.firePropertyChange("displayClock",!displayClock, displayClock);
    }

    /** Returns whether Clock will be displayed for Time Modification. */
    public boolean getDisplayClock()
    {
        return displayClock;
    }

    /** Sets whether Clock will be displayed for Time Modification.
     *  Both DisplayCalendar and DisplayClock cannot be false. 
     *  If DisplayCalendar is already false and this method is called
     *  to set DisplayClock also to false, then DisplayCalendar will be
     *  set to true.
     */
    public void setDisplayClock(boolean disp)
    {
        if(disp == displayClock)
            return;

        boolean fireCalChange = false;

        displayClock = disp;
        if(!displayCalendar && !displayClock)
        {
            displayCalendar = true;
            fireCalChange = true;
        }
        popup.setCalendarClockDisplay(displayCalendar, displayClock);
        if(propertySupport != null)
            propertySupport.firePropertyChange("displayClock",!displayClock, displayClock);
        if(fireCalChange && propertySupport != null)
            propertySupport.firePropertyChange("displayCalendar",!displayCalendar, displayCalendar);
    }

    /** Returns whether the analog clock time change can affect the Date(Increment date when needle
     *  is moved past 23:59:59 in the clockwise direction and decrement date when needle is moved
     *  past 00:00:00 in the anti-clockwise direction. 
     *  This date roll over will happen only when the Clock time is changed by dragging 
     *  the clock needles. The default value is true.
     */
    public boolean isDateRollOverByClockEnabled()
    {
        return dateRollOverByClockEnabled;
    }

    /** Sets whether the analog clock time change can affect the Date(Increment date when needle
     *  is moved past 23:59:59 in the clockwise direction and decrement date when needle is moved
     *  past 00:00:00 in the anti-clockwise direction. 
     *  This date roll over will happen only when the Clock time is changed by dragging 
     *  the clock needles. The default value is true.
     */
    public void setDateRollOverByClockEnabled(boolean roll)
    {
        if(dateRollOverByClockEnabled == roll)
            return;

        dateRollOverByClockEnabled = roll;
        if(propertySupport != null)
            propertySupport.firePropertyChange("dateRollOverByClockEnabled",
                !dateRollOverByClockEnabled, dateRollOverByClockEnabled);
    }

    /** Returns whether the Date and Time popup window will be automatically cancelled when the
     *  user clicks on some other component in the same window as the DateTimePicker. 
     *  The auto cancel is only valid for mouse clicks. If the Component is moved or resized, the
     *  Popup will still be closed. The default value is true.
     */
    public boolean isDateTimePopupAutoCancelEnabled()
    {
        return popup.autoCancelEnabled;
    }

    /** Sets whether the Date and Time popup window will be automatically cancelled when the
     *  user clicks on some other component in the same window as the DateTimePicker. 
     *  The auto cancel is only valid for mouse clicks. If the Component is moved or resized, the
     *  Popup will still be closed. The default value is true.
     */
    public void setDateTimePopupAutoCancelEnabled(boolean enable)
    {
        if(popup.autoCancelEnabled == enable)
            return;

        popup.autoCancelEnabled = enable;
        if(propertySupport != null)
            propertySupport.firePropertyChange("dateTimePopupAutoCancelEnabled",
                !popup.autoCancelEnabled, popup.autoCancelEnabled);
    }

	/** Returns whether the popup window for the DateTimePicker will be modal or not.
	  */
	public boolean getPopupModal()
	{
		return popup.popupModal;
	}

	/** Sets whether the popup window for the DateTimePicker wll be modal or not.
	  */
	public void setPopupModal(boolean modal)
	{
		popup.popupModal = modal;
		if(popup.popupWin != null)
			popup.popupWin.setModal(modal);
	}

    /** Returns whether a today button will be displayed to change calendar to today's date. */
    public boolean isDisplayTodayButton()
    {
        return popup.isDisplayTodayButton();
    }

    /** Sets whether a today button will be displayed to change calendar to today's date. */
    public void setDisplayTodayButton(boolean enable)
    {
        popup.setDisplayTodayButton(enable);
    }

    /** Returns whether a now button will be displayed to change calendar to current date and time. */
    public boolean isDisplayNowButton()
    {
        return popup.isDisplayNowButton();
    }

    /** Sets whether a today button will be displayed to change calendar to current date and time. */
    public void setDisplayNowButton(boolean enable)
    {
        popup.setDisplayNowButton(enable);
    }

	/** Returns whether a Clear button will be displayed to clear the date time selection. When cleared,
	  * getDate() and getCalender() will return null. 
	  */
	public boolean isDisplayClearButton()
	{
		return popup.isDisplayClearButton();
	}

	/** Sets whether a Clear button will be displayed to clear the date time selection. When cleared,
	  * getDate() and getCalender() will return null. 
	  */
	public void setDisplayClearButton(boolean enable)
	{
		popup.setDisplayClearButton(enable);
		dateTimeEditor.setAllowNullValue(enable);
	}

	private Date oldDate = null;
    private void dateTimeChanged()
    {
        dateTimeRenderer.updateDateTime();
        dateTimeEditor.updateDateTime();
        popup.updateDateTime();
        if(propertySupport != null)
            propertySupport.firePropertyChange("date", oldDate, nullDate?null:(Date)(getDate().clone()));
        notifyListeners();
    }

    /** Returns the DateUnavailabilityModel for the CalendarPanel in the Picker.
     *  If no DateUnavailabilityModel is set before or Display Calendar is not enabled,
     *  the return value will be null. 
     */
    public DateUnavailabilityModel getDateUnavailabilityModel()
    {
        return popup.getDateUnavailabilityModel();
    }

    /** Sets the DateUnavailabilityModel for the CalendarPanel in the Picker.
     *  DateUnavailabilityModel provides a list of unavailable dates that 
     *  will be disabled for selection in the CalendarPanel.
     */
    public void setDateUnavailabilityModel(DateUnavailabilityModel model)
    {
        popup.setDateUnavailabilityModel(model);
    }

    /** Returns the CalendarPanel used in the picker popup. */
    public CalendarPanel getCalendarPanel()
    {
        return popup.getCalendarPanel();
    }

    /** Returns the TimePanel used in the picker poup. */
    public TimePanel getTimePanel()
    {
        return popup.getTimePanel();
    }

    /** Returns the current selected Date. */
    public Date getDate()
    {
		if(nullDate)
			return null;
		else
        	return calendar.getTime();
    }

    /** Set the selected Date and Time. Use setCalendar to set TimeZone.
     *  @param date Date to be selected
     */
    public void setDate(Date date)
    {
		if(!nullDate)
			oldDate = (Date)(getDate().clone());
		else
			oldDate = null;

		Date updateTime = date;
		if(updateTime == null)
			updateTime = new Date();
		if((minSelectableTime != null) && (updateTime.before(minSelectableTime.getTime())))
			updateTime = minSelectableTime.getTime();
		if((maxSelectableTime != null) && (updateTime.after(maxSelectableTime.getTime())))
			updateTime = maxSelectableTime.getTime();
		calendar.setTime(updateTime);

		if(date != null)
			nullDate = false;
		else
			nullDate = true;

        dateTimeChanged();
    }


    /** Returns the current selected Date, Time and Timezone. 
     *  @return Calendar with the selected Date, Time and Timezone.
     */
    public GregorianCalendar getCalendar()
    {
		if(nullDate)
			return null;
		else
        	return (GregorianCalendar) calendar.clone();
    }

    /** Set the selected Date, Time and Timezone.
     *  @param cal Calendar to use to set the selected Date, Time and Timezone.
     *  @exception IllegalArgumentException If min or max time limit is set and
     *             the given time is beyond the set limit. 
     */
    public void setCalendar(GregorianCalendar cal) throws IllegalArgumentException
    {
		GregorianCalendar oldCal = null;
		if(!nullDate)
		{
			oldCal = (GregorianCalendar)calendar.clone();
			oldDate = (Date)(getDate().clone());
		}
		else
		{
			oldDate = null;
		}


		GregorianCalendar updateCal;
		if(cal != null)
		{
			updateCal = (GregorianCalendar)cal.clone();
			nullDate = false;
		}
		else 
		{
			updateCal = new GregorianCalendar();
			nullDate = true;
		}

		if((minSelectableTime != null) && (updateCal.getTime().before(minSelectableTime.getTime())))
			updateCal.setTime(minSelectableTime.getTime());
		if((maxSelectableTime != null) && (updateCal.getTime().after(maxSelectableTime.getTime())))
			updateCal.setTime(maxSelectableTime.getTime());

		calendar.setTime(updateCal.getTime());
		calendar.setTimeZone(updateCal.getTimeZone());
		dateFormatter.setTimeZone(calendar.getTimeZone());

        dateTimeChanged();
        if(propertySupport != null)
            propertySupport.firePropertyChange("calendar", oldCal, nullDate?null:calendar.clone());
    }

    /** Returns the Panel that is used in the Popup Window. The Popup Panel
     *  has the Calendar Panel and Time Panel in it.
     */
    public JPanel getPopupPanel()
    {
        return popup;
    }

    /** Returns the drop down button used in the picker */
    public JButton getDropDownButton()
    {
        return dropButton;
    }

    /** Returns the Renderer(Used in non editable mode) that is used by the Picker. */
    public JButton getRenderer()
    {
        return dateTimeRenderer;
    }

    /** Returns the Editor(Used in editable mode) that is used by the Picker. */
    public JFormattedTextField getEditor()
    {
        return dateTimeEditor;
    }

    /** Adds an ActionListener.
     *  The ActionListener gets notified each time the date or time is changed.
     */
    public void addActionListener(ActionListener ls)
    {
        if(!actionListeners.contains(ls))
            actionListeners.add(ls);
    }

    /** Removes an ActionListener. */
    public void removeActionListener(ActionListener ls)
    {
        actionListeners.remove(ls);
    }

    private void notifyListeners()
    {
        Vector vector;
        synchronized(this)
        {
            vector = (Vector)actionListeners.clone();
        }
        for(int i = 0; i < vector.size(); i++)
            ((ActionListener)vector.elementAt(i)).actionPerformed(new ActionEvent(this, 0, "Date Changed"));

    }

    /** Adds an PickerEditorListener.
     *  The PickerEditorListener gets notified on the editor events.
     */
    public void addEditorListener(PickerEditorListener ls)
    {
        if(!editorListeners.contains(ls))
            editorListeners.add(ls);
    }

    /** Removes an PickerEditorListener. */
    public void removeEditorListener(PickerEditorListener ls)
    {
        editorListeners.remove(ls);
    }

    private void notifyEditorListeners(int type)
    {
        Vector vector;
        synchronized(this)
        {
            vector = (Vector)editorListeners.clone();
        }

        switch(type)
        {
            case EDIT_START : 
                for(int i = 0; i < vector.size(); i++)
                    ((PickerEditorListener)vector.elementAt(i)).editingStarted(new ChangeEvent(this));
                break;
            case EDIT_STOP : 
                for(int i = 0; i < vector.size(); i++)
                    ((PickerEditorListener)vector.elementAt(i)).editingStopped(new ChangeEvent(this));
                break;
            case EDIT_CANCEL : 
                for(int i = 0; i < vector.size(); i++)
                    ((PickerEditorListener)vector.elementAt(i)).editingCanceled(new ChangeEvent(this));
                break;
        }
    }

    /** Add a PropertyChangeListener for this Date Time Picker. */
    /** Add a PropertyChangeListener for this Date Time Picker. */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove a PropertyChangeListener for this Date Time Picker. */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        if(propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.removePropertyChangeListener(l);
    }

    /** DateTimePopup class handles all the popup display when the "Down" arrow
     *  of the Date Time Picker is clicked. This class has a JWindow, TimePanel and CalendarPanel.
     */
    private class DateTimePopup extends JPanel
           implements ActionListener, ComponentListener, MouseListener
    {
        GregorianCalendar calendar;
        GregorianCalendar editedCalendar;

        JDialog popupWin = null;
        JPanel centerPanel = null;
        JPanel southPanel = null;
		JPanel okCancelPanel = null;

        JButton todayButton;
        JButton okButton;
        JButton cancelButton;
        JButton nowButton;
		JButton clearButton;

        JComponent parentComponent;

        JPanel todayPanel = null;
        JPanel nowPanel = null;
        CalendarPanel calPanel = null;
        TimePanel timePanel = null;

        boolean autoCancelEnabled = true;
        boolean enableListeners = true;
        boolean displayTodayButton = true;
        boolean displayNowButton = true;
		boolean displayClearButton = false;
		boolean popupModal = false;


        public DateTimePopup(GregorianCalendar cal, boolean dispCal, boolean dispClock, 
            JComponent parentComp)
        {
            super(new BorderLayout());
            setBorder(new EtchedBorder());

            parentComponent = parentComp;

            calendar = cal;
            editedCalendar = (GregorianCalendar)calendar.clone();


            centerPanel = new JPanel(new CompactGridLayout(1, 0));
            add(centerPanel, BorderLayout.CENTER);
            calPanel = new CalendarPanel(editedCalendar, locale);
            calPanel.addActionListener(this);
            timePanel = new TimePanel(editedCalendar, locale);
            timePanel.addActionListener(this);

            if(!dispCal && !dispClock)
                dispCal = true;
            if(dispCal)
                centerPanel.add(calPanel);
            if(dispClock)
                centerPanel.add(timePanel);

            southPanel = new JPanel(new BorderLayout());
            add(southPanel, BorderLayout.SOUTH);

            if(dispCal)
            {
                todayPanel = new JPanel(new FlowLayout());
                southPanel.add(todayPanel, BorderLayout.WEST);

                todayButton = new JButton(LocaleSpecificResources.getLabelString("today"));
                todayButton.addActionListener(this);
                todayPanel.add(todayButton);
            }

            if(dispClock)
            {
                nowPanel = new JPanel(new FlowLayout());
                southPanel.add(nowPanel, BorderLayout.EAST);

                nowButton = new JButton(LocaleSpecificResources.getLabelString("now"));
                nowButton.addActionListener(this);
                nowPanel.add(nowButton);
            }

            okCancelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPanel.add(okCancelPanel);

            okButton = new JButton(LocaleSpecificResources.getLabelString("okay"));
            okButton.addActionListener(this);

            cancelButton = new JButton(LocaleSpecificResources.getLabelString("cancel"));
            cancelButton.addActionListener(this);

			clearButton = new JButton(LocaleSpecificResources.getLabelString("clear"));
			clearButton.addActionListener(this);


			if(CalendarPanel.macOSX)
			{
				okCancelPanel.add(cancelButton);
				okCancelPanel.add(okButton);
			}
			else
			{
				okCancelPanel.add(okButton);
				okCancelPanel.add(cancelButton);
			}
        }

        public void setDisplayTodayButton(boolean enable)
        {
			if(displayTodayButton == enable)
				return;
            displayTodayButton = enable;
            todayPanel.removeAll();
            if(displayTodayButton)
            {
                todayPanel.add(todayButton);
                revalidate();
            }
        }

        public boolean isDisplayTodayButton()
        {
            return displayTodayButton;
        }

        public void setDisplayNowButton(boolean enable)
        {
			if(displayNowButton == enable)
				return;
            displayNowButton = enable;
            nowPanel.removeAll();
            if(displayNowButton)
            {
                nowPanel.add(nowButton);
                revalidate();
            }
        }

        public boolean isDisplayNowButton()
        {
            return displayNowButton;
        }

		public boolean isDisplayClearButton()
		{
			return displayClearButton;
		}

		public void setDisplayClearButton(boolean enable)
		{
			if(displayClearButton == enable)
				return;

			displayClearButton = enable;
			if(displayClearButton)
				okCancelPanel.add(clearButton);
			else
				okCancelPanel.remove(clearButton);
			revalidate();
		}

        public void setMinSelectableTime(GregorianCalendar cal)
        {
            if((cal != null) && (editedCalendar.getTime().before(cal.getTime())))
                editedCalendar.setTime(cal.getTime());
            if(calPanel != null)
                calPanel.setMinSelectableDate(cal);
            if(timePanel != null)
                timePanel.setMinSelectableTime(cal);
        }

        public void setMaxSelectableTime(GregorianCalendar cal)
        {
            if((cal != null) && (editedCalendar.getTime().after(cal.getTime())))
                editedCalendar.setTime(cal.getTime());
            if(calPanel != null)
                calPanel.setMaxSelectableDate(cal);
            if(timePanel != null)
                timePanel.setMaxSelectableTime(cal);
        }

        public void setFont(Font font)
        {
            super.setFont(font);
            if(centerPanel == null) //during JPanel constructor
                return;
            centerPanel.setFont(font);
            southPanel.setFont(font);
            okButton.setFont(font);
            cancelButton.setFont(font);
			nowButton.setFont(font);
			todayButton.setFont(font);
			clearButton.setFont(font);
            calPanel.setFont(font);
            timePanel.setFont(font);
        }

        public void setForeground(Color fg)
        {
            super.setForeground(fg);
            if(centerPanel == null) //during JPanel constructor
                return;
            centerPanel.setForeground(fg);
            southPanel.setForeground(fg);
            okButton.setForeground(fg);
            cancelButton.setForeground(fg);
			nowButton.setForeground(fg);
			todayButton.setForeground(fg);
			clearButton.setForeground(fg);
            calPanel.setForeground(fg);
            timePanel.setForeground(fg);
        }

        public void setBackground(Color bg)
        {
            super.setBackground(bg);
            if(centerPanel == null) //during JPanel constructor
                return;
            centerPanel.setBackground(bg);
            southPanel.setBackground(bg);
            okButton.setBackground(bg);
            cancelButton.setBackground(bg);
			nowButton.setBackground(bg);
			todayButton.setBackground(bg);
			clearButton.setBackground(bg);
            calPanel.setBackground(bg);
            timePanel.setBackground(bg);
        }

        /** Returns the DateUnavailabilityModel for the CalendarPanel in the Picker.
         *  If no DateUnavailabilityModel is set before or Display Calendar is not enabled,
         *  the return value will be null. 
         */
        public DateUnavailabilityModel getDateUnavailabilityModel()
        {
            if(calPanel != null)
                return calPanel.getDateUnavailabilityModel();
            else
                return null;
        }

        /** Sets the DateUnavailabilityModel for the CalendarPanel in the Picker. */
        public void setDateUnavailabilityModel(DateUnavailabilityModel dateunavailabilitymodel)
        {
            if(calPanel != null)
                calPanel.setDateUnavailabilityModel(dateunavailabilitymodel);
        }

        /** Returns the CalendarPanel used in the picker. */
        public CalendarPanel getCalendarPanel()
        {
            return calPanel;
        }

        /** Returns the TimePanel used in the picker. */
        public TimePanel getTimePanel()
        {
            return timePanel;
        }

        public void setCalendarClockDisplay(boolean dispCal, boolean dispTime)
        {
            if(!dispCal && !dispTime)
                dispCal = true;

            centerPanel.removeAll();
            if(dispCal)
                centerPanel.add(calPanel);
            if(dispTime)
                centerPanel.add(timePanel);
            revalidate();
            repaint();
        }

        public void updateDateTime()
        {
            editedCalendar.setTime(calendar.getTime());
            editedCalendar.setTimeZone(calendar.getTimeZone());
            if(calPanel != null)
            {
                enableListeners = false;
                calPanel.setCalendar(editedCalendar);
                enableListeners = true;
            }
            if(timePanel != null)
            {
                enableListeners = false;
                timePanel.setCalendar(editedCalendar);
                enableListeners = true;
            }
        }

        public void actionPerformed(ActionEvent evt)
        {
            if(!enableListeners)
                return;

            if(evt.getSource() == okButton)
            {
				if(!nullDate)
					oldDate = (Date)(getDate().clone());

                calendar.setTime(editedCalendar.getTime());
                calendar.setTimeZone(editedCalendar.getTimeZone());
				nullDate = false;
                hide();
                notifyEditorListeners(EDIT_STOP);
                dateTimeChanged();
            }
            else if(evt.getSource() == cancelButton)
            {
                notifyEditorListeners(EDIT_CANCEL);
                hide();
            }
			else if(evt.getSource() == clearButton)
			{
				if(!nullDate)
				{
					oldDate = (Date)(getDate().clone());
					nullDate = true;
					hide();
                                        notifyEditorListeners(EDIT_STOP);
					dateTimeChanged();
				}
				else
                                {
					hide();
                                        notifyEditorListeners(EDIT_STOP);
                                }
			}
            else if(evt.getSource() == todayButton)
            {
                GregorianCalendar today = new GregorianCalendar();
                editedCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
                editedCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
                editedCalendar.set(Calendar.DATE, today.get(Calendar.DATE));
                if(calPanel != null)
                {
                    enableListeners = false;
                    calPanel.setCalendar(editedCalendar);
                    enableListeners = true;
                }
                if(timePanel != null)
                {
                    enableListeners = false;
                    timePanel.setCalendar(editedCalendar);
                    enableListeners = true;
                }
            }
            else if(evt.getSource() == nowButton)
            {
                GregorianCalendar today = new GregorianCalendar();
                editedCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
                editedCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
                editedCalendar.set(Calendar.DATE, today.get(Calendar.DATE));
                editedCalendar.set(Calendar.HOUR_OF_DAY, today.get(Calendar.HOUR_OF_DAY));
                editedCalendar.set(Calendar.MINUTE, today.get(Calendar.MINUTE));
                editedCalendar.set(Calendar.SECOND, today.get(Calendar.SECOND));
                if(calPanel != null)
                {
                    enableListeners = false;
                    calPanel.setCalendar(editedCalendar);
                    enableListeners = true;
                }
                if(timePanel != null)
                {
                    enableListeners = false;
                    timePanel.setCalendar(editedCalendar);
                    enableListeners = true;
                }
            }
            else if(evt.getSource() == timePanel)
            {
                boolean applyTimeOnly = !dateRollOverByClockEnabled;
                if(dateRollOverByClockEnabled)
                {
                    GregorianCalendar newCal = timePanel.getCalendar();
                    //Check if the roll over will move the day to an unavailable date.
                    if(calPanel != null)
                    {
                        enableListeners = false;
                        calPanel.setCalendar(newCal);
                        enableListeners = true;
                        if(calPanel.isCurrentSelectedDayUnavailable())
                            applyTimeOnly = true;
                    }
                }

                if(!applyTimeOnly)
                {
                    editedCalendar.setTime(timePanel.getCalendar().getTime());
                    editedCalendar.setTimeZone(timePanel.getCalendar().getTimeZone());
                }
                else
                {
                    // If Time Panel shouldn't change the day.
                    int date = editedCalendar.get(Calendar.DATE); 
                    int month = editedCalendar.get(Calendar.MONTH); 
                    int year = editedCalendar.get(Calendar.YEAR); 
                    editedCalendar.setTime(timePanel.getCalendar().getTime());
                    editedCalendar.setTimeZone(timePanel.getCalendar().getTimeZone());
                    editedCalendar.set(Calendar.DATE, date);
                    editedCalendar.set(Calendar.MONTH, month);
                    editedCalendar.set(Calendar.YEAR, year);
                    if(calPanel != null)
                    {
                        enableListeners = false;
                        calPanel.setCalendar(editedCalendar);
                        enableListeners = true;
                    }
                    if(dateRollOverByClockEnabled)
                    {
                        enableListeners = false;
                        timePanel.setCalendar(editedCalendar);
                        enableListeners = true;
                    }
                }
            }
            else if(evt.getSource() == calPanel)
            {
                editedCalendar.set(Calendar.DATE, calPanel.getCalendar().get(Calendar.DATE));
                editedCalendar.set(Calendar.MONTH, calPanel.getCalendar().get(Calendar.MONTH));
                editedCalendar.set(Calendar.YEAR, calPanel.getCalendar().get(Calendar.YEAR));

                if((minSelectableTime != null) && 
                    (editedCalendar.getTime().before(minSelectableTime.getTime())))
                {
                    editedCalendar.setTime(minSelectableTime.getTime());
                }
                if((maxSelectableTime != null) && 
                    (editedCalendar.getTime().after(maxSelectableTime.getTime())))
                {
                    editedCalendar.setTime(maxSelectableTime.getTime());
                }
                if(timePanel != null)
                {
                    enableListeners = false;
                    timePanel.setCalendar(editedCalendar);
                    enableListeners = true;
                }
            }
        }

        private void closePopupIfOpen()
        {
            if(popupWin != null && popupWin.isShowing())
            {
                hide();
                notifyEditorListeners(EDIT_CANCEL);
            }
        }

        /** Side effect of implementation */
        public void componentHidden(ComponentEvent componentevent)
        {
            closePopupIfOpen();
        }

        /** Side effect of implementation */
        public void componentShown(ComponentEvent componentevent)
        {
        }

        /** Side effect of implementation */
        public void componentMoved(ComponentEvent componentevent)
        {
            if(componentevent.getComponent() == windowAncestor
                && windowAncestorLocation != null)
            {
                Point newLoc = windowAncestor.getLocation();
                if(
                    (newLoc == null)
                    || (Math.abs(windowAncestorLocation.x - newLoc.x) > 1)
                    || (Math.abs(windowAncestorLocation.y - newLoc.y) > 1))
                {
                    closePopupIfOpen();
                }
            }
            else
            {
                closePopupIfOpen();
            }
        }

        /** Side effect of implementation */
        public void componentResized(ComponentEvent componentevent)
        {
            closePopupIfOpen();
        }

        public void mousePressed(MouseEvent evt)
        {
            /* Mouse pressed somewhere else. Cancel popup . */
            if(autoCancelEnabled)
            {
                closePopupIfOpen();
            }
        }

        public void mouseReleased(MouseEvent evt)
        {
        }

        public void mouseClicked(MouseEvent evt)
        {
        }

        public void mouseEntered(MouseEvent evt)
        {
        }

        public void mouseExited(MouseEvent evt)
        {
        }

        public void togglePopup()
        {
            if(popupWin == null)
            {
                notifyEditorListeners(EDIT_START);
                show();
				okButton.requestFocus();
            }
            else if(popupWin.isShowing())
            {
                hide();
                notifyEditorListeners(EDIT_CANCEL);
            }
            else
            {
                notifyEditorListeners(EDIT_START);
                show();
				okButton.requestFocus();
            }
        }

        public void show()
        {
            updateDateTime();

            Rectangle screenBounds = parentComponent.getGraphicsConfiguration().getBounds();
            Point point = parentComponent.getLocationOnScreen();
            point.y = point.y + parentComponent.getHeight();

            //Make sure we don't go beyond the screen limits
            if(point.x < screenBounds.x)
                point.x = screenBounds.x;
            if(point.x > 
                    ((screenBounds.x+screenBounds.width) - getPreferredSize().width))
                point.x = (screenBounds.x+screenBounds.width) - getPreferredSize().width;

            if(point.y < screenBounds.y)
                point.y = screenBounds.y;
            if(point.y >
                    ((screenBounds.y+screenBounds.height) - getPreferredSize().height))
                point.y = parentComponent.getLocationOnScreen().y - getPreferredSize().height;

            if(popupWin == null)
            {
                Window parentWin = SwingUtilities.getWindowAncestor(parentComponent);
                if(parentWin == null)
                    return;
				if(parentWin instanceof Dialog)
					popupWin = new JDialog((Dialog)parentWin);
				else if(parentWin instanceof Frame)
					popupWin = new JDialog((Frame)parentWin);
				else
                	popupWin = new JDialog();
				popupWin.setModal(popupModal);
				popupWin.setUndecorated(true);
                popupWin.setFocusableWindowState(true);
                popupWin.getContentPane().add(this);
                popupWin.getRootPane().setDefaultButton(okButton);
                invalidate();
            }
            popupWin.pack();
            popupWin.setLocation(point);
            popupWin.show();
            installListeners();

            popupWin.requestFocus();
            if(timePanel != null)
            {
                timePanel.requestFocus();
                ((JSpinner.DefaultEditor)timePanel.hourS.getEditor()).getTextField().requestFocus();
            }
        }

        public void hide()
        {
            if(popupWin == null)
                return;

            popupWin.hide();

            editedCalendar.setTime(calendar.getTime());
            editedCalendar.setTimeZone(calendar.getTimeZone());

            uninstallListeners();
        }

        Window windowAncestor = null;
        Point windowAncestorLocation = null;
        private void installListeners()
        {
            //Install Mouse and Component listener for all component in the current window
            // except parentComponent
            windowAncestor = (Window)SwingUtilities.getWindowAncestor(parentComponent);
            if(windowAncestor != null)
            {
                recursiveAddListener(windowAncestor);
                windowAncestor.addComponentListener(this);
            }
            windowAncestorLocation = windowAncestor.getLocation();

            // Install only Component listener on parent component
            parentComponent.addComponentListener(this); 
        }
        
        private void uninstallListeners()
        {
            //Uninstall all listeners
            windowAncestor = (Window)SwingUtilities.getWindowAncestor(parentComponent);
            if(windowAncestor != null)
            {
                recursiveRemoveListener(windowAncestor);
                windowAncestor.removeComponentListener(this);
            }
            windowAncestor = null;
            windowAncestorLocation = null;

            parentComponent.removeComponentListener(this); 
        }

        private void recursiveAddListener(Container comp)
        {
            if(comp == parentComponent)
                return;
			try
			{
				comp.addMouseListener(this);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
            Component[] childs = comp.getComponents();
            for(int i=0; i<childs.length; i++)
            {
                if(childs[i] == parentComponent)
                    return;
                if(childs[i] instanceof Container)
                    recursiveAddListener((Container)childs[i]);
                else
                {
					try
					{
						childs[i].addMouseListener(this);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
                }
            }
        }

        private void recursiveRemoveListener(Container comp)
        {
            if(comp == parentComponent)
                return;
            comp.removeMouseListener(this);
            Component[] childs = comp.getComponents();
            for(int i=0; i<childs.length; i++)
            {
                if(childs[i] == parentComponent)
                    return;
                if(childs[i] instanceof Container)
                    recursiveRemoveListener((Container)childs[i]);
                else
                {
                    childs[i].removeMouseListener(this);
                }
            }
        }
    }

    private class DateTimeRenderer extends JButton implements ActionListener
    {
        public DateTimeRenderer()
        {
			this.putClientProperty("JButton.buttonType", "segmented");
			this.putClientProperty("JButton.segmentPosition", "first");
            setHorizontalAlignment(SwingConstants.CENTER);
            setText(dateFormatter.format(calendar.getTime()));
            addActionListener(this);
        }

        public void updateDateTime()
        {
			if(nullDate)
				setText("");
			else
            	setText(dateFormatter.format(calendar.getTime()));
        }

        public void actionPerformed(ActionEvent evt)
        {
            popup.togglePopup();
        }
    }

    private static final String[] formatTokens = {
                "G","y","M",
                "w","W","D",
                "d","F","E",
                "a","H","k",
                "K","h","m",
                "s","S","z",
                "Z" };

    private static final int[] calElements = {
                Calendar.ERA, Calendar.YEAR, Calendar.MONTH,
                Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH, Calendar.DAY_OF_YEAR,
                Calendar.DAY_OF_MONTH, Calendar.DAY_OF_WEEK_IN_MONTH, Calendar.DAY_OF_WEEK,
                Calendar.AM_PM, Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY, 
                Calendar.HOUR, Calendar.HOUR, Calendar.MINUTE,
                Calendar.SECOND, Calendar.MILLISECOND, Calendar.ZONE_OFFSET,
                Calendar.ZONE_OFFSET};

    private class DateTimeEditor extends JFormattedTextField implements PropertyChangeListener
    {
        boolean enableListeners = true;
		DateTimeFormatter formatter;

        public DateTimeEditor(DateTimeFormatter formatter)
        {
            super(formatter);
			this.formatter = formatter;
            setValue(calendar.getTime());
            addPropertyChangeListener(this);
        }

        public void updateDateTime()
        {
            enableListeners = false;
			if(!nullDate)
            	setValue(calendar.getTime());
			else
				setText("");
            enableListeners = true;
        }

		public void setAllowNullValue(boolean enable)
		{
			formatter.setAllowNullValue(enable);
		}

        public void updatePattern()
        {
            formatter.setFormat(dateFormatter);
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(!enableListeners)
                return;

			if(evt.getPropertyName().equals("value"))
            {
				if(!nullDate)
					oldDate = (Date)(getDate().clone());

				if(getValue() == null)
					nullDate = true;
				else
				{
					GregorianCalendar editedCal = new GregorianCalendar(calendar.getTimeZone(),locale);

					editedCal.setTime((Date)getValue());

					GregorianCalendar newPossibleTime = (GregorianCalendar)calendar.clone();
					// Apply all time components that are part of format string from
					// editedCal. 
					String formatString = dateFormatter.toPattern();
					for(int i=0; i<formatTokens.length; i++)
					{
						if(formatString.indexOf(formatTokens[i])>=0)
						{
							newPossibleTime.set(calElements[i], editedCal.get(calElements[i]));
						}
					}

					//Check for min limit.
					if((minSelectableTime != null) &&
						(newPossibleTime.getTime().before(minSelectableTime.getTime())))
					{
						editedCal.setTime(minSelectableTime.getTime());
						enableListeners = false;
						setValue(editedCal.getTime());
						enableListeners = true;
					}

					//Check for max limit.
					if((maxSelectableTime != null) &&
						(newPossibleTime.getTime().after(maxSelectableTime.getTime())))
					{
						editedCal.setTime(maxSelectableTime.getTime());
						enableListeners = false;
						setValue(editedCal.getTime());
						enableListeners = true;
					}

					//Apply only date elements used in the format.
					for(int i=0; i<formatTokens.length; i++)
					{
						if(formatString.indexOf(formatTokens[i])>=0)
						{
							calendar.set(calElements[i], editedCal.get(calElements[i]));
						}
					}
				}
                dateTimeChanged();
            }
        }
    }

    private class DropDownButton extends JButton
    {
        public DropDownButton()
        {
            setPreferredSize(new Dimension(20,20));
			//Mac OS X properties
			if(CalendarPanel.macOSX)
			{
				this.putClientProperty("JButton.buttonType", "segmented");
				this.putClientProperty("JButton.segmentPosition", "last");
			}
        }

        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            boolean isPressed, isEnabled;
            isPressed = getModel().isPressed();
            isEnabled = isEnabled();
            Dimension dim = getSize();

            if(dim.height < 5 || dim.width < 5)
                return;

            if(isPressed)
                g.translate(1,1);

            Color origColor = g.getColor();


            if(isEnabled)
                g.setColor(getForeground());
            else
                g.setColor(Color.gray);

            int size = Math.min((dim.height - 4) / 3, ( dim.width - 4) / 3);
            size = Math.max(size, 2);
            int mid = (size / 2) - 1;

            int centerX = (dim.width - size)/2;
            int centerY = (dim.height - size)/2;

            g.translate(centerX, centerY);
            int j=0;
            for(int i= size-1; i >= 0; i--)
            {
                g.drawLine(mid-i, j, mid+i, j);
                j++;
            }

            g.translate(-centerX, -centerY);
            g.setColor(origColor);
            if(isPressed)
                g.translate(-1,-1);
        }
    }

	private class DateTimeFormatter extends DateFormatter
	{
		private boolean allowNullValue = false;
		public DateTimeFormatter(SimpleDateFormat format, boolean allowNull)
		{
			super(format);
			allowNullValue = allowNull;
		}

		public boolean isAllowNullValue()
		{
			return allowNullValue; 
		}

		public void setAllowNullValue(boolean enable)
		{
			allowNullValue = enable;
		}

		public Object stringToValue(String text) throws java.text.ParseException
		{
			if(this.allowNullValue && text.trim().equals(""))
				return null;
			else
				return super.stringToValue(text);
		}

		public String valueToString(Object value) throws java.text.ParseException
		{
			if(value == null)
				return "";
			else
				return super.valueToString(value);
		}
	}
}
