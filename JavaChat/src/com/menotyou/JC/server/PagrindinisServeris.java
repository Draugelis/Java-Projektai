package com.menotyou.JC.server;

public class PagrindinisServeris {
	
	private final static int DEFAULT_PORT = 8192;
	private int port;


	public static void main(String[] args) {
		int port;
		if (args.length > 1) {
			System.out.println("Usage: java -jar ChatServer.jat [port]");
			return;
		} else if (args.length == 1){
			port = Integer.parseInt(args[0]);
			new Serveris(port);
		} else {
			System.out.println("No port specified, the server will start on default port:" + DEFAULT_PORT);
			new Serveris(DEFAULT_PORT);
		}
	}
}
