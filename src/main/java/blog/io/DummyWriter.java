/**
 * 
 */
package blog.io;

import blog.model.Query;

/**
 * A dummy writer that does nothing when printing results from queries.
 * 
 * @author leili
 * @since Oct 13, 2014
 * 
 */
public class DummyWriter extends ResultWriter {

  /**
   * 
   */
  public DummyWriter() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.io.ResultWriter#writeResult(blog.model.Query)
   */
  @Override
  public void writeResult(Query query) {
    // doing nothing
  }

}
