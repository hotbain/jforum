package net.jforum.search;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.lucene.JcsegAnalyzer4X;





import net.jforum.dao.DataAccessDriver;
import net.jforum.entities.Post;


public class Lucene4XManager implements SearchManager {
	private static Map<String, Post> processiongMap =new ConcurrentHashMap<String, Post>();
	private static AtomicInteger unprocessingCounter = new AtomicInteger(0);
	private static int count = 1;
	private static Lock lock =new ReentrantLock();
	private static  Analyzer analyzer =null;
	private static IndexWriter indexWriter =null;
	private static IndexSearcher searcher =null;
	private static IndexReader indexReader =null;
	static{
		analyzer = new JcsegAnalyzer4X(JcsegTaskConfig.COMPLEX_MODE);
		IndexWriterConfig indexWriterConfig =new IndexWriterConfig(Version.LUCENE_46, analyzer);
		indexWriterConfig.setMaxBufferedDocs(4);
		indexWriterConfig.setUseCompoundFile(true);
		indexWriterConfig.setOpenMode(OpenMode.APPEND);
		try {
			Directory directory =new SimpleFSDirectory(new File("C:\\index"));
			indexWriter =new IndexWriter(directory, indexWriterConfig);
			indexReader=DirectoryReader.open( directory);
			searcher =new IndexSearcher(indexReader, Executors.newCachedThreadPool());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	public static void main(String[] args) throws IOException {
//		aaaa();
		Lucene4XManager manager =new Lucene4XManager();
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
		SearchArgs searchArgs =new SearchArgs();
		searchArgs.setKeywords("quote");
		searchArgs.setForumId(1);
		manager.search(searchArgs);
		
		indexWriter.close();
		
//		Map<String, String> map =new ConcurrentHashMap<String, String>();
//		map.put("a", "a");
//		map.put("b", "b");
//		map.put("c", "c");
//		Set<java.util.Map.Entry<String, String>> entries =map.entrySet();
//		for(java.util.Map.Entry<String, String> entry : entries){
//			System.out.println(entry.getKey());
//		}
		
	}
	private static Post getPost() {
		Post post =new Post();
		post.setForumId(123);
		post.setText("这里是内容");
		post.setSubject("这个是标题");
		post.setTopicId(333);
		post.setId(111);
		post.setTime(new Date());
		return post;
	}
	private static void aaaa() throws IOException{

		System.out.println("hello world");
		TokenStream tokenStream =analyzer.tokenStream("xxxx", new StringReader("我是中国人我在窝窝团工作郭德纲"));
		tokenStream.reset();
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);

		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);

		TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
		PayloadAttribute payloadAttribute =tokenStream.addAttribute(PayloadAttribute.class);
		//+ "payload="+new String(payloadAttribute.getPayload().bytes)
		while (tokenStream.incrementToken()) {
			
			System.out.println(typeAttribute.type() + " : " + charTermAttribute.toString() + ":position("
					+ positionIncrementAttribute.getPositionIncrement() + ")" + " StartOffset:" + offsetAttribute.startOffset() + " endOffset:"
					+ offsetAttribute.endOffset()+";");
			if(payloadAttribute!=null){
				if(payloadAttribute.getPayload()!=null){
					System.out.println("分析后的payload为"+ new String(payloadAttribute.getPayload().bytes));
				}
			}
		}
	
	}
	@Override
	public void init() {
		
		
	}

