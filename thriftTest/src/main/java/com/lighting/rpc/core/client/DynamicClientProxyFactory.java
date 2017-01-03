package com.lighting.rpc.core.client;

import java.util.List;

import com.lighting.rpc.core.configure.RpcServerConfiguration;
import com.lighting.rpc.core.model.ServerNode;

public class DynamicClientProxyFactory {
	
	public static Object createIface(String clazzIfaceName, List<String> servers) {
		
		try {

			int idx = clazzIfaceName.lastIndexOf('$');
			
			String clazzClientName = clazzIfaceName.substring(0, idx) + "$Client";
			Class clientClazz = Class.forName(clazzClientName);
			
			RpcServerConfiguration configuration = new RpcServerConfiguration();
			for ( String server : servers ) {
				server = server.trim();
				String[] metas = server.split(":");
				if ( metas.length != 2 ) {
					continue;
				}
				configuration.getServerNodes().add(
						new ServerNode(metas[0], Integer.parseInt(metas[1])));
			}
			configuration.setTimeout(1000 * 5);
			
			DynamicClientProxy proxy = new DynamicClientProxy();
			return proxy.createProxy(clientClazz, configuration);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
