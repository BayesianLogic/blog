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
 */
public abstract class ResultWriter {
  /**
   * Construct a ResultWriter for the given collection of queries.
   */
  public ResultWriter(final Collection<Query> queries) {
    this.queries = queries;
  }

  /**
   * Set a header to be printed before the query results.
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Write the query results to a file at the given path.
   * 
   * Overwrites any existing file.
   */
  public void writeResults(String outputPath) {
    try {
      PrintStream stream = new PrintStream(new FileOutputStream(outputPath));
      writeResults(stream);
      stream.close();
    } catch (Exception e) {
      System.err.println("Could not write to file: " + outputPath);
      Util.fatalError(e);
    }
  }

  /**
   * Write the query results to the given PrintStream.
   */
  abstract public void writeResults(PrintStream stream);

  protected final Collection<Query> queries;
  protected String header;
}
