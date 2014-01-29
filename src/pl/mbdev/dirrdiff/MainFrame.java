package pl.mbdev.dirrdiff;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pl.mbdev.gui.Borders;
import pl.mbdev.gui.GridBagConstraintsExtended;
import pl.mbdev.gui.GridBagFrame;
import pl.mbdev.gui.GridBagPanel;
import pl.mbdev.util.File;
import pl.mbdev.util.MonitoredThread;
import pl.mbdev.util.ThreadMonitor;

/**
 * Main window of DirrDiff.<br />
 * <br />
 * File creation date: Jul 28, 2012, 5:23:18 PM. This is a part of DirrDiff.
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
 */
public final class MainFrame extends GridBagFrame implements ThreadMonitor {
	
	/**
	 * ID.
	 */
	private static final long serialVersionUID = 1944142176468654719L;
	
	private static final String[] StagesDescriptions = { "Idle.",
			"Scanning directories...", "Diff existing files...",
			"Diff files' modification date and size...",
			"Diff files' modification date and contents...", "Diff files' size...",
			"Diff files' contents...", "Diff files' modification date...", "Unknown stage!" };
	
	/**
	 * Main menu bar.
	 */
	private JMenuBar menuBar = new JMenuBar();
	
	// main menu options
	private JMenu menuApplication = new JMenu("Application");
	private JMenuItem optionAbout = new JMenuItem("About");
	private JMenuItem optionWebPage = new JMenuItem("Author's web page");
	private JMenuItem optionExit = new JMenuItem("Exit");
	
	/**
	 * Panel containing parameters of directory scan.
	 */
	private GridBagPanel panelParams = new GridBagPanel(
			Borders.Titled("Dir diff parameters"));
	
	/**
	 * Panel depicting differences in file existence between two scanned directories.
	 */
	private FileDiffPanel panelFilesExist = new FileDiffPanel(
			Borders.Titled("Diff by existence"));
	
	private GridBagPanel panelFilesMetadata = new GridBagPanel(
			Borders.Titled("Diff by metadata (for files existing in both dirs)"));
	
	// metadata diff panels
	private FileDiffPanel panelFilesModifiedSize = new FileDiffPanel(
			Borders.Titled("... by modification date/time and size"));
	private FileDiffPanel panelFilesModifiedHash = new FileDiffPanel(
			Borders.Titled("... by modification date/time and contents"
					+ " (identical size)"));
	private FileDiffPanel panelFilesSize = new FileDiffPanel(
			Borders.Titled("... only by size"));
	private FileDiffPanel panelFilesHash = new FileDiffPanel(
			Borders.Titled("... only by difference in contents"));
	private FileDiffPanel panelFilesModified = new FileDiffPanel(
			Borders.Titled("... only by modification date/time"));
	
	// params panel
	private JTextField dirOne = new JTextField();
	private JTextField dirTwo = new JTextField();
	private JCheckBox boxParallelScan = new JCheckBox("parallel scan", true);
	private JCheckBox boxReadContents = new JCheckBox("read file contents", false);
	private JButton bDiff = new JButton("Diff");
	private JProgressBar barScan1 = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
	private JProgressBar barScan2 = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
	private JProgressBar barDiffStageNo = new JProgressBar(SwingConstants.HORIZONTAL, 0, 7);
	private JProgressBar barDiffStageProgress = new JProgressBar(
			SwingConstants.HORIZONTAL, 0, 1);
	
	/**
	 * Used to handle two path searching threads.
	 */
	private Object semaphore = new Object();
	private int threadsLeft = 0;
	
	/**
	 * Thread searching the first path.
	 */
	private DirScanThread t1 = null;
	
	/**
	 * Thread searching the second path.
	 */
	private DirScanThread t2 = null;
	
	/**
	 * True if the data was already read from both threads.
	 */
	//private boolean dataFetched = false;
	
	//private int stagesCount = 7;
	
