/**
 * 
 */
package blog.msg;

import java.io.PrintStream;

/**
 * @author leili
 * @since Apr 18, 2012
 * 
 */
public class ErrorMsg {

  private String filename;
  private PrintStream out;

  public ErrorMsg(String f, PrintStream out) {
    filename = f;
    this.out = out;
  }

  public ErrorMsg(String f) {
    this(f, System.err);
  }

  /**
   * indicating error on line, col with the message
   * 
   * @param line
   * @param col
   * @param msg
   */
  public void error(int line, int col, String msg) {
    errorMark = false;
    out.print(filename);
    out.print("::");
    out.print(line);
    out.print(".");
    out.print(col);
    out.print(": ");
    out.println(msg);
  }

  public boolean OK() {
    return errorMark;
  }

  private boolean errorMark = true;
}
