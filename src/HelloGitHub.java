import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;


public class HelloGitHub {

	public static void main(String[] args)  throws Exception{
		GitHub gitHub =GitHub.connectUsingPassword("hotbain", "gz19893707gz");
//		GHRepository repository=gitHub.createRepository("gongzheng1", "hello gongzheng1", "http://www.baidu.com", true);
		List<GHEventInfo> events =gitHub.getEvents();
		GHMyself mysqlGhMyself =gitHub.getMyself();
		Map<String,GHRepository> myRepos =mysqlGhMyself.getRepositories();
		for(Entry<String, GHRepository> repo : myRepos.entrySet()){
			System.out.println(repo.getKey()+"="+ repo.getValue());
		}
//		for(GHEventInfo info : events){
//			System.out.println(info.getRepository());
//			System.out.println(info.getType());
//		}
		
//		repository.delete();
	}
}
