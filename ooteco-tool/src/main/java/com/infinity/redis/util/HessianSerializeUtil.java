package com.infinity.redis.util;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: xuehuayang
 * Date: 12-4-12
 * To change this template use File | Settings | File Templates.
 */
public class HessianSerializeUtil {
    public static byte[] encode(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        try {
            out.writeObject(o);
            out.flush();

            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static Object decode(byte[] bArr) {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(bArr);
            Hessian2Input in = new Hessian2Input(bin);
            Object o = (Object) in.readObject(Object.class);
            return o;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
