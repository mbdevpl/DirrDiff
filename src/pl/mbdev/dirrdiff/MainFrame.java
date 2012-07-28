/**
 * 
 */
package pl.mbdev.dirrdiff;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pl.mbdev.gui.Borders;
import pl.mbdev.gui.GridBagFrame;
import pl.mbdev.gui.GridBagPanel;
import pl.mbdev.util.MonitoredThread;
import pl.mbdev.util.ThreadMonitor;

/**
 * <code></code><br />
 * <br />
 * File creation date: Jul 28, 2012, 5:23:18 PM. This is a part of DirrDiff.
 * 
 * @author &copy; 2012 Mateusz Bysiek <a href="http://mbdev.pl/">http://mbdev.pl/</a>
 * 
 */
public class MainFrame extends GridBagFrame implements ThreadMonitor {
	
	/**
	 * ID.
	 */
	private static final long serialVersionUID = 1944142176468654719L;
	/**
	 * Main menu bar.
	 */
	private JMenuBar menuBar = new JMenuBar();
	
	// main menu options
	private JMenu menuApplication = new JMenu("Application");
	private JMenuItem optionAbout = new JMenuItem("About");
	private JMenuItem optionExit = new JMenuItem("Exit");
	
	private GridBagPanel panelParams = new GridBagPanel(
			Borders.Titled("Dir diff parameters"));
	private FileDiffPanel panelFilesExist = new FileDiffPanel(
			Borders.Titled("Diff by existence"));
	private GridBagPanel panelFilesMetadata = new GridBagPanel(
			Borders.Titled("Diff by metadata"));
	private FileDiffPanel panelFilesSize = new FileDiffPanel(
			Borders.Titled("... by date, time and size"));
	private FileDiffPanel panelFilesHash = new FileDiffPanel(
			Borders.Titled("... by date, time and hash"));
	
	// params panel
	private JTextField dirOne = new JTextField();
	private JTextField dirTwo = new JTextField();
	private JButton bDiff = new JButton("Diff");
	
	private Object semaphore = new Object();
	private DirScanThread t1 = null;
	private DirScanThread t2 = null;
	
	/**
	 * @param title
	 * @param location
	 * @param minimumSize
	 */
	public MainFrame() {
		super("Dirr Diff", new Point(20, 20), new Dimension(600, 700));
		
		createMenu();
		create();
	}
	
	private void createMenu() {
		menuApplication.add(optionAbout);
		optionAbout.addActionListener(this);
		menuApplication.add(optionExit);
		optionExit.addActionListener(this);
		menuBar.add(menuApplication);
		
		// menuProfiles.add(optionNewProfile);
		// menuProfiles.addSeparator();
		// optionNewProfile.addActionListener(this);
		// for (JMenuItem optionProfile : optionProfiles)
		// menuProfiles.add(optionProfile);
		// menuBar.add(menuProfiles);
		
		setJMenuBar(menuBar);
	}
	
	private void create() {
		gb.fill = GridBagConstraints.BOTH;
		gb.gridwidth = GridBagConstraints.REMAINDER;
		gb.weightx = 1;
		this.add(panelParams);
		gb.weighty = 1;
		this.add(panelFilesExist);
		this.add(panelFilesMetadata);
		panelFilesMetadata.gb = this.gb;
		panelFilesMetadata.add(panelFilesSize);
		panelFilesMetadata.add(panelFilesHash);
		
		// panel params
		panelParams.gb.fill = GridBagConstraints.NONE;
		panelParams.gb.gridwidth = 1;
		panelParams.add(new JLabel("Dir One: "));
		panelParams.gb.fill = GridBagConstraints.HORIZONTAL;
		panelParams.gb.gridwidth = GridBagConstraints.REMAINDER;
		panelParams.add(dirOne);
		
		panelParams.gb.fill = GridBagConstraints.NONE;
		panelParams.gb.gridwidth = 1;
		panelParams.gb.weightx = 0;
		panelParams.add(new JLabel("Dir Two: "));
		panelParams.gb.fill = GridBagConstraints.HORIZONTAL;
		panelParams.gb.gridwidth = GridBagConstraints.REMAINDER;
		panelParams.gb.weightx = 1;
		panelParams.add(dirTwo);
		
		panelParams.gb.gridx = 1;
		panelParams.gb.fill = GridBagConstraints.NONE;
		panelParams.gb.anchor = GridBagConstraints.EAST;
		panelParams.add(bDiff);
	}
	
	@Override
	public void threadedActionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(bDiff)) {
			String d1 = dirOne.getText();
			String d2 = dirTwo.getText();
			if (d1.isEmpty() || d2.isEmpty() || d1.equals(d2) || d1.indexOf(d2) != -1
					|| d2.indexOf(d1) != -1)
				return;
			if (t1 != null && !t1.isStopped())
				return;
			if (t2 != null && !t2.isStopped())
				return;
			
			bDiff.setEnabled(false);
			try {
				t1 = new DirScanThread(d1);
				t1.addMonitor(this);
				t1.start();
				
				t2 = new DirScanThread(d2);
				t2.addMonitor(this);
				t2.start();
			} catch (Exception ex) {
				bDiff.setEnabled(true);
				System.err.println(ex);
			}
			return;
		}
		if (src.equals(optionExit)) {
			this.dispose();
		}
	}
	
	@Override
	public void threadStateChanged(String newState) {
		synchronized (semaphore) {
			if (!newState.equals(MonitoredThread.STOPPED))
				return;
			if (!t1.isStopped() || !t2.isStopped())
				return;
			
			//t1.getFiles();
			
			bDiff.setEnabled(true);
		}
	}
	
}
