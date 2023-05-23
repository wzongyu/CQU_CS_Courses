package view;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import view.MyFileRenderer;

/**
 * 该类用于文件界面的展示 其中的成员变量包括了文件和文件夹菜单 以及各种所实现功能的图像 以及对图像的渲染工作
 */
public class MyFileDisplay extends JList {
	/*
	 * 记下来几行的私有成员变量对应相应的事件和功能
	 */
	private JMenuItem copyOption; // 复制选项
	private JMenuItem pasteOption; // 粘贴选项
	private JMenuItem encryptOption; // 加密选项
	private JMenuItem decryptOption; // 解密选项
	private JMenuItem createItem; // 创建文件夹选项
	private JMenuItem deleteItem; // 删除菜单项
	private JPopupMenu fileMenu; // 文件目录
	private JPopupMenu foldMenu; // 文件夹目录
	private JMenuItem zipOption; // 压缩选项
	private JMenuItem unzipOption; // 解压选项
	/*
	 * 自行定义构造函数
	 */

	public MyFileDisplay() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		Font wordFont = new Font("微软雅黑", Font.PLAIN, 16);
		initGlobalFontSetting(wordFont);
		/*
		 * 依次对成员变量进行初始化
		 */
		copyOption = new JMenuItem();
		encryptOption = new JMenuItem();
		decryptOption = new JMenuItem();
		zipOption = new JMenuItem();
		unzipOption = new JMenuItem();
		createItem = new JMenuItem();
		pasteOption = new JMenuItem();
		deleteItem = new JMenuItem();
		fileMenu = new JPopupMenu();
		foldMenu = new JPopupMenu();
		fileMenu.setBorderPainted(false);
		setMenuItemStyle(copyOption, "复制");
		fileMenu.add(copyOption);
		setMenuItemStyle(encryptOption, "加密");
		fileMenu.add(encryptOption);
		setMenuItemStyle(decryptOption, "解密");
		fileMenu.add(decryptOption);
		setMenuItemStyle(zipOption, "压缩");
		fileMenu.add(zipOption);
		setMenuItemStyle(unzipOption, "解压");
		fileMenu.add(unzipOption);
		setMenuItemStyle(createItem, "新建文件夹");
		foldMenu.add(createItem);
		foldMenu.setBorderPainted(false);
		setMenuItemStyle(pasteOption, "粘贴");
		foldMenu.add(pasteOption);
		setMenuItemStyle(deleteItem, "删除");
		fileMenu.add(deleteItem);
		setCellRenderer(new MyFileRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	// 对函数进行重载
	@Override
	public int locationToIndex(Point dim) {
		int cnt = super.locationToIndex(dim);
		if (cnt != -1 && !getCellBounds(cnt, cnt).contains(dim)) {
			return -1;
		} else {
			return cnt;
		}
	}

	// 该方法用于设置字体
	private void initGlobalFontSetting(Font fnt) {
		FontUIResource fontRes = new FontUIResource(fnt);
		for (Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, fontRes);
		}
	}

	// 接下来的几个方法用于添加相应的监听事件
	public void addCreateListener(ActionListener listener) {
		createItem.addActionListener(listener);
	}

	public void addZipListener(ActionListener listener) {
		zipOption.addActionListener(listener);
	}

	public void addUnzipListener(ActionListener listener) {
		unzipOption.addActionListener(listener);
	}

	public void addDeleteListener(ActionListener listener) {
		deleteItem.addActionListener(listener);
	}

	public void addMyMouseListener(MouseAdapter adapter) {
		addMouseListener(adapter);
	}

	public void showFileMenu(MouseEvent event) {
		fileMenu.show(event.getComponent(), event.getX(), event.getY());
	}

	public void showDicMenu(MouseEvent event) {
		foldMenu.show(event.getComponent(), event.getX(), event.getY());
	}

	public void addCopyListener(ActionListener listener) {
		copyOption.addActionListener(listener);
	}

	public void addPasteListener(ActionListener listener) {
		pasteOption.addActionListener(listener);
	}

	public void addEncryptListener(ActionListener listener) {
		encryptOption.addActionListener(listener);
	}

	public void addDecryptListener(ActionListener listener) {
		decryptOption.addActionListener(listener);
	}

	private void setMenuItemStyle(JMenuItem p, String q) {
		p.setText(q);
		p.setBorderPainted(false);
	}

	/*
	 * 用于对整个界面图形的更新
	 */
	public void updateView(List<File> files) {
		DefaultListModel<File> initialModel = new DefaultListModel<>();
		for (File tmp : files) {
			initialModel.addElement(tmp);
		}
		setModel(initialModel);
	}
}
