package de.fhg.fokus.teaglewebsite;

import java.util.Random;

public class Token {
	private String allowedChars ="0123456789abcdefghijklmnopqrstuvwxyz";
	private String token = "";
	
	public Token(){
		generateToken();
	}
	
	public void generateToken(){
		Random random = new Random();
		int max = allowedChars.length();
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<8; i++){
			int value = random.nextInt(max);
			buffer.append(allowedChars.charAt(value));
		}
		token = buffer.toString();
	}
	
	
	public String getToken(){
		return token;
	}
}