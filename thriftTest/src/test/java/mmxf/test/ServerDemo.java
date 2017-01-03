package mmxf.test;

import mmxf.thrift.EchoService;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class ServerDemo {

	public static void main(String[] args) {

		try {
			// 传输层说明
			TServerTransport serverTransport = new TServerSocket(9000);

			EchoService.Processor<EchoService.Iface> processor = new EchoService.Processor<EchoService.Iface>(
					new EchoService.Iface() {
						@Override
						public String echo(String msg) throws TException {
							return "hello " + msg;
						}
					});

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(
					serverTransport).processor(processor));

			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}

	}

}
