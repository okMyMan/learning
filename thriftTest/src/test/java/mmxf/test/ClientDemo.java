package mmxf.test;

import mmxf.thrift.EchoService;

import org.apache.thrift.TException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientDemo {

	public static void main(String[] args) throws TException {
	
		ClassPathXmlApplicationContext applicationContext = 
						new ClassPathXmlApplicationContext("application_context.xml");
		
		EchoService.Iface service = (EchoService.Iface)applicationContext.getBean("echoService");
		
		System.out.println(service.echo("lilei"));
		
	}
	
}
