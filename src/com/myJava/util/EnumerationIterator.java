package com.myJava.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * 
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class EnumerationIterator implements Iterator {

    private Enumeration en;

    public EnumerationIterator() {
        this.en = null;
    }

    public EnumerationIterator(Enumeration en) {
        this.en = en;
    }
    
    public boolean hasNext() {
        return en != null && en.hasMoreElements();
    }
    
    public Object next() {
        return en.nextElement();
    }
    
    public void remove() {
        throw new UnsupportedOperationException("'remove' not supported.");
    }
}
