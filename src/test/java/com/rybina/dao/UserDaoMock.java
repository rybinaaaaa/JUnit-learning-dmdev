package com.rybina.dao;


import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

//Просто показ реализации создания мока
public class UserDaoMock extends UserDao{
    private Map<Integer, Boolean> answers = new HashMap<>();
//    private Answer1<Integer, Boolean> answer1;

    @Override
    public boolean delete(Integer id) {
        return answers.getOrDefault(id, false);
    }
}