	// contain all scanned data
	private ArrayList<File> filesOneAll;
	private ArrayList<File> filesTwoAll;
	private ArrayList<String> pathsOneAll;
	private ArrayList<String> pathsTwoAll;
	
	/**
	 * Constructs the main frame of DirrDiff.
	 */
	public MainFrame() {
		super("Dirr Diff", new Point(100, 100), new Dimension(600, 400));
		
		createMenu();
		create();
		
		dirOne.setText("D:\\Projects (waiting for import)\\JavaME_2010");
		dirTwo.setText("D:\\Projects (waiting for import)\\JavaME_2011");
	}
	
	private void createMenu() {
		menuApplication.add(optionAbout);
		if (isBrowsingSupported())
			menuApplication.add(optionWebPage);
		menuApplication.add(optionExit);
		menuBar.add(menuApplication);
		
		setJMenuBar(menuBar);
	}
	
	private void create() {
		createParams();
		
		gb.fill = GridBagConstraints.BOTH;
		gb.gridwidth = GridBagConstraints.REMAINDER;
		gb.weightx = 1;
		add(panelParams);
		
		panelFilesExist
				.setColumnLabels("Only in dir one", "Common part", "Only in dir two");
		panelFilesExist.setButtonLabels("Copy to two", "Sync", "Copy to one");
		
		panelFilesModifiedSize.setColumnLabels("Later & larger in one", "Inconclusive",
				"Later & larger in two");
		panelFilesModifiedSize.setButtonLabels("Overwrite in two", "Overwrite to both",
				"Overwrite in one");
		
		panelFilesModifiedHash.setColumnLabels("Later and diff. contents in one",
				"Inconclusive", "Later and diff. contents in two");
		panelFilesModifiedHash.setButtonLabels("Overwrite in two", "Overwrite to both",
				"Overwrite in one");
		
		panelFilesSize.setColumnLabels("Larger in dir one", "Same size",
				"Larger in dir two");
		panelFilesSize.setButtonLabels("Overwrite smaller in two", "Overwrite all smaller",
				"Overwrite smaller in one");
		
		panelFilesHash.setColumnLabels("Different contents", "Identical contents",
				"Different contents");
		panelFilesHash.setButtonLabels("Overwrite in two", "Overwrite to both",
				"Overwrite in one");
		
		panelFilesModified.setColumnLabels("Later in one", "Completely identical",
				"Later in two");
		panelFilesModified.setButtonLabels("Overwrite in two", "Overwrite to both",
				"Overwrite in one");
		
		GridBagPanel p = new GridBagPanel(Borders.None);
		JScrollPane s = new JScrollPane(p);
		p.gb.fill = GridBagConstraints.BOTH;
		p.gb.gridwidth = GridBagConstraints.REMAINDER;
		p.gb.weightx = 1;
		p.gb.weighty = 1;
		
		p.add(panelFilesExist);
		
		// gb.weighty = 1;
		// add(panelFilesExist);
		
		panelFilesMetadata.gb.fill = GridBagConstraints.BOTH;
		panelFilesMetadata.gb.gridwidth = GridBagConstraints.REMAINDER;
		panelFilesMetadata.gb.weightx = 1;
		panelFilesMetadata.gb.weighty = 1;
		panelFilesMetadata.add(panelFilesModifiedSize);
		panelFilesMetadata.add(panelFilesModifiedHash);
		panelFilesMetadata.add(panelFilesSize);
		panelFilesMetadata.add(panelFilesHash);
		panelFilesMetadata.add(panelFilesModified);
		
		p.add(panelFilesMetadata);
		
		gb.weighty = 1;
		add(s);
		
		// gb.weighty = 0;
		// add(panelFilesMetadata);
	}
	
