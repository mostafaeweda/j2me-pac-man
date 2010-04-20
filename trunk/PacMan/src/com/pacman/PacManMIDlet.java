/*
 * SchlaboMIDlet.java
 *
 * Created on 21. November 2006, 13:45
 */
package com.pacman;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;



/**
 *
 * @author  Andreas Jaklxx fx
 */
public class PacManMIDlet extends MIDlet
{
    private Display iDisplay;
    private boolean iFirstStart = true;
    private PacManCanvas iCanvas;

    public PacManMIDlet()
    {
    }

    public void startApp()
    {
        if (iFirstStart)
        {
            iDisplay = Display.getDisplay(this);
            iCanvas = new PacManCanvas(this);
            iCanvas.init();
            iDisplay.setCurrent(iCanvas);
            iFirstStart = false;
        }
    }

    public void pauseApp()
    {
    }

    public void destroyApp(boolean unconditional)
    {
        if (iCanvas != null)
        {
            iCanvas.stop();
        }
    }

    public void exit()
    {
        destroyApp(true);
        notifyDestroyed();
    }
}
