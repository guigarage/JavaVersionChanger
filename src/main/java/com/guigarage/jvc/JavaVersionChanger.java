package com.guigarage.jvc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

public class JavaVersionChanger extends JFrame {

	private static final long serialVersionUID = 1L;

	private List<JavaVersion> versions;
	
	public JavaVersionChanger() {
		setTitle("Change your Java Environment on Mac OS");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JTable versionTable = new JTable();
		versionTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		SwingWorker<List<JavaVersion>, Void> versionFetcherWorker = new SwingWorker<List<JavaVersion>, Void>() {

			@Override
			protected List<JavaVersion> doInBackground() throws Exception {
				JavaVersionFetcher versionFetcher = new JavaVersionFetcher();
				return versionFetcher.fetch();
			}
			
			@Override
			protected void done() {
				try {
					versions = get();
					versionTable.setModel(new AbstractTableModel() {
						
						private static final long serialVersionUID = 1L;

						public String getColumnName(int column) {
							if(column == 0) {
								return "Name";
							}
							if(column == 1) {
								return "Version";
							}
							if(column == 2) {
								return "Path";
							}
							return super.getColumnName(column);
						};
						
						@Override
						public Object getValueAt(int rowIndex, int columnIndex) {
							JavaVersion version = versions.get(rowIndex);
							if(columnIndex == 0) {
								return version.getName();
							}
							if(columnIndex == 1) {
								return version.getVersion();
							}
							if(columnIndex == 2) {
								return version.getPath();
							}
							return null;
						}
						
						@Override
						public int getRowCount() {
							return versions.size();
						}
						
						@Override
						public int getColumnCount() {
							return 3;
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
			}
		};
	
		versionFetcherWorker.execute();
		getContentPane().add(new JScrollPane(versionTable), BorderLayout.CENTER);
		
		final JButton setVersionButton = new JButton("set Version");
		setVersionButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVersionButton.setEnabled(false);
				try {
					int selectedRow = versionTable.getSelectedRow();
					if(selectedRow >= 0) {
						JavaVersion version = versions.get(selectedRow);
						
						final JDialog dialog = new JDialog(JavaVersionChanger.this);
						dialog.setModal(true);
						dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						dialog.setTitle("Please insert root password");
						
						ActionListener closeDialogActionListener = new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								dialog.setVisible(false);
								dialog.dispose();
							}
						};
						
						JPasswordField passwordField = new JPasswordField(24);
						passwordField.addActionListener(closeDialogActionListener);
						dialog.getContentPane().add(passwordField, BorderLayout.CENTER);
						JButton okButton = new JButton("ok");
						okButton.addActionListener(closeDialogActionListener);
						dialog.getContentPane().add(okButton, BorderLayout.SOUTH);
						dialog.pack();
						dialog.setLocationRelativeTo(JavaVersionChanger.this);
						dialog.setVisible(true);
						
						LaunchdConfig launchdConfig = new LaunchdConfig(new String(passwordField.getPassword()));
						launchdConfig.setJavaVersion(version);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} finally {
					setVersionButton.setEnabled(true);
				}
			}
		});
		getContentPane().add(setVersionButton, BorderLayout.SOUTH);
		
		pack();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				new JavaVersionChanger().setVisible(true);
			}
		});
	}

}
