/**
 * 
 */
package pl.mbdev.dirrdiff;

import java.util.ArrayList;

import javax.swing.JProgressBar;

import pl.mbdev.util.File;
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
	
	private final JProgressBar diffProgress;
	
	private int filesCount = 0;
	
	private ArrayList<File> files;
	
	public DirScanThread(String path) {
		this(path, null);
	}
	
	/**
	 * Constructs a new directory scanner.
	 * 
	 * @param path
	 *           absolute path
	 * @param diffProgress
	 */
	public DirScanThread(String path, JProgressBar diffProgress) {
		super("Scanning " + path);
		this.path = path;
		this.diffProgress = diffProgress;
		files = new ArrayList<File>();
	}
	
	@Override
	public void doActions() {
		try {
			if (path.isEmpty())
				return;
			File dir = new File(path);
			if (!dir.isDirectory())
				return;
			
			if (diffProgress != null) {
				diffProgress.setValue(diffProgress.getMinimum());
				diffProgress.setString("Scanning...");
				diffProgress.setStringPainted(true);
				// long size = dir.length();
				// diffProgress.setMaximum((int) size);
			}
			
			scanDirTree(path, true);
			
			if (diffProgress != null) {
				diffProgress.setValue(diffProgress.getMaximum());
				diffProgress.setString("Finished. Scanned " + filesCount + " files.");
			}
		} catch (Exception ex) {
			System.err.println("Exception in thread '" + path + "': " + ex);
			ex.printStackTrace(System.err);
		}
	}
	
	private void scanDirTree(String mainPath, boolean recursive) {
		// StringBuffer buf = new StringBuffer(path);
		// buf.append(File.pathSeparator); //non-sense
		if (!mainPath.endsWith(File.separator))
			mainPath = mainPath.concat(File.separator);
		File dir = new File(mainPath);
		if (!dir.exists())
			return;
		if (!dir.canRead())
			return;
		files.add(dir);
		String[] subPaths = dir.list();
		if (subPaths == null)
			return;
		for (String subPath : subPaths) {
			String path = mainPath.concat(subPath);
			File file = new File(path);
			if (file.isDirectory()) {
				// System.out.println(file.length());
				
				scanDirTree(path, true);
				continue;
			}
			files.add(file);
			filesCount++;
			if (filesCount % 100 == 0)
				diffProgress.setString("Scanned " + filesCount + " files...");
		}
		// if (diffProgress != null) {
		// int size = (int) dir.length();
		// int newValue = diffProgress.getValue() + size;
		// if (newValue <= diffProgress.getMaximum())
		// diffProgress.setValue(newValue);
		// }
	}
	
	/**
	 * @return the files
	 */
	public ArrayList<File> getFiles() {
		return files;
	}
	
	// /**
	// * @param files the files to set
	// */
	// protected void setFiles(ArrayList<File> files) {
	// this.files = files;
	// }
	
}
