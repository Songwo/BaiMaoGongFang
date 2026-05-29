package com.bmgf;

import com.bmgf.service.impl.UserService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class CsStudentApplicationTests {
    @Autowired
    private UserService imUserService;
}
