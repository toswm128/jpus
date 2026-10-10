package com.minsu.jpus.basic;
import org.springframework.stereotype.Service;


@Service
public class GreetingService {

  public String greet(String name) {
    return "Hello!! "+name;
  }

  public String helloAge(String name ,int age){
    return "Hello! "+ name+" your age is " + age + "DeSyou?";
  }
}

