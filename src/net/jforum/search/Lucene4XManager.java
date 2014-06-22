package net.jforum.search;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.lucene.JcsegAnalyzer4X;





import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.PostDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.entities.Post;
import net.jforum.repository.TopicRepository;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;


public class Lucene4XManager implements SearchManager {
	private  Map<String, Post> processiongMap =new ConcurrentHashMap<String, Post>();
	private  AtomicInteger unprocessingCounter = new AtomicInteger(0);
	private  int count = SystemGlobals.getIntValue(ConfigKeys.INDEX_COUNT_THREOLD);
	private  ReadWriteLock readWriteLock =new ReentrantReadWriteLock();
	private  Analyzer analyzer =null;
	private  IndexWriter indexWriter =null;
	private  IndexSearcher searcher =null;
	private  IndexReader indexReader =null;
	private  Directory directory =null;
	private static Lucene4XManager INSTANCE= new Lucene4XManager();
	private ExecutorService threadPool =Executors.newCachedThreadPool();
	
	static{
		
	}
	public static  void main(String[] args) throws IOException {
//		aaaa();
		Lucene4XManager manager =new Lucene4XManager();
		manager.init();
		
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		manager.create(getPost());
		
		manager.indexWriter.commit();
		SearchArgs searchArgs =new SearchArgs();
		searchArgs.setKeywords("窝窝团");
		searchArgs.setForumId(1);
		searchArgs.startFetchingAtRecord(0);
		manager.search(searchArgs);
		
		manager.destroy();
		
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
		post.setForumId(1);
		post.setText("我是中国人我在窝窝团工作郭德纲quote");
		post.setSubject("娃哈哈");
		post.setTopicId(12);
		post.setId(35);
		post.setTime(new Date());
		return post;
	}
	private  void aaaa() throws IOException{

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
	
	public static Lucene4XManager getInstance(){
		return INSTANCE;
	}
	@Override
	public void init() {

		analyzer = new JcsegAnalyzer4X(JcsegTaskConfig.SIMPLE_MODE);
		IndexWriterConfig indexWriterConfig =new IndexWriterConfig(Version.LUCENE_46, analyzer);
		indexWriterConfig.setMaxBufferedDocs(4);
		indexWriterConfig.setUseCompoundFile(true);
//		indexWriterConfig.setOpenMode(OpenMode.APPEND);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		
		try {
			directory =new SimpleFSDirectory(new File("C:\\index"));
//			Directory directory =new RAMDirectory();
			indexWriter =new IndexWriter(directory, indexWriterConfig);
			System.out.println("初始化完毕222222222222");
		} catch (IOException e) {
			e.printStackTrace();

			if(indexWriter!=null){
				try {
					indexWriter.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		
			throw new RuntimeException(e);
		}finally {}
		
	
	}

	@Override
	public void create(Post post) {
		try {
			int c =unprocessingCounter.addAndGet(1);
			
			processiongMap.put("create"+c, post);

			if(c>=count){
				unprocessingCounter.set(0);
				processAll();
				processiongMap.clear();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doCreate(Post post){
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
			//添加文档成功
			System.out.println("添加文档成功");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	
	private void processAll(){//每一次的processAll的过程中搜索都是中断的。
		
		try {
			Set<java.util.Map.Entry<String, Post>> entries =processiongMap.entrySet();
			for(java.util.Map.Entry<String, Post> entry : entries){
				String operation=entry.getKey();
				if(operation.contains("create")){
					doCreate(entry.getValue());
				}
			}
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();

			try {
				indexWriter.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		}finally{}
		try {//只要进入processAll就会进行重新打开reader，否则
			reOpenReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		try {
			indexWriter.deleteDocuments(new Term(SearchFields.Keyword.POST_ID, String.valueOf(post.getId())));
		} catch (Exception e) {
			e.printStackTrace();
			if(indexWriter!=null){
				try {
					indexWriter.rollback();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}finally{
			
		}
		create(post);
	}
	@SuppressWarnings("deprecation")
	/**
			1、 读取锁允许多个reader线程同时持有, 而写入锁最多只能有一个writter线程持有.
		   	2、 读写锁的使用场合: 读取共享数据的频率远大于修改共享数据的频率. 在上述场合下, 使用读写锁控制共享资源的访问, 可以提高并发性能.
		    3、如果一个线程已经持有了写入锁, 则可以再持有读写锁. 相反, 如果一个线程已经持有了读取锁, 则在释放该读取锁之前, 不能再持有写入锁.
		    4、可以调用写入锁的newCondition()方法获取与该写入锁绑定的Condition对象, 此时与普通的互斥锁并没有什么区别. 但是调用读取锁的newCondition()方法将抛出异常. 
	 **/
	private void reOpenReader() throws Exception{
//		readWriteLock.writeLock().lock();//再打开读取的过程中是不允许搜索的
		try {
			if(indexReader!=null){//程序第一次启动
				indexReader.close();
				System.out.println("关闭程序");
			}
			threadPool.shutdownNow();
			threadPool =Executors.newCachedThreadPool();
			indexReader = null;
			indexReader=IndexReader.open(directory);
			searcher = new IndexSearcher(indexReader, threadPool);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error occur when lucene4xmanger reOpenReader method"+ e.getMessage());
		}finally{
			System.out.println("释放了读锁");
//			readWriteLock.writeLock().unlock();
		}
		
	}

	@Override
	public SearchResult search(SearchArgs args) {
		ensureSearcherExist();//防止死锁----获得读锁的时候，还要获得写锁，就会出现问题。
//		Lock  readLock =readWriteLock.readLock();
//		readLock.lock();
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
//			TopDocs docs = searcher.search(query,null,SystemGlobals.getIntValue(ConfigKeys.SEARCH_FETCH_ALL_COUNT),Sort.RELEVANCE);
			TopDocs docs = searcher.search(query,null,SystemGlobals.getIntValue(ConfigKeys.SEARCH_FETCH_ALL_COUNT),Sort.RELEVANCE);

			System.err.println("docs count="+ docs.totalHits);
			
			
			int pageCount =args.fetchCount();
			int start = args.startFrom();
			int index = start;
			int end	=0;
			if(docs.scoreDocs.length>=1){
				end= start+ pageCount-1> docs.scoreDocs.length-1?  docs.scoreDocs.length-1: start+pageCount-1;
			}
			
			System.out.println("start="+ start+";end="+ end);
			int[] post_ids =new int[end-start+1];
			for(;start<=end;start++){
				Document document = searcher.doc(docs.scoreDocs[start].doc);
//				System.out.println("docId="+ docs.scoreDocs[start].doc+" score="+ docs.scoreDocs[start].score);
//				Terms terms =indexReader.getTermVector(doc.doc, "hobby");
//				parseDocument(document);
				post_ids[start-index]= Integer.parseInt(document.getField(SearchFields.Keyword.POST_ID).stringValue());
			
			}

		
//			if (hits != null && hits.length() > 0) {
////				return  new SearchResult(resultCollector.collect(args, hits, query), hits.length());
//			}
//			else {
//			}
			System.out.println("顺利返回 " + docs.scoreDocs.length + "; acctual count =" + post_ids.length);
			return new SearchResult(retrieveRealPosts(post_ids, query), docs.scoreDocs.length);
//			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
//			readLock.lock();
		}
		return new SearchResult(new ArrayList(), 0);	
	}


	private void ensureSearcherExist() {
		if(searcher!=null){
			return ;
		}
		try {
			reOpenReader();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		searcher =new IndexSearcher(indexReader,threadPool );
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
			indexWriter.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(indexWriter!=null){
				try {
					indexWriter.rollback();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			lock.unlock();
		}
	
	}

	public void destroy(){
		try {
			indexWriter.close();
			indexReader.close();
			threadPool.shutdownNow();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void reIndex() {
		TopicDAO topicDAO =DataAccessDriver.getInstance().newTopicDAO();
		PostDAO  postDAO = DataAccessDriver.getInstance().newPostDAO();
		//TODO 清空以前的索引----删除锁文件、清空目录;
		List<Integer> postIds =topicDAO.getAllFirstPostIds();
		for(Integer postId : postIds){
			Post post =postDAO.selectById(postId);
			create(post);
		}
	}
	
}
