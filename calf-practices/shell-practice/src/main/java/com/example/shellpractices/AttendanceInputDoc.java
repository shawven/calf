package com.example.shellpractices;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author xw
 * @date 2022/11/28
 */
@Data
public class AttendanceInputDoc {

    @ExcelProperty("人员")
    private String name;

    @ExcelProperty("日期")
    private String date;

    @ExcelProperty("班次")
    private String workNo;

    @ExcelProperty("最早内勤")
    private String lastStartTime;

    @ExcelProperty("最晚内勤")
    private String lastEndTime;

    @ExcelProperty("内勤工时")
    private String officeHours;

    @ExcelProperty("加班单据")
    private String overtimeDoc;

    @ExcelProperty("出勤天数")
    private String attendanceDays;
}
