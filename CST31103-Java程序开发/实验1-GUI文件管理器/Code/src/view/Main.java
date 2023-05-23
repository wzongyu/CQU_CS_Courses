package view;

import model.MyFileModel;
import model.MyTreeModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

import controller.FileController;

/**
 * @author WuZongyu 该类中包括了启动函数 用户可以通过这个类启动整个文件管理器
 */

public class Main extends JFrame {
	/*
	 * 接下来三个定义了三个button
	 */
	private JButton forwardButton; // 前进按钮
	private JButton backButton; // 后退按钮
	private JButton desButton; // 跳转按钮

	private MyFileDisplay appearance;
	private JToolBar toolBar;
	private JTextField locationField;
	private MyFileTree filesTree;

	/*
	 * 分别为主界面左右两端添加滑块
	 */
	private JScrollPane fileList;
	private JScrollPane fileTree;

	// 自定义构造函数来初始化主界面的GUI
	public Main() {
		setTitle("文件管理器");
		setSize(800, 600);
		setLocationRelativeTo(null);
		// 对前进按钮进行初始化
		forwardButton = new JButton();
		forwardButton.setText("下一级—>");
		forwardButton.setBorderPainted(false);
		forwardButton.setBackground(Color.orange);

		// 对后退按钮进行初始化
		backButton = new JButton();
		backButton.setText("<—上一级");
		backButton.setBorderPainted(false);
		backButton.setBackground(Color.orange);

		// goButton 跳转按钮
		desButton = new JButton();
		desButton.setText("跳转");
		desButton.setBorderPainted(false);
		desButton.setBackground(Color.orange);

		// 对工具栏进行初始化
		toolBar = new JToolBar();
		toolBar.setAlignmentY(11F);
		toolBar.setBorder(null);
		toolBar.setFloatable(false);
		toolBar.setEnabled(false);
		toolBar.setFocusCycleRoot(true);
		toolBar.addSeparator(new Dimension(25, 10));
		add(toolBar, BorderLayout.NORTH);
		toolBar.add(backButton);
		toolBar.add(forwardButton);
		toolBar.add(desButton);

		appearance = new MyFileDisplay();
		fileList = new JScrollPane(appearance);
		fileList.setBorder(new EmptyBorder(0, 0, 0, 0));
		add(fileList, BorderLayout.CENTER);
		locationField = new JTextField();
		toolBar.add(locationField);

		// 对文件树进行初始化
		filesTree = new MyFileTree();
		fileTree = new JScrollPane(filesTree);
		fileTree.setPreferredSize(new Dimension(300, 0));
		add(fileTree, BorderLayout.WEST);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/*
	 * 接下来几个方法分别添加相应的监听事件
	 */
	public void addforwardListener(ActionListener listener) {
		backButton.addActionListener(listener);
	}

	public void addbackListener(ActionListener listener) {
		forwardButton.addActionListener(listener);
	}

	public void addGoListener(ActionListener listener) {
		desButton.addActionListener(listener);
	}

	public MyFileTree getFilesTree() {
		return filesTree;
	}

	/*
	 * 获取相应的变量
	 */
	public MyFileDisplay getFileDisplay() {
		return appearance;
	}

	public String locationInfo() {
		return locationField.getText();
	}

	public void updateLocation(String str) {
		locationField.setText(str);
	}

	public static void main(String[] args) {

		Main myManager = new Main(); // 生成一个主界面类
		MyFileModel myFileModel = new MyFileModel();
		MyTreeModel myTreeModel = new MyTreeModel();
		FileController fileController = new FileController(myManager, myFileModel, myTreeModel);
		myManager.setVisible(true); // 使用setVisible方法使整个界面可见
	}

}
