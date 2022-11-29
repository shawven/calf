package com.example.shellpractices;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShellPracticesApplicationTests {

	@Test
	void contextLoads() {
	}

    @Autowired
    private CalcCommand calcCommand;

    @Test
    public void calc() {
        String fileName = "C:/Users/kingdee/Downloads/个人考勤统计表_2022-07-01-2022-11-28.xlsx";
        calcCommand.calc(fileName);
    }
}
