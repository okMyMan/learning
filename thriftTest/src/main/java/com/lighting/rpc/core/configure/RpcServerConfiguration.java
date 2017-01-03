package com.lighting.rpc.core.configure;

import java.util.ArrayList;
import java.util.List;

import com.lighting.rpc.core.model.ServerNode;

public class RpcServerConfiguration {

	public static final int DEFAULT_TIMEOUT = 1000 * 5;
	
	private List<ServerNode> serverNodes = new ArrayList<ServerNode>();
	
	private int timeout = DEFAULT_TIMEOUT;

	public List<ServerNode> getServerNodes() {
		return serverNodes;
	}

	public void setServerNodes(List<ServerNode> serverNodes) {
		this.serverNodes = serverNodes;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
}
