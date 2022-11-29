package com.example.shellpractices;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author xw
 * @date 2022/11/28
 */
@Data
public class AttendanceOutputDoc {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("餐补金额")
    private String money;

    @ExcelProperty("原因")
    private String reason;

    @ExcelProperty("工作日期")
    private String date;

    @ExcelProperty("班次")
    private String workNo;

    @ExcelProperty("上班时间")
    private String lastStartTime;

    @ExcelProperty("下班时间")
    private String lastEndTime;

    @ExcelProperty("内勤工时")
    private String officeHours;

    @ExcelProperty("可打车次数")
    private String carCount;
}
