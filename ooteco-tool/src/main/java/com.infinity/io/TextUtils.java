package com.infinity.io;

import java.io.*;

/**
 * Created by xule on 2017/3/8.
 */
public class TextUtils {

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readByLine(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            // 一次读入一行，直到读入null为文件结束
            String line = reader.readLine();

            if (null != line) {
                buffer.append(line);
            }
            while (null != (line = reader.readLine())) {
                // 显示行号
                buffer.append("\n").append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return buffer.toString();
    }
}
