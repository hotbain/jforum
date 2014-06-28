package net.jforum.search.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import net.jforum.search.tokenfilter.EnhancedTokenFilter;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.lionsoul.jcseg.Dictionary;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.lucene.JcsegTokenizer;




public class EnhancedJcsegAnalyzer extends StopwordAnalyzerBase {
	

	  private final CharArraySet stemExclusionSet;
	   
	  /**
	   * Returns an unmodifiable instance of the default stop words set.
	   * @return default stop words set.
	   */
	  public static CharArraySet getDefaultStopSet(){
	    return DefaultSetHolder.DEFAULT_STOP_SET;
	  }
	  
	  /**
	   * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer class 
	   * accesses the static final set the first time.;
	   */
	  private static class DefaultSetHolder {
	    static final CharArraySet DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
	  }

	  /**
	   * Builds an analyzer with the default stop words: {@link #getDefaultStopSet}.
	   */
	  public EnhancedJcsegAnalyzer(Version matchVersion) {
	    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
	  }
	  
	  /**
	   * Builds an analyzer with the given stop words.
	   * 
	   * @param matchVersion lucene compatibility version
	   * @param stopwords a stopword set
	   */
	  public EnhancedJcsegAnalyzer(Version matchVersion, CharArraySet stopwords) {
	    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
	  }

	  /**
	   * Builds an analyzer with the given stop words. If a non-empty stem exclusion set is
	   * provided this analyzer will add a {@link SetKeywordMarkerFilter} before
	   * stemming.
	   * 
	   * @param matchVersion lucene compatibility version
	   * @param stopwords a stopword set
	   * @param stemExclusionSet a set of terms not to be stemmed
	   */
	  public EnhancedJcsegAnalyzer(Version matchVersion, CharArraySet stopwords, CharArraySet stemExclusionSet) {
	    super(matchVersion, stopwords);
	    this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(
	        matchVersion, stemExclusionSet));
	  }

	  /**
	   * Creates a
	   * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	   * which tokenizes all the text in the provided {@link Reader}.
	   * 
	   * @return A
	   *         {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	   *         built from an {@link StandardTokenizer} filtered with
	   *         {@link StandardFilter}, {@link EnglishPossessiveFilter}, 
	   *         {@link LowerCaseFilter}, {@link StopFilter}
	   *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is
	   *         provided and {@link PorterStemFilter}.
	   */
	  @Override
	  protected TokenStreamComponents createComponents(String fieldName,
	      Reader reader) {

			JcsegTaskConfig config =new JcsegTaskConfig("D:\\jcseg.properties");
			config.setAppendCJKPinyin(true);
			Dictionary dictionary =new Dictionary(config, false);
			try {
				dictionary.loadFromLexiconDirectory("E:\\dropbox\\Dropbox\\jcseg\\lexicon");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Tokenizer source =null;
			try {
				//TokenFilter过滤器
				source=new JcsegTokenizer(reader, JcsegTaskConfig.COMPLEX_MODE, config, dictionary);
//				source=new WhitespaceTokenizer(Version.LUCENE_46, reader);
				
				//TokenFilten可以用来过滤或者同义词替换。
				
//				ts = new MysameTokenFilter(new JcsegTokenizer(reader, JcsegTaskConfig.COMPLEX_MODE, config, dictionary));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    TokenStream tokenStream=source;
	    
	    tokenStream= new StandardFilter(matchVersion, tokenStream);
	    
//	    tokenStream = new PorterStemFilter(tokenStream);

    	
    	tokenStream = new LowerCaseFilter(Version.LUCENE_46,tokenStream);
    	
    	try {
			tokenStream =new EnhancedTokenFilter(tokenStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
//    	Collection<String>  chars =new ArrayList<String>();
//    	chars.add("gongzheng");
    	
//    	CharArraySet stopSet= new CharArraySet(Version.LUCENE_46, chars, true);
//    	tokenStream = new StopFilter(Version.LUCENE_46, tokenStream, stopSet);
//    	tokenStream= new WhitespaceTokenFilter(tokenStream);
//    	tokenStream =new WhitespaceTokenizer(Version.LUCENE_46, tokenStr)
    	
//    	tokenStream= new EdgeNGramTokenFilter(Version.LUCENE_46, tokenStream, 1, 12);
//    	tokenStream =new ReverseStringFilter(Version.LUCENE_46, tokenStream);
//    	tokenStream= new EdgeNGramTokenFilter(Version.LUCENE_46, tokenStream, 1, 8);

	    return new TokenStreamComponents(source, tokenStream);
	  }
	  


	public static void main(String[] args) throws IOException {
		// 通过为jcseg 的jcseg.clearstopword=1 来将停用词去掉
		@SuppressWarnings("resource")
		//对人名的辨别非常的好
		TokenStream tokenStream = new EnhancedJcsegAnalyzer(Version.LUCENE_46).tokenStream("name", new StringReader("北京市郭德纲hello world my name is gongzheng what's your name? hers is very goods drived a.mapbar@dfad.net http://www.baidu.com?name=hello《jav编程思想》习近瓶宫正 闫加宝mike我喜欢篮球《货币》吴华：“：‘;':>?<<>、，、》编程 bian cheng、是都短发牛奶不如果汁好喝"));
//		System.out.println(tokenStream);
		// 必须调用否则不会出现结果
		tokenStream.reset();

		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);

		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);

		TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

		while (tokenStream.incrementToken()) {
			System.out.println(typeAttribute.type() + " : " + charTermAttribute.toString() + ":position("
					+ positionIncrementAttribute.getPositionIncrement() + ")" + " StartOffset:" + offsetAttribute.startOffset() + " endOffset:"
					+ offsetAttribute.endOffset());
		}

	}
	

	  
}
