package net.jforum.search;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.lucene.JcsegAnalyzer4X;



import net.jforum.entities.Post;

public class Lucene4XManager implements SearchManager {

	private static Map<String, Post> processiongMap =new ConcurrentHashMap<String, Post>();
	private static AtomicInteger unprocessingCounter = new AtomicInteger(0);
	private static int count = 1;
	private static Lock lock =new ReentrantLock();
	private static  Analyzer analyzer =null;
	private static IndexWriter indexWriter =null;
	static{
		analyzer = new JcsegAnalyzer4X(JcsegTaskConfig.COMPLEX_MODE);
		IndexWriterConfig indexWriterConfig =new IndexWriterConfig(Version.LUCENE_46, analyzer);
		indexWriterConfig.setMaxBufferedDocs(4);
		indexWriterConfig.setUseCompoundFile(true);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		try {
			Directory directory =new SimpleFSDirectory(new File("C:\\index"));
			indexWriter =new IndexWriter(directory, indexWriterConfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	public static void main(String[] args) throws IOException {
//		aaaa();
//		Lucene4XManager manager =new Lucene4XManager();
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
//		manager.create(getPost());
//		indexWriter.close();
		Map<String, String> map =new ConcurrentHashMap<String, String>();
		map.put("a", "a");
		map.put("b", "b");
		map.put("c", "c");
		Set<java.util.Map.Entry<String, String>> entries =map.entrySet();
		for(java.util.Map.Entry<String, String> entry : entries){
			System.out.println(entry.getKey());
		}
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

			if(c>count){
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
		typeSubject.setStored(true);
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
			/**
			 * else if(operation.equalsIgnoreCase("update")){
				doUpdate(entry.getValue());
			}else if(operation.equalsIgnoreCase("delete")){
				doDelete(entry.getValue());
			}
			 * */
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
	
	private void doDelete(Post value) {
		// TODO Auto-generated method stub
		
	}
	private void doUpdate(Post value) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
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
