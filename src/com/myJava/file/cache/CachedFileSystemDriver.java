package com.myJava.file.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.AbstractLinkableFileSystemDriver;
import com.myJava.file.FileInformations;
import com.myJava.file.FileSystemDriver;
import com.myJava.file.LinkableFileSystemDriver;
import com.myJava.file.attributes.Attributes;
import com.myJava.file.event.EventFileSystemDriver;
import com.myJava.file.event.FileSystemDriverListener;
import com.myJava.file.event.LoggerFileSystemDriverListener;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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
public class CachedFileSystemDriver
extends AbstractLinkableFileSystemDriver 
implements LinkableFileSystemDriver {

    private static final boolean DEBUG_PREDECESSOR = FrameworkConfiguration.getInstance().isFSCacheDebug();

    private FileDataCache cache;
    private File root;
    private int maxDepth;

    public CachedFileSystemDriver(FileSystemDriver predecessor, File root, int maxDepth) {
        this.maxDepth = maxDepth;
        this.root = root;
        if (DEBUG_PREDECESSOR) {
            FileSystemDriverListener lst = new LoggerFileSystemDriverListener();
            EventFileSystemDriver evt = new EventFileSystemDriver(predecessor, "Cache FS access", lst);
            this.predecessor = evt;
        } else {
            this.predecessor = predecessor;
        }
        this.cache = new FileDataCache(predecessor.getAbsolutePath(root), maxDepth);
    }

    public void applyAttributes(Attributes p, File f) throws IOException {
        predecessor.applyAttributes(p, f);
    }

    public boolean canRead(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isReadableSet()) {
                entry.setReadable(predecessor.canRead(file));
            }

            return entry.isReadable();
        } catch (MaxDepthReachedException e) {
            return predecessor.canRead(file);
        }
    }

    public boolean canWrite(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isWritableSet()) {
                entry.setWritable(predecessor.canWrite(file));
            }

            return entry.isWritable();
        } catch (MaxDepthReachedException e) {
            return predecessor.canWrite(file);
        }
    }

    public boolean createNewFile(File file) throws IOException {
        if (predecessor.createNewFile(file)) {
            try {
                DataEntry entry = getOrCreateDataEntry(file, false, true);
                entry.reset();
                entry.setExists(true);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean createSymbolicLink(File symlink, String realPath) throws IOException {
        if (predecessor.createSymbolicLink(symlink, realPath)) {
            try {
                DataEntry entry = getOrCreateDataEntry(symlink, false, true);
                entry.reset();
                entry.setExists(true);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean delete(File file) {
        if (predecessor.delete(file)) {
            try {
                DataEntry entry = getDataEntry(file);
                if (entry != null) {
                    entry.reset();
                    entry.setExists(false);
                }
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public void deleteOnExit(File f) {
        predecessor.deleteOnExit(f);
    }

    public boolean directFileAccessSupported() {
        return predecessor.directFileAccessSupported();
    }

    public boolean exists(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isExistsSet()) {
                entry.setExists(predecessor.exists(file));
            }

            return entry.isExists();
        } catch (MaxDepthReachedException e) {
            return predecessor.exists(file);
        }
    }

    public void flush() throws IOException {
        predecessor.flush();
    }

    public File getAbsoluteFile(File file) {
        return predecessor.getAbsoluteFile(file);
    }

    public String getAbsolutePath(File file) {
        return predecessor.getAbsolutePath(file);
    }

    public short getAccessEfficiency() {
        return predecessor.getAccessEfficiency();
    }

    public Attributes getAttributes(File f) throws IOException {
        return predecessor.getAttributes(f);
    }

    public File getCanonicalFile(File file) throws IOException {
        return predecessor.getCanonicalFile(file);
    }

    public String getCanonicalPath(File file) throws IOException {
        return predecessor.getCanonicalPath(file);
    }

    public InputStream getFileInputStream(File file) throws IOException {
        return predecessor.getFileInputStream(file);
    }

    public OutputStream getCachedFileOutputStream(File file) throws IOException {
        OutputStream os = predecessor.getCachedFileOutputStream(file);
        if (os != null) {
            try {
                DataEntry entry = this.getDataEntry(file);
                if (entry != null) {
                    entry.reset();
                }
            } catch (MaxDepthReachedException ignored) {
            }
        }
        return os;
    }

    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        OutputStream os = predecessor.getFileOutputStream(file, append);
        if (os != null) {
            try {
                DataEntry entry = this.getOrCreateDataEntry(file, false, true);
                entry.reset();
                entry.setExists(true);
            } catch (MaxDepthReachedException ignored) {
            }
        }
        return os;
    }

    public OutputStream getFileOutputStream(File file) throws IOException {
        return getFileOutputStream(file, false);
    }

    public String getName(File file) {
        return predecessor.getName(file);
    }

    public String getParent(File file) {
        return predecessor.getParent(file);
    }

    public File getParentFile(File file) {
        return predecessor.getParentFile(file);
    }

    public String getPath(File file) {
        return predecessor.getPath(file);
    }

    public boolean isAbsolute(File file) {
        return predecessor.isAbsolute(file);
    }

    public boolean isContentSensitive() {
        return predecessor.isContentSensitive();
    }

    public boolean isDirectory(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isDirectorySet()) {
                entry.setDirectory(predecessor.isDirectory(file));
            }

            return entry.isDirectory();
        } catch (MaxDepthReachedException e) {
            return predecessor.isDirectory(file);
        }
    }

    public boolean isFile(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isDirectorySet()) {
                entry.setDirectory(predecessor.isDirectory(file));
            }

            return ! (entry.isDirectory());
        } catch (MaxDepthReachedException e) {
            return predecessor.isFile(file);
        }
    }

    public boolean isHidden(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return false;
            }

            if (! entry.isHiddenSet()) {
                entry.setHidden(predecessor.isHidden(file));
            }

            return entry.isHidden();
        } catch (MaxDepthReachedException e) {
            return predecessor.isHidden(file);
        }
    }

    public long lastModified(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return 0;
            }

            if (! entry.isSet(entry.getLastModified())) {
                entry.setLastModified(predecessor.lastModified(file));
            }

            return entry.getLastModified();
        } catch (MaxDepthReachedException e) {
            return predecessor.lastModified(file);
        }
    }

    public long length(File file) {
        try {
            DataEntry entry = getOrCreateDataEntry(file, true, false);
            if (entry == null) {
                return 0;
            }

            if (! entry.isSet(entry.getLength())) {
                entry.setLength(predecessor.length(file));
            }

            return entry.getLength();
        } catch (MaxDepthReachedException e) {
            return predecessor.length(file);
        }
    }

    public String[] list(File file, FilenameFilter filter) {
        File[] files = this.listFiles(file, filter);
        if (files == null) {
            return null;
        }

        String[] ret = new String[files.length];
        for (int i=0; i<files.length; i++) {
            ret[i] = normalize(files[i].getAbsolutePath());
        }

        return ret;
    }

    public String[] list(File file) {
        File[] files = this.listFiles(file);
        if (files == null) {
            return null;
        }

        String[] ret = new String[files.length];
        for (int i=0; i<files.length; i++) {
            ret[i] = normalize(files[i].getAbsolutePath());
        }

        return ret;
    }

    public File[] listFiles(File file, FileFilter filter) {
        try {
            return lst(file, filter);
        } catch (MaxDepthReachedException e) {
            return predecessor.listFiles(file, filter);
        }
    }

    public File[] listFiles(File file, FilenameFilter filter) {
        try {
            return lst(file, filter);
        } catch (MaxDepthReachedException e) {
            return predecessor.listFiles(file, filter);
        }
    }

    public File[] listFiles(File file) {
        try {
            return lst(file, null);
        } catch (MaxDepthReachedException e) {
            return predecessor.listFiles(file);
        }
    }

    private File[] lst(File file, Object filter) throws MaxDepthReachedException {
        DataEntry entry = getOrCreateDataEntry(file, false, false);
        if (entry == null) {
            return null;
        }

        if (! entry.isPopulated(filter)) {
            File[] children = null;
            if (filter == null) {
                children = predecessor.listFiles(file);
            } else if (filter instanceof FilenameFilter) {
                children = predecessor.listFiles(file, (FilenameFilter)filter);
            } else {
                children = predecessor.listFiles(file, (FileFilter)filter);
            }

            if (children != null) {
                for (int i=0; i<children.length; i++) {
                    DataEntry chld = this.getOrCreateDataEntry(children[i], true, true);
                    chld.setExists(true);
                }
            }
            
            if (children == null || children.length == 0) {
                // Special case : no children files and we have reached the
                //                       max depth -> in this case, a MaxDepthReachedException
                //                       should have been thrown, which was not the case since there
                //                       are no children ! -> raise it explicitely
                this.checkDepth(new File(file, "dummy"));
            }
            entry.setPopulated(filter);
        }

        Iterator iter = entry.getNames().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            DataEntry child = null;
            try {
                child = (DataEntry)entry.getEntry(name);
            } catch (NonExistingEntryException e) { /// Shall not happen
            }
            if (child.isExistsSet() && child.isExists()) {
                File f = new File(file, name);
                boolean accepted = false;
                if (filter == null) {
                    accepted = true;
                } else if (filter instanceof FilenameFilter) {
                    accepted = ((FilenameFilter)filter).accept(file, name);
                } else {
                    accepted = ((FileFilter)filter).accept(f);
                }

                if (accepted) {
                    list.add(f);
                }
            }
        }

        return (File[])list.toArray(new File[list.size()]);
    }

    public boolean mkdir(File file) {
        if (predecessor.mkdir(file)) {
            try {
                DataEntry entry = getOrCreateDataEntry(file, false, true);
                entry.setDirectory(true);
                entry.reset();
                entry.setExists(true);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean renameTo(File source, File dest) {
        if (predecessor.renameTo(source, dest)) {
            try {
                DataEntry s = getOrCreateDataEntry(source, false, false);
                DataEntry d = getOrCreateDataEntry(dest, false, true);

                d.update(s);
                s.reset();
                
                s.setExists(false);
                d.setExists(true);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setLastModified(File file, long time) {
        if (predecessor.setLastModified(file, time)) {
            try {
                DataEntry entry = getOrCreateDataEntry(file, false, false);
                entry.setLastModified(time);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setReadOnly(File file) {
        if (predecessor.setReadOnly(file)) {
            try {
                DataEntry entry = getOrCreateDataEntry(file, false, false);
                entry.setWritable(false);
            } catch (MaxDepthReachedException ignored) {
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean supportsLongFileNames() {
        return predecessor.supportsLongFileNames();
    }

    public void unmount() throws IOException {
        predecessor.unmount();
    }

    public FileInformations getInformations(File file) {
        return new FileInformations(this, file);
    }

    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.maxDepth);
        h = HashHelper.hash(h, this.root);
        h = HashHelper.hash(h, this.predecessor);
        return h;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof CachedFileSystemDriver) {
            CachedFileSystemDriver other = (CachedFileSystemDriver)o;

            return (
                    EqualsHelper.equals(other.maxDepth, this.maxDepth) 
                    && EqualsHelper.equals(other.root, this.root) 
                    && EqualsHelper.equals(other.predecessor, this.predecessor) 
            );
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Root", this.root, sb);
        ToStringHelper.append("Max depth", this.maxDepth, sb);
        ToStringHelper.append("Predecessor", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }
    
    /**
     * Returns null if the entry /file does not exist
     * Returns an initialized entry otherwise
     */
    private synchronized DataEntry getOrCreateDataEntry(File file, boolean init, boolean forceCreation) throws MaxDepthReachedException {
        String path = predecessor.getAbsolutePath(file);
        DataEntry entry = null;
        try {
            entry = cache.lookup(path);
        } catch (NonExistingEntryException e) {
            if (! forceCreation) {
                return null;
            }
        }

        if (entry == null) {
            entry = cache.put(path);
            if (init) {
                initEntry(file, entry);
            }
        }

        return entry;
    }
    
    private void checkDepth(File file) throws MaxDepthReachedException {
        String path = predecessor.getAbsolutePath(file);
        cache.checkDepth(path);
    }

    /**
     * Returns null if the entry/file does not exist or if it is not contained in the cache
     */
    private DataEntry getDataEntry(File file) throws MaxDepthReachedException {
        String path = predecessor.getAbsolutePath(file);
        DataEntry entry = null;
        try {
            entry = cache.lookup(path);
        } catch (NonExistingEntryException e) {
            return null;
        }

        return entry;
    }

    private void initEntry(File file, DataEntry entry) {
        FileInformations infos = predecessor.getInformations(file);
        if (infos.isDirectorySet()) {
            entry.setDirectory(infos.isDirectory());
        }

        if (infos.isExistsSet()) {
            entry.setExists(infos.isExists());
        }

        if (infos.isLastModifiedSet()) {
            entry.setLastModified(infos.getLastModified());
        }

        if (infos.isLengthSet()) {
            entry.setLength(infos.getLength());
        }

        if (infos.isReadableSet()) {
            entry.setReadable(infos.isReadable());
        }

        if (infos.isWritableSet()) {
            entry.setWritable(infos.isWritable());
        }
    }
}