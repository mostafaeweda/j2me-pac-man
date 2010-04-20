/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pacman;

import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

/**
 *
 * @author mostafa
 */
public class SoundPlayer implements  Runnable
{
    private String iSoundRes;
    private PacManCanvas iCanvas;

    public SoundPlayer(String aSoundRes, PacManCanvas aMidlet)
    {
        iSoundRes = aSoundRes;
        iCanvas = aMidlet;
    }

    public void run()
    {
        while (! iCanvas.IsActive())
        {
            try {
              InputStream in = getClass().getResourceAsStream(iSoundRes);
              Player player = Manager.createPlayer(in, "audio/x-wav");
              player.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}