package model;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import controller.*;

/**
 * 
 * @author WuZongyu 该类是文件的模板 成员变量hasBeenCopy用来记录被拷贝的文件 nowFile记录当前文件
 *         recordStack用于后续的回退和前进等功能 initialModel是默认生成的模块
 */
public class MyFileModel {
	/*
	 * 为了保持类的封装性 成员变量尽量使用private类型
	 */

	private List<String> hasBeenCopy;
	private File nowFile;
	private Stack<File> recordStack;
	private List<File> initialModel;

	// 接下来自行定义构造函数
	public MyFileModel() {
		initialModel = new ArrayList<>();
		hasBeenCopy = new ArrayList<>();
		recordStack = new Stack<>();
		createRoot();
	}

	/*
	 * 接下来定义的一些函数用于获取类的成员变量
	 */
	public List<File> getInitialModel() {
		return initialModel;
	}

	public String getLocation() { // 在FileController类中会使用到
		if (nowFile == null) {
			return null;
		}
		return nowFile.getPath(); // 获取当前文件路径
	}

	public void createRoot() { // 生成节点
		initialModel.clear();
		initialModel.addAll(Arrays.asList(File.listRoots()));
	}

	public void updateModels(File f) {
		nowFile = f;
		initialModel.clear();
		File[] files = f.listFiles();
		initialModel.addAll(Arrays.asList(files));
	}

	public void addhasBeenCopy(List<File> files) {
		for (File f : files) {
			String t = f.getPath();
			hasBeenCopy.add(t);
		}
	}

	public void enCrypt(List<File> files, int key) {
		try {
			for (File file : files) {
				if (file.isFile()) {
					String name = file.getName();
					String parent = file.getParent();
					File tmp = new File(parent + File.separator + "Encrpted" + name);
					FileEncryptAndDecrypt.Encrypt(file, tmp, key);
					file.delete();
					tmp.renameTo(new File(parent + File.separator + name));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deCrypt(List<File> files, int key) {
		try {
			for (File file : files) {
				File tmp = new File(file.getParent() + File.separator + "Decrpted" + file.getName());
				FileEncryptAndDecrypt.Decrypt(file, tmp, key);
				if (!initialModel.contains(tmp)) {
					initialModel.add(tmp);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void zipCompress(List<File> files, String str) {
		List<String> zipFiles = new ArrayList<>();
		for (File file : files) {
			zipFiles.add(file.getPath());
		}

		if (str != null) {
			FileAndFolderZipAndUnzip.compress(zipFiles, nowFile.getPath() + File.separator + str + ".zip");
			updateModels(nowFile);
		}
	}

	public void zipDeCompress(List<File> fileList) {
		for (File f : fileList) {
			try {
				FileAndFolderZipAndUnzip.zipUncompress(f.getPath(), nowFile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		updateModels(nowFile);
	}

	public void paste() {
		for (String s : hasBeenCopy) {
			File tmp = new File(s);
			if (tmp.isFile()) {
				FileAndFolderCopy.copyFile(s, nowFile.getPath() + File.separator + tmp.getName());
			} else {
				FileAndFolderCopy.copyDir(s, nowFile.getPath() + File.separator + tmp.getName());
			}
		}
		hasBeenCopy.clear();
		updateModels(nowFile);
	}

	public void createFile(String name) {
		File dir = new File(nowFile.getPath() + File.separator + name);
		if (!dir.exists()) {
			dir.mkdir();
			getInitialModel().add(dir);
		}
	}

	private boolean deleteHelp(File f) {
		if (!f.exists()) {
			return false;
		}
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File tmp : files) {
				deleteHelp(f);
			}
		}
		return f.delete();
	}

	public void delete(List<File> fileList) {
		for (File f : fileList) {
			initialModel.remove(f);
			deleteHelp(f);
		}
	}

	// 判断是否成功打开了文件
	public boolean openFile(File file) {
		if (file.isFile()) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException ex) {
				return false;
			}
		} else {
			updateModels(file);
			recordStack.clear();
		}
		return true;
	}

	// 该函数实现了回退功能
	public void back() {
		File f = nowFile;

		if (f == null) {
			createRoot();
			nowFile = null;
		}

		if (f.getParentFile() == null) {
			createRoot();
			nowFile = null;
		} else {
			updateModels(f.getParentFile());
		}
		recordStack.push(f);
	}

	// 该函数实现了前进功能
	public void forward() {
		if (recordStack.isEmpty()) {
			return;
		}
		File tmp = recordStack.pop();
		updateModels(tmp);
	}
}
