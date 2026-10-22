package com.minsu.jpus.persona;

public class PersonaNotFoundException extends RuntimeException{
  public PersonaNotFoundException(String key,String value) {
    super(String.format("존재하지 않는 프로필입니다. %s= %s",key,value));
  }

}