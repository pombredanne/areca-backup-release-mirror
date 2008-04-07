package com.myJava.file.delta.sequence;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class FileSequencer {    
    private InputStream in;
    private FileSequencerByteProcessor proc;

    public FileSequencer(InputStream in, int blockSize) {
        this.in = in;
        this.proc = new FileSequencerByteProcessor(blockSize);
    }

    public HashSequence getHash() throws IOException, ByteProcessorException {
        proc.open();
        int read;
        while ((read = in.read()) != -1) {
            proc.processByte(read);
        }
        proc.close();
        return proc.getSequence();
    }

    public static void main(String[] args) {
        try {
            FileInputStream f = new FileInputStream("/home/olivier/Desktop/idees_areca.txt");
            FileSequencer s = new FileSequencer(f, 50);
            HashSequence seq = s.getHash();
            System.out.println(seq.toString());
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
