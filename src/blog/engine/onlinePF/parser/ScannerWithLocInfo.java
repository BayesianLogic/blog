package blog.engine.onlinePF.parser;

import java_cup.runtime.Scanner;

/**
 * Extension of the Scanner interface that includes methods for getting the
 * current file and line number.
 */
public interface ScannerWithLocInfo extends Scanner {
	/**
	 * Returns the name of the file being scanned.
	 */
	String getCurFilename();

	/**
	 * Returns the number of the line containing the beginning of the last token
	 * matched. Line numbers are 1-based.
	 */
	int getCurLineNum();
	
	int getCurColNum();
}
