package com.azure.cosmos.sample.common;

public class Employee {

    private String id;
    private String name;
    private int age;
    private String dept;
    private String group;

    public Employee(String id, String name, int age, String dept, String group) {
        this.name = name;
        this.age = age;
        this.dept = dept;
        this.group = group;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartitionKey() {
        return dept + "-" + group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
