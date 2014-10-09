package blog.io;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import blog.common.Util;
import blog.model.Query;

/**
 * Abstract base class for outputting query results.
 * 
 * Supports writing results to a PrintStream or to a file. Concrete subclasses
 * must implement the {@link #writeResults(PrintStream)} method.
 * 
 * @author cberzan
 * @since Jun 9, 2014
 * 
 *        modifying original interfaces
 * @author leili
 * @date Oct 8, 2014
 */
public abstract class ResultWriter {

  public ResultWriter() {
  }

  /**
   * set output to the underlying PrintStream
   * 
   * @param out
   */
  public void setOutput(PrintStream out) {
    flush();
    this.out = out;
  }

  /**
   * set output to the underlying file
   * 
   * @param outputPath
   */
  public void setOutput(String outputPath) {
    flush();
    try {
      out = new PrintStream(new FileOutputStream(outputPath));
    } catch (Exception e) {
      System.err.println("Could not write to file: " + outputPath);
      Util.fatalError(e);
    }
  }

  /**
   * Set a header to be printed before the query results.
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * flush the underlying print stream.
   * Only after flushing, the results are guaranteed written to the underlying
   * file.
   */
  public void flush() {
    // default doing nothing
  }

  /**
   * Write the query results. After writing, the {@link #flush()} method will be
   * automatically invoked.
   * 
   */
  public void writeAllResults(Collection<Query> queries) {
    if (header != null) {
      out.println(header);
    }
    for (Query q : queries) {
      writeResult(q);
    }
    flush();
  }

  /**
   * write/append single query result to the underlying output stream.
   * 
   * @param q
   */
  abstract public void writeResult(Query query);

  protected PrintStream out;
  protected String header;
}
