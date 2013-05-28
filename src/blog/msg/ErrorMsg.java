/**
 * 
 */
package blog.msg;

/**
 * @author leili
 * @since Apr 18, 2012
 * 
 */
public class ErrorMsg {

	// private List<Integer> linePos;
	// private int lineNum=1;
	private String filename;

	// public boolean anyErrors = false;

	public ErrorMsg(String f) {
		// linePos = new LinkedList<Integer>();
		filename = f;
	}

	// public void newline(int pos) {
	// lineNum++;
	// linePos.add(pos);
	// }

	public void error(int line, int col, String msg) {
		errorMark = false;
		// anyErrors = true;
		String sayPos = String.valueOf(line) + "." + String.valueOf(col);
		System.out.println(filename + "::" + sayPos + ": " + msg);
	}

	public boolean OK() {
		return errorMark;
	}

	private boolean errorMark = true;
	
	public static class quietErrorMsg extends ErrorMsg{

		public quietErrorMsg(String f) {
			super(f);
		}
		public void error(int line, int col, String msg) {
		}
	}
}
