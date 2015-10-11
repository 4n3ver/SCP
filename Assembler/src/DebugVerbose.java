
/**
 * Debug message printing interface
 *
 * @author Yoel Ivan (yivan3@gatech.edu)
 */
public interface DebugVerbose {
    public static String DEBUG_TAG = "[DEBUG] ";

    public void println(String msg);
    public void printf(String msg, Object... args);
    public void print(String msg);
    public void printStackTrace(Exception e);

    /**
     * Initializes {@link DebugVerbose} with this class if -d option enabled.
     *
     * @author Yoel Ivan (yivan3@gatech.edu)
     */
    public class EnableDebug implements DebugVerbose {

        public EnableDebug() {
            println("Debug mode enabled!");
        }

        @Override
        public void println(String msg) {
            System.err.println(DEBUG_TAG + msg);
            System.err.flush();
        }

        @Override
        public void printf(String msg, Object... args) {
            System.err.printf(DEBUG_TAG + msg, args);
            System.err.flush();
        }

        @Override
        public void print(String msg) {
            System.err.print(msg);
            System.err.flush();
        }

        @Override
        public void printStackTrace(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes {@link DebugVerbose} with this class if -d option disabled.
     *
     * @author Yoel Ivan (yivan3@gatech.edu)
     */
    public class DisableDebug implements DebugVerbose {
        @Override
        public void println(String msg) {

        }

        @Override
        public void printf(String msg, Object... args) {

        }

        @Override
        public void print(String msg) {

        }

        @Override
        public void printStackTrace(Exception e) {

        }
    }
}
