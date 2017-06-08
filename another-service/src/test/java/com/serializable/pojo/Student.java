package com.serializable.pojo;

import java.io.Serializable;

/**
 * Created by xule on 2017/6/8.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 4131732682923135927L;
    private String name;
    private Integer age;
    private String sex;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
