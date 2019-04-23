package com.lavantech.gui.comp;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import javax.swing.*;

/** This Component displays a Analog Clock for a given GregorianCalendar. 
  * The ClockPanel can be used to display a live Clock. Here is an example.
  *  <pre>
  *  import com.lavantech.gui.comp.*;
  *  import java.util.*;
  *  import java.awt.*;
  *  import javax.swing.*;
  *
  *  class LiveClock extends JPanel implements Runnable
  *  {
  *      Thread updateThread = null;
  *      public boolean liveMode = true;
  *      ClockPanel clockPanel = null;
  *
  *      public LiveClock()
  *      {
  *          super(new BorderLayout());
  *          clockPanel = new ClockPanel(new GregorianCalendar());
  *          add(clockPanel, BorderLayout.CENTER);
  *          updateThread = new Thread(this);
  *          updateThread.start();
  *      }
  *      
  *      public void run()
  *      {
  *          while(liveMode)
  *          {
  *              try
  *              {
  *                  Thread.sleep(1000);
  *              }
  *              catch(Exception exp)
  *              {
  *              }
  *              clockPanel.setCalendar(new GregorianCalendar());
  *              SwingUtilities.invokeLater(clockPanel);
  *          }
  *      }
  *      
  *      public static void main(String args[])
  *      {
  *           JFrame frame = new JFrame();
  *           frame.getContentPane().add(new LiveClock());
  *           frame.pack();
  *           frame.show();
  *      }
  *  }
  *  </pre>
  */

