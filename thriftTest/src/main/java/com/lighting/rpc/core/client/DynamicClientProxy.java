package com.lighting.rpc.core.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.lighting.rpc.core.configure.RpcServerConfiguration;
import com.lighting.rpc.core.model.ServerNode;

public class DynamicClientProxy<T> implements InvocationHandler {

	private Class<T> ts = null;
	
	private RpcServerConfiguration configuration = null;
	
	public Object createProxy(Class<T> ts, RpcServerConfiguration configuration) {
		this.ts = ts;
		this.configuration = configuration;
		return Proxy.newProxyInstance(ts.getClassLoader(), ts.getInterfaces(), this);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		
		List<ServerNode> serverNodes = configuration.getServerNodes();
		int timeout = configuration.getTimeout();
		for ( ServerNode serverNode : serverNodes ) {
			String ip = serverNode.getIp();
			int port = serverNode.getPort();
			
			TSocket tsocket = null;
			try {
				tsocket = new TSocket(ip, port);
				tsocket.setTimeout(timeout);
				
				TProtocol protocol = new  TBinaryProtocol(tsocket);
				
				Class[] argsClass = new Class[] {
						TProtocol.class
				};
				Constructor<T> cons = (Constructor<T>) ts.getConstructor(argsClass);
				T client = (T)cons.newInstance(protocol);
				
				tsocket.open();
				return method.invoke(client, args);

			} catch (TTransportException e) {
				
				Throwable te = e.getCause();
				if ( te != null && (te instanceof ConnectException || te instanceof IOException) ) {
					// DO nothing
				} else {
					throw e;
				}
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			} catch (Throwable e) {
				throw e;
			} finally {
				if ( tsocket != null ) {
					tsocket.close();
				}
			}
		}
		
		throw new Exception("Server is not avaiable");
		
	}
	
}