	private void createParams() {
		GridBagConstraintsExtended gb = panelParams.gb;
		
		// directory number one
		gb.setGrid(0, 0, 2, 1).setFillNone().setWeightX(0);
		panelParams.add(new JLabel("Dir One: "));
		
		gb.setGrid(2, 0, 6, 1).setFillHorizontal().setWeightX(1);
		panelParams.add(dirOne);
		
		// directory number two
		gb.setGrid(0, 1, 2, 1).setFillNone().setWeightX(0);
		panelParams.add(new JLabel("Dir Two: "));
		
		gb.setGrid(2, 1, 6, 1).setFillHorizontal().setWeightX(1);
		panelParams.add(dirTwo);
		
		// check boxes
		gb.setGrid(1, 2, 6, 1).setFillNone().setAnchorWest().setWeightX(0);
		panelParams.add(boxParallelScan);
		
		gb.setGrid(1, 3, 6, 1).setFillNone().setAnchorWest().setWeightX(0);
		panelParams.add(boxReadContents);
		
		// differentiation button
		gb.setGrid(6, 2, 2, 2).setFillNone().setAnchorEast().setWeightX(0);
		panelParams.add(bDiff);
		
		// scan progress bars
		gb.setGrid(0, 4, 4, 1).setFillHorizontal().setAnchorCenter().setWeightX(1);
		barScan1.setString("Ready.");
		barScan1.setStringPainted(true);
		panelParams.add(barScan1);
		
		gb.setGrid(4, 4, 4, 1).setFillHorizontal().setAnchorCenter().setWeightX(1);
		barScan2.setString("Ready.");
		barScan2.setStringPainted(true);
		panelParams.add(barScan2);
		
		gb.setGrid(0, 5, 8, 1).setFillHorizontal().setAnchorCenter().setWeightX(1);
		barDiffStageProgress.setStringPainted(false);
		panelParams.add(barDiffStageProgress);
		
		gb.setGrid(0, 6, 8, 1).setFillHorizontal().setAnchorCenter().setWeightX(1);
		barDiffStageNo.setStringPainted(true);
		panelParams.add(barDiffStageNo);
		
		initializeStage(0, 1);
	}
	
	// private void initializeStageNo() {
	// barDiffStageProgress.setValue(0);
	// barDiffStageProgress.setMaximum(1);
	// barDiffStageNo.setValue(0);
	// barDiffStageNo.setString("0 / 7 - idle");
	// barDiffStageNo.setStringPainted(true);
	//
	// }
	
	private void initializeStage(int stageNo, int maxValue) {
		barDiffStageProgress.setValue(0);
		barDiffStageProgress.setMaximum(maxValue);
		if (stageNo <= 0)
			barDiffStageNo.setValue(0);
		barDiffStageNo.setValue(stageNo - 1);
		if (stageNo >= StagesDescriptions.length)
			stageNo = StagesDescriptions.length - 1;
		String desc = StagesDescriptions[stageNo];
		barDiffStageNo.setString(stageNo + " / 7 - " + desc);
	}
	
	private void initializeNextStage(int maxValue) {
		initializeStage(barDiffStageNo.getValue() + 1, maxValue);
	}
	
	private void setStageProgress(int value) {
		barDiffStageProgress.setValue(value);
	}
	
	private void finalizeStage() {
		int max = barDiffStageProgress.getMaximum();
		barDiffStageProgress.setValue(max);
		barDiffStageNo.setValue(barDiffStageNo.getValue() + 1);
	}
	
	// private void setStageNo(int number) {
	// barDiffStageProgress.setValue(0);
	// barDiffStageNo.setValue(barDiffStageNo.getValue() + 1);
	// }
	
	// private void setNextStageNo() {
	//
	// }
	
	// private void setStageNoFinished() {
	// barDiffStageProgress.setValue(0);
	// barDiffStageNo.setValue(barDiffStageNo.getValue() + 1);
	// }
	
	@Override
	public void threadedActionPerformed(ActionEvent e) {
		Object src = e.getSource();
		System.out.println(src.getClass().getSimpleName() + " " + e.getActionCommand());
		if (src.equals(bDiff)) {
			launchDiff();
		} else if (src.equals(optionAbout)) {
			StringBuffer msg = new StringBuffer("<html><body width=300>");
			msg.append("<h2>DirrDiff - DIRectory Recursive DIFFerentiation tool</h2>");
			msg.append("<p>by Mateusz Bysiek<br>http://mbdev.pl/</p>");
			msg.append("<h3>About application</h3>");
			msg.append("<p>Designed for comperhensive comparison of two selected")
					.append(" directories.")
					.append(" Files are compared by: existence, modification date, size and")
					.append(" contents. Two directories are scanned asynchroneously,")
					.append(" which can save time when directories are on two different ")
					.append("physical discs.</p>");
			msg.append("<br>");
			msg.append("<p>I use it to compare backup drive with one containing original")
					.append(" contents. Option of copying files from inside the app is")
					.append(" planned.</p>");
			msg.append("<h3>License</h3>");
			msg.append("<p>Licensed under the Apache License, Version 2.0.")
					.append(" This is a open source application that uses several closed")
					.append(" source components. Licensing details are available in the")
					.append(" source code, which should be reachable via the author's")
					.append(" web page.</p>");
			msg.append("</body></html>");
			launchInfoDialog("DirrDiff", msg.toString());
		} else if (src.equals(optionWebPage)) {
			browseTo("http://mbdev.pl/");
		} else if (src.equals(optionExit)) {
			this.dispose();
		}
	}
	
	private void setParamsEnabled(boolean enabled) {
		dirOne.setEnabled(enabled);
		dirTwo.setEnabled(enabled);
		boxParallelScan.setEnabled(enabled);
		boxReadContents.setEnabled(enabled);
		bDiff.setEnabled(enabled);
	}
	
	/**
	 * Starts Dir. R. Diff.
	 */
	private void launchDiff() {
		String d1 = dirOne.getText();
		if (!d1.endsWith(File.separator)) {
			d1 = d1.concat(File.separator);
			dirOne.setText(d1);
		}
		String d2 = dirTwo.getText();
		if (!d2.endsWith(File.separator)) {
			d2 = d2.concat(File.separator);
			dirTwo.setText(d2);
		}
		if (d1.isEmpty() || d2.isEmpty() || d1.equals(d2) || d1.indexOf(d2) != -1
				|| d2.indexOf(d1) != -1)
			return;
		if (t1 != null && !t1.isStopped())
			return;
		if (t2 != null && !t2.isStopped())
			return;
		
		setParamsEnabled(false);
		//dataFetched = false;
		t1 = null;
		t2 = null;
		initializeNextStage(2);
		try {
			t1 = new DirScanThread(d1, barScan1, this);
			t1.addMonitor(this);
			if (boxParallelScan.isSelected()) {
				t2 = new DirScanThread(d2, barScan2, this);
				t2.addMonitor(this);
				threadsLeft = 2;
			} else
				threadsLeft = 1;
			t1.start();
			if (t2 != null)
				t2.start();
		} catch (Exception ex) {
			setParamsEnabled(true);
			// System.err.println("Exception when starting scan: " + ex);
			// ex.printStackTrace(System.err);
			launchExceptionDialog("DirrDiff: Java exception", "Error while starting scan.",
					ex, 400);
		}
	}
	
