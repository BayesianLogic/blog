/**
 * 
 */
package blog.parse;

import java_cup.runtime.ComplexSymbolFactory;
import blog.absyn.Absyn;
import blog.absyn.PrettyPrinter;
import blog.msg.ErrorMsg;

/**
 * Parser for BLOG program. Use enableDebug() for debugging the parser itself.
 * 
 * @author leili
 * @since Apr 18, 2012
 * 
 */
public class Parse {

  protected ErrorMsg errorMsg;
  protected Absyn absyn;

  public Parse(java.io.Reader inp, ErrorMsg errorMsg) {
    this(inp, errorMsg, null);
  }

  public Parse(java.io.Reader inp, ErrorMsg errorMsg, String srcname) {
    this.errorMsg = errorMsg;
    BLOGParser parser;
    ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
    try {
      BLOGLexer lexer = new BLOGLexer(inp, symbolFactory, errorMsg);
      lexer.setFilename(srcname);
      parser = new BLOGParser(lexer, symbolFactory, errorMsg);
      if (DEBUG_TAG)
        parser.debug_parse();
      else
        parser.parse();
      absyn = parser.parseResult;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new Error(e.toString());
    } finally {
      try {
        inp.close();
      } catch (java.io.IOException e) {
      }
    }
  }

  public static Parse parseFile(String filename) {
    blog.msg.ErrorMsg errorMsg = new blog.msg.ErrorMsg(filename);
    java.io.Reader inp;
    try {
      inp = new java.io.FileReader(filename);
    } catch (java.io.FileNotFoundException e) {
      throw new Error("File not found: " + filename);
    }
    return new Parse(inp, errorMsg, filename);
  }

  public static Parse parseString(String content) {
    blog.msg.ErrorMsg errorMsg = new blog.msg.ErrorMsg("String: "
        + content.hashCode());
    java.io.Reader inp;
    try {
      inp = new java.io.StringReader(content);
    } catch (Throwable e) {
      throw new Error("String is null!");
    }
    return new Parse(inp, errorMsg);
  }

  public ErrorMsg getErrorMsg() {
    return errorMsg;
  }

  public Absyn getResult() {
    return absyn;
  }

  public static void enableDebug() {
    DEBUG_TAG = true;
  }

  private static boolean DEBUG_TAG = false;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("  Usage: java blog.parse.Parse <filename> [--debug]");
      return;
    }
    if (args.length > 1 && args[1].equals("--debug"))
      enableDebug();
    Parse parse = parseFile(args[0]);
    if (parse.getResult() != null) {
      new PrettyPrinter(System.out).printSyntax(parse.getResult());
    }
  }
}
