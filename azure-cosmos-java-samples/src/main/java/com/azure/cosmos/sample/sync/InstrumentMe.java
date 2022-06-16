package com.azure.cosmos.sample.sync;

public class InstrumentMe {
   /* public static void main(String[] args) {
        System.out.println("Hello from main method!");
        invokeCustomMethod();
        System.out.println("invokefc "+invokefc());
        System.out.println("invokefcwithparams "+invokefcwithparams("age is ", 230));
    }*/

    public void invokeCustomMethod() {
        System.out.println("Hello from custom method!");
    }

    public String invokefc() {
        return "FC_Here";
    }

    public String invokefcwithparams(final String name, final int no) {
        return name + " ---> " + no;
    }


}
