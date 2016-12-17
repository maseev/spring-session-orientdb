package io.github.maseev.spring.session.orientdb.entity;

import java.io.Serializable;

public class Person implements Serializable {

  private String name;
  private int age;

  public Person() {
  }

  public Person(final String name, final int age) {
    this.name = name;
    this.age = age;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Person)) {
      return false;
    }

    Person person = (Person) o;

    if (age != person.age) {
      return false;
    }

    return name.equals(person.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + age;
    return result;
  }
}