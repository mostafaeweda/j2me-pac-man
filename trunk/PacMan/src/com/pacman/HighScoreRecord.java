/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pacman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 */
public class HighScoreRecord
{
    private String name; // Saves name of the contact
    private int highScore; // Saves age

    public HighScoreRecord (String name, int age)
    {
        this.name = name;
        this.highScore = age;
    }

    /** Write all relevant data to the stream, which will allow rebuilding the object when reading the stream. */
    public void externalize(DataOutputStream dos) throws IOException
    {
        dos.writeUTF (name);
        dos.writeInt (highScore);
    }

    /** Read data from the stream and use it to create and return a new instance of this object. */
    public static HighScoreRecord internalize(DataInputStream dis) throws IOException
    {
        String tmpName = dis.readUTF();
        int tmpAge = dis.readInt();
        return new HighScoreRecord(tmpName, tmpAge);
    }

    /** Return relevant information in string form. */
    public String toString()
    {
        return ("Name = " + name + ", Score = " + highScore);
    }

    int compare(HighScoreRecord record2)
    {
        return this.highScore - record2.highScore;
    }

    public static void saveMyScore(String name, int aScore)
    {
        HighScoreRecord myRec = new HighScoreRecord(name, aScore);
        try {
//            RecordStore.deleteRecordStore("PacManHighScores");
            RecordStore rs = RecordStore.openRecordStore("PacManHighScores", true);
            // First create the byte array output stream which can generate the array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // On top of that, create the data output stream,
            // which can serialize several standard Java data types
            DataOutputStream dos = new DataOutputStream(bos);
            // Write data to the stream
            myRec.externalize(dos);
            // Flush data to make sure everything is commited down streams
            dos.flush();
            // Grab byte array from the stream
            byte[] recordOut = bos.toByteArray();
            // Add a new record to the record store, write the whole array from 0 to its
            int newRecordId = rs.addRecord(recordOut, 0, recordOut.length);
            bos.reset();
            // Finished working on the record store – close it
            dos.close(); // Closes underlying output stream as well
            rs.closeRecordStore();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }

    }

    public static Vector loadHighScores()
    {
        Vector highScores = null;
        try {
        RecordStore rs = RecordStore.openRecordStore("PacManHighScores", false);
        // Read back data using a record enumerator to go through all elements
        // Record enumerator would be more powerful, we do not use the advanced features here
        RecordComparator copmartor = new RecordComparator() {
                public int compare(byte[] rec1, byte[] rec2)
                {
                    try {
                        HighScoreRecord record1 = HighScoreRecord.internalize(new DataInputStream(new ByteArrayInputStream(rec1)));
                        HighScoreRecord record2 = HighScoreRecord.internalize(new DataInputStream(new ByteArrayInputStream(rec2)));
                        return record2.compare(record1);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return 0;
                }
            };
        int n = rs.getNumRecords();
        RecordEnumeration renum = rs.enumerateRecords(null, copmartor, false);
        highScores = new Vector();
        for (int i = 0; i < n && renum.hasNextElement(); i++)
        {
            byte []record = renum.nextRecord(); // Get data of the next record
            ByteArrayInputStream bis = new ByteArrayInputStream(record); // Input streams for parsing data
            DataInputStream dis = new DataInputStream(bis);
            HighScoreRecord cd = HighScoreRecord.internalize (dis); // Internalize and create new instance (static method)
            highScores.addElement(cd);
            dis.close();
        }
        rs.closeRecordStore (); // Close the record store
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return highScores;
    }

    public String getName()
    {
        return name;
    }

    public int getHighScore()
    {
        return highScore;
    }
}