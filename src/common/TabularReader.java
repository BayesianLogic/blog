/*
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package common;

import java.io.*;
import java.util.regex.*;

/**
 * Reads tabular files, that is, files where each line represents a row, and 
 * each row is broken up into fields (columns) by separator characters.  For 
 * example, a comma-separated values (CSV) file is a tabular file where the 
 * separator character is a comma.  Other common separator characters are 
 * tabs and colons.  
 *
 * A TabularReader supports reading one line at a time.  As with
 * BufferedReader.readLine, a line is terminated by "\r", "\n", or
 * "\r\n" (or the end of the file).  Each line is returned in the form
 * of an array of Strings, where each String is a field.  Fields may
 * be empty (if a line contains two consecutive separator characters)
 * and the number of fields may vary from line to line.  
 *
 * By default, a TabularReader handles empty lines just like any other 
 * line (a blank line yields a zero-length array of fields).  But if 
 * you use the three-argument constructor with <code>skipEmptyLines</code> 
 * set to <code>true</code>, it behaves as if empty lines were not in 
 * the file.  
 */
public class TabularReader {
    /**
     * Create a new TabularReader that reads from the given input stream 
     * and uses the given separator character.
     */
    public TabularReader(InputStream stream, char sep) 
           throws java.io.IOException {
	input = new BufferedReader(new InputStreamReader(stream));
	sepPat = Pattern.compile(String.valueOf(sep));

	loadNextLine();
    }

    /**
     * Create a new TabularReader that reads from the given input stream 
     * and uses the given separator character.  If <code>skipEmptyLines</code> 
     * is true, this reader will act as if empty lines were not in the file.  
     */
    public TabularReader(InputStream stream, char sep, boolean skipEmptyLines) 
           throws java.io.IOException {
	input = new BufferedReader(new InputStreamReader(stream));
	sepPat = Pattern.compile(String.valueOf(sep));
	this.skipEmptyLines = skipEmptyLines;

	loadNextLine();
    }
    /**
     * Close the underlying stream.
     */
    public void close() throws java.io.IOException {
	input.close();
    }

    /**
     * Returns true if there is another line to read from the input stream.
     */
    public boolean ready() {
	return (nextLine != null);
    }

    /**
     * Returns an array of strings representing the fields of the next 
     * line in the input stream, and moves on to the line after that.  
     */
    public String[] readLine() throws java.io.IOException {
	if (!ready()) {
	    throw new IOException("No more lines for TabularReader to read.");
	}

	String[] toReturn = (String[]) nextLine.clone(); 
	loadNextLine();
	return toReturn;
    }

    /**
     * Returns an array of strings representing the fields of the next 
     * line in the input stream, but does not move on to the following 
     * line.
     */
    public String[] peek() throws java.io.IOException {
	if (!ready()) {
	    throw new IOException("No next line to peek at.");
	}
	
	return (String[]) nextLine.clone();
    }

    private void loadNextLine() throws java.io.IOException {
	nextLine = null;
	while (input.ready()) {
	    String line = input.readLine();
	    if (!(skipEmptyLines && (line.length() == 0))) {
		// Call split with limit of -1 to include trailing empty tokens
		nextLine = sepPat.split(line, -1); 
		break;
	    }
	}
    }

    private BufferedReader input;
    private Pattern sepPat;
    private String[] nextLine;
    private boolean skipEmptyLines = false;

    /**
     * Test program: given a filename, read it as a tabular file and 
     * print it in a comma-separated format.
     */
    public static void main(String[] args) {
	try {
	    File infile = new File(args[0]);
	    TabularReader reader 
		= new TabularReader(new FileInputStream(infile), ':');
	    
	    while (reader.ready()) {
		String[] row = reader.readLine();
		for (int i = 0; i < row.length; ++i) {
		    System.out.print(row[i]);
		    if (i < row.length - 1) {
			System.out.print(',');
		    }
		}
		System.out.println();
	    }

	    reader.close();
	} catch (java.io.IOException e) {
	    System.out.println(e.getMessage());
	}
    }
}
