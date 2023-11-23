package com.rybina.service;


import lombok.AllArgsConstructor;
import lombok.Value;

@Value(staticConstructor = "of")
@AllArgsConstructor
public class User {
    String name;
    String password;
    Integer id;
}
