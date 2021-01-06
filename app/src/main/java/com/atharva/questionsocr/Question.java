package com.atharva.questionsocr;

public class Question {

    public String q;
    public String o1;
    public String o2;
    public String o3;
    public String o4;
    public String ans;

    public Question(){

    }

    public Question(String q, String o1, String o2, String o3, String o4, String ans){
        this.ans = ans;
        this.o1 = o1;
        this.o2 = o2;
        this.o3 = o3;
        this.o4 = o4;
        this.q = q;
    }
}
