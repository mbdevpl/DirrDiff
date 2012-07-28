/**
 * 
 */
package pl.mbdev.dirrdiff;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import pl.mbdev.gui.GridBagPanel;

/**
 * <code></code><br />
 * <br />
 * File creation date: Jul 28, 2012, 6:19:02 PM. This is a part of DirrDiff.
 * 
 * @author &copy; 2012 Mateusz Bysiek <a href="http://mbdev.pl/">http://mbdev.pl/</a>
 * 
 */
public class FileDiffPanel extends GridBagPanel {
	
	/**
	 * ID.
	 */
	private static final long serialVersionUID = 5558140896560699229L;
	
	// dir one column
	private JLabel labelOne = new JLabel("Only in dir one");
	private DefaultListModel<String> dataOne = new DefaultListModel<String>();
	private JList<String> listOne = new JList<String>(dataOne);
	private JScrollPane scrollerOne = new JScrollPane(listOne);
	private JButton bCopyToTwo = new JButton("Copy to two");
	
	// both dirs column
	private JLabel labelBoth = new JLabel("Common part");
	private DefaultListModel<String> dataBoth = new DefaultListModel<String>();
	private JList<String> listBoth = new JList<String>(dataBoth);
	private JScrollPane scrollerBoth = new JScrollPane(listBoth);
	private JButton bSync = new JButton("Sync");
	
	// dir two column
	private JLabel labelTwo = new JLabel("Only in dir two");
	private DefaultListModel<String> dataTwo = new DefaultListModel<String>();
	private JList<String> listTwo = new JList<String>(dataTwo);
	private JScrollPane scrollerTwo = new JScrollPane(listTwo);
	private JButton bCopyToOne = new JButton("Copy to one");
	
	/**
	 * @param border
	 */
	public FileDiffPanel(Border border) {
		super(border);
		scrollerOne.setPreferredSize(new Dimension(100, 100));
		scrollerBoth.setPreferredSize(new Dimension(50, 100));
		scrollerTwo.setPreferredSize(new Dimension(100, 100));

		gb.fill = GridBagConstraints.NONE;
		gb.anchor = GridBagConstraints.CENTER;
		gb.gridwidth = 1;
		gb.weighty = 0;
		add(labelOne);

		gb.anchor = GridBagConstraints.CENTER;
		add(labelBoth);

		gb.anchor = GridBagConstraints.CENTER;
		gb.gridwidth = GridBagConstraints.REMAINDER;
		add(labelTwo);
		
		//lists
		gb.fill = GridBagConstraints.BOTH;
		gb.anchor = GridBagConstraints.CENTER;
		gb.gridwidth = 1;
		gb.weighty = 1;
		add(scrollerOne);
		
		add(scrollerBoth);
		
		gb.gridwidth = GridBagConstraints.REMAINDER;
		add(scrollerTwo);
		
		//buttons
		gb.fill = GridBagConstraints.NONE;
		gb.anchor = GridBagConstraints.WEST;
		gb.gridwidth = 1;
		gb.weightx = 2;
		gb.weighty = 0;
		add(bCopyToTwo);
		
		gb.anchor = GridBagConstraints.CENTER;
		gb.weightx = 1;
		add(bSync);
		
		gb.anchor = GridBagConstraints.EAST;
		gb.weightx = 2;
		add(bCopyToOne);
	}
	
}
