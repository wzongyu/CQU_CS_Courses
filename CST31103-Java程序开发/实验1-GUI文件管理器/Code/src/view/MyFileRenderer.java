package view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.border.Border;
import java.io.File;

/**
 * 定义我们自己的文件渲染
 */
public class MyFileRenderer extends DefaultListCellRenderer {
	Border myBorder = BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 1, true);

	// 接下来重写父类中的函数
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		File tmp = (File) value;
		Icon image = MyFileIcon.getFileIcon(tmp);
		setBorder(myBorder);
		setIcon(image);
		if (tmp.getName().equals("")) {
			setText(tmp.getPath());
		} else {
			setText(tmp.getName());
		}
		if (isSelected) {
			setBackground(Color.pink);
			setForeground(Color.black);
		} else {
			setBackground(Color.white);
			setForeground(Color.black);
		}
		return this;
	}
}
