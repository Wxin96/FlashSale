package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.UserDao;
import com.apollo.flashsale.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class UserService {

    @Resource
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    // 事务
    @Transactional
    public boolean tx() {
        User u1 = new User();
        u1.setId(2);
        u1.setName("2222");
        userDao.insert(u1);

        User u2 = new User();
        u1.setId(1);
        u1.setName("1111");
        userDao.insert(u1);
        userDao.insert(u2);

        return true;
    }

}
