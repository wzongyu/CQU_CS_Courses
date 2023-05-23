package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import model.MyFileModel;
import model.MyTreeModel;
import view.Main;
import view.MyFileDisplay;
import view.MyFileTree;

/**
 * 该类用于实现总体的控制功能
 * 
 * @author WuZongyu
 *
 */
public class FileController {
	private MyFileModel fileModel; // 文件模板
	private MyFileTree filesTree;
	private MyTreeModel treeModel;
	private MyFileDisplay fileImage;
	private Main gui; // 与用于交互的界面

	/*
	 * 自定义构造函数
	 */
	public FileController(Main image, MyFileModel fm, MyTreeModel tm) {
		// 初始化赋值
		this.gui = image;
		this.filesTree = image.getFilesTree();
		this.fileModel = fm;
		this.treeModel = tm;
		this.fileImage = image.getFileDisplay();

		// 初始化添加事件监听
		fileImage.addCopyListener(new CopyListener());
		fileImage.addCreateListener(new CreateFileListener());
		fileImage.addPasteListener(new PasteListener());
		fileImage.addUnzipListener(new UnZipListener());
		fileImage.addZipListener(new ZipListener());
		fileImage.addDeleteListener(new DeleteListener());
		fileImage.addMyMouseListener(new ClickListener());
		fileImage.addEncryptListener(new EncryptListener());
		fileImage.addDecryptListener(new DecyptListener());
		image.addforwardListener(new forwardListener());
		image.addbackListener(new backListener());
		image.addGoListener(new GoListener());
		filesTree.addTreeSelectionListener(new FileTreeSelListener());
		filesTree.setRootVisible(false);
		fileImage.updateView(fileModel.getInitialModel());

	}

	/**
	 * 该类定义了前进事件的监听器
	 *
	 */
	class forwardListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			fileModel.back();
			fileImage.updateView(fileModel.getInitialModel());
			String loc = fileModel.getLocation();
			gui.updateLocation(loc);
		}
	}

	/**
	 * 该类定义了后退事件的监听器
	 *
	 */
	class backListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			fileModel.forward();
			fileImage.updateView(fileModel.getInitialModel());
			String loc = fileModel.getLocation();
			gui.updateLocation(loc);
		}
	}

	/**
	 * 该类定义了跳转事件的监听器
	 */
	class GoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String str = gui.locationInfo();
			File file = new File(str);
			fileModel.updateModels(file);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	/**
	 * 该类定义了压缩事件的监听器
	 */
	class ZipListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> fileList = fileImage.getSelectedValuesList();
			String name = JOptionPane.showInputDialog("请输入压缩后的文件名");
			fileModel.zipCompress(fileList, name);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	/**
	 * 该类定义了解压事件的监听器
	 */
	class UnZipListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> fileList = fileImage.getSelectedValuesList();
			fileModel.zipDeCompress(fileList);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class CopyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> t = fileImage.getSelectedValuesList();
			fileModel.addhasBeenCopy(t);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class PasteListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			fileModel.paste();
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class CreateFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			String name = JOptionPane.showInputDialog("请输入文件夹名字");
			if (name != null) {
				fileModel.createFile(name);
			}
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class DeleteListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> fileList = fileImage.getSelectedValuesList();
			fileModel.delete(fileList);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class EncryptListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> files = (List<File>) fileImage.getSelectedValuesList();
			int code = Integer.parseInt(JOptionPane.showInputDialog("请输入加密密码(整数)"));
			fileModel.enCrypt(files, code);
			fileImage.updateView(fileModel.getInitialModel());
		}
	}

	class DecyptListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<File> files = (List<File>) fileImage.getSelectedValuesList();
			int key = Integer.parseInt(JOptionPane.showInputDialog("请输入加密时输入的密码"));
			fileModel.deCrypt(files, key);
			fileImage.updateView(fileModel.getInitialModel());
			JOptionPane.showMessageDialog(null, "完成(如果密码错误将得到错误信息)");
		}
	}

	// 继承自鼠标适配器
	class ClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			JList<?> list = (JList<?>) e.getSource();
			if (list.locationToIndex(e.getPoint()) == -1 && !e.isShiftDown()) {
				list.clearSelection();
			}
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				if (!fileImage.getSelectedValuesList().isEmpty()) {
					fileImage.showFileMenu(event);
				} else {
					fileImage.showDicMenu(event);
				}
			} else if (event.getButton() == MouseEvent.BUTTON1 && !fileImage.getSelectedValuesList().isEmpty()) {
				if (event.getClickCount() == 2) {
					File file = (File) fileImage.getSelectedValue();
					if (!fileModel.openFile(file)) {
						JOptionPane.showMessageDialog(null, "没有应用与当前目录关联");
					}
					fileImage.updateView(fileModel.getInitialModel());
					String str = fileModel.getLocation();
					gui.updateLocation(str);
				}
			}
		}
	}

	class FileTreeSelListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			File file = filesTree.getCurrentFile();
			filesTree.loadingTree((DefaultMutableTreeNode) filesTree.getLastSelectedPathComponent(),
					treeModel.getFiles(file));
			fileModel.updateModels(file);
			fileImage.updateView(fileModel.getInitialModel());
			String str = fileModel.getLocation();
			gui.updateLocation(str);
		}
	}
}
