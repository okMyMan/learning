package com.lighting.rpc.core.model;

import java.io.Serializable;

public class ServerNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4259215025618898530L;

	private String ip;
	
	private int port;
	
	public ServerNode() {
	}
	
	public ServerNode(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