	/**
	 * Fetches raw data after the scan was finished.
	 */
	private void fetchData() {
		if (boxParallelScan.isSelected()) {
			// reinitializes array lists
			// final Object lock1 = new Object(), lock2 = new Object();
			// final MainFrame mf = this;
			threadsLeft = 2;
			
			MonitoredThread mt1 = new MonitoredThread("fetchData:Dir1") {
				@Override
				public void doActions() {
					int lenOne = dirOne.getText().length() - 1; // '-1' is for trailing slash
					filesOneAll = t1.getFiles();
					pathsOneAll = new ArrayList<String>();
					for (int i = 0; i < filesOneAll.size(); i++) {
						File f = filesOneAll.get(i);
						String s = f.getAbsolutePath();
						if (f.isDirectory() && !s.endsWith(File.separator))
							s = s.concat(File.separator);
						if (s.isEmpty())
							continue;
						if (s.length() - lenOne < 0)
							continue;
						pathsOneAll.add(s.substring(lenOne, s.length()));
					}
				}
			};
			mt1.addMonitor(this);
			mt1.start();
			
			MonitoredThread mt2 = new MonitoredThread("fetchData:Dir2") {
				@Override
				public void doActions() {
					int lenTwo = dirTwo.getText().length() - 1; // also trailing slash
					filesTwoAll = t2.getFiles();
					pathsTwoAll = new ArrayList<String>();
					for (int i = 0; i < filesTwoAll.size(); i++) {
						File f = filesTwoAll.get(i);
						String s = f.getAbsolutePath();
						if (f.isDirectory() && !s.endsWith(File.separator))
							s = s.concat(File.separator);
						if (s.isEmpty())
							continue;
						if (s.length() - lenTwo < 0)
							continue;
						pathsTwoAll.add(s.substring(lenTwo, s.length()));
					}
				}
			};
			mt2.addMonitor(this);
			mt2.start();
		} else {
			threadsLeft = 1;
			
			MonitoredThread mt1 = new MonitoredThread("fetchData:Dir1") {
				@Override
				public void doActions() {
					int lenOne = dirOne.getText().length() - 1; // '-1' is for trailing slash
					filesOneAll = t1.getFiles();
					pathsOneAll = new ArrayList<String>();
					for (int i = 0; i < filesOneAll.size(); i++) {
						File f = filesOneAll.get(i);
						String s = f.getAbsolutePath();
						if (f.isDirectory() && !s.endsWith(File.separator))
							s = s.concat(File.separator);
						if (s.isEmpty())
							continue;
						if (s.length() - lenOne < 0)
							continue;
						pathsOneAll.add(s.substring(lenOne, s.length()));
					}
					
					int lenTwo = dirTwo.getText().length() - 1; // also trailing slash
					filesTwoAll = t2.getFiles();
					pathsTwoAll = new ArrayList<String>();
					for (int i = 0; i < filesTwoAll.size(); i++) {
						File f = filesTwoAll.get(i);
						String s = f.getAbsolutePath();
						if (f.isDirectory() && !s.endsWith(File.separator))
							s = s.concat(File.separator);
						if (s.isEmpty())
							continue;
						if (s.length() - lenTwo < 0)
							continue;
						pathsTwoAll.add(s.substring(lenTwo, s.length()));
					}
				}
			};
			mt1.addMonitor(this);
			mt1.start();
		}
		
		// boolean ok = false;
		// synchronized (lock1) {
		// synchronized (lock2) {
		// if (mt1.isStopped() && mt2.isStopped())
		// ok = true;
		// }
		// }
		// if (!ok) {
		// while (!mt1.isStopped() && !mt2.isStopped())
		// ;
		// }
		
	}
	
	private ArrayList<File> copyFileArray(ArrayList<? extends File> arg) {
		ArrayList<File> copy = new ArrayList<File>();
		for (File f : arg)
			copy.add(new File(f.getAbsolutePath()));
		return copy;
	}
	
