package com.lavantech.gui.comp;

import javax.swing.event.*;

public interface PickerEditorListener
{
    /** Called when the editing starts. This happens when the 
    * user clicks and opens the Popup window.
    */
    public void editingStarted(ChangeEvent e);

    /** Called when the editing stops. This happens when the 
    * user clicks the OK button. 
    */
    public void editingStopped(ChangeEvent e);

    /** Called when the editing is canceled. This happens when the
    * user clicks the Cancel button.
    */
    public void editingCanceled(ChangeEvent e);
}
