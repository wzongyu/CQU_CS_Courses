package controller;

import java.io.*;
import java.util.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipFile;

/**
 * 该类用于实现文件和文件夹的压缩以及解压
 */
public class FileAndFolderZipAndUnzip {

	/**
	 * 该方法实现了文件的压缩
	 */
	public static void compress(List<String> name, String path) {
		File FS = new File(path);
		ZipOutputStream zout = null;
		try {
			FileOutputStream fos = new FileOutputStream(FS);
			CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32());
			zout = new ZipOutputStream(cos);
			String s = "";
			for (int i = 0; i < name.size(); i++) {
				compress(new File(name.get(i)), zout, s);
			}
			zout.close(); // 关闭输出流
		} catch (Exception e) { // 捕获相应的异常
			throw new RuntimeException(e);
		}
	}

	/**
	 * 压缩单个文件
	 */
	public static void compress(String s1, String s2) {
		File f1 = new File(s1);
		File f2 = new File(s2);
		if (!f1.exists())
			throw new RuntimeException(s1 + "--此文件不存在");
		try {
			FileOutputStream fout = new FileOutputStream(f2);
			CheckedOutputStream cos = new CheckedOutputStream(fout, new CRC32());
			String str = "";
			ZipOutputStream zout = new ZipOutputStream(cos);
			compress(f1, zout, str);
			zout.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对一个文件夹进行压缩(目录）
	 */
	private static void compressFold(File fold, ZipOutputStream zout, String str) {
		if (!fold.exists())
			return;
		File[] files = fold.listFiles();
		for (int i = 0; i < files.length; i++) {
			compress(files[i], zout, str + fold.getName() + "/");
		}
	}

	private static void compress(File f, ZipOutputStream zout, String str) {
		// 对应于目录
		if (f.isDirectory()) {
			System.out.println("压缩：" + str + f.getName());
			compressFold(f, zout, str);
			// 对应于如果不是目录
		} else {
			System.out.println("压缩：" + str + f.getName());
			compressFile(f, zout, str);
		}
	}

	private static void compressFile(File f, ZipOutputStream out, String str) {
		if (!f.exists()) {
			return;
		}
		try {
			int t;
			byte record[] = new byte[8192];
			BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
			ZipEntry en = new ZipEntry(str + f.getName());
			out.putNextEntry(en);
			while ((t = fin.read(record, 0, 8192)) != -1) {
				out.write(record, 0, t);
			}
			fin.close();
		} catch (Exception e) { // 捕获相应的异常
			throw new RuntimeException(e);
		}
	}

	/**
	 * 接下来定义方法即为相应的解压
	 */
	public static void zipUncompress(String F, String str) throws Exception {
		File f1 = new File(F);
		ZipFile f2 = new ZipFile(f1);
		int cnt;
		byte[] buf = new byte[1024];
		Enumeration<?> starts = f2.entries();
		if (!f1.exists()) {
			throw new Exception(f1.getPath() + "处不存在文件"); // 如果文件不存在则抛出异常
		}
		// 从此处开始进行解压
		while (starts.hasMoreElements()) {
			ZipEntry q = (ZipEntry) starts.nextElement();
			// 如果是目录则进行相应的处理
			if (q.isDirectory()) {
				String dd = str + "/" + q.getName();
				f1.mkdirs();
			} else {
				File to = new File(str + "/" + q.getName());
				if (!to.getParentFile().exists()) {
					to.getParentFile().mkdirs();
				}
				to.createNewFile();
				InputStream fin = f2.getInputStream(q);
				FileOutputStream fout = new FileOutputStream(to);
				while ((cnt = fin.read(buf)) != -1) {
					fout.write(buf, 0, cnt);
				}
				// 关闭输入输出流
				fout.close();
				fin.close();
			}
		}
	}
}
