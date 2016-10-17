package server;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Test2 {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		String str = "123";
		MessageDigest instance = MessageDigest.getInstance("SHA-1");
		instance.update(str.getBytes());
		byte[] md = instance.digest();
		StringBuffer stringBuffer = new StringBuffer();

		for(int i = 0 ; i < md.length; i++){
			String shahex = Integer.toHexString(md[i]);
			if(shahex.length()<2){
				stringBuffer.append(0);
			}
			stringBuffer.append(shahex);
		}
		System.out.println(stringBuffer.toString());
		
	}
}
