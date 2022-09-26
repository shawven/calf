package com.github.shawven.calf.practices.demo;

import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTests.class);

	@Test
	public void testSpringData() {
        A a = new A();
        B b = new B();
        BeanUtils.copyProperties(a, b);
        System.out.println(b);
    }

    @Data
    class A {
        String a = "1";
        String b = "1";
    }

    @Data
    class B {
        String a = "2";
        String b = "2";
        String c = "2";
        String d = "2";
    }
}
