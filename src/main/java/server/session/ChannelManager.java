package server.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import util.EnDeCryProcess;
import com.alibaba.fastjson.JSON;
import json.server.session.SendBackJSON;
import json.util.JSONNameandString;
import server.db.DBCallable;
import server.db.StatementManager;

public class ChannelManager {

	private static ChannelManager channelmanager = new ChannelManager();
	
	volatile private ConcurrentHashMap<String,Channel> id2channel = new ConcurrentHashMap<String,Channel> ();
	//key is Channel id ,value is ChannelObject;
	volatile private ConcurrentHashMap<String,SecretKey> id2secrekey = new ConcurrentHashMap<String,SecretKey>();
	//key is Channel id ,value is Secrekey;
	volatile private ConcurrentHashMap<String,String> id2username = new ConcurrentHashMap<String,String>();
	//key is Channelid ,value is username;
	volatile private ConcurrentHashMap<String, String>uername2channel = new ConcurrentHashMap<String, String>(); 
	//key is username ,value is Channelid;
	private static BlockingQueue<SendBackJSON> sendback = new ArrayBlockingQueue<SendBackJSON>(100);
	
	private ChannelManager(){
		super();
		ExecutorService sendBackThreadPool = Executors.newFixedThreadPool(2);
		sendBackThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						SendBackJSON DBResult =sendback.take();
						JSONNameandString SendBack = new JSONNameandString();
						SendBack.setJSONName(DBResult.getJSONName());
						SendBack.setJSONStr(DBResult.getJSONStr());
						String ret = JSON.toJSONString(SendBack);
						ret = EnDeCryProcess.SysKeyEncryWithBase64(ret, ChannelManager.getSecreKeybyId(DBResult.getChannelID()));
						Channel channel = ChannelManager.getChannelbyId(DBResult.getChannelID());
						channel.writeAndFlush(new TextWebSocketFrame(ret));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
	}

	public static  void addId2Channel(String asLongText, Channel channel) {
		channelmanager.id2channel.put(asLongText, channel);
	}

	public static  void rmId2Channel(String asLongText) {
		channelmanager.id2channel.remove(asLongText);
	}

	public static  Channel getChannelbyId(String asLongText){
		return channelmanager.id2channel.get(asLongText);
	}

	public static  void addId2Secrekey(String asLongText, SecretKeySpec secretKeySpec) {
		channelmanager.id2secrekey.put(asLongText, secretKeySpec);	
	}

	public static void rmId2Secrekey(String asLongText){
		channelmanager.id2secrekey.remove(asLongText);
	}

	public static  SecretKey getSecreKeybyId(String channelID) {
		return channelmanager.id2secrekey.get(channelID);
	}
	
	public static synchronized void addId2Username(String asLongText, String Name){
		synchronized (channelmanager.id2username) {
		channelmanager.id2username.put(asLongText, Name);
		channelmanager.uername2channel.put(Name,asLongText);
		}
	}
	
	public static  void rmId2Username(String asLongText) {
		synchronized (channelmanager.id2username) {
			channelmanager.id2username.remove(asLongText);
			channelmanager.uername2channel.remove(asLongText);	
		}

	}

	public static  String getUsernamebyId(String asLongText){
		return channelmanager.id2username.get(asLongText);
	}

	private static String getIdbyName(String name) {
		return channelmanager.uername2channel.get(name);
	}

	public static boolean sendback(final SendBackJSON back,final String name){
		String longid = getIdbyName(name);
		if(longid==null){
			StatementManager.sendDBCallable(new DBCallable() {
				
				@Override
				protected SendBackJSON run() {
//					back.setChannelID("defalut");
					String sql = "INSERT INTO offline (username,jsonclass,jsonstring) VALUES('"+name+"','"+back.getJSONName()+"','"+back.getJSONStr()+"');";
					System.out.println(JSON.toJSONString(sql));
					try {
						protectsta.executeUpdate(sql);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			});
			return false;
		}
		back.setChannelID(longid);
		return sendback.offer(back);
	}
	
}
