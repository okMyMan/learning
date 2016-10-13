package com.utils.mq.tool;

import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * CommonUtils
 *
 * @author Alvin Xu
 * @date 2016/10/9
 */
public class CommonUtils {
    public static String uniqueId() {
        return UUID.randomUUID().toString().replace('-', '.');
    }
    private static List<String> ADDRESS = new ArrayList<>();
    /**
     * Get host IP address
     *
     * @return IP Address
     */
    public static List<String> getAddresses() {
        if (ADDRESS.isEmpty()) {
            synchronized (ADDRESS) {
                if (ADDRESS.isEmpty()) {
                    try {
                        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                        for (; nis.hasMoreElements(); ) {
                            NetworkInterface ni = nis.nextElement();
                            Enumeration<InetAddress> ias = ni.getInetAddresses();
                            for (; ias.hasMoreElements(); ) {
                                InetAddress ia = ias.nextElement();
                                if (ia instanceof Inet4Address && !ia.getHostAddress().equals("127.0.0.1")) {
                                    ADDRESS.add(ia.toString().substring(1));
                                }
                            }

                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ADDRESS;
    }

    public static <T> ResponseMessage<T> buildResponse(T content, Throwable throwable, RequestMessage requestMessage) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setMessageId(uniqueId());
        responseMessage.setRequestMessageId(requestMessage.getMessageId());
        responseMessage.setForegroundName(requestMessage.getForegroundName());
        responseMessage.setBackgroundSerialId(requestMessage.getBackgroundSerialId());
        responseMessage.setSessionId(requestMessage.getSessionId());
        responseMessage.setServiceType(requestMessage.getServiceType());
        responseMessage.setOverTime(requestMessage.getOverTime());
        responseMessage.setForegroundTimestamp(requestMessage.getForegroundTimestamp());
        responseMessage.setBackgroundTimestamp(new Date());
        responseMessage.setContent(content);
        responseMessage.setThrowable(throwable);
        return responseMessage;
    }


    public static byte[] readAllData(BytesMessage bytesMessage) throws JMSException {
        byte[] buffer = new byte[1024];
        byte[] total = new byte[0];
        int curSize = 0;
        int i;
        while (-1 != (i = bytesMessage.readBytes(buffer))) {
            curSize = total.length;
            total = Arrays.copyOf(total, curSize + i);
            System.arraycopy(buffer, 0, total, curSize, i);
        }
        return total;
    }
}
