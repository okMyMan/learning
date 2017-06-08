package com.serializable;

import com.serializable.pojo.Student;

import java.io.*;

/**
 * Created by xule on 2017/6/8.
 */
public class Test {

    public static void main(String[] args) {
        Student st = new Student();
        st.setAge(20);
        st.setName("徐乐");

        File file = new File("G:/student.txt");
        try
        {
            file.createNewFile();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            //Student对象序列化过程
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(st);
            oos.flush();
            oos.close();
            fos.close();

            //Student对象反序列化过程
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Student st1 = (Student) ois.readObject();
            System.out.println("name = " + st1.getName());
            System.out.println("age = " + st1.getAge());
            ois.close();
            fis.close();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
