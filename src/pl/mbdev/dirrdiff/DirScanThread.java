/**
 * 
 */
package pl.mbdev.dirrdiff;

import java.util.ArrayList;

import javax.swing.JProgressBar;

import pl.mbdev.util.File;
import pl.mbdev.util.MonitoredThread;

/**
 * Thread that scans the all directories under a given root directory, listing all the
 * files it contains.<br />
 * <br />
 * File creation date: Jul 28, 2012, 6:47:05 PM. This is a part of DirrDiff.
 * 
 * <pre>
 * Copyright 2012 Mateusz Bysiek,
 *     mb@mbdev.pl, http://mbdev.pl/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre>
 * 
 * @author &copy; 2012 Mateusz Bysiek <a href="http://mbdev.pl/">http://mbdev.pl/</a>
 * 
 */
public final class DirScanThread extends MonitoredThread {
	
	private final String path;
	
	private final JProgressBar diffProgress;
	
	/**
	 * Number of scanned files.
	 */
	private int fileCount = 0;
	
	/**
	 * Number of scanned folders.
	 */
	private int dirCount = 0;
	
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
				diffProgress.setString("Finished. Scanned " + dirCount + " directories and "
						+ fileCount + " files.");
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
		dirCount++;
		if (dirCount % 100 == 0)
			diffProgress.setString("Scanned " + dirCount + " directories and " + fileCount
					+ " files...");
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
			fileCount++;
			if (fileCount % 100 == 0)
				diffProgress.setString("Scanned " + dirCount + " directories and "
						+ fileCount + " files...");
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
