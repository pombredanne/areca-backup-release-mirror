package com.application.areca.launcher.gui.menus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.application.areca.ResourceManager;

/**
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
public class MenuBuilder 
extends AppActionReferenceHolder {
    
    private static final ResourceManager RM = ResourceManager.instance();
    
    public static Menu buildMainMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.BAR);
        
        // WORKSPACE
        Menu mnWorkspace = buildSubMenu("menu.workspace", menu);
        add(AC_OPEN, mnWorkspace);
        add(AC_BACKUP_WS, mnWorkspace);
        addSeparator(mnWorkspace);
        add(AC_PREFERENCES, mnWorkspace);
        addSeparator(mnWorkspace);
        add(AC_EXIT, mnWorkspace);

        // EDIT
        Menu mnEdit = buildSubMenu("menu.edit", menu);
        add(AC_NEW_PROCESS, mnEdit);
        add(AC_EDIT_PROCESS, mnEdit);
        add(AC_DEL_PROCESS, mnEdit);
        addSeparator(mnEdit);
        add(AC_NEW_TARGET, mnEdit);
        add(AC_EDIT_TARGET, mnEdit);
        add(AC_DEL_TARGET, mnEdit);
        addSeparator(mnEdit);
        add(AC_DUP_TARGET, mnEdit);
        addSeparator(mnEdit);
        add(AC_BUILD_BATCH, mnEdit);
        
        // ACTION
        Menu mnRun = buildSubMenu("menu.run", menu);
        add(AC_SIMULATE, mnRun);
        add(AC_BACKUP, mnRun);
        add(AC_BACKUP_MANIFEST, mnRun);
        addSeparator(mnRun);
        add(AC_MERGE, mnRun);
        add(AC_DELETE_ARCHIVES, mnRun);
        addSeparator(mnRun);
        add(AC_RECOVER, mnRun);
      
        // HELP
        Menu mnHelp = buildSubMenu("menu.help", menu);
        add(AC_HELP, mnHelp);
        addSeparator(mnHelp);
        add(AC_ABOUT, mnHelp);    
        
        return menu;
    }
    
    public static Menu buildActionContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_MERGE, menu);
        add(AC_DELETE_ARCHIVES, menu);
        addSeparator(menu);
        add(AC_RECOVER, menu);
        addSeparator(menu);
        add(AC_VIEW_MANIFEST, menu);  

        return menu;
    }
    
    public static Menu buildProcessContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_BACKUP, menu);
        addSeparator(menu);
        add(AC_NEW_PROCESS, menu);      
        add(AC_EDIT_PROCESS, menu);                    
        add(AC_DEL_PROCESS, menu);
        addSeparator(menu);
        add(AC_NEW_TARGET, menu);  
        addSeparator(menu);
        add(AC_BUILD_BATCH, menu);  
        
        return menu;
    }
    
    public static Menu buildTargetContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);

        add(AC_SIMULATE, menu);
        add(AC_BACKUP, menu);
        add(AC_BACKUP_MANIFEST, menu);        
        addSeparator(menu);
        add(AC_NEW_TARGET, menu);    
        add(AC_EDIT_TARGET, menu);
        add(AC_DEL_TARGET, menu);
        addSeparator(menu);
        add(AC_DUP_TARGET, menu);   
        addSeparator(menu);
        add(AC_BUILD_BATCH, menu);  

        return menu;
    }
    
    public static Menu buildWorkspaceContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_NEW_PROCESS, menu);
        
        return menu;
    }
    
    public static Menu buildLogContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_CLEAR_LOG, menu);
        
        return menu;
    }
    
    public static Menu buildArchiveContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_FILTER, menu);
        
        return menu;
    }
    
    public static Menu buildArchiveContextMenuLogical(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_FILTER_LATEST, menu);
        
        return menu;
    }
    
    public static Menu buildHistoryContextMenu(Shell parent) {
        Menu menu = new Menu(parent, SWT.POP_UP);
        
        add(AC_RECOVER_HISTORY, menu);
        add(AC_TEXTEDIT_HISTORY, menu);
        
        return menu;
    }
    
    private static Menu buildSubMenu(String resourceKeyPrefix, Menu parent) {
        MenuItem menuItem = new MenuItem(parent, SWT.CASCADE);
        
        String str = RM.getLabel(resourceKeyPrefix + ".label");
        char mnemonic = RM.getChar(resourceKeyPrefix + ".mnemonic");
        
        str = AppAction.addMnemonic(str, mnemonic);

        menuItem.setText(str);
        
        Menu menu = new Menu(menuItem);
        menuItem.setMenu(menu);
        return menu;
    }
    
    private static void addSeparator(Menu menu) {
        new MenuItem(menu, SWT.SEPARATOR);
    }
    
    private static void add(AppAction action, Menu parent) {
        action.addToMenu(parent);
    }
}
