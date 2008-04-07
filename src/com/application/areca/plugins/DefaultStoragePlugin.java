package com.application.areca.plugins;


import com.application.areca.adapters.DefaultFileSystemPolicyXMLHandler;
import com.application.areca.version.VersionInfos;
import com.myJava.util.version.VersionData;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8363716858549252512
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
public class DefaultStoragePlugin
extends AbstractStoragePlugin 
implements StoragePlugin {
    public static final String PLG_NAME = "Local Hard Drive";
    public static final String PLG_ID = "hd";

    public DefaultStoragePlugin() {
        super();
        this.setId(PLG_ID);
    }

    public String getFullName() {
        return PLG_NAME;
    }

    public VersionData getVersionData() {
        return VersionInfos.getLastVersion();
    }
    
    public FileSystemPolicyXMLHandler buildFileSystemPolicyXMLHandler() {
        return new DefaultFileSystemPolicyXMLHandler();
    }
    
    public StorageSelectionHelper getStorageSelectionHelper() {
        return null;
    }
}
