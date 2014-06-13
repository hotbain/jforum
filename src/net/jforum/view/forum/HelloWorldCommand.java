package net.jforum.view.forum;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.NEW;

import net.jforum.Command;

public class HelloWorldCommand extends Command {

	@Override
	public void list() {
		
	}
	
	public void test(){
		this.context.put("name", "¹¬Õþ");
		this.setTemplateName("helloworld.test");
		this.context.put("names", new String[]{"a","b"});
		this.context.put("pwd", "123");
		List<User> users =new ArrayList<HelloWorldCommand.User>();
		users.add(new User("¹¬Õþ", 23));
		users.add(new User("yang", 121));
		this.context.put("123", "xxxxx");
		this.context.put("users", users);
		
	}
	public  static class User{
		private String name;
		private int code;
		public User(String name, int code) {
			super();
			this.name = name;
			this.code = code;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		
	}

}
