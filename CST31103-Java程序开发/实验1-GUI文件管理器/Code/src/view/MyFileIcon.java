package view;

import javax.swing.*;
import java.io.File;
import javax.swing.filechooser.FileSystemView;

/*
 * 该类是用于获取系统图标
 */
public class MyFileIcon {
	public static Icon getFileIcon(File myfile) {
		if (myfile != null && myfile.exists()) {
			FileSystemView t = FileSystemView.getFileSystemView();
			return (t.getSystemIcon(myfile));
		}
		return (null);
	}
}
