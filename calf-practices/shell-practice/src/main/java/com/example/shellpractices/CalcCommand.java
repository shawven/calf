package com.example.shellpractices;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xw
 * @date 2022/11/24
 */
@Slf4j
@ShellComponent
public class CalcCommand {

    // calc e:\doc.xlsx

    @ShellMethod("计算考勤")
    public void calc(String f) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行

        File file = new File(f);
        List<AttendanceInputDoc> inputDocs ;
        try {
            inputDocs = EasyExcel.read(file.getPath(), AttendanceInputDoc.class, new PageReadListener<AttendanceInputDoc>(dataList -> {
                for (AttendanceInputDoc attendanceInputDoc : dataList) {
                    log.info("读取到一条数据{}", JSON.toJSONString(attendanceInputDoc));
                }
            })).sheet().headRowNumber(2).doReadSync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        LocalTime mealAllowance = LocalTime.of(20, 0);
        LocalTime carAllowance = LocalTime.of(21, 0);

        List<AttendanceOutputDoc> outputDocs = new ArrayList<>();

        int i = 0;
        int sum = 0;
        for (AttendanceInputDoc inputDoc : inputDocs) {
            if (i ++ == 0) {
                continue;
            }

            String lastEndTime = inputDoc.getLastEndTime();
            if (lastEndTime == null || lastEndTime.isBlank() || "-".equals(lastEndTime)) {
                continue;
            }

            LocalTime endTime = null;
            try {
                endTime = LocalTime.parse(inputDoc.getLastEndTime(), DateTimeFormatter.ISO_TIME);
            } catch (Exception e) {
                log.info("最晚内勤无法解析：{}", JSON.toJSONString(inputDoc));
                continue;
            }

            AttendanceOutputDoc outputDoc = new AttendanceOutputDoc();

            if ("休息".equals(inputDoc.getWorkNo()) && inputDoc.getOvertimeDoc().contains("休息日加班")) {
                sum += 30;
                outputDoc.setMoney("+30元");
                outputDoc.setWorkNo("休息日加班");
            } else {
                if (mealAllowance.isAfter(endTime)) {
                    continue;
                }

                if (mealAllowance.isBefore(endTime)) {
                    sum += 15;
                    outputDoc.setMoney("+15元");
                }

                if (carAllowance.isBefore(endTime)) {
                    outputDoc.setCarCount("1");
                }
                outputDoc.setWorkNo(inputDoc.getWorkNo());
            }

            outputDoc.setName(inputDoc.getName());
            outputDoc.setReason("工作日20点后下班");
            outputDoc.setDate(inputDoc.getDate());

            outputDoc.setLastStartTime(inputDoc.getLastStartTime());
            outputDoc.setLastEndTime(inputDoc.getLastEndTime());
            outputDoc.setOfficeHours(inputDoc.getOfficeHours());
            outputDocs.add(outputDoc);
        }
        AttendanceOutputDoc outputDoc = new AttendanceOutputDoc();
        outputDoc.setName("餐补共计：" + sum);
        outputDocs.add(outputDoc);

        String createFile =  file.getPath().split("\\.")[0] + "_加班计算.xlsx";

        try {
            EasyExcel.write(createFile, AttendanceOutputDoc.class)
                    .sheet(2, "加班计算")
                    .doWrite(() -> {
                        // 分页查询数据
                        return outputDocs;
                    });
            log.info("计算完成：{}", createFile);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
