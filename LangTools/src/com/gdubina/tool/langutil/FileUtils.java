package com.gdubina.tool.langutil;

import java.io.*;

public class FileUtils {
    public static void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        String line; // 用来保存每行读取的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // 读取第一行
        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }
        reader.close();
        is.close();
    }

    public static String seekToResPath(String path) {
        String outPath = path;
        if (outPath != null) {
            int i =  outPath.indexOf("/res");
            if ( i >= 0) {
                outPath = outPath.substring(0, i + "/res".length());
                System.out.println("outPath trim : " + outPath);
            } else {
                // find in children
                File file = new File(outPath);
                outPath = findFilePath(file, "res");
                System.out.println("outPath child : " + outPath);
            }
        }
        return outPath;
    }

    private static String findFilePath(File srouceFile, String target) {
        File[] files = srouceFile.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("outPath child find >> " + file.getName());
                if (file.getName().equals(target)) {
                    return file.getAbsolutePath();
                } else {
                    String result = findFilePath(file, target);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
}
