package com.infinity.codec;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.ArraySchemas;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * 对象序列化以及GZIP压缩工具
 *
 * @author meizs
 */
public class SerializeUtils {

    /**
     * 通过包装bean序列�?
     *
     * @param t
     * @return
     */
    public static <T> byte[] SerialObjInBean(T t) {
        if (null == t) {
            return null;
        }
        SerialObj<T> serial = new SerializeUtils().new SerialObj(t);
        Schema<SerialObj<T>> schema = (Schema<SerialObj<T>>) RuntimeSchema.getSchema(serial.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        return ProtobufIOUtil.toByteArray(serial, schema, buffer);
    }

    /**
     * 包装bean序列化并GZIP
     *
     * @param t
     * @return
     * @throws IOException
     */
    public static <T> byte[] SerialAndGzipObjInBean(T t) throws IOException {
        if (null == t) {
            return null;
        }
        SerialObj<T> serial = new SerializeUtils().new SerialObj(t);
        Schema<SerialObj<T>> schema = (Schema<SerialObj<T>>) RuntimeSchema.getSchema(serial.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        return compress(ProtobufIOUtil.toByteArray(serial, schema, buffer));
    }

    /**
     * 通过包装Bean反序列化
     *
     * @param bytes 原始字节�?
     * @param t
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T unSerialObjInBean(byte[] bytes, Class<T> t) throws InstantiationException, IllegalAccessException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        SerialObj<T> serial = new SerializeUtils().new SerialObj(t.newInstance());
        Schema<SerialObj<T>> schema = (Schema<SerialObj<T>>) RuntimeSchema.getSchema(serial.getClass());
        ProtobufIOUtil.mergeFrom(bytes, serial, schema);
        if (null != serial.getT()) {
            return serial.getT();
        }
        return null;
    }

    /**
     * 包装BeanUnGzip并反序列�?
     *
     * @param bytes
     * @param t
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public static <T> T unGZIPAndSerialObjInBean(byte[] bytes, Class<T> t) throws InstantiationException, IllegalAccessException, IOException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        bytes = uncompress(bytes);
        SerialObj<T> serial = new SerializeUtils().new SerialObj(t.newInstance());
        Schema<SerialObj<T>> schema = (Schema<SerialObj<T>>) RuntimeSchema.getSchema(serial.getClass());
        ProtobufIOUtil.mergeFrom(bytes, serial, schema);
        if (null != serial.getT()) {
            return serial.getT();
        }
        return null;
    }


    /**
     * 序列化对�?
     *
     * @param t
     * @return
     */
    public static <T> byte[] SerializeObject(T t) {
        if (null == t) {
            return null;
        }
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(t.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        return ProtobufIOUtil.toByteArray(t, schema, buffer);

    }

    /**
     * 反序列化对象
     *
     * @param bytes 原始字节�?
     * @param t
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T unSerializeObject(byte[] bytes, Class<T> t) throws InstantiationException, IllegalAccessException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(t);
        T c = t.newInstance();
        ProtobufIOUtil.mergeFrom(bytes, c, schema);
        return c;
    }

    /**
     * 序列化后gzip
     *
     * @param t
     * @return
     * @throws IOException
     */
    public static <T> byte[] SerializeAndGZipObject(T t) throws IOException {
        if (null == t) {
            return null;
        }
        byte[] bytes = SerializeObject(t);
        return compress(bytes);
    }

    /**
     * ungzip后反序列�?
     *
     * @param bytes
     * @param t
     * @return
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T UnGZipAndunSerializeObject(byte[] bytes, Class<T> t) throws IOException, InstantiationException,
            IllegalAccessException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        byte[] serialied_bytes = uncompress(bytes);
        return unSerializeObject(serialied_bytes, t);
    }

    /**
     * gzip压缩
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] bytes) throws IOException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        byte[] bufferByte = new byte[1024];
        int count;
        while ((count = bis.read(bufferByte, 0, 1024)) != -1) {
            gos.write(bufferByte, 0, count);
        }
        gos.flush();
        gos.close();
        bos.close();
        return bos.toByteArray();
    }

    /**
     * gzip解压�?
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] bytes) throws IOException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(in);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = gis.read(buffer, 0, 1024)) != -1) {
            out.write(buffer, 0, count);
        }
        gis.close();
        in.close();
        out.close();
        return out.toByteArray();
    }

    public static byte[] jdkSerialize(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
        objectOutputStream.writeObject(object);
        try {
            objectOutputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static Object jdkDeserialize(byte[] source) throws ClassNotFoundException, IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(source);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
            return objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw e;
        }
    }

    class SerialObj<T> {

        private T t;

        public SerialObj(T t) {
            super();
            this.t = t;
        }

        public T getT() {
            return t;
        }

        public void setT(T t) {
            this.t = t;
        }

    }

    public static <T> String serialize(T t) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(t);
            return new String(Base64.encodeBase64(bos.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException("serialize session error", e);
        }
    }

    public static <T> T deserialize(String sessionStr) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(sessionStr));
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("deserialize session error", e);
        }
    }

}
