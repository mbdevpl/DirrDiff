package pl.mbdev.dirrdiff;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import pl.mbdev.util.File;
import pl.mbdev.gui.GridBagPanel;

/**
 * Panel that displays three provided sets of files and the sizes of those sets. Uses
 * three <code>JScrollPane</code>s, each containing a <code>JList</code>.<br />
 * <br />
 * File creation date: Jul 28, 2012, 6:19:02 PM. This is a part of DirrDiff.
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
public final class FileDiffPanel extends GridBagPanel {
	
	/**
	 * ID.
	 */
	private static final long serialVersionUID = 5558140896560699229L;
	
	private String ll1 = "";
	private String ll12 = "";
	private String ll2 = "";
	
	private String lb1 = "";
	private String lb12 = "";
	private String lb2 = "";
	
	// dir one column
	private JLabel l1 = new JLabel(ll1);
	private DefaultListModel<String> dataOne = new DefaultListModel<String>();
	private JList<String> listOne = new JList<String>(dataOne);
	private JScrollPane scrollerOne = new JScrollPane(listOne);
	private JButton b1 = new JButton(lb1);
	
	// both dirs column
	private JLabel l12 = new JLabel(ll12);
	private DefaultListModel<String> dataBoth = new DefaultListModel<String>();
	private JList<String> listBoth = new JList<String>(dataBoth);
	private JScrollPane scrollerBoth = new JScrollPane(listBoth);
	private JButton b12 = new JButton(lb12);
	
	// dir two column
	private JLabel l2 = new JLabel(ll2);
	private DefaultListModel<String> dataTwo = new DefaultListModel<String>();
	private JList<String> listTwo = new JList<String>(dataTwo);
	private JScrollPane scrollerTwo = new JScrollPane(listTwo);
	private JButton b2 = new JButton(lb2);
	
	private ArrayList<? extends File> files1;
	private ArrayList<String> pathsOne;
	private ArrayList<? extends File> files12;
	private ArrayList<String> pathsBoth;
	private ArrayList<? extends File> files2;
	private ArrayList<String> pathsTwo;
	
	/**
	 * @param border
	 */
	public FileDiffPanel(Border border) {
		super(border);
		
		create();
		
		scrollerOne.setPreferredSize(new Dimension(200, 300));
		scrollerBoth.setPreferredSize(new Dimension(200, 300));
		scrollerTwo.setPreferredSize(new Dimension(200, 300));
		
		b1.setEnabled(false);
		b12.setEnabled(false);
		b2.setEnabled(false);
		
		// setColumnLabels("Only in dir one", "Common part", "Only in dir two");
		// setButtonLabels("Copy to two", "Sync", "Copy to one");
		
		// setFilesOne(new ArrayList<File>(), new ArrayList<String>());
		// setFilesBoth(new ArrayList<File>(), new ArrayList<String>());
		// setFilesTwo(new ArrayList<File>(), new ArrayList<String>());
	}
	
	private void create() {
		gb.fill = GridBagConstraints.NONE;
		gb.anchor = GridBagConstraints.CENTER;
		gb.gridwidth = 1;
		gb.weightx = 1;
		gb.weighty = 0;
		add(l1);
		
		add(l12);
		
		gb.gridwidth = GridBagConstraints.REMAINDER;
		add(l2);
		
		// lists
		gb.fill = GridBagConstraints.BOTH;
		gb.gridwidth = 1;
		gb.weighty = 1;
		add(scrollerOne);
		
		add(scrollerBoth);
		
		gb.gridwidth = GridBagConstraints.REMAINDER;
		add(scrollerTwo);
		
		// buttons
		gb.fill = GridBagConstraints.NONE;
		gb.anchor = GridBagConstraints.WEST;
		gb.gridwidth = 1;
		gb.weighty = 0;
		add(b1);
		
		gb.anchor = GridBagConstraints.CENTER;
		add(b12);
		
		gb.anchor = GridBagConstraints.EAST;
		add(b2);
	}
	
	public void setColumnLabels(String labelOne, String labelBoth, String labelTwo) {
		ll1 = labelOne;
		ll12 = labelBoth;
		ll2 = labelTwo;
		l1.setText(ll1);
		l12.setText(ll12);
		l2.setText(ll2);
	}
	
	public void setButtonLabels(String labelOne, String labelBoth, String labelTwo) {
		lb1 = labelOne;
		lb12 = labelBoth;
		lb2 = labelTwo;
		b1.setText(lb1);
		b12.setText(lb12);
		b2.setText(lb2);
	}
	
	private void putIntoListModel(DefaultListModel<String> m, ArrayList<String> array) {
		if (m == null || array == null)
			return;
		int size = array.size();
		m.setSize(size);
		if (size == 0)
			return;
		for (int i = 0; i < size; i++)
			m.setElementAt(array.get(i), i);
		JLabel l = null;
		String s = null;
		if (m == dataOne) {
			l = l1;
			s = ll1;
		} else if (m == dataBoth) {
			l = l12;
			s = ll12;
		} else if (m == dataTwo) {
			l = l2;
			s = ll2;
		} else
			return;
		s = s.concat(" (").concat(String.valueOf(array.size())).concat(")");
		l.setText(s);
	}
	
	public ArrayList<? extends File> getFilesOne() {
		return files1;
	}
	
	public ArrayList<String> getPathsOne() {
		return pathsOne;
	}
	
	public void setFilesOne(ArrayList<? extends File> filesOne, ArrayList<String> pathsOne) {
		this.files1 = filesOne;
		this.pathsOne = pathsOne;
		putIntoListModel(dataOne, pathsOne);
	}
	
	public ArrayList<? extends File> getFilesBoth() {
		return files12;
	}
	
	public ArrayList<String> getPathsBoth() {
		return pathsBoth;
	}
	
	public void setFilesBoth(ArrayList<? extends File> filesBoth,
			ArrayList<String> pathsBoth) {
		this.files12 = filesBoth;
		this.pathsBoth = pathsBoth;
		putIntoListModel(dataBoth, pathsBoth);
	}
	
	public ArrayList<? extends File> getFilesTwo() {
		return files2;
	}
	
	public ArrayList<String> getPathsTwo() {
		return pathsTwo;
	}
	
	public void setFilesTwo(ArrayList<? extends File> filesTwo, ArrayList<String> pathsTwo) {
		this.files2 = filesTwo;
		this.pathsTwo = pathsTwo;
		putIntoListModel(dataTwo, pathsTwo);
	}
	
}
