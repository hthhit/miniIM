package server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import json.client.access.ClosingChannel;
import json.client.login.ClientRegister;
import json.util.JSONNameandString;
import org.junit.Test;
import com.alibaba.fastjson.JSON;
import client.BaseClient;
import client.ClientManage;

@Deprecated
public class BaseClientandServer {

	@Test
	public void TestBaseLogin() throws InterruptedException{
		BaseClient baseclient1 = new BaseClient();
		BaseServer baseserver = new BaseServer();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient1);
		Thread.sleep(3000);
	}
	
	@Test
	public void TestBaseLoginWithWrongName() throws InterruptedException{
		BaseClient baseclient = new BaseClient();
		BaseServer baseserver = new BaseServer();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient);
		Thread.sleep(3000);
	}
	
	@Test
	public void TestBaseLoginWithWrongPassword() throws InterruptedException{
		BaseClient baseclient = new BaseClient();
		BaseServer baseserver = new BaseServer();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient);
		Thread.sleep(3000);
	}
	
	@Deprecated
	@Test
	public void TestBaseRegister() throws InterruptedException{
		BaseServer baseserver = new BaseServer();
		ClientRegister clientRegister = new ClientRegister();
		clientRegister.setUserName("user10");
		clientRegister.setUserPassword("123");
		clientRegister.setEmail("user10email@123.com");
		
		JSONNameandString json = new JSONNameandString();
		json.setJSONName(ClientRegister.class.getName());
		json.setJSONStr(JSON.toJSONString(clientRegister));
		
		BaseClient baseclient = new BaseClient();

		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient);
		Thread.sleep(3000);
		ClientManage.sendJSONNameandString(json);
		Thread.sleep(1000);
		
/*		String query = "select * from user where username =\'user10\';";
		Statement sta = StatementManager.getStatement();
		try {
			ResultSet resultSet = sta.executeQuery(query);
			if(resultSet.next()){
				System.out.println("find "+resultSet.getString("username")+" in database");
			}
			String Sql = "delete from user where username=\'user10\';";
			System.out.println(Sql);
			sta.executeUpdate(Sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(sta!=null)
				StatementManager.backStatement(sta);
		}*/
		Thread.sleep(1000);
	}
	
	@Test
	public void TestBaseRegisterWithExistName() throws InterruptedException{
		BaseServer baseserver = new BaseServer();
		ClientRegister clientRegister = new ClientRegister();
		clientRegister.setUserName("user1");
		clientRegister.setUserPassword("123");
		clientRegister.setEmail("user10email@123.com");
		
		JSONNameandString json = new JSONNameandString();
		json.setJSONName(ClientRegister.class.getName());
		json.setJSONStr(JSON.toJSONString(clientRegister));
		
		BaseClient baseclient = new BaseClient();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient);
		Thread.sleep(3000);
		ClientManage.sendJSONNameandString(json);
		Thread.sleep(1000);
	}
	
	@Test
	public void TestCloseChannel() throws InterruptedException{
		BaseClient baseclient = new BaseClient();
		BaseServer baseserver = new BaseServer();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		threadPool.submit(baseserver);
		threadPool.submit(baseclient);
		Thread.sleep(3000);
		
		ClosingChannel closingChannel = new ClosingChannel();
		JSONNameandString json = new JSONNameandString();
		json.setJSONName(ClosingChannel.class.getName());
		json.setJSONStr(JSON.toJSONString(closingChannel));
		
		ClientManage.sendJSONNameandString(json);
		Thread.sleep(1000);
	}
}
