package net.jforum.util;

import java.io.File;

/**
 * <strong>   
 * 	1���ƶ�smile��Ŀ¼
 * 2�����б�����
 * 3����jforum���ݿ����� delete from jforum.smiles;
 * 4�����б������������
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
