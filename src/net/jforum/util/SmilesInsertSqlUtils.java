package net.jforum.util;

import java.io.File;

/**
 * <strong>   
 * 	1、制定smile的目录
 * 2、运行本程序
 * 3、在jforum数据库运行 delete from jforum.smiles;
 * 4、运行本程序的输出结果
 * </strong>
 * @author gongzheng </p>
 * at  2014-6-18
 */
public class SmilesInsertSqlUtils {

	private static String smilesUrl="E:\\55tuan_workspace\\jforum\\WebContent\\images\\smilies"; 

	public static void main(String[] args) {
		File smilesDir=new File(smilesUrl);
		File[] files =smilesDir.listFiles();
		for(File file : files){
			//INSERT INTO jforum_smilies (code, url, disk_name) VALUES (':(', '<img src=\"#CONTEXT#/images/smilies/9d71f0541cff0a302a0309c5079e8dee.gif\" />', '9d71f0541cff0a302a0309c5079e8dee.gif');

			String s =file.getName().split("\\.")[0];
			System.out.println("INSERT INTO jforum_smilies (code, url, disk_name) VALUES ('(:"+s+")', '<img src=\"#CONTEXT#/images/smilies/"+file.getName()+"\" />', '"+file.getName()+"');");
			
		}
	}
}
