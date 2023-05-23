package controller;

import java.io.*;

/**
 * 该类对文件进行加密和解密 首先写加密函数 其次写解密函数 成员变量content用于判断是否读取到了文件结尾
 */
public class FileEncryptAndDecrypt {
	private static int content = 0;

	// 接下来定义加密函数
	public static void Encrypt(File F1, File F2, int num) throws Exception {
		InputStream fi = new FileInputStream(F1);
		OutputStream fo = new FileOutputStream(F2);
		System.out.println("开始加密...");
		while ((content = fi.read()) > -1) {
			fo.write(content ^ num);
		}
		System.out.println("加密成功...");
		fi.close();
		fo.flush();
		fo.close();
	}

	// 接下来定义解密函数
	public static void Decrypt(File F1, File F2, int num) throws Exception {
		InputStream fi = new FileInputStream(F1);
		OutputStream fo = new FileOutputStream(F2);
		System.out.println("开始解密...");
		while ((content = fi.read()) > -1) {
			fo.write(content ^ num);
		}
		System.out.println("解密成功...");
		fi.close();
		fo.flush();
		fo.close();
	}
}
