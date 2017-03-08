package com.infinity.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * CSVUtils
 *
 * @author Alvin Xu
 * @date 2016/10/16
 * @description
 */
public class CSVUtils {

    public static File write(List exportData, LinkedHashMap titleMap,
                             String outPutPath, String filename) {

        File csvFile = null;
        BufferedWriter csvFileOutputStream = null;
        try {
            csvFile = new File(outPutPath + filename + ".csv");
            // csvFile.getParentFile().mkdir();
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            csvFile.createNewFile();

            // GB2312使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(csvFile), "UTF-8"), 1024);  //GB2312
            // 写入文件头部
            for (Iterator propertyIterator = titleMap.entrySet().iterator(); propertyIterator.hasNext(); ) {
                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator
                        .next();
                csvFileOutputStream.write("\""
                        + propertyEntry.getValue().toString() + "\"");
                if (propertyIterator.hasNext()) {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.newLine();


            // 写入文件内容
            for (Iterator iterator = exportData.iterator(); iterator.hasNext(); ) {
                // Object row = (Object) iterator.next();
                LinkedHashMap row = (LinkedHashMap) iterator.next();
                System.out.println(row);

                for (Iterator propertyIterator = row.entrySet().iterator(); propertyIterator.hasNext(); ) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    // System.out.println( BeanUtils.getProperty(row, propertyEntry.getKey().toString()));
                    csvFileOutputStream.write("\""
                            + propertyEntry.getValue().toString() + "\"");
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                if (iterator.hasNext()) {
                    csvFileOutputStream.newLine();
                }
            }
            csvFileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }


    /**
     * 导入
     *
     * @param file csv文件(路径+文件)
     * @return
     */
    public static List<String> read(File file){
        List<String> dataList=new ArrayList<String>();
        BufferedReader br=null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }

    public static void main(String[] args) {
        List exportData = new ArrayList<Map>();
        Map row1 = new LinkedHashMap<String, String>();
        row1.put("1", "11");
        row1.put("2", "12");
        row1.put("3", "13");
        row1.put("4", "14");
        exportData.add(row1);
        Map row2 = new LinkedHashMap<String, String>();
        row2.put("1", "21");
        row2.put("2", "22");
        row2.put("3", "23");
        row2.put("4", "24");
        exportData.add(row2);
        LinkedHashMap titleMap = new LinkedHashMap();
        titleMap.put("1", "第一列");
        titleMap.put("2", "第二列");
        titleMap.put("3", "第三列");
        titleMap.put("4", "第四列");
        CSVUtils.write(exportData, titleMap, "e:/", "12");

        List<String> result = read(new File("e:/12.csv"));
        System.out.println();

    }
}