	@Override
	public void create(Post post) {
		lock.lock();
		try {
			int c =unprocessingCounter.addAndGet(1);
			
			processiongMap.put("create"+c, post);

			if(c>=count){
				processAll();
				unprocessingCounter.set(0);
				processiongMap.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}

	private void doCreate(Post post){
		ensureWriterOpen();
		Document d = new Document();
		
		int forumId =post.getForumId();
		d.add(new IntField(SearchFields.Keyword.FORUM_ID, forumId, getFieldType(true, true, false, NumericType.INT)));
		
		int postId = post.getId();
		d.add(new IntField(SearchFields.Keyword.POST_ID, postId, getFieldType(true,true, false, NumericType.INT)));
		
		int topicId = post.getTopicId();
		d.add(new IntField(SearchFields.Keyword.TOPIC_ID, topicId, getFieldType(true,true, false, NumericType.INT)));
		
		int userId = post.getUserId();
		d.add(new IntField(SearchFields.Keyword.USER_ID, userId, getFieldType(true,true, false, NumericType.INT)));
		
		Date createDate = post.getTime();
		d.add(new LongField(SearchFields.Keyword.DATE, createDate.getTime(), getFieldType(true, true, false, NumericType.LONG)));
		
		String subject = post.getSubject();
		FieldType typeSubject = new FieldType();
		typeSubject.setStored(true);//将标题设置为存储。用于后期的高亮处理
		typeSubject.setIndexed(true);
		typeSubject.setTokenized(true);
		d.add(new Field(SearchFields.Keyword.SUBJECT, subject, typeSubject));
		
		
		String content = post.getText();
		FieldType typeContent = new FieldType();
		typeContent.setStored(false);
		typeContent.setIndexed(true);
		typeContent.setTokenized(true);
		d.add(new Field(SearchFields.Keyword.CONTENT, content, typeContent));
		
		try {
			indexWriter.addDocument(d);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	private void ensureWriterOpen() {
	}
	private void processAll(){
		Set<java.util.Map.Entry<String, Post>> entries =processiongMap.entrySet();
		for(java.util.Map.Entry<String, Post> entry : entries){
			String operation=entry.getKey();
			if(operation.contains("create")){
				doCreate(entry.getValue());
			}
		}
		try {
			indexWriter.commit();
//			indexWriter.close();
			System.out.println("顺利关闭");
			//TODO 通知search,将索引重新载入
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private FieldType getFieldType(boolean indexed, boolean stored, boolean tokenized,
			NumericType numericType) {
		FieldType t = new FieldType();
		t.setIndexed(indexed);
		t.setStored(stored);
		t.setTokenized(tokenized);
		t.setNumericType(numericType);
		return t;
	}
	@Override
	public void update(Post post) {

//		lock.lock();
		try {
			indexWriter.deleteDocuments(new Term(SearchFields.Keyword.POST_ID, String.valueOf(post.getId())));
			create(post);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
//			lock.unlock();
		}
	}

	@Override
	public SearchResult search(SearchArgs args) {
		SearchResult searchResult = null;
		try {
			StringBuffer criteria = new StringBuffer(256);
			this.filterByForum(args, criteria);
			this.filterByKeywords(args, criteria);
//			this.filterByDateRange(args, criteria);
			System.out.println(criteria.toString());
//			Query query = new QueryParser("", new StandardAnalyzer()).parse(criteria.toString());
			Query query = new QueryParser(Version.LUCENE_46, "hobby", analyzer).parse(criteria.toString());
//			if (logger.isDebugEnabled()) {
//				logger.debug("Generated query: " + query);
//			}
			

//			qr.setBoost(5f);
//			TopDocs docs = searcher.search(qr,getFilter(),12,getSort());
			TopDocs docs = searcher.search(query,null,12,Sort.RELEVANCE);
			System.out.println("docs count="+ docs.totalHits);
			//与getSumDocFreq功能相同
			int[] post_ids =new int[docs.totalHits];
			int  index = 0;
			
			for (ScoreDoc doc : docs.scoreDocs) {
				
				Document document = searcher.doc(doc.doc);
				System.out.println("docId="+ doc.doc+" score="+ doc.score);
//				Terms terms =indexReader.getTermVector(doc.doc, "hobby");
				parseDocument(document);
				post_ids[index]= Integer.parseInt(document.getField(SearchFields.Keyword.POST_ID).stringValue());
			}
		
//			if (hits != null && hits.length() > 0) {
////				return  new SearchResult(resultCollector.collect(args, hits, query), hits.length());
//			}
//			else {
//			}
			System.out.println("顺利返回");
			return new SearchResult(retrieveRealPosts(post_ids, query), docs.totalHits);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SearchResult(new ArrayList(), 0);	
	}


	private List retrieveRealPosts(int[] postIds, Query query) throws Exception
	{
		List posts = DataAccessDriver.getInstance().newLuceneDAO().getPostsData(postIds);
		
		for (Iterator iter = posts.iterator(); iter.hasNext(); ) {
			Post post = (Post)iter.next();
			
			Scorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(scorer);
			
			TokenStream tokenStream = analyzer.tokenStream(
				SearchFields.Indexed.CONTENT, new StringReader(post.getText()));

			String fragment = highlighter.getBestFragment(tokenStream, post.getText());
			post.setText(fragment != null ? fragment : post.getText());
		}
		
		return posts;
	}
	
	public static void parseDocument(Document document){
		//得到存储的field的列表，一个field没有设置为存储的话，那么这个field只能用来搜索document，得到document之后，
		//再次访问 fields就可以访问别的具有存储特性的field了
		//要搜索到一个document，可以通过一个具有索引特性的field搜索，然后再去访问这个document中具有存储特性的field
		Iterator<IndexableField> iterator =document.getFields().iterator();
		
		while(iterator.hasNext()){
			
			IndexableField field = iterator.next();
		
			String fieldName =field.name();
			IndexableFieldType fieldType =field.fieldType();
//			byte[]  binaryValue= field.binaryValue().bytes;
			String value =field.stringValue();
			System.err.println(fieldName+"("+fieldType+")---"+ value);
		}
	}
	
//	private void filterByDateRange(SearchArgs args, StringBuffer criteria)
//	{
//		if (args.getFromDate() != null) {
//			criteria.append('(')
//			.append(SearchFields.Keyword.DATE)
//			.append(": [")
//			.append(this.settings.formatDateTime(args.getFromDate()))
//			.append(" TO ")
//			.append(this.settings.formatDateTime(args.getToDate()))
//			.append(']')
//			.append(')');
//		}
//	}
	
	public List<String> analyzeKeywords(String rawKeywords){
		TokenStream tStream;
		List<String> result =null;
		try {
			tStream = analyzer.tokenStream("xxxxx", new StringReader(rawKeywords));
			CharTermAttribute charTermAttribute =tStream.addAttribute(CharTermAttribute.class);
			tStream.reset();
			result= new ArrayList<String>();
			while (tStream.incrementToken()) {
				System.out.println(charTermAttribute.toString());
				result.add(charTermAttribute.toString());
			}
			tStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private void filterByKeywords(SearchArgs args, StringBuffer criteria)
	{
		
		List<String> keywords = this.analyzeKeywords(args.rawKeywords());
		
		for(String str : keywords){
			if (args.shouldMatchAllKeywords()) {
				criteria.append(" +");
			}
			criteria.append('(')
			.append(SearchFields.Indexed.CONTENT)
			.append(':')
			.append(QueryParser.escape(str))
			.append(") ");
		}
	}

	private void filterByForum(SearchArgs args, StringBuffer criteria)
	{
		if (args.getForumId() > 0) {
			criteria.append("(")
				.append(SearchFields.Keyword.FORUM_ID)
				.append(':')
				.append(args.getForumId())
				.append(") ");
		}
	}
	
	
	@Override
	public void delete(Post p) {

//		lock.lock();
		try {
			indexWriter.deleteDocuments(new Term(SearchFields.Keyword.POST_ID, String.valueOf(p.getId())));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
//			lock.unlock();
		}
	
	}

	public void destroy(){
		try {
			indexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
