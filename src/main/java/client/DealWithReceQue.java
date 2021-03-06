package client;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.gui.GUIVerifyAddFriend;
import client.gui.GUILoginDialog;
import com.alibaba.fastjson.JSON;
import json.client.session.AddFriend;
import json.client.session.AddFriendResult;
import json.client.session.RequestFriendList;
import json.client.session.SendFile;
import json.client.session.SendGroupMessage;
import json.client.session.SendMessage;
import json.server.login.RegisiterResult;
import json.server.session.FriendList;
import json.server.session.FriendMeta;
import json.server.session.RemoveFriendResult;
import json.server.session.RmFriendSlid;
import json.util.JSONNameandString;

public class DealWithReceQue implements Runnable{

    private BlockingQueue<JSONNameandString> receque;
    private final Logger logger = LoggerFactory.getLogger(DealWithReceQue.class);
    
	public DealWithReceQue(BlockingQueue<JSONNameandString> receque) {
		super();
		this.receque = receque;
	}

	@Override
	public void run() {
		while(true){
			try {
				JSONNameandString take = receque.take();
				logger.info("Receive: {}",take.getJSONStr());
				switch(take.getJSONName()){
				case "json.server.login.WrongNameorPassword":
					dealwithWrongNameorPassword(take.getJSONStr());
					break;
				case "json.server.session.FriendList":
					DealWithFriendList(take.getJSONStr());
					break;
				case "json.server.login.RegisiterResult":
					dealwithRegisterResult(take.getJSONStr());
					break;
				case "json.client.session.AddFriend":
					dealwithAddFriend(take.getJSONStr());
					break;
				case "json.server.login.SuccessLogin":
					dealwithSuccessLogin();
					break;
				case "json.client.session.SendMessage":
					dealwithSendMessage(take.getJSONStr());
					break;
				case "json.client.session.AddFriendResult":
					dealwithAddFriendResult(take.getJSONStr());
					break;
				case "json.server.session.RemoveFriendResult":
					dealwithRemoveFriendResult(take.getJSONStr());
					break;
				case "json.server.session.RmFriendSlid":
					dealwithRmFriendSlid(take.getJSONStr());
					break;
				case "json.client.session.SendGroupMessage":
					dealwithSendGroupMessage(take.getJSONStr());
					break;
				case "json.client.session.SendFile":
					dealwithSendFile(take.getJSONStr());
					break;
				default:
					System.err.println("Client : can't deal "+take.getJSONName());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void dealwithSendFile(String jsonStr) {
		SendFile sendfile = JSON.parseObject(jsonStr, SendFile.class);
		JFileChooser chooser = new JFileChooser();

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//设置保存路径
		int returnVal = chooser.showSaveDialog(new JPanel());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getPath();
			try {

				File f = new File(path);
				if(f.isDirectory()){
					String string = System.getProperty("user.dir");
					string = string+System.getProperty("file.separator")+"resource"+System.getProperty("file.separator")+"Receive"+System.getProperty("file.separator")+sendfile.getFilename();
					f = new File(string);
				}
				f.createNewFile();
				FileOutputStream out = new FileOutputStream(f);
				out.write(sendfile.getContent());
				out.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	private void dealwithSendGroupMessage(String jsonStr) {
		SendGroupMessage groupMessage = JSON.parseObject(jsonStr,SendGroupMessage.class);
		for(int i = 0 ;i< groupMessage.getFriendlist().size();i++){
			if(groupMessage.getFriendlist().get(i).equals(ClientManage.getName())){
				groupMessage.getFriendlist().remove(i);
				break;
			}
		}
		ClientManage.displayMessage(groupMessage);
	}

	private void dealwithRmFriendSlid(String jsonStr) {
		RmFriendSlid slid = JSON.parseObject(jsonStr, RmFriendSlid.class);
		ClientManage.rmPathNode(slid.getName());
		
	}

	private void dealwithRemoveFriendResult(String jsonStr) {
		RemoveFriendResult result = JSON.parseObject(jsonStr, RemoveFriendResult.class);
		ClientManage.rmPathNode(result.getName());
		
	}

	private void dealwithAddFriendResult(String jsonStr) {
		AddFriendResult friendResult = JSON.parseObject(jsonStr, AddFriendResult.class);
		if(friendResult.isReceiverestate()){
			ClientManage.addPathNode(friendResult.getRequestorgroup()+"."+friendResult.getReceivername());
			JOptionPane.showMessageDialog(null, "new friend:"+friendResult.getReceivername()+"\ngroup:"+friendResult.getRequestorgroup());			
		}else{
			JOptionPane.showMessageDialog(null, "Adding Friend request was rejected by "+friendResult.getReceivername());
		}
		
	}

	private void dealwithSendMessage(String jsonStr) {
		SendMessage sendmessage = JSON.parseObject(jsonStr, SendMessage.class);
		ClientManage.displayMessage(sendmessage);
	}

	private void dealwithSuccessLogin() {
		logger.info("try to enable main windows");
		GUILoginDialog logindaialog = ClientManage.getLogindaialog();
		logindaialog.setVisible(false);
		
		RequestFriendList requestFriendList = new RequestFriendList();
		requestFriendList.setGroup("Friends");
		JSONNameandString json = new JSONNameandString();
		json.setJSONName(RequestFriendList.class.getName());
		json.setJSONStr(JSON.toJSONString(requestFriendList));
		ClientManage.sendJSONNameandString(json);
	}

	private void dealwithAddFriend(String jsonStr) {
		AddFriend addfriend = JSON.parseObject(jsonStr, AddFriend.class);
		@SuppressWarnings("unused")
		GUIVerifyAddFriend verifyAddFriend = new GUIVerifyAddFriend(addfriend);
	}

	private void DealWithFriendList(String jsonStr) {
		FriendList friendList = JSON.parseObject(jsonStr, FriendList.class);
		for( FriendMeta fm : friendList.getFriends()){
			ClientManage.addPathNode(fm.getGroup()+"."+fm.getName());
		}
		ClientManage.setMainWindowVisible(true);
		ClientManage.hasInit();
	}

	private void dealwithRegisterResult(String jsonStr) {
		RegisiterResult regisiterResult = JSON.parseObject(jsonStr, RegisiterResult.class);
		if (regisiterResult.isSuccess()) {
			JOptionPane.showMessageDialog(null, "register successfully");
		}else{
			JOptionPane.showMessageDialog(null, "Can't register : "+regisiterResult.getReason());
		}
	}

	private void dealwithWrongNameorPassword(String jsonStr) {
		logger.info("send wrong name or password message to gui");
		JOptionPane.showMessageDialog(null, "Wrong name or password");
		ClientManage.getLogindaialog().setEnabled(true);
	}
	
}
