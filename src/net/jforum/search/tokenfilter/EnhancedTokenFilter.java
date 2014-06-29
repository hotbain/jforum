package net.jforum.search.tokenfilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.BytesRef;

public class EnhancedTokenFilter  extends TokenFilter{

	private CharTermAttribute charTermAttribute ;
	private PositionIncrementAttribute pia ;
	private PayloadAttribute payloadAttribute;
	private Stack<String> sameWords = new Stack<String>();
	
	private State currentState;
	/**
	 * ��һ��tokenstream��Ϊ���룬����������Ϊ��������
	 * @param input
	 * @throws IOException
	 */
	public EnhancedTokenFilter(TokenStream input) throws IOException {
		super(input);
		this.charTermAttribute =this.addAttribute(CharTermAttribute.class);
		this.pia =this.addAttribute(PositionIncrementAttribute.class);
		this.payloadAttribute =this.addAttribute(PayloadAttribute.class);
	}

	

	 
	 /**
	  * �ַ�������ת���ַ���(���ŷָ�)
	  * @author wyh
	  * @param stringSet
	  * @return
	  */
	 public static String makeStringByStringSet(Set<String> stringSet){
	  StringBuilder str = new StringBuilder();
	  int i=0;
	  for(String s : stringSet){
	   if(i == stringSet.size() - 1){
	    str.append(s);
	   }else{
	    str.append(s + ",");
	   }
	   i++;
	  }
	  return str.toString().toLowerCase();
	 }
	 
	 /**
	  * ��ȡƴ������
	  * @author wyh
	  * @param src
	  * @return Set<String>
	  */
	 public static Set<String> getPinyin(String src){
	  if(src!=null && !src.trim().equalsIgnoreCase("")){
	   char[] srcChar ;
	   srcChar=src.toCharArray();
	   //����ƴ����ʽ�����
	   HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

	//������ã���Сд�����귽ʽ��
	   hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE); 
	   hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	   hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	   
	   String[][] temp = new String[src.length()][];
	   for(int i=0;i<srcChar.length;i++){
	    char c = srcChar[i];
	    //�����Ļ���a-z����A-Zת��ƴ��(�ҵ������Ǳ������Ļ���a-z����A-Z)
	    if(String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")){
	     try{
	      temp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
	     }catch(BadHanyuPinyinOutputFormatCombination e) {
	      e.printStackTrace();
	     }
	    }else if(((int)c>=65 && (int)c<=90) || ((int)c>=97 && (int)c<=122)){
	     temp[i] = new String[]{String.valueOf(srcChar[i])};
	    }else{
	     temp[i] = new String[]{""};
	    }
	   }
	   StringBuilder stringBuilder =new StringBuilder();
	   try {
		   for(String[] pinyinGroup : temp){
//			   pinyinGroup[0].length()>=1? stringBuilder.append(pinyinGroup[0].charAt(0)),"";
			   stringBuilder.append(pinyinGroup[0].charAt(0));
		   }
	} catch (Exception e) {
		stringBuilder=null;
	}
	   String[] pingyinArray = Exchange(temp);
	   Set<String> pinyinSet = new HashSet<String>();
	  
	   for(int i=0;i<pingyinArray.length;i++){
		   pinyinSet.add(pingyinArray[i]);
	   }
	   if(stringBuilder!=null&&!stringBuilder.toString().equals(src)){
		   System.out.println(src+"="+ stringBuilder.toString());
		   pinyinSet.add(stringBuilder.toString());
	   }
	   return pinyinSet;
	  }
	  return null;
	 }
	 
	 /**
	  * �ݹ�
	  * @author wyh
	  * @param strJaggedArray
	  * @return
	  */
	    public static String[] Exchange(String[][] strJaggedArray){
	        String[][] temp = DoExchange(strJaggedArray);
	        return temp[0];       
	    }
	   
	    /**
	     * �ݹ�
	     * @author wyh
	     * @param strJaggedArray
	     * @return
	     */
	    private static String[][] DoExchange(String[][] strJaggedArray){
	        int len = strJaggedArray.length;
	        if(len >= 2){           
	            int len1 = strJaggedArray[0].length;
	            int len2 = strJaggedArray[1].length;
	            int newlen = len1*len2;
	            String[] temp = new String[newlen];
	            int Index = 0;
	            for(int i=0;i<len1;i++){
	                for(int j=0;j<len2;j++){
	                    temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
	                    Index ++;
	                }
	            }
	            String[][] newArray = new String[len-1][];
	            for(int i=2;i<len;i++){
	                newArray[i-1] = strJaggedArray[i];                           
	            }
	            newArray[0] = temp;
	            return DoExchange(newArray);
	        }else{
	         return strJaggedArray;   
	        }
	    }
	 /**
	  * @param args
	  */
	 public static void main(String[] args) {
	  String str = "�����ඥ";
	  System.out.println(makeStringByStringSet(getPinyin(str)));

	}


	 
	
	@Override
	public boolean incrementToken() throws IOException {
//		System.err.println("�Ƿ�����Ϊ�գ� "+ (this.payloadAttribute==null));
		BytesRef ref = new BytesRef(UUID.randomUUID().toString().getBytes());
//		System.out.println("��ӡ������payLoadΪ"+new String(ref.bytes));
		if(!this.sameWords.empty()){
			//�Ƿ���ͬ���
			String str = sameWords.pop();
			//�ظ�״̬
			super.restoreState(currentState);
			//��ǰ�����ֻ����µģ���Ҫ��
			this.charTermAttribute.setEmpty();
			//�滻ԭ�ȵ�����
			this.charTermAttribute.append(str);
			//������������Ϊ0
			pia.setPositionIncrement(0);
			this.payloadAttribute.setPayload(ref);
			return true;
		}
		if(!input.incrementToken()){
			return false;
		}
		boolean hasSame =getSameWords(this.charTermAttribute.toString());
		if(hasSame){
			this.currentState= super.captureState();
		}
		this.payloadAttribute.setPayload(ref);
		return true;
	}




	private boolean getSameWords(String k) {
		Set<String> aliasSet= getPinyin(k);
		if(aliasSet==null){
			return false;
		}
		if(aliasSet.contains(k.toString())){
			aliasSet.remove(k.toString());
		}
		
		if (!aliasSet.isEmpty()) {
			for(String alias : aliasSet){
				this.sameWords.add(alias);
			}
			System.out.println(k+"��ͬ���Ϊ"+ sameWords);
//			System.out.println(k+"'s alias size=" +aliasSet.size()+";alias content is "+ aliasSet);
			return true;
		}
		return false;
		
	}

}
