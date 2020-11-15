package com.starter.demo;

public class ExtendTest {
    public static void main(String[] args) throws ClassNotFoundException {

        ESingleton instance = ESingleton.INSTANCE;

    }
}



enum  ESingleton {
    INSTANCE;

    static {
        System.out.println("1");
    }

    {
        System.out.println("2");
    }

    private ESingleton() {
        System.out.println("3");
    }
}

class Singleton {

    public static final int i = 1;

    private static Singleton instance = new Singleton();

    static {
        System.out.println("11");
    }

    {
        System.out.println("22");
    }

    private Singleton() {
        System.out.println("33");
    }

    public static Singleton getInstance() {
        return new Singleton();
    }
}

class Son extends Father {
    {
        System.out.println("Son init block");
    }

    static {
        System.out.println("Son static block");
    }

    public Son() {
        System.out.println("Son construct");
    }
}

class Father {

    static Son son = new Son();

    {
        System.out.println("Father init block");
    }

    static {
        System.out.println("Father static block");
    }

    public Father() {
        System.out.println("Father construct");
    }
}
