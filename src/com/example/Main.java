package com.example;

public class Main{
    public static void main(String[] args){
        new Server((req, resp) ->{
            return "<html><body><h1>Test test test</h1></body></html>";
        }).bootstrap();
    }
}