package com.gasen.usercenterbackend;

import com.gasen.usercenterbackend.model.dao.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserCenterBackendApplicationTests {


    @Test
    void contextLoads() {
        User user = new User();
        System.out.println(user.getOnlyBallNumber());
    }

}
