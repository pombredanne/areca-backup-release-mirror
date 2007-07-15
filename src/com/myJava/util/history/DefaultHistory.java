package com.myJava.util.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.CalendarUtils;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.Logger;

/**
 * 
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class DefaultHistory implements History {
    
    private static final int VERSION = 2;
    private static final String HDR = "[HISTORY_HDR]";
    
    protected File file;
    protected HashMap content;
    protected GregorianCalendar lastActionDate = null;
    protected FileTool tool = new FileTool();
    
    public DefaultHistory(File file) throws IOException {
        this.file = file;
        if (! FileSystemManager.exists(FileSystemManager.getParentFile(this.file))) {
            tool.createDir(FileSystemManager.getParentFile(this.file));
        }
        this.load();
    }
    
    public synchronized void importHistory(History h) throws IOException {
        Map map = h.getContent();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            HistoryEntry value = (HistoryEntry)entry.getValue();
            
            addEntryWithoutFlush(value);
        }

        this.flush();
    }
    
    private void addEntryWithoutFlush(HistoryEntry entry) {        
        addEntryToContent(entry);
    }
    
    public synchronized void addEntry(HistoryEntry entry) throws IOException {      
        addEntryWithoutFlush(entry);
        this.flush();
    }
    
    /**
     * Ferme l'historique et flush son contenu en fichier.
     * <BR>Ce mode peut para�tre non optimis� dans la mesure ou il r��crit tout
     * le contenu de l'historique au lieu de n'ajouter que la derni�re entr�e au fichier.
     * <BR>Ce choix a �t� fait pour rendre cette classe compatible avec les EncryptedFileSystemDrivers,
     * qui ne supportent pas l'�criture en mode "append" 
     */
    public synchronized void flush() throws IOException {       
        if (file != null) {
            FileTool tool = new FileTool();
            if (! FileSystemManager.exists(FileSystemManager.getParentFile(file))) {
                tool.createDir(FileSystemManager.getParentFile(file));
            }
            
            Writer fw = new OutputStreamWriter(new GZIPOutputStream(FileSystemManager.getFileOutputStream(file)));
            fw.write(HDR + VERSION);
            
            Iterator iter = this.content.keySet().iterator();
            while (iter.hasNext()) {
                GregorianCalendar date = (GregorianCalendar)iter.next();
                HistoryEntry entry = this.getEntry(date);
                
                fw.write("\n" + encode(entry));
            }
            fw.flush();
            fw.close();
        }
    }
    
    public void clearData() {
        if (file != null && FileSystemManager.exists(file)) {
            FileSystemManager.delete(file);
        }
    }
    
    /**
     * @see History#getContent()
     */
    public synchronized HashMap getContent() {
        return content;
    }
    
    public synchronized GregorianCalendar[] getOrderedKeys() {
        GregorianCalendar[] keys = (GregorianCalendar[])this.content.keySet().toArray(new GregorianCalendar[0]);
        Arrays.sort(keys, new GregorianCalendarComparator());
        
        return keys;
    }
    
    public synchronized HistoryEntry getEntry(GregorianCalendar date) {
        return (HistoryEntry)(content.get(date));
    }
    
    public synchronized GregorianCalendar getLastEntryDate() {
        return lastActionDate;
    }
    
    public void clear() {
        this.content.clear();
        this.lastActionDate = null;
    }
    
    public void load() throws IOException {
        this.content = new HashMap();
        if (this.file != null && FileSystemManager.exists(this.file)) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(FileSystemManager.getFileInputStream(file))));
                String strDate;
                String strContent;
                int type;
                String header = readNextSignificantLine(reader);
                int version;
                
                if (header != null) {
	                // Backward compatibility
	                if (header.indexOf(HDR) != -1) {
	                    version = 2;
	                    strDate = readNextSignificantLine(reader);
	                } else {
	                    version = 1;
	                    strDate = header;
	                }
	                
	                while (true) {
	                    strContent = readNextSignificantLine(reader);
	
	                    if (strDate != null && strContent != null) {
	                        if (version == 2) {
	                            type = Integer.parseInt(readNextSignificantLine(reader));
	                        } else {
	                            type = HistoryEntry.TYPE_UNKNOWN;
	                        }
	                        
	                        HistoryEntry entry = new HistoryEntry();
	                        entry.setDate(CalendarUtils.resolveDate(strDate, new GregorianCalendar()));
	                        entry.setDescription(decodeString(strContent));
	                        entry.setType(type);
	                        addEntryToContent(entry);
	                    } else {
	                        break;
	                    }
	                    
	                    strDate = readNextSignificantLine(reader);
	                }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }
    
    public void updateLocation(Object newLocation) throws IOException {
        if (newLocation == null || newLocation.equals(this.file)) {
            return;
        } else {
            
            // Rename File
            while (! FileSystemManager.renameTo(this.file, (File)newLocation)) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Logger.defaultLogger().error(e);
                }
            }
            
            // Update file
            this.file = (File)newLocation;
        }
    }
    
    private String readNextSignificantLine(BufferedReader reader) throws IOException {
        String str = reader.readLine();
        if (str == null || str.length() != 0) {
            return str;
        } else {
            return readNextSignificantLine(reader);
        }
    }
    
    private void addEntryToContent(HistoryEntry entry) {
        GregorianCalendar date = entry.getDate();
        
        if (this.lastActionDate == null || this.lastActionDate.before(date)) {
            this.lastActionDate = date;
        }
        
        content.put(date, entry); 
    }
    
    private String encode(HistoryEntry source) {
        GregorianCalendar date = source.getDate();
        
        String strCal = CalendarUtils.getDateToString(date) + "-" + CalendarUtils.getFullTimeToString(date);
        return strCal + "\n" + Utilitaire.replace(Utilitaire.replace(source.getDescription(), "\n", "<<N>>"), "\r", "<<R>>") + "\n" + source.type;
    }
    
    private String decodeString(String source) {
        return Utilitaire.replace(Utilitaire.replace(source, "<<N>>", "\n"), "<<R>>", "\r");
    }  
    
    protected static class GregorianCalendarComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            GregorianCalendar c1 = (GregorianCalendar)o1;
            GregorianCalendar c2 = (GregorianCalendar)o2;            
            
            if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 == null) {
                return 0;
            } else if (o2 == null && o1 != null) {
                return 1;
            } else {
                if (c1.before(c2)) {
                    return -1;
                } else if (c2.before(c1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
