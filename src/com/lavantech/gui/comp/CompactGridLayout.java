package com.lavantech.gui.comp;

import java.awt.*;

/** This class is similar to GridLayout except that columns and rows are not always
 *  equal size. Each column width is determined by the maximum width of its element.
 */
public class CompactGridLayout extends GridLayout 
{
    public CompactGridLayout()
    {
        this(1, 0, 0, 0);
    }

    public CompactGridLayout(int rows, int cols)
    {
        this(rows, cols, 0, 0);
    }

    public CompactGridLayout(int rows, int cols, int hgap, int vgap)
    {
        super(rows, cols, hgap, vgap);
    }

    public Dimension preferredLayoutSize(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = getRows();
            int ncols = getColumns();
            if (nrows > 0)
                ncols = (ncomponents + nrows - 1) / nrows;
            else
                nrows = (ncomponents + ncols - 1) / ncols;

            int[] w = new int[ncols];
			for(int i=0; i < ncols; i++)
				w[i] = 0;

            int[] h = new int[nrows];
			for(int i=0; i < nrows; i++)
				h[i] = 0;

            for (int i = 0; i < ncomponents; i ++)
            {
                int r = (int)Math.floor(i / ncols);
                int c = i % ncols;
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (w[c] < d.width)
                    w[c] = d.width;
                if (h[r] < d.height)
                    h[r] = d.height;
            }

            int nw = 0;
            for (int j = 0; j < ncols; j ++)
                nw += w[j];

            int nh = 0;
            for (int i = 0; i < nrows; i ++)
                nh += h[i];

            return new Dimension(
                insets.left + insets.right + nw + (ncols-1)*getHgap(), 
                insets.top + insets.bottom + nh + (nrows-1)*getVgap());
        }
    }

    public Dimension minimumLayoutSize(Container parent)
	{
		return preferredLayoutSize(parent);
	}

    public void layoutContainer(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = getRows();
            int ncols = getColumns();
            if (ncomponents == 0)
                return;

            if (nrows > 0)
                ncols = (ncomponents + nrows - 1) / nrows;
            else 
                nrows = (ncomponents + ncols - 1) / ncols;

            int hgap = getHgap();
            int vgap = getVgap();

            // scaling factors      
            Dimension pd = preferredLayoutSize(parent);
            double sw = (1.0 * parent.getWidth()) / pd.width;
            double sh = (1.0 * parent.getHeight()) / pd.height;

            // scale
            int[] w = new int[ncols];
			for(int i=0; i < ncols; i++)
				w[i] = 0;

            int[] h = new int[nrows];
			for(int i=0; i < nrows; i++)
				h[i] = 0;

            for (int i = 0; i < ncomponents; i ++)
            {
                int r = (int)Math.floor(i / ncols);
                int c = i % ncols;
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
				int compW = d.width;
				int compH = d.height;

				if(sw < 0.9 || sw > 1.1)
                	compW = (int) (sw * compW);
				if(sh < 0.9 || sh > 1.1)
                	compH = (int) (sh * compH);

                if (w[c] < compW) 
                    w[c] = compW;
                if (h[r] < compH)
                    h[r] = compH;
            }
            for (int c = 0, x = insets.left; c < ncols; c ++)
            {
                for (int r = 0, y = insets.top; r < nrows; r ++)
                {
                    int i = r * ncols + c;
                    if (i < ncomponents)
                        parent.getComponent(i).setBounds(x, y, w[c], h[r]);
                    y += h[r] + vgap;
                }
                x += w[c] + hgap;
            }
        }
    }  
}
