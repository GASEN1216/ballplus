package com.gasen.usercenterbackend;

import com.gasen.usercenterbackend.model.dao.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
class UserCenterBackendApplicationTests {
    private static final String SALT = "20240225";

    @Test
    void contextLoads() {
        System.out.println(DigestUtils.md5DigestAsHex(("12345678" + SALT).getBytes()));
    }

}
