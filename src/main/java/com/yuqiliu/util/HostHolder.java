package com.yuqiliu.util;

import com.yuqiliu.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author yuqiliu
 * @create 2020-05-19  0:58
 */

@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user)
    {
        users.set(user);
    }

    public User getUser()
    {
        return users.get();
    }

    public void clear()
    {
        users.remove();
    }
}