public class ClockPanel extends JComponent
    implements MouseListener, MouseMotionListener, Runnable
{
    private static final int NONE = 0;
    private static final int HOUR_NEEDLE = 1;
    private static final int MIN_NEEDLE = 2;
    private static final int SEC_NEEDLE = 3;

    private Insets margin = new Insets(2,2,2,2);
    private GregorianCalendar calendar;
    private Color faceColor = Color.white;
    private Color hourNeedleColor = new Color(0, 0, 200);
    private Color minNeedleColor = new Color(0, 200, 0);
    private Color secNeedleColor = new Color(200, 0, 0);
    private Shape hourNeedleShape = null;
    private Shape minNeedleShape = null;
    private Shape secNeedleShape = null;

    private String numberFontType = "Arial";
    private int selectedNeedle = 0;
    private int circleRadius = 0;
    private Dimension compDimension = null;
    private long needlePointingTime = 0L;
    private boolean editable = true;
    private Point previousPt;
    private Vector actionListeners = new Vector();
    private boolean enabled = true;

    private GregorianCalendar minSelectableTime = null;
    private GregorianCalendar maxSelectableTime = null;

    private Image faceImage = null;
    private BufferedImage faceRenderImage = null;

    private Shape hourNeedleRenderShape = null;
    private Shape minNeedleRenderShape = null;
    private Shape secNeedleRenderShape = null;

    private double hourNeedleHeightRatio = 0.50;
    private double hourNeedleWidthRatio = 0.06;
    private double minNeedleHeightRatio = 0.70;
    private double minNeedleWidthRatio = 0.06;
    private double secNeedleHeightRatio = 0.80;
    private double secNeedleWidthRatio = 0.02;

    private int hourFormat = LocaleSpecificResources.HOUR_FORMAT_12;

    private boolean hourDisplayed = true;
    private boolean minDisplayed = true;
    private boolean secDisplayed = true;

    /** Constructs a TimePanel with the given GregorianCalendar 
     *  @param  cal    The calendar time to which the clock will be initially set to. 
     */
    public ClockPanel(GregorianCalendar cal)
    {
        this(cal, true);
    }

    /** Constructs a TimePanel with the given GregorianCalendar
     *  @param  cal    The calendar time to which the clock will be initially set to. 
     *  @param editable If true, User can change the clock by dragging the needles.
     */
    public ClockPanel(GregorianCalendar cal, boolean editable)
    {
        calendar = (GregorianCalendar)cal.clone();
        editable = editable;
        setPreferredSize(new Dimension(150, 150));
        setDoubleBuffered(false);
        addComponentListener(
            new ComponentAdapter()
            {
                public void componentResized(ComponentEvent componentevent)
                {
                    needlePointingTime = 0;
                    processComponentResize();
                }
            });
        if(editable)
        {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        Polygon polygon = new Polygon();
        polygon.addPoint(5,5);
        polygon.addPoint(0,8);
        polygon.addPoint(-5,5);
        polygon.addPoint(-5,-85);
        polygon.addPoint(0,-100);
        polygon.addPoint(5,-85);
        polygon.addPoint(5,5);
        hourNeedleShape = polygon;

        polygon = new Polygon();
        polygon.addPoint(5,5);
        polygon.addPoint(0,8);
        polygon.addPoint(-5,5);
        polygon.addPoint(-5,-85);
        polygon.addPoint(0,-100);
        polygon.addPoint(5,-85);
        polygon.addPoint(5,5);
        minNeedleShape = polygon;

        polygon = new Polygon();
        polygon.addPoint(5,5);
        polygon.addPoint(0,8);
        polygon.addPoint(-5,5);
        polygon.addPoint(-5,-85);
        polygon.addPoint(0,-100);
        polygon.addPoint(5,-85);
        polygon.addPoint(5,5);
        secNeedleShape = polygon;
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
    public void setMinSelectableTime(GregorianCalendar minTime)
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
                calendar.setTime(minSelectableTime.getTime());
        }
        updateTime();
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
    public void setMaxSelectableTime(GregorianCalendar maxTime)
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
                calendar.setTime(maxSelectableTime.getTime());
        }
        updateTime();
    }

    /** Sets whether the ClockPanel is enabled or not. */
    public void setEnabled(boolean enable)
    {
        enabled = enable;
        super.setEnabled(enable);
        repaint();
    }

    /** Sets if the clock is changable
     *  @param editable  If true, User can change the clock by dragging the needles.
     */
    public void setEditable(boolean editable)
    {
        this.editable = editable;
        if(editable)
        {
            addMouseListener(this);
            addMouseMotionListener(this);
        } else
        {
            removeMouseListener(this);
            removeMouseMotionListener(this);
        }
    }

    /** Returns if this ClockPanel is editable */
    public boolean isEditable()
    {
        return editable;
    }

	void recalculateSize()
	{
		compDimension = null;
	}

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

		if(compDimension == null)
			processComponentResize();

        Graphics2D g2d = (Graphics2D)g;

        if(calendar.getTime().getTime() != needlePointingTime)
            positionNeedle(g2d);

        // Paint the face.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(compDimension.width / 2, compDimension.height / 2);

        if(faceImage != null)
        {
            g2d.drawImage(faceRenderImage, null, -circleRadius, -circleRadius );
        }
        else
        {
            Color color = getForeground();
            g2d.setColor(faceColor);
            g2d.fillOval(-circleRadius, -circleRadius, circleRadius * 2, circleRadius * 2);
            g2d.setColor(color);

            java.awt.Stroke stroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(-circleRadius, -circleRadius, circleRadius * 2, circleRadius * 2);
            g2d.setStroke(stroke);

            if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
            {
                for(int i = 0; i < 12; i++)
                {
                    g2d.drawRect(-1, -circleRadius + 1, 2, 4);
                    g2d.rotate(Math.PI/6);
                }

                int fontSize = (circleRadius * 12) / 50;
                Font font = new Font(numberFontType, 1, fontSize);
                g2d.setFont(font);

                FontMetrics fontmetrics = g2d.getFontMetrics();
                int fontHeight = fontmetrics.getHeight();
                int strWidth = fontmetrics.stringWidth("12");
                g2d.drawString("12", -(strWidth / 2), -circleRadius + fontHeight + 4);
                strWidth = fontmetrics.stringWidth("3");
                g2d.drawString("3", circleRadius - strWidth - 8, fontHeight / 2);
                strWidth = fontmetrics.stringWidth("6");
                g2d.drawString("6", -(strWidth / 2), circleRadius - 8);
                g2d.drawString("9", -circleRadius + 8, fontHeight / 2);
            }
            else
            {
                for(int i = 0; i < 24; i++)
                {
                    g2d.drawRect(-1, -circleRadius + 1, 2, 4);
                    g2d.rotate(Math.PI/12);
                }

                int fontSize = (circleRadius * 12) / 50;
                Font font = new Font(numberFontType, 1, fontSize);
                g2d.setFont(font);

                FontMetrics fontmetrics = g2d.getFontMetrics();
                int fontHeight = fontmetrics.getHeight();
                int strWidth = fontmetrics.stringWidth("00");
                g2d.drawString("00", -(strWidth / 2), -circleRadius + fontHeight + 4);
                strWidth = fontmetrics.stringWidth("6");
                g2d.drawString("6", circleRadius - strWidth - 8, fontHeight / 2);
                strWidth = fontmetrics.stringWidth("12");
                g2d.drawString("12", -(strWidth / 2), circleRadius - 8);
                g2d.drawString("18", -circleRadius + 8, fontHeight / 2);
            }
        }

        Color color = g2d.getColor();
        if(!enabled)
            g2d.setColor(Color.gray);
        else
            g2d.setColor(hourNeedleColor);
        if(hourDisplayed)
            g2d.fill(hourNeedleRenderShape);

        if(!enabled)
            g2d.setColor(Color.gray);
        else
            g2d.setColor(minNeedleColor);
        if(minDisplayed)
            g2d.fill(minNeedleRenderShape);

        if(!enabled)
            g2d.setColor(Color.gray);
        else
            g2d.setColor(secNeedleColor);
        if(secDisplayed)
            g2d.fill(secNeedleRenderShape);

        g2d.setColor(color);
    }

    /** Returns the selected time.
     */
    public GregorianCalendar getCalendar()
    {
        return (GregorianCalendar)calendar.clone();
    }

    /** Sets the selected time. */
    public void setCalendar(GregorianCalendar cal)
    {
        calendar.setTime(cal.getTime());
        calendar.setTimeZone(cal.getTimeZone());
        updateTime();
        notifyListeners();
    }

    /** The updateTime method updates the ClockPanel with any changes in the calendar time. 
     */
    private void updateTime()
    {
        if(compDimension != null)
            paintImmediately(new Rectangle(0, 0, compDimension.width, compDimension.height));
    }

    /** The run method updates the ClockPanel just like updateTime().
     *  This method is used to implement the Runnable interface,
     *  the Runnable interface can be used to update the clock from
     *  another thread using the SwingUtilies.invokeLater() method.
     */
    public void run()
    {
        if(compDimension != null)
            paintImmediately(new Rectangle(0, 0, compDimension.width, compDimension.height));
    }


    /** Sets the hour format for the clock. The two possible values are
     *  LocaleSpecificResource.HOUR_FORMAT_12 and LocaleSpecificResource.HOUR_FORMAT_24.
     *  The 12 hour format is the default value.
     */
    public void setHourFormat(int format)
    {
        hourFormat = format;
    }

    /** Gets the hour format for the clock.
     *  The hour format returned is either LocaleSpecificResource.HOUR_FORMAT_12 or
     *  LocaleSpecificResource.HOUR_FORMAT_24.
     */
    public int getHourFormat()
    {
        return hourFormat;
    }

    /** Returns the clock's face color in the default face graphics. The default face graphics
     *  is used when no faceImage is set.
     */
    public Color getFaceColor()
    {
        return faceColor;
    }

    /** Sets the clock's face color in the default face graphics. The default face graphics
     *  is used when no faceImage is set.
     */
    public void setFaceColor(Color color)
    {
        faceColor = color;
        repaint();
    }

    /** Returns the clock's hour needle's color. */
    public Color getHourNeedleColor()
    {
        return hourNeedleColor;
    }

    /** Sets the clock's hour needle's color. */
    public void setHourNeedleColor(Color color)
    {
        hourNeedleColor = color;
        repaint();
    }

    /** Gets the clock's minute needle's color */
    public Color getMinNeedleColor()
    {
        return minNeedleColor;
    }

    /** Sets the clock's minute needle's color.  */
    public void setMinNeedleColor(Color color)
    {
        minNeedleColor = color;
        repaint();
    }

    /** Gets the clock's second needle's color */
    public Color getSecNeedleColor()
    {
        return secNeedleColor;
    }

    /** Sets the clock's second needle's color. */
    public void setSecNeedleColor(Color color)
    {
        secNeedleColor = color;
        repaint();
    }

    /** Gets the image used for the clock face. 
     *  If no image was set, null is returned. 
     */
    public Image getFaceImage()
    {
        return faceImage;
    }

    /** Sets the image to use for the clock face. 
     *  If set to null, the default face graphics is used.
     */
    public void setFaceImage(Image face)
    {
        faceImage = face;
        if((circleRadius != 0) && (faceImage != null))
        {
            int rightWidth = circleRadius * 2;
            faceRenderImage = ImageUtils.toBufferedImage(faceImage.getScaledInstance(
                    rightWidth, rightWidth, Image.SCALE_SMOOTH));
        }
    }

    /** Returns the shape of the hour needle. */
    public Shape getHourNeedleShape()
    {
        return hourNeedleShape;
    }

    /** Sets the hour needle shape. The shape should be pointing north (12oClock position)
     *  and the pivot center of the needle should be coordinate 0,0 (x,y). The shape can be of
     *  any size. The shape is scaled to a height and width relative to clock radius. The
     *  hourNeedleHeightRatio and hourNeedleWidthRatio properties determine the relative height 
     *  and width.  If null is passed, the shape is set to the default shape. 
     *  The default shape is a polygon with the vertices 
     *  (5,5) , (0,8), (-5,5),(-5,-85), (0,-100), (5,-85), (5,5).
     *  @see #setHourNeedleHeightRatio(double)
     *  @see #setHourNeedleWidthRatio(double)
     */
    public void setHourNeedleShape(Shape newShape)
    {
        hourNeedleShape = newShape;
        if(hourNeedleShape == null)
        {
            Polygon polygon = new Polygon();
            polygon.addPoint(5,5);
            polygon.addPoint(0,8);
            polygon.addPoint(-5,5);
            polygon.addPoint(-5,-85);
            polygon.addPoint(0,-100);
            polygon.addPoint(5,-85);
            polygon.addPoint(5,5);
            hourNeedleShape = polygon;
        }
    }

    /** Returns the shape of the minute needle. */
    public Shape getMinNeedleShape()
    {
        return minNeedleShape;
    }

    /** Sets the minute needle shape. The shape should be pointing north (12oClock position)
     *  and the pivot center of the needle should be coordinate 0,0 (x,y). The shape can be of
     *  any size. The shape is scaled to a height and width relative to clock radius. The
     *  minNeedleHeightRatio and minNeedleWidthRatio properties determine the relative height 
     *  and width.  If null is passed, the shape is set to the default shape. 
     *  The default shape is a polygon with the vertices 
     *  (5,5) , (0,8), (-5,5),(-5,-85), (0,-100), (5,-85), (5,5).
     *  @see #setMinNeedleHeightRatio(double)
     *  @see #setMinNeedleWidthRatio(double)
     */
    public void setMinNeedleShape(Shape newShape)
    {
        minNeedleShape = newShape;
        if(minNeedleShape == null)
        {
            Polygon polygon = new Polygon();
            polygon.addPoint(5,5);
            polygon.addPoint(0,8);
            polygon.addPoint(-5,5);
            polygon.addPoint(-5,-85);
            polygon.addPoint(0,-100);
            polygon.addPoint(5,-85);
            polygon.addPoint(5,5);
            minNeedleShape = polygon;
        }
    }

    /** Returns the shape of the seconds needle. */
    public Shape getSecNeedleShape()
    {
        return secNeedleShape;
    }

    /** Sets the second needle shape. The shape should be pointing north (12oClock position)
     *  and the pivot center of the needle should be coordinate 0,0 (x,y). The shape can be of
     *  any size. The shape is scaled to a height and width relative to clock radius. The
     *  secNeedleHeightRatio and secNeedleWidthRatio properties determine the relative height 
     *  and width.  If null is passed, the shape is set to the default shape. 
     *  The default shape is a polygon with the vertices 
     *  (5,5) , (0,8), (-5,5),(-5,-85), (0,-100), (5,-85), (5,5).
     *  @see #setSecNeedleHeightRatio(double)
     *  @see #setSecNeedleWidthRatio(double)
     */
    public void setSecNeedleShape(Shape newShape)
    {
        secNeedleShape = newShape;
        if(secNeedleShape == null)
        {
            Polygon polygon = new Polygon();
            polygon.addPoint(5,5);
            polygon.addPoint(0,8);
            polygon.addPoint(-5,5);
            polygon.addPoint(-5,-85);
            polygon.addPoint(0,-100);
            polygon.addPoint(5,-85);
            polygon.addPoint(5,5);
            secNeedleShape = polygon;
        }
    }

    /** Returns the hour needle height to the clock face radius ratio.  */
    public double getHourNeedleHeightRatio()
    {
        return hourNeedleHeightRatio;
    }

    /** Sets the relative height of hour needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setHourNeedleHeightRatio(double ratio)
    {
        hourNeedleHeightRatio = ratio;
    }

    /** Returns the hour needle width to the clock face radius ratio.  */
    public double getHourNeedleWidthRatio()
    {
        return hourNeedleWidthRatio;
    }

    /** Sets the relative width of hour needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setHourNeedleWidthRatio(double ratio)
    {
        hourNeedleWidthRatio = ratio;
    }

    /** Returns the minute needle height to the clock face radius ratio.  */
    public double getMinNeedleHeightRatio()
    {
        return hourNeedleHeightRatio;
    }

    /** Sets the relative height of minute needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setMinNeedleHeightRatio(double ratio)
    {
        minNeedleHeightRatio = ratio;
    }

    /** Returns the minute needle width to the clock face radius ratio.  */
    public double getMinNeedleWidthRatio()
    {
        return minNeedleWidthRatio;
    }

    /** Sets the relative width of minute needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setMinNeedleWidthRatio(double ratio)
    {
        minNeedleWidthRatio = ratio;
    }

    /** Returns the second needle height to the clock face radius ratio.  */
    public double getSecNeedleHeightRatio()
    {
        return hourNeedleHeightRatio;
    }

    /** Sets the relative height of second needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setSecNeedleHeightRatio(double ratio)
    {
        secNeedleHeightRatio = ratio;
    }

    /** Returns the second needle width to the clock face radius ratio.  */
    public double getSecNeedleWidthRatio()
    {
        return secNeedleWidthRatio;
    }

    /** Sets the relative width of second needle to the clock face radius. The value
     *  should be between 0 - 1. 
     */
    public void setSecNeedleWidthRatio(double ratio)
    {
        secNeedleWidthRatio = ratio;
    }


    /** Returns whether the minute needle is displayed or not. */
    public boolean isMinDisplayed()
    {
        return minDisplayed;
    }

    /** Sets whether the minute needle is displayed or not. */
    public void setMinDisplayed(boolean val)
    {
        if(val == minDisplayed)
            return;
        minDisplayed = val;
        repaint();
    }

    /** Returns whether the second needle is displayed or not. */
    public boolean isSecDisplayed()
    {
        return secDisplayed;
    }

    /** Sets whether the second needle is displayed or not. */
    public void setSecDisplayed(boolean val)
    {
        if(val == secDisplayed)
            return;
        secDisplayed = val;
        repaint();
    }

    private void processComponentResize()
    {
        compDimension = getSize();
        int availWidth = compDimension.width - margin.left - margin.right;
        int availHeight = compDimension.height - margin.top - margin.bottom;
        circleRadius = availWidth <= availHeight ? availWidth / 2 : availHeight / 2;

        if((circleRadius != 0) && (faceImage != null))
        {
            int rightWidth = circleRadius * 2;
            faceRenderImage = ImageUtils.toBufferedImage(faceImage.getScaledInstance(
                    rightWidth, rightWidth, Image.SCALE_SMOOTH));
        }
    }

    private void positionNeedle(Graphics g)
    {
        if(compDimension == null)
            processComponentResize();

        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        float hour;
        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
            hour = (float)calendar.get(Calendar.HOUR) + (float)min / 60F;
        else
            hour = (float)calendar.get(Calendar.HOUR_OF_DAY) + (float)min / 60F;
        int fontSize = (circleRadius * 12) / 50;
        Font font = new Font(numberFontType, 1, fontSize);
        g.setFont(font);
        FontMetrics fontmetrics = g.getFontMetrics();
        int fontHeight = fontmetrics.getHeight();

        AffineTransform affinetransform = new AffineTransform();
        Rectangle bounds = hourNeedleShape.getBounds();
        affinetransform.scale(((circleRadius * hourNeedleWidthRatio)/bounds.width),
            ((circleRadius * hourNeedleHeightRatio)/bounds.height));
        hourNeedleRenderShape = affinetransform.createTransformedShape(hourNeedleShape);
        affinetransform = new AffineTransform();
        if(hourFormat == LocaleSpecificResources.HOUR_FORMAT_12)
            affinetransform.rotate(((double)hour * Math.PI) / 6);
        else
            affinetransform.rotate(((double)hour * Math.PI) / 12);
        hourNeedleRenderShape = affinetransform.createTransformedShape(hourNeedleRenderShape);

        affinetransform = new AffineTransform();
        bounds = minNeedleShape.getBounds();
        affinetransform.scale(((circleRadius * minNeedleWidthRatio)/bounds.width),
            ((circleRadius * minNeedleHeightRatio)/bounds.height));
        minNeedleRenderShape = affinetransform.createTransformedShape(minNeedleShape);
        affinetransform = new AffineTransform();
        affinetransform.rotate(((double)min * Math.PI) / 30);
        minNeedleRenderShape = affinetransform.createTransformedShape(minNeedleRenderShape);

        affinetransform = new AffineTransform();
        bounds = secNeedleShape.getBounds();
        affinetransform.scale(((circleRadius * secNeedleWidthRatio)/bounds.width),
            ((circleRadius * secNeedleHeightRatio)/bounds.height));
        secNeedleRenderShape = affinetransform.createTransformedShape(secNeedleShape);
        affinetransform = new AffineTransform();
        affinetransform.rotate(((double)sec * Math.PI) / 30);
        secNeedleRenderShape = affinetransform.createTransformedShape(secNeedleRenderShape);

        needlePointingTime = calendar.getTime().getTime();
    }

    /** Implementation side effect. */
    public void mouseClicked(MouseEvent evt)
    {
    }

    /** Implementation side effect. */
    public void mouseEntered(MouseEvent evt)
    {
    }

    /** Implementation side effect. */
    public void mouseExited(MouseEvent evt)
    {
    }

    /** Implementation side effect. */
    public void mousePressed(MouseEvent evt)
    {
        if(!enabled)
            return;
        previousPt = evt.getPoint();
        previousPt.x = previousPt.x - compDimension.width / 2;
        previousPt.y = previousPt.y - compDimension.height / 2;

        if(secDisplayed && secNeedleRenderShape.getBounds().contains(previousPt.x, previousPt.y))
            selectedNeedle = SEC_NEEDLE;
        else if(minDisplayed && minNeedleRenderShape.contains(previousPt.x, previousPt.y))
            selectedNeedle = MIN_NEEDLE;
        else if(hourDisplayed && hourNeedleRenderShape.contains(previousPt.x, previousPt.y))
            selectedNeedle = HOUR_NEEDLE;
    }

    /** Implementation side effect. */
    public void mouseReleased(MouseEvent evt)
    {
        if(!enabled)
            return;
        if(selectedNeedle != NONE)
            notifyListeners();
        selectedNeedle = NONE;
        repaint();
    }

    /** Implementation side effect. */
    public void mouseDragged(MouseEvent evt)
    {
        if(!enabled)
            return;
        if(selectedNeedle == NONE)
            return;
        Point point = evt.getPoint();
        point.x = point.x - compDimension.width / 2;
        point.y = point.y - compDimension.height / 2;
        int direction = 0;
        if(point.y < 0) // top half
        {
            if(point.x > previousPt.x)
                direction = 1;
            else if(point.x < previousPt.x)
                direction = -1;
        }
        else // bottom half
        {
            if(point.x > previousPt.x)
                direction = -1;
            else if(point.x < previousPt.x)
                direction = 1;
        }

        if(point.x > 0) // right half
        {
            if(point.y > previousPt.y)
                direction = 1;
            else if(point.y < previousPt.y)
                direction = -1;
        }
        else // left half
        {
            if(point.y > previousPt.y)
                direction = -1;
            else if(point.y < previousPt.y)
                direction = 1;
        }

        previousPt = point;

        if(selectedNeedle == HOUR_NEEDLE)
        {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + direction);
            if((minSelectableTime != null) && 
                (calendar.getTime().before(minSelectableTime.getTime())))
                calendar.setTime(minSelectableTime.getTime());
            if((maxSelectableTime != null) && 
                (calendar.getTime().after(maxSelectableTime.getTime())))
                calendar.setTime(maxSelectableTime.getTime());
            repaint();
        }
        else if(selectedNeedle == MIN_NEEDLE)
        {
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + direction);
            if((minSelectableTime != null) && 
                (calendar.getTime().before(minSelectableTime.getTime())))
                calendar.setTime(minSelectableTime.getTime());
            if((maxSelectableTime != null) && 
                (calendar.getTime().after(maxSelectableTime.getTime())))
                calendar.setTime(maxSelectableTime.getTime());
            repaint();
        }
        else if(selectedNeedle == SEC_NEEDLE)
        {
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + direction);
            if((minSelectableTime != null) && 
                (calendar.getTime().before(minSelectableTime.getTime())))
                calendar.setTime(minSelectableTime.getTime());
            if((maxSelectableTime != null) && 
                (calendar.getTime().after(maxSelectableTime.getTime())))
                calendar.setTime(maxSelectableTime.getTime());
            repaint();
        }
    }

    /** Implementation side effect. */
    public void mouseMoved(MouseEvent evt)
    {
    }

    /** Add an action listener that will be notified when the time is changed. */
    public void addActionListener(ActionListener ls)
    {
        if(!actionListeners.contains(ls))
            actionListeners.add(ls);
    }

    /** Remove an action listener. */
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
        ActionEvent actionevent = new ActionEvent(this, 1, null);
        for(int i = 0; i < vector.size(); i++)
            ((ActionListener)vector.elementAt(i)).actionPerformed(actionevent);
    }
}
