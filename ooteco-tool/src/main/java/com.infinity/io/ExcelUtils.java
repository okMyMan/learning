package com.infinity.io;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by xule on 2017/3/8.
 */
public class ExcelUtils {

    /**
     * 读取excel的第几页
     *
     * @param file
     * @param sheetIndex 从0开始
     * @return
     */
    public static Sheet readFile(File file, Integer sheetIndex) throws IOException, BiffException {
        InputStream is = new FileInputStream(file);
        try {
            Workbook wb = Workbook.getWorkbook(is);

            Sheet[] sheets = wb.getSheets();

            if (null == sheets || sheets.length < 0) {
                return null;
            } else {
                return sheets[sheetIndex];
            }

        } finally {
            if (null != is) {
                is.close();
            }
        }
    }

    /**
     * 读取excel的第几页
     *
     * @param url        例如 ftp://115.29.204.48/webdata/spperf.xls
     * @param sheetIndex 从0开始
     * @return
     */
    public static Sheet readUrl(String url, Integer sheetIndex) throws IOException, BiffException {
        URLConnection connection = new URL(url).openConnection();
        InputStream is = connection.getInputStream();
        try {
            Workbook wb = Workbook.getWorkbook(is);
            Sheet[] sheets = wb.getSheets();
            if (null == sheets || sheets.length < 0) {
                return null;
            } else {
                return sheets[sheetIndex];
            }
        } finally {
            if (null != is) {
                is.close();
            }
        }
    }

}
