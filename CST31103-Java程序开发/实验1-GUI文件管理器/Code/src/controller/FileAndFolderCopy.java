package controller;

import javax.swing.*;
import java.io.*;

/*
 * 该类用于实现复制和粘贴功能
 * 同时实现了文件与文件夹
 */
public class FileAndFolderCopy {
	/*
	 * 该方法用于判断其是否是其父类
	 */
	private static boolean isParent(File F1, File F2) {
		if (F2.getParentFile() == null) {
			return false;
		} else if (F2.getParentFile().equals(F1)) {
			return true;
		} else {
			return isParent(F1, F2.getParentFile());
		}
	}

	/*
	 * 接下来定义的函数实现了文件的复制功能
	 * 
	 * @param f1 为待复制文件名
	 * 
	 * @param f2 为将被复制到何处的文件名
	 */
	public static void copyFile(String s1, String s2) {
		File F1 = new File(s1);
		File F2 = new File(s2);
		try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(F1));
				BufferedOutputStream fou = new BufferedOutputStream(new FileOutputStream(F2))) {
			byte[] flush = new byte[1024];
			int cnt = 0;
			while ((cnt = fin.read(flush)) != -1) {
				fou.write(flush, 0, cnt);
			}
			fou.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 接下来定义的函数实现了文件夹的复制功能
	 */
	public static void copyDir(String s1, String s2) {
		File F1 = new File(s1);
		File F2 = new File(s2);
		if (isParent(F1, F2)) {
			System.out.println("不能够进行复制");
			JOptionPane.showMessageDialog(null, "待拷贝目录为当前目录的父节点", "拷贝错误r", JOptionPane.ERROR_MESSAGE);
			return;
		}
		;

		if (!F2.exists()) {
			F2.mkdir();
		}
		String[] arr = F1.list();
		for (String f : arr) {
			String t1 = s1 + File.separator + f;
			String t2 = s2 + File.separator + f;
			if (new File(t1).isDirectory()) {
				copyDir(t1, t2);
			} else {
				copyFile(t1, t2);
			}
		}
	}
}