	private ArrayList<String> copyStringArray(ArrayList<String> arg) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String s : arg)
			copy.add(new String(s));
		return copy;
	}
	
	/**
	 * Looks for existing files.
	 */
	private void refreshPanelFilesExist() {
		ArrayList<File> filesOne = copyFileArray(filesOneAll);
		ArrayList<File> filesTwo = copyFileArray(filesTwoAll);
		ArrayList<String> pathsOne = copyStringArray(pathsOneAll);
		ArrayList<String> pathsTwo = copyStringArray(pathsTwoAll);
		
		// comparing string arrays, and moving found files from One&Two to Both
		ArrayList<File> filesBoth = new ArrayList<File>();
		ArrayList<String> pathsBoth = new ArrayList<String>();
		initializeNextStage(pathsOne.size());
		int scanned = 0;
		for (int i = pathsOne.size() - 1; i >= 0; i--) {
			if (scanned % 100 == 0)
				setStageProgress(scanned);
			scanned++;
			String s = pathsOne.get(i);
			int index = pathsTwo.indexOf(s);
			if (index == -1)
				continue;
			File f = filesOne.get(i);
			filesBoth.add(0, f);
			pathsBoth.add(0, s);
			filesOne.remove(i);
			pathsOne.remove(i);
			filesTwo.remove(index);
			pathsTwo.remove(index);
		}
		finalizeStage();
		
		// filling existence panels
		panelFilesExist.setFilesOne(filesOne, pathsOne);
		panelFilesExist.setFilesTwo(filesTwo, pathsTwo);
		panelFilesExist.setFilesBoth(filesBoth, pathsBoth);
	}
	
	/**
	 * Looks for files that were modified later.
	 */
	private void refreshPanelFilesModifiedSize() {
		ArrayList<File> files1 = new ArrayList<File>();
		ArrayList<String> paths1 = new ArrayList<String>();
		ArrayList<File> files2 = new ArrayList<File>();
		ArrayList<String> paths2 = new ArrayList<String>();
		ArrayList<File> files12 = copyFileArray(panelFilesExist.getFilesBoth());
		ArrayList<String> paths12 = copyStringArray(panelFilesExist.getPathsBoth());
		
		int size = paths12.size();
		initializeNextStage(size);
		int scanned = 0;
		for (int i = size - 1; i >= 0; i--) {
			if (scanned % 50 == 0)
				setStageProgress(scanned);
			scanned++;
			String path = paths12.get(i);
			if (path.endsWith(File.separator))
				continue;
			int index1 = pathsOneAll.indexOf(path);
			int index2 = pathsTwoAll.indexOf(path);
			File f1 = filesOneAll.get(index1);
			File f2 = filesTwoAll.get(index2);
			int s = File.CompareSize(f1, f2);
			if (s == 0)
				continue;
			int dif = File.CompareModified(f1, f2);
			if (dif == 0)
				continue;
			if (s > 0 && dif > 0) {
				files1.add(0, f1);
				paths1.add(0, path);
			} else if (s < 0 && dif < 0) {
				files2.add(0, f2);
				paths2.add(0, path);
			} else
				continue;
			files12.remove(i);
			paths12.remove(i);
		}
		finalizeStage();
		
		panelFilesModifiedSize.setFilesOne(files1, paths1);
		panelFilesModifiedSize.setFilesBoth(files12, paths12);
		panelFilesModifiedSize.setFilesTwo(files2, paths2);
	}
	
	/**
	 * Looks for files that were modified later and have different contents.
	 */
	private void refreshPanelFilesModifiedHash() {
		ArrayList<File> files1 = new ArrayList<File>();
		ArrayList<String> paths1 = new ArrayList<String>();
		ArrayList<File> files2 = new ArrayList<File>();
		ArrayList<String> paths2 = new ArrayList<String>();
		ArrayList<File> files12 = copyFileArray(panelFilesModifiedSize.getFilesBoth());
		ArrayList<String> paths12 = copyStringArray(panelFilesModifiedSize.getPathsBoth());
		
		int size = paths12.size();
		initializeNextStage(size);
		int scanned = 0;
		for (int i = size - 1; i >= 0; i--) {
			if (scanned % 5 == 0)
				setStageProgress(scanned);
			scanned++;
			String path = paths12.get(i);
			if (path.endsWith(File.separator))
				continue;
			int index1 = pathsOneAll.indexOf(path);
			int index2 = pathsTwoAll.indexOf(path);
			File f1 = filesOneAll.get(index1);
			File f2 = filesTwoAll.get(index2);
			if (boxReadContents.isSelected()) {
				if (!File.CompareContents(f1, f2))
					continue;
			}
			int dif = File.CompareModified(f1, f2);
			if (dif > 0) {
				files1.add(f1);
				paths1.add(path);
			} else if (dif < 0) {
				files2.add(f2);
				paths2.add(path);
			} else
				continue;
			files12.remove(i);
			paths12.remove(i);
		}
		finalizeStage();
		
		panelFilesModifiedHash.setFilesOne(files1, paths1);
		panelFilesModifiedHash.setFilesBoth(files12, paths12);
		panelFilesModifiedHash.setFilesTwo(files2, paths2);
	}
	
	/**
	 * Looks for files that differ in size.
	 */
	private void refreshPanelFilesSize() {
		ArrayList<File> files1Size = new ArrayList<File>();
		ArrayList<String> paths1Size = new ArrayList<String>();
		ArrayList<File> files2Size = new ArrayList<File>();
		ArrayList<String> paths2Size = new ArrayList<String>();
		ArrayList<File> files12Size = copyFileArray(panelFilesModifiedHash.getFilesBoth());
		ArrayList<String> paths12Size = copyStringArray(panelFilesModifiedHash
				.getPathsBoth());
		
		int size = paths12Size.size();
		initializeNextStage(size);
		int scanned = 0;
		for (int i = size - 1; i >= 0; i--) {
			if (scanned % 50 == 0)
				setStageProgress(scanned);
			scanned++;
			String path = paths12Size.get(i);
			if (path.endsWith(File.separator))
				continue;
			int index1 = pathsOneAll.indexOf(path);
			int index2 = pathsTwoAll.indexOf(path);
			File f1 = filesOneAll.get(index1);
			File f2 = filesTwoAll.get(index2);
			int s = File.CompareSize(f1, f2);
			if (s == 0)
				continue;
			if (s > 0) {
				files1Size.add(f1);
				paths1Size.add(path);
			} else {
				files2Size.add(f2);
				paths2Size.add(path);
			}
			files12Size.remove(i);
			paths12Size.remove(i);
		}
		finalizeStage();
		
		panelFilesSize.setFilesOne(files1Size, paths1Size);
		panelFilesSize.setFilesBoth(files12Size, paths12Size);
		panelFilesSize.setFilesTwo(files2Size, paths2Size);
	}
	
	/**
	 * Looks for files that differ in contents.
	 */
	private void refreshPanelFilesHash() {
		ArrayList<File> files1 = new ArrayList<File>();
		ArrayList<String> paths1 = new ArrayList<String>();
		ArrayList<File> files2 = new ArrayList<File>();
		ArrayList<String> paths2 = new ArrayList<String>();
		ArrayList<File> files12 = copyFileArray(panelFilesSize.getFilesBoth());
		ArrayList<String> paths12 = copyStringArray(panelFilesSize.getPathsBoth());
		
		int size = paths12.size();
		initializeNextStage(size);
		int scanned = 0;
		for (int i = size - 1; i >= 0; i--) {
			if (scanned % 5 == 0)
				setStageProgress(scanned);
			scanned++;
			if (boxReadContents.isSelected())
				continue;
			String path = paths12.get(i);
			if (path.endsWith(File.separator))
				continue;
			int index1 = pathsOneAll.indexOf(path);
			int index2 = pathsTwoAll.indexOf(path);
			File f1 = filesOneAll.get(index1);
			File f2 = filesTwoAll.get(index2);
			if (!File.CompareContents(f1, f2))
				continue;
			// if (dif > 0) {
			files1.add(f1);
			paths1.add(path);
			// } else if (dif < 0) {
			files2.add(f2);
			paths2.add(path);
			// } else
			// continue;
			files12.remove(i);
			paths12.remove(i);
		}
		finalizeStage();
		
		panelFilesHash.setFilesOne(files1, paths1);
		panelFilesHash.setFilesBoth(files12, paths12);
		panelFilesHash.setFilesTwo(files2, paths2);
	}
	
	/**
	 * Looks for files that differ in modification date.
	 */
	private void refreshPanelFilesModified() {
		ArrayList<File> files1 = new ArrayList<File>();
		ArrayList<String> paths1 = new ArrayList<String>();
		ArrayList<File> files2 = new ArrayList<File>();
		ArrayList<String> paths2 = new ArrayList<String>();
		ArrayList<File> files12 = copyFileArray(panelFilesHash.getFilesBoth());
		ArrayList<String> paths12 = copyStringArray(panelFilesHash.getPathsBoth());
		
		int size = paths12.size();
		initializeNextStage(size);
		int scanned = 0;
		for (int i = size - 1; i >= 0; i--) {
			if (scanned % 50 == 0)
				setStageProgress(scanned);
			scanned++;
			String path = paths12.get(i);
			if (path.endsWith(File.separator))
				continue;
			int index1 = pathsOneAll.indexOf(path);
			int index2 = pathsTwoAll.indexOf(path);
			File f1 = filesOneAll.get(index1);
			File f2 = filesTwoAll.get(index2);
			int dif = pl.mbdev.util.File.CompareModified(f1, f2);
			if (dif > 0) {
				files1.add(f1);
				paths1.add(path);
			} else if (dif < 0) {
				files2.add(f2);
				paths2.add(path);
			} else
				continue;
			files12.remove(i);
			paths12.remove(i);
		}
		finalizeStage();
		
		panelFilesModified.setFilesOne(files1, paths1);
		panelFilesModified.setFilesBoth(files12, paths12);
		panelFilesModified.setFilesTwo(files2, paths2);
	}
	
	@Override
	public void threadStateChanged(MonitoredThread thread, String newState) {
		synchronized (semaphore) {
			
			if (thread.getName().startsWith("DirScanThread")) {
				if (!newState.equals(MonitoredThread.STOPPED))
					return;
				
				if (t2 == null && !boxParallelScan.isSelected()) {
					t2 = new DirScanThread(dirTwo.getText(), barScan2, this);
					t2.addMonitor(this);
					t2.start();
					return;
				}
				setStageProgress(1);
				
				threadsLeft--;
				if (threadsLeft > 0)
					return;
				try {
					fetchData();
				} catch (Exception ex) {
					setParamsEnabled(true);
					// System.err.println("Exception when scan ended: " + ex);
					// ex.printStackTrace(System.err);
					launchExceptionDialog("DirrDiff: Java exception",
							"Error after finishing scan.", ex, 400);
					return;
				}
				return;
			}
			
			if (thread.getName().startsWith("dataFetch")) {
				if (!newState.equals(MonitoredThread.STOPPED))
					return;
				threadsLeft--;
				if (threadsLeft > 0)
					return;
				try {
					finalizeStage();
					refreshPanelFilesExist();
					refreshPanelFilesModifiedSize();
					refreshPanelFilesModifiedHash();
					refreshPanelFilesSize();
					refreshPanelFilesHash();
					refreshPanelFilesModified();
					barDiffStageNo.setString("Diff finished.");
				} catch (Exception ex) {
					// System.err.println("Exception when performing data analysis: " + ex);
					// ex.printStackTrace(System.err);
					launchExceptionDialog("DirrDiff: Java exception",
							"Error while performing data analysis.", ex, 400);
				}
				setParamsEnabled(true);
				return;
			}
			// if (!t1.isStopped() || !t2.isStopped())
			// return;
			// if (dataFetched)
			// return;
			// dataFetched = true;
			// return;
		}
	}
}
