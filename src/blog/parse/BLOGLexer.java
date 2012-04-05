package blog.parse;

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
import blog.common.ScannerWithLocInfo;
import java_cup.runtime.Symbol;

public class BLOGLexer implements ScannerWithLocInfo {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

	// For assembling string and character constants
	StringBuffer string_buf = new StringBuffer();

	// For line numbers
	public int getCurLineNum() {
		return (yyline + 1);
	}

	private String filename;

	public void setFilename(String fname) {
		filename = fname;
	}

	public String getCurFilename() {
		return filename;
	}

	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	public BLOGLexer(java.io.Reader reader) {
		this();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	public BLOGLexer(java.io.InputStream instream) {
		this();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(
				instream));
	}

	private BLOGLexer() {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;

		// empty for now
	}

	private boolean yy_eof_done = false;
	private final int CHAR_LIT = 4;
	private final int LINE_COMMENT = 1;
	private final int YYINITIAL = 0;
	private final int STR_LIT = 3;
	private final int PAREN_COMMENT = 2;
	private final int yy_state_dtrans[] = { 0, 97, 99, 101, 107 };

	private void yybegin(int state) {
		yy_lexical_state = state;
	}

	private int yy_advance() throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer, yy_buffer_read, yy_buffer.length
					- yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer, yy_buffer_read, yy_buffer.length
					- yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}

	private void yy_move_end() {
		if (yy_buffer_end > yy_buffer_start && '\n' == yy_buffer[yy_buffer_end - 1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start && '\r' == yy_buffer[yy_buffer_end - 1])
			yy_buffer_end--;
	}

	private boolean yy_last_was_cr = false;

	private void yy_mark_start() {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr = true;
			} else
				yy_last_was_cr = false;
		}
		yy_buffer_start = yy_buffer_index;
	}

	private void yy_mark_end() {
		yy_buffer_end = yy_buffer_index;
	}

	private void yy_to_mark() {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start)
				&& ('\r' == yy_buffer[yy_buffer_end - 1]
						|| '\n' == yy_buffer[yy_buffer_end - 1]
						|| 2028/* LS */== yy_buffer[yy_buffer_end - 1] || 2029/* PS */== yy_buffer[yy_buffer_end - 1]);
	}

	private java.lang.String yytext() {
		return (new java.lang.String(yy_buffer, yy_buffer_start, yy_buffer_end
				- yy_buffer_start));
	}

	private int yylength() {
		return yy_buffer_end - yy_buffer_start;
	}

	private char[] yy_double(char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2 * buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}

	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = { "Error: Internal error.\n",
			"Error: Unmatched input.\n" };

	private void yy_error(int code, boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}

	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex == -1) ? st : st.substring(0, commaIndex);
				st = st.substring(commaIndex + 1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j] = Integer.parseInt(workString);
					continue;
				}
				lengthString = workString.substring(colonIndex + 1);
				sequenceLength = Integer.parseInt(lengthString);
				workString = workString.substring(0, colonIndex);
				sequenceInteger = Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}

	private int yy_acpt[] = {
	/* 0 */YY_NOT_ACCEPT,
	/* 1 */YY_NO_ANCHOR,
	/* 2 */YY_NO_ANCHOR,
	/* 3 */YY_NO_ANCHOR,
	/* 4 */YY_NO_ANCHOR,
	/* 5 */YY_NO_ANCHOR,
	/* 6 */YY_NO_ANCHOR,
	/* 7 */YY_NO_ANCHOR,
	/* 8 */YY_NO_ANCHOR,
	/* 9 */YY_NO_ANCHOR,
	/* 10 */YY_NO_ANCHOR,
	/* 11 */YY_NO_ANCHOR,
	/* 12 */YY_NO_ANCHOR,
	/* 13 */YY_NO_ANCHOR,
	/* 14 */YY_NO_ANCHOR,
	/* 15 */YY_NO_ANCHOR,
	/* 16 */YY_NO_ANCHOR,
	/* 17 */YY_NO_ANCHOR,
	/* 18 */YY_NO_ANCHOR,
	/* 19 */YY_NO_ANCHOR,
	/* 20 */YY_NO_ANCHOR,
	/* 21 */YY_NO_ANCHOR,
	/* 22 */YY_NO_ANCHOR,
	/* 23 */YY_NO_ANCHOR,
	/* 24 */YY_NO_ANCHOR,
	/* 25 */YY_NO_ANCHOR,
	/* 26 */YY_NO_ANCHOR,
	/* 27 */YY_NO_ANCHOR,
	/* 28 */YY_NO_ANCHOR,
	/* 29 */YY_NO_ANCHOR,
	/* 30 */YY_NO_ANCHOR,
	/* 31 */YY_NO_ANCHOR,
	/* 32 */YY_NO_ANCHOR,
	/* 33 */YY_NO_ANCHOR,
	/* 34 */YY_NO_ANCHOR,
	/* 35 */YY_NO_ANCHOR,
	/* 36 */YY_NO_ANCHOR,
	/* 37 */YY_NO_ANCHOR,
	/* 38 */YY_NO_ANCHOR,
	/* 39 */YY_NO_ANCHOR,
	/* 40 */YY_NO_ANCHOR,
	/* 41 */YY_NO_ANCHOR,
	/* 42 */YY_NO_ANCHOR,
	/* 43 */YY_NO_ANCHOR,
	/* 44 */YY_NO_ANCHOR,
	/* 45 */YY_NO_ANCHOR,
	/* 46 */YY_NO_ANCHOR,
	/* 47 */YY_NO_ANCHOR,
	/* 48 */YY_NO_ANCHOR,
	/* 49 */YY_NO_ANCHOR,
	/* 50 */YY_NO_ANCHOR,
	/* 51 */YY_NO_ANCHOR,
	/* 52 */YY_NO_ANCHOR,
	/* 53 */YY_NO_ANCHOR,
	/* 54 */YY_NO_ANCHOR,
	/* 55 */YY_NO_ANCHOR,
	/* 56 */YY_NO_ANCHOR,
	/* 57 */YY_NO_ANCHOR,
	/* 58 */YY_NO_ANCHOR,
	/* 59 */YY_NO_ANCHOR,
	/* 60 */YY_NO_ANCHOR,
	/* 61 */YY_NO_ANCHOR,
	/* 62 */YY_NO_ANCHOR,
	/* 63 */YY_NO_ANCHOR,
	/* 64 */YY_NO_ANCHOR,
	/* 65 */YY_NO_ANCHOR,
	/* 66 */YY_NO_ANCHOR,
	/* 67 */YY_NO_ANCHOR,
	/* 68 */YY_NO_ANCHOR,
	/* 69 */YY_NO_ANCHOR,
	/* 70 */YY_NO_ANCHOR,
	/* 71 */YY_NO_ANCHOR,
	/* 72 */YY_NO_ANCHOR,
	/* 73 */YY_NO_ANCHOR,
	/* 74 */YY_NO_ANCHOR,
	/* 75 */YY_NO_ANCHOR,
	/* 76 */YY_NO_ANCHOR,
	/* 77 */YY_NO_ANCHOR,
	/* 78 */YY_NOT_ACCEPT,
	/* 79 */YY_NO_ANCHOR,
	/* 80 */YY_NO_ANCHOR,
	/* 81 */YY_NO_ANCHOR,
	/* 82 */YY_NO_ANCHOR,
	/* 83 */YY_NO_ANCHOR,
	/* 84 */YY_NO_ANCHOR,
	/* 85 */YY_NOT_ACCEPT,
	/* 86 */YY_NO_ANCHOR,
	/* 87 */YY_NO_ANCHOR,
	/* 88 */YY_NOT_ACCEPT,
	/* 89 */YY_NO_ANCHOR,
	/* 90 */YY_NO_ANCHOR,
	/* 91 */YY_NOT_ACCEPT,
	/* 92 */YY_NO_ANCHOR,
	/* 93 */YY_NOT_ACCEPT,
	/* 94 */YY_NO_ANCHOR,
	/* 95 */YY_NOT_ACCEPT,
	/* 96 */YY_NO_ANCHOR,
	/* 97 */YY_NOT_ACCEPT,
	/* 98 */YY_NO_ANCHOR,
	/* 99 */YY_NOT_ACCEPT,
	/* 100 */YY_NO_ANCHOR,
	/* 101 */YY_NOT_ACCEPT,
	/* 102 */YY_NO_ANCHOR,
	/* 103 */YY_NOT_ACCEPT,
	/* 104 */YY_NO_ANCHOR,
	/* 105 */YY_NOT_ACCEPT,
	/* 106 */YY_NO_ANCHOR,
	/* 107 */YY_NOT_ACCEPT,
	/* 108 */YY_NO_ANCHOR,
	/* 109 */YY_NO_ANCHOR,
	/* 110 */YY_NO_ANCHOR,
	/* 111 */YY_NO_ANCHOR,
	/* 112 */YY_NO_ANCHOR,
	/* 113 */YY_NO_ANCHOR,
	/* 114 */YY_NO_ANCHOR,
	/* 115 */YY_NO_ANCHOR,
	/* 116 */YY_NO_ANCHOR,
	/* 117 */YY_NO_ANCHOR,
	/* 118 */YY_NO_ANCHOR,
	/* 119 */YY_NOT_ACCEPT,
	/* 120 */YY_NOT_ACCEPT,
	/* 121 */YY_NO_ANCHOR,
	/* 122 */YY_NO_ANCHOR,
	/* 123 */YY_NO_ANCHOR,
	/* 124 */YY_NO_ANCHOR,
	/* 125 */YY_NO_ANCHOR,
	/* 126 */YY_NO_ANCHOR,
	/* 127 */YY_NO_ANCHOR,
	/* 128 */YY_NO_ANCHOR,
	/* 129 */YY_NO_ANCHOR,
	/* 130 */YY_NO_ANCHOR,
	/* 131 */YY_NO_ANCHOR,
	/* 132 */YY_NO_ANCHOR,
	/* 133 */YY_NO_ANCHOR,
	/* 134 */YY_NO_ANCHOR,
	/* 135 */YY_NO_ANCHOR,
	/* 136 */YY_NO_ANCHOR,
	/* 137 */YY_NO_ANCHOR,
	/* 138 */YY_NO_ANCHOR,
	/* 139 */YY_NO_ANCHOR,
	/* 140 */YY_NO_ANCHOR,
	/* 141 */YY_NO_ANCHOR,
	/* 142 */YY_NO_ANCHOR,
	/* 143 */YY_NO_ANCHOR,
	/* 144 */YY_NO_ANCHOR,
	/* 145 */YY_NO_ANCHOR,
	/* 146 */YY_NO_ANCHOR,
	/* 147 */YY_NO_ANCHOR,
	/* 148 */YY_NO_ANCHOR,
	/* 149 */YY_NO_ANCHOR,
	/* 150 */YY_NO_ANCHOR,
	/* 151 */YY_NO_ANCHOR,
	/* 152 */YY_NO_ANCHOR,
	/* 153 */YY_NO_ANCHOR,
	/* 154 */YY_NO_ANCHOR,
	/* 155 */YY_NO_ANCHOR,
	/* 156 */YY_NO_ANCHOR,
	/* 157 */YY_NO_ANCHOR,
	/* 158 */YY_NO_ANCHOR,
	/* 159 */YY_NO_ANCHOR,
	/* 160 */YY_NO_ANCHOR,
	/* 161 */YY_NO_ANCHOR,
	/* 162 */YY_NO_ANCHOR,
	/* 163 */YY_NO_ANCHOR,
	/* 164 */YY_NO_ANCHOR,
	/* 165 */YY_NO_ANCHOR,
	/* 166 */YY_NO_ANCHOR,
	/* 167 */YY_NO_ANCHOR,
	/* 168 */YY_NO_ANCHOR,
	/* 169 */YY_NO_ANCHOR,
	/* 170 */YY_NO_ANCHOR,
	/* 171 */YY_NO_ANCHOR,
	/* 172 */YY_NO_ANCHOR,
	/* 173 */YY_NO_ANCHOR,
	/* 174 */YY_NO_ANCHOR,
	/* 175 */YY_NO_ANCHOR,
	/* 176 */YY_NO_ANCHOR,
	/* 177 */YY_NO_ANCHOR };
	private int yy_cmap[] = unpackFromString(
			1,
			130,
			"5:9,1,4,5,1,4,5:18,1,43,6,57,5:2,44,7,51,52,3,22,47,18,20,2,15:4,14:4,19:2,"
					+ "50,49,58,42,48,5,23,28,17,37,30,21,36,33,38,34,60:2,39,32,29,31,26,41,27,40"
					+ ",24,35,60:2,59,25,60,55,8,56,5,61,5,28,9,37,30,21,12,33,38,34,60:2,39,32,11"
					+ ",31,26,41,13,40,10,16,60:2,59,25,60,54,45,53,46,5,0:2")[0];

	private int yy_rmap[] = unpackFromString(
			1,
			178,
			"0,1:2,2,1:2,3,4,5,1,6,1:4,7,1:9,8,1:3,9,10,3,1:3,11,12,13,3:4,14,15,3:12,1:"
					+ "17,16,1:4,17,1,18,19,20,21,22,23,24,25,26,10,27,13,28,29,30,14,31,32,33,34,"
					+ "35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,5,54,55,56,57,58,5"
					+ "9,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,8"
					+ "4,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,"
					+ "107,108,109,110,111")[0];

	private int yy_nxt[][] = unpackFromString(
			112,
			62,
			"1,2,3,79,2,79,4,5,79,6,137,173,116,160,7:2,6:2,86,7,8,139,79,89,140,6,174,1"
					+ "60,6,173,6,121,6,177,80,6,138,6:4,151,9,10,11,12,13,14,15,16,17,18,19,20,21"
					+ ",22,23,24,25,6:2,79,-1:64,26,27,-1:67,6:9,-1,6,78,6,-1:2,6:18,-1:17,6:3,-1:"
					+ "14,7:2,-1:3,7,85,88,-1:54,29:2,-1:3,29,-1:84,32,-1:61,33,-1:61,34,-1:33,29:"
					+ "2,-1:3,29,-1,93,-1:54,30:2,-1:3,30,-1:51,35:9,-1,35,78,35,-1:2,35:18,-1:17,"
					+ "35:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,129,6:13,-1:17,6:3,-1:14,37:2,-1:3,37,-1:5"
					+ "6,42:2,-1:3,42,-1:51,6:9,-1,6,78,6,-1:2,6:10,109,6:7,-1:17,6:3,-1:14,74:2,-"
					+ "1:55,35:5,-1:2,35:2,-1:3,35,-1:2,35:18,-1:17,35:2,-1:10,6:3,31,6:5,-1,6,78,"
					+ "6,-1:2,6:12,31,6:5,-1:17,6:3,-1:2,60,-1:60,64:3,-1,64,65,66,67,68,69,70,71,"
					+ "72,73,118,83,64:45,-1:9,103,-1:2,103,-1,103:2,-1,103,-1,103,-1,103,-1:6,103"
					+ ",-1,103,-1:5,103:2,-1:38,75:2,-1:60,117:2,-1:3,117,-1:56,7:2,-1:3,7,119,-1:"
					+ "27,28,-1:22,6:4,36,6:4,-1,6,78,6,-1:2,6:3,36,6:14,-1:17,6:3,-1:14,37:2,-1:2"
					+ ",91,37,-1:2,91,-1:48,6:9,-1,6,78,6,-1:2,6:16,38,6,-1:17,6:3,-1:9,6:9,-1,6,7"
					+ "8,39,-1:2,6:18,-1:17,6:3,-1:14,42:2,-1:2,95,42,-1:2,95,-1:48,6:9,-1,6,78,40"
					+ ",-1:2,6:18,-1:17,6:3,-1:9,6:2,41,6:6,-1,6,78,6,-1:2,6:5,41,6:12,-1:17,6:3,1"
					+ ",56:3,57,56:57,-1:9,6:9,-1,6,78,43,-1:2,6:18,-1:17,6:3,1,58:2,81,59,58:57,-"
					+ "1:9,6:9,-1,6,78,44,-1:2,6:18,-1:17,6:3,1,61:3,62,61,63,61,82,61:53,-1:9,6:9"
					+ ",-1,6,78,6,-1:2,6,45,6:16,-1:17,6:3,-1:9,120,-1:2,120,-1,120:2,-1,120,-1,12"
					+ "0,-1,120,-1:6,120,-1,120,-1:5,120:2,-1:33,6:4,46,6:4,-1,6,78,6,-1:2,6:3,46,"
					+ "6:14,-1:17,6:3,-1:9,76,-1:2,76,-1,76:2,-1,76,-1,76,-1,76,-1:6,76,-1,76,-1:5"
					+ ",76:2,-1:33,6:9,-1,6,78,6,-1:2,6:15,47,6:2,-1:17,6:3,1,61:3,62,61:2,77,82,6"
					+ "1:53,-1:9,6:9,-1,6,78,6,-1:2,6:8,48,6:9,-1:17,6:3,-1:9,6:3,49,6:5,-1,6,78,6"
					+ ",-1:2,6:12,49,6:5,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:16,50,6,-1:17,6:3,-1:"
					+ "9,6:2,51,6:6,-1,6,78,6,-1:2,6:5,51,6:12,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6"
					+ ":8,52,6:9,-1:17,6:3,-1:9,6:4,53,6:4,-1,6,78,6,-1:2,6:3,53,6:14,-1:17,6:3,-1"
					+ ":9,6:9,-1,6,78,6,-1:2,6:6,54,6:11,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:9,55,"
					+ "6:8,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,141,6:2,87,6:10,-1:17,6:3,-1:14,1"
					+ "17:2,-1:3,117,-1,88,-1:54,84:2,-1:55,105,-1:2,105,-1,105:2,-1,105,-1,105,-1"
					+ ",105,-1:6,105,-1,105,-1:5,105:2,-1:33,90,6:3,154,6:3,90,-1,6,78,6,-1:2,6:3,"
					+ "154,6:14,-1:17,6:3,-1:9,6:7,92,6,-1,6,78,6,-1:2,6:11,92,6:6,-1:17,6:3,-1:9,"
					+ "6:9,-1,6,78,6,-1:2,6:2,94,6:15,-1:17,6:3,-1:9,6:9,-1,6,78,96,-1:2,6:18,-1:1"
					+ "7,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:16,98,6,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6"
					+ ":16,100,6,-1:17,6:3,-1:9,6:4,102,6:4,-1,6,78,6,-1:2,6:3,102,6:14,-1:17,6:3,"
					+ "-1:9,6:9,-1,6,78,6,-1:2,6:7,104,6:10,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:15"
					+ ",106,6:2,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:7,108,6:10,-1:17,6:3,-1:9,6,11"
					+ "0,6:7,-1,6,78,6,-1:2,110,6:17,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:10,111,6:"
					+ "7,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:7,112,6:10,-1:17,6:3,-1:9,6:9,-1,6,78"
					+ ",6,-1:2,6:7,113,6:10,-1:17,6:3,-1:9,6:9,-1,6,78,114,-1:2,6:18,-1:17,6:3,-1:"
					+ "9,6:2,115,6:6,-1,6,78,6,-1:2,6:5,115,6:12,-1:17,6:3,-1:9,6:4,122,6:4,-1,6,7"
					+ "8,6,-1:2,6,123,6,122,6:10,124,6:3,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,155"
					+ ",6:2,87,6:10,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:15,125,6:2,-1:17,153,6:2,-"
					+ "1:9,6:9,-1,6,78,6,-1:2,6,123,6:12,124,6:3,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2"
					+ ",6:13,143,6,126,6:2,-1:17,6:3,-1:9,6:9,-1,6,78,127,-1:2,6:18,-1:17,6:3,-1:9"
					+ ",6,128,6:7,-1,6,78,6,-1:2,128,6:17,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:6,13"
					+ "0,6:11,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:16,131,6,-1:17,6:3,-1:9,6:9,-1,6"
					+ ",78,6,-1:2,6:9,132,6:8,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:6,133,6:11,-1:17"
					+ ",6:3,-1:9,6,134,6:7,-1,6,78,6,-1:2,134,6:17,-1:17,6:3,-1:9,6:9,-1,6,78,135,"
					+ "-1:2,6:18,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:10,136,6:7,-1:17,6:3,-1:9,6:7"
					+ ",142,6,-1,6,78,6,-1:2,6:11,142,6:6,-1:17,6:3,-1:9,6:2,144,6:6,-1,6,78,6,-1:"
					+ "2,6:5,144,6:12,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:10,145,6:7,-1:17,6:3,-1:"
					+ "9,6:9,-1,6,78,6,-1:2,6:10,146,6:7,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:13,14"
					+ "3,6:4,-1:17,6:3,-1:9,6:2,147,6:6,-1,6,78,6,-1:2,6:5,147,6:12,-1:17,6:3,-1:9"
					+ ",6:9,-1,6,78,6,-1:2,6:13,148,6:4,-1:17,6:3,-1:9,6,149,6:7,-1,6,78,6,-1:2,14"
					+ "9,6:17,-1:17,6:3,-1:9,6,150,6:7,-1,6,78,6,-1:2,150,6:17,-1:17,6:3,-1:9,6:9,"
					+ "-1,6,78,6,-1:2,6:4,152,6:13,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,156,6:13,"
					+ "-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,157,6:13,-1:17,6:3,-1:9,6:2,158,6:6,-"
					+ "1,6,78,6,-1:2,6:5,158,6:12,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,159,6:13,-"
					+ "1:17,6:3,-1:9,6:4,161,6:4,-1,6,78,6,-1:2,6:3,161,6:14,-1:17,6:3,-1:9,6:3,16"
					+ "2,6:5,-1,6,78,6,-1:2,6:12,162,6:5,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,163"
					+ ",6:13,-1:17,6:3,-1:9,6:4,164,6:4,-1,6,78,6,-1:2,6:3,164,6:14,-1:17,6:3,-1:9"
					+ ",6:2,165,6:6,-1,6,78,6,-1:2,6:5,165,6:12,-1:17,6:3,-1:9,6:4,166,6:4,-1,6,78"
					+ ",6,-1:2,6:3,166,6:14,-1:17,6:3,-1:9,6:4,167,6:4,-1,6,78,6,-1:2,6:3,167,6:14"
					+ ",-1:17,6:3,-1:9,6:9,-1,6,78,168,-1:2,6:18,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2"
					+ ",6:7,169,6:10,-1:17,6:3,-1:9,6:9,-1,6,78,6,-1:2,6:4,170,6:13,-1:17,6:3,-1:9"
					+ ",6:9,-1,6,78,6,-1:2,6:4,171,6:13,-1:17,6:3,-1:9,6:2,172,6:6,-1,6,78,6,-1:2,"
					+ "6:5,172,6:12,-1:17,6:3,-1:9,6:7,175,6,-1,6,78,176,-1:2,6:11,175,6:6,-1:17,6"
					+ ":3");

	public java_cup.runtime.Symbol next_token() throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol)
				yy_lookahead = YY_BOL;
			else
				yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

				switch (yy_lexical_state) {
				case YYINITIAL:
					/* nothing special to do in the initial state */
					break;
				case PAREN_COMMENT:
					return new Symbol(BLOGTokenConstants.ERROR,
							"File ended before comment was terminated.");
				case STR_LIT:
				case CHAR_LIT:
					return new Symbol(BLOGTokenConstants.ERROR,
							"File ended before string or character literal "
									+ "was terminated.");
				}
				/* Reinitialize everything before signaling EOF */
				string_buf = new StringBuffer();
				yybegin(YYINITIAL);
				return new Symbol(BLOGTokenConstants.EOF);
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			} else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				} else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:

					case -2:
						break;
					case 2: { /* Do nothing */
					}
					case -3:
						break;
					case 3: {
						return new Symbol(BLOGTokenConstants.ERROR, yytext());
					}
					case -4:
						break;
					case 4: {
						yybegin(STR_LIT);
					}
					case -5:
						break;
					case 5: {
						yybegin(CHAR_LIT);
					}
					case -6:
						break;
					case 6: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -7:
						break;
					case 7: { /* Integers */
						return new Symbol(BLOGTokenConstants.INT_CONST, yytext());
					}
					case -8:
						break;
					case 8: {
						return new Symbol(BLOGTokenConstants.DOT);
					}
					case -9:
						break;
					case 9: {
						return new Symbol(BLOGTokenConstants.EQ);
					}
					case -10:
						break;
					case 10: {
						return new Symbol(BLOGTokenConstants.NEG);
					}
					case -11:
						break;
					case 11: {
						return new Symbol(BLOGTokenConstants.AND);
					}
					case -12:
						break;
					case 12: {
						return new Symbol(BLOGTokenConstants.OR);
					}
					case -13:
						break;
					case 13: {
						return new Symbol(BLOGTokenConstants.DISTRIB);
					}
					case -14:
						break;
					case 14: {
						return new Symbol(BLOGTokenConstants.COMMA);
					}
					case -15:
						break;
					case 15: {
						return new Symbol(BLOGTokenConstants.GT);
					}
					case -16:
						break;
					case 16: {
						return new Symbol(BLOGTokenConstants.SEMI);
					}
					case -17:
						break;
					case 17: {
						return new Symbol(BLOGTokenConstants.COLON);
					}
					case -18:
						break;
					case 18: {
						return new Symbol(BLOGTokenConstants.LPAREN);
					}
					case -19:
						break;
					case 19: {
						return new Symbol(BLOGTokenConstants.RPAREN);
					}
					case -20:
						break;
					case 20: {
						return new Symbol(BLOGTokenConstants.RBRACE);
					}
					case -21:
						break;
					case 21: {
						return new Symbol(BLOGTokenConstants.LBRACE);
					}
					case -22:
						break;
					case 22: {
						return new Symbol(BLOGTokenConstants.LBRACKET);
					}
					case -23:
						break;
					case 23: {
						return new Symbol(BLOGTokenConstants.RBRACKET);
					}
					case -24:
						break;
					case 24: {
						return new Symbol(BLOGTokenConstants.NUMSIGN);
					}
					case -25:
						break;
					case 25: {
						return new Symbol(BLOGTokenConstants.LT);
					}
					case -26:
						break;
					case 26: {
						yybegin(LINE_COMMENT);
					}
					case -27:
						break;
					case 27: {
						yybegin(PAREN_COMMENT);
					}
					case -28:
						break;
					case 28: {
						return new Symbol(BLOGTokenConstants.RIGHTARROW);
					}
					case -29:
						break;
					case 29: {
						return new Symbol(BLOGTokenConstants.DOUBLE_CONST, yytext());
					}
					case -30:
						break;
					case 30: {
						return new Symbol(BLOGTokenConstants.TIME_CONST, yytext());
					}
					case -31:
						break;
					case 31: {
						return new Symbol(BLOGTokenConstants.IF);
					}
					case -32:
						break;
					case 32: {
						return new Symbol(BLOGTokenConstants.NEQ);
					}
					case -33:
						break;
					case 33: {
						return new Symbol(BLOGTokenConstants.GEQ);
					}
					case -34:
						break;
					case 34: {
						return new Symbol(BLOGTokenConstants.LEQ);
					}
					case -35:
						break;
					case 35: {
						return new Symbol(BLOGTokenConstants.CLASS_NAME, yytext());
					}
					case -36:
						break;
					case 36: {
						return new Symbol(BLOGTokenConstants.FOR);
					}
					case -37:
						break;
					case 37: {
						return new Symbol(BLOGTokenConstants.DOUBLE_CONST, yytext());
					}
					case -38:
						break;
					case 38: {
						return new Symbol(BLOGTokenConstants.OBS);
					}
					case -39:
						break;
					case 39: {
						return new Symbol(BLOGTokenConstants.TRUE);
					}
					case -40:
						break;
					case 40: {
						return new Symbol(BLOGTokenConstants.TYPE);
					}
					case -41:
						break;
					case 41: {
						return new Symbol(BLOGTokenConstants.THEN);
					}
					case -42:
						break;
					case 42: {
						return new Symbol(BLOGTokenConstants.DOUBLE_CONST, yytext());
					}
					case -43:
						break;
					case 43: {
						return new Symbol(BLOGTokenConstants.ELSE);
					}
					case -44:
						break;
					case 44: {
						return new Symbol(BLOGTokenConstants.FALSE);
					}
					case -45:
						break;
					case 45: {
						return new Symbol(BLOGTokenConstants.QUERY);
					}
					case -46:
						break;
					case 46: {
						return new Symbol(BLOGTokenConstants.FACTOR);
					}
					case -47:
						break;
					case 47: { /* universal quantifier */
						return new Symbol(BLOGTokenConstants.FORALL);
					}
					case -48:
						break;
					case 48: {
						return new Symbol(BLOGTokenConstants.RANDOM);
					}
					case -49:
						break;
					case 49: {
						return new Symbol(BLOGTokenConstants.ELSEIF);
					}
					case -50:
						break;
					case 50: { /* existential quantifier */
						return new Symbol(BLOGTokenConstants.EXISTS);
					}
					case -51:
						break;
					case 51: {
						return new Symbol(BLOGTokenConstants.GENERATING);
					}
					case -52:
						break;
					case 52: {
						return new Symbol(BLOGTokenConstants.NONRANDOM);
					}
					case -53:
						break;
					case 53: {
						return new Symbol(BLOGTokenConstants.PARFACTOR);
					}
					case -54:
						break;
					case 54: {
						return new Symbol(BLOGTokenConstants.GUARANTEED);
					}
					case -55:
						break;
					case 55: {
						return new Symbol(BLOGTokenConstants.GENERATING);
					}
					case -56:
						break;
					case 56: {
					}
					case -57:
						break;
					case 57: {
						yybegin(YYINITIAL);
					}
					case -58:
						break;
					case 58: { /* do nothing */
					}
					case -59:
						break;
					case 59: { /* do nothing */
					}
					case -60:
						break;
					case 60: {
						yybegin(YYINITIAL);
					}
					case -61:
						break;
					case 61: { /* Char in quotes, not matched by any rule above */
						string_buf.append(yytext());
					}
					case -62:
						break;
					case 62: {
						return new Symbol(BLOGTokenConstants.ERROR,
								"Line terminator in string or character literal.");
					}
					case -63:
						break;
					case 63: { /* closing double-quote not matched by \" rule below */
						Symbol s = new Symbol(BLOGTokenConstants.STR_CONST,
								string_buf.toString());
						string_buf = new StringBuffer(); /* reinitialize the buffer */
						yybegin(YYINITIAL);
						return s;
					}
					case -64:
						break;
					case 64: {
						return new Symbol(BLOGTokenConstants.ERROR,
								"Unrecognized escape character: \'" + yytext() + "\'");
					}
					case -65:
						break;
					case 65: {
						string_buf.append('\"');
					}
					case -66:
						break;
					case 66: {
						string_buf.append('\'');
					}
					case -67:
						break;
					case 67: {
						string_buf.append('\\');
					}
					case -68:
						break;
					case 68: {
						string_buf.append('\b');
					}
					case -69:
						break;
					case 69: {
						string_buf.append('\t');
					}
					case -70:
						break;
					case 70: {
						string_buf.append('\n');
					}
					case -71:
						break;
					case 71: {
						string_buf.append('\f');
					}
					case -72:
						break;
					case 72: {
						string_buf.append('\r');
					}
					case -73:
						break;
					case 73: {
						int code = Integer.parseInt(yytext().substring(1), 8);
						string_buf.append((char) code);
					}
					case -74:
						break;
					case 74: {
						int code = Integer.parseInt(yytext().substring(1), 8);
						string_buf.append((char) code);
					}
					case -75:
						break;
					case 75: {
						int code = Integer.parseInt(yytext().substring(1), 8);
						string_buf.append((char) code);
					}
					case -76:
						break;
					case 76: {
						int code = Integer.parseInt(yytext().substring(2), 16);
						string_buf.append((char) code);
					}
					case -77:
						break;
					case 77: { /* closing single-quote not matched by \' rule below */
						Symbol s;
						if (string_buf.length() == 1) {
							s = new Symbol(BLOGTokenConstants.CHAR_CONST, new Character(
									string_buf.charAt(0)));
						} else {
							s = new Symbol(BLOGTokenConstants.ERROR,
									"Character literal must contain exactly one " + "character");
						}
						string_buf = new StringBuffer(); /* re-init buffer */
						yybegin(YYINITIAL);
						return s;
					}
					case -78:
						break;
					case 79: {
						return new Symbol(BLOGTokenConstants.ERROR, yytext());
					}
					case -79:
						break;
					case 80: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -80:
						break;
					case 81: { /* do nothing */
					}
					case -81:
						break;
					case 82: { /* Char in quotes, not matched by any rule above */
						string_buf.append(yytext());
					}
					case -82:
						break;
					case 83: {
						return new Symbol(BLOGTokenConstants.ERROR,
								"Unrecognized escape character: \'" + yytext() + "\'");
					}
					case -83:
						break;
					case 84: {
						int code = Integer.parseInt(yytext().substring(1), 8);
						string_buf.append((char) code);
					}
					case -84:
						break;
					case 86: {
						return new Symbol(BLOGTokenConstants.ERROR, yytext());
					}
					case -85:
						break;
					case 87: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -86:
						break;
					case 89: {
						return new Symbol(BLOGTokenConstants.ERROR, yytext());
					}
					case -87:
						break;
					case 90: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -88:
						break;
					case 92: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -89:
						break;
					case 94: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -90:
						break;
					case 96: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -91:
						break;
					case 98: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -92:
						break;
					case 100: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -93:
						break;
					case 102: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -94:
						break;
					case 104: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -95:
						break;
					case 106: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -96:
						break;
					case 108: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -97:
						break;
					case 109: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -98:
						break;
					case 110: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -99:
						break;
					case 111: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -100:
						break;
					case 112: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -101:
						break;
					case 113: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -102:
						break;
					case 114: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -103:
						break;
					case 115: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -104:
						break;
					case 116: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -105:
						break;
					case 117: {
						return new Symbol(BLOGTokenConstants.DOUBLE_CONST, yytext());
					}
					case -106:
						break;
					case 118: {
						int code = Integer.parseInt(yytext().substring(1), 8);
						string_buf.append((char) code);
					}
					case -107:
						break;
					case 121: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -108:
						break;
					case 122: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -109:
						break;
					case 123: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -110:
						break;
					case 124: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -111:
						break;
					case 125: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -112:
						break;
					case 126: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -113:
						break;
					case 127: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -114:
						break;
					case 128: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -115:
						break;
					case 129: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -116:
						break;
					case 130: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -117:
						break;
					case 131: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -118:
						break;
					case 132: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -119:
						break;
					case 133: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -120:
						break;
					case 134: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -121:
						break;
					case 135: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -122:
						break;
					case 136: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -123:
						break;
					case 137: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -124:
						break;
					case 138: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -125:
						break;
					case 139: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -126:
						break;
					case 140: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -127:
						break;
					case 141: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -128:
						break;
					case 142: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -129:
						break;
					case 143: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -130:
						break;
					case 144: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -131:
						break;
					case 145: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -132:
						break;
					case 146: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -133:
						break;
					case 147: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -134:
						break;
					case 148: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -135:
						break;
					case 149: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -136:
						break;
					case 150: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -137:
						break;
					case 151: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -138:
						break;
					case 152: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -139:
						break;
					case 153: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -140:
						break;
					case 154: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -141:
						break;
					case 155: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -142:
						break;
					case 156: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -143:
						break;
					case 157: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -144:
						break;
					case 158: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -145:
						break;
					case 159: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -146:
						break;
					case 160: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -147:
						break;
					case 161: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -148:
						break;
					case 162: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -149:
						break;
					case 163: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -150:
						break;
					case 164: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -151:
						break;
					case 165: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -152:
						break;
					case 166: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -153:
						break;
					case 167: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -154:
						break;
					case 168: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -155:
						break;
					case 169: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -156:
						break;
					case 170: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -157:
						break;
					case 171: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -158:
						break;
					case 172: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -159:
						break;
					case 173: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -160:
						break;
					case 174: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -161:
						break;
					case 175: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -162:
						break;
					case 176: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -163:
						break;
					case 177: {
						return new Symbol(BLOGTokenConstants.ID, yytext());
					}
					case -164:
						break;
					default:
						yy_error(YY_E_INTERNAL, false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
