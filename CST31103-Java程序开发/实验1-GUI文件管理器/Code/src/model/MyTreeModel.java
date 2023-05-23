package model;

import java.io.File;

/**
 * 该类是文件树的模本
 * 
 * @author WuZongyu
 */
public class MyTreeModel {
	public File[] getRoot() {
		return File.listRoots();
	}

	public File[] getFiles(File FileNow) {
		return FileNow.listFiles();
	}
}
