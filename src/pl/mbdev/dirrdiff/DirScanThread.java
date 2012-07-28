/**
 * 
 */
package pl.mbdev.dirrdiff;

import pl.mbdev.util.MonitoredThread;

/**
 * <code></code><br />
 * <br />
 * File creation date: Jul 28, 2012, 6:47:05 PM. This is a part of DirrDiff.
 *
 * @author &copy; 2012 Mateusz Bysiek <a href="http://mbdev.pl/">http://mbdev.pl/</a>
 *
 */
public class DirScanThread extends MonitoredThread {
	
	private final String path;

	/**
	 * @param name
	 */
	public DirScanThread(String path) {
		super("Scanning " + path);
		this.path = path;
	}
	
	@Override
	public void doActions() {
		if(path.isEmpty())
			return;
		
	}
	
}
