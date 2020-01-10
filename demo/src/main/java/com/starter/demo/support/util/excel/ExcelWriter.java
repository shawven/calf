package com.starter.demo.support.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Excel工具 "xlsx"类型
 *
 * @author Shoven
 * @date 2018-12-27
 */
public class ExcelWriter {

    public static final Function<Object, ?> NULL_VALUE_FUNC = o -> null;

    /**
     * 字体名称
     */
    private static final String DEFAULT_FONT_NAME = "宋体";

    /**
     * 标题高度
     */
    private static final int DEFAULT_TITLE_HEIGHT = 20;

    /**
     * 列高度
     */
    private static final int DEFAULT_COLUMN_HEIGHT = 20;

    /**
     * 列宽度
     */
    private static final short DEFAULT_COLUMN_WIDTH = 10;

    /**
     * 标题字体大小
     */
    private static final short DEFAULT_TITLE_FONT_SIZE = 12;
    /**
     * 数据行字体大小
     */
    private static final short DEFAULT_ROW_FONT_SIZE = 11;

    /**
     * 数据行位置
     */
    private int dataRowIndex;

    /**
     * 标题行位置
     */
    private int titleRowIndex;

    /**
     * 列名
     */
    private List<Column> columns;

    /**
     * 表名
     */
    private String title;

    /**
     * 工作表名
     */
    private String sheetName;

    /**
     * 数据
     */
    private List<?> data;

    /**
     * 工作薄
     */
    private XSSFWorkbook workbook;

    /**
     * 工作表
     */
    private XSSFSheet sheet;

    /**
     * 后置任务
     */
    private List<Runnable> postTask;

    /**
     * 是否保存
     */
    private boolean saved;

    public ExcelWriter() {
        workbook = new XSSFWorkbook();
    }

    public ExcelWriter(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    /**
     * 切换工作表
     *
     * @param index 工作表序号
     * @return ExcelWriter
     */
    public ExcelWriter switchSheet(int index) {
        dataRowIndex = 0;
        titleRowIndex = 0;
        sheet = workbook.getSheetAt(index);
        workbook.setActiveSheet(index);
        saved = false;
        return this;
    }

    /**
     * 切换工作表
     *
     * @param name 工作表名称
     * @return ExcelWriter
     */
    public ExcelWriter switchSheet(String name) {
        dataRowIndex = 0;
        titleRowIndex = 0;
        sheet = workbook.getSheet(name);
        if (sheet == null) {
            throw new IllegalArgumentException("工作表[" + name +"]不存在");
        }
        workbook.setActiveSheet(workbook.getSheetIndex(sheet));
        saved = false;
        return this;
    }

    /**
     * 创建工作表
     *
     * @return ExcelWriter
     */
    public ExcelWriter createSheet() {
        int numberOfSheets = workbook.getNumberOfSheets();
        String displaySheetName = "Sheet" + (numberOfSheets + 1);
        sheet = workbook.createSheet(displaySheetName);
        workbook.setActiveSheet(workbook.getSheetIndex(sheet));
        saved = false;
        return this;
    }

    /**
     * 创建工作表
     *
     * @param name 工作表名称
     * @return ExcelWriter
     */
    public ExcelWriter createSheet(String name) {
        // 删除存在的工作表
        int sheetIndex = workbook.getSheetIndex(name);
        if (sheetIndex != -1) {
            workbook.removeSheetAt(sheetIndex);
        }
        sheet = workbook.createSheet(name);
        workbook.setActiveSheet(workbook.getSheetIndex(name));
        saved = false;
        return this;
    }

    /**
     * 设置标题名称
     *
     * @param title 名称
     * @return ExcelWriter
     */
    public ExcelWriter setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 工作表名称
     *
     * @param sheetName 名称
     * @return ExcelWriter
     */
    public ExcelWriter setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * 设置数据
     *
     * @param data 数据集合
     * @return ExcelWriter
     */
    public ExcelWriter setData(List<?> data) {
        this.data = data;
        return this;
    }

    /**
     * 设置标题填充起始行
     *
     * @param rowIndex 行号
     * @return ExcelWriter
     */
    public ExcelWriter setStartRowIndex(int rowIndex) {
        this.titleRowIndex = rowIndex;
        this.dataRowIndex = rowIndex;
        return this;
    }

    public ExcelWriter addEmptyColumn(String key) {
        return addEmptyColumn(key, ColumnType.STRING);
    }

    public ExcelWriter addEmptyColumn(String key, ColumnType columnType) {
        return addEmptyColumn(key, DEFAULT_COLUMN_WIDTH, columnType, null);
    }

    public ExcelWriter addEmptyColumn(String key, ColumnAlign columnAlign) {
        return addEmptyColumn(key, DEFAULT_COLUMN_WIDTH, ColumnType.STRING, columnAlign);
    }

    public ExcelWriter addEmptyColumn(String key, int width) {
        return addEmptyColumn(key, width, ColumnType.STRING, null);
    }

    public ExcelWriter addEmptyColumn(String key, int width, ColumnType columnType, ColumnAlign columnAlign) {
        return addColumn(null, key, width, columnType, columnAlign);
    }

    public <T> ExcelWriter addEmptyColumn(Function<? super T, ?> valueFunc) {
        return addEmptyColumn(valueFunc, ColumnType.STRING);
    }

    public <T> ExcelWriter addEmptyColumn(Function<? super T, ?> valueFunc, ColumnType columnType) {
        return addEmptyColumn(valueFunc, DEFAULT_COLUMN_WIDTH, columnType, null);
    }

    public <T> ExcelWriter addEmptyColumn(Function<? super T, ?> valueFunc, ColumnAlign columnAlign) {
        return addEmptyColumn(valueFunc, DEFAULT_COLUMN_WIDTH, ColumnType.STRING, columnAlign);
    }

    public <T> ExcelWriter addEmptyColumn(Function<? super T, ?> valueFunc, int width) {
        return addEmptyColumn(valueFunc, width, ColumnType.STRING, null);
    }

    public <T> ExcelWriter addEmptyColumn(Function<? super T, ?> valueFunc, int width, ColumnType columnType,
                                          ColumnAlign columnAlign) {
        return addColumn(null, valueFunc, width, columnType, columnAlign);
    }

    public ExcelWriter addColumn(String title, String key) {
        return addColumn(title, key, ColumnType.STRING);
    }

    public ExcelWriter addColumn(String title, String key, ColumnType columnType) {
        return addColumn(title, key, DEFAULT_COLUMN_WIDTH, columnType, null);
    }

    public ExcelWriter addColumn(String title, String key, ColumnAlign columnAlign) {
        return addColumn(title, key, DEFAULT_COLUMN_WIDTH, ColumnType.STRING, columnAlign);
    }

    public ExcelWriter addColumn(String title, String key, int width) {
        return addColumn(title, key, width, ColumnType.STRING, null);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc) {
        return addColumn(title, valueFunc, ColumnType.STRING);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc, ColumnType columnType) {
        return addColumn(title, valueFunc, DEFAULT_COLUMN_WIDTH, columnType, null);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc, ColumnAlign columnAlign) {
        return addColumn(title, valueFunc, DEFAULT_COLUMN_WIDTH, ColumnType.STRING, columnAlign);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc, int width) {
        return addColumn(title, valueFunc, width, ColumnType.STRING, null);
    }

    public ExcelWriter addColumn(String title, String key, int width, ColumnType columnType, ColumnAlign columnAlign) {
        Column column = new Column().setTitle(title).setKey(key).setWidth(width)
                .setColumnType(columnType).setColumnAlign(columnAlign);
        return addColumn(column);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc, int width, ColumnType columnType,
                                     ColumnAlign columnAlign) {
        Column column = new Column().setTitle(title).setValueFunc(valueFunc).setWidth(width)
                .setColumnType(columnType).setColumnAlign(columnAlign);
        return addColumn(column);
    }

    public <T> ExcelWriter addColumn(String title, String key, Consumer<XSSFCellStyle> styleFunc) {
        return addColumn(title, key, styleFunc, DEFAULT_COLUMN_WIDTH);
    }

    public <T> ExcelWriter addColumn(String title, String key, Consumer<XSSFCellStyle> styleFunc, int width) {
        Column column = new Column().setTitle(title).setKey(key)
                .setStyleFunc(styleFunc).setWidth(width);
        return addColumn(column);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc, Consumer<XSSFCellStyle> styleFunc) {
        return addColumn(title, valueFunc, styleFunc, DEFAULT_COLUMN_WIDTH);
    }

    public <T> ExcelWriter addColumn(String title, Function<? super T, ?> valueFunc,
                                     Consumer<XSSFCellStyle> styleFunc, int width) {
        Column column = new Column().setTitle(title).setValueFunc(valueFunc)
                .setStyleFunc(styleFunc).setWidth(width);
        return addColumn(column);
    }

    public ExcelWriter addColumn(Column column) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(column);
        return this;
    }

    /**
     * 自定义添加行
     *
     * @param rowIndex 指定行号
     * @param consumer 具体逻辑
     * @return ExcelWriter
     */
    public ExcelWriter addRow(int rowIndex, Consumer<XSSFRow> consumer) {
        if (postTask == null) {
            postTask = new ArrayList<>();
        }
        postTask.add(() -> {
            XSSFRow row = sheet.createRow(rowIndex);
            consumer.accept(row);
        });
        return this;
    }

    /**
     * 末尾添加一行
     *
     * @param consumer 具体逻辑
     * @return ExcelWriter
     */
    public ExcelWriter addEndRow(Consumer<XSSFRow> consumer) {
        if (postTask == null) {
            postTask = new ArrayList<>();
        }
        postTask.add(() -> {
            dataRowIndex++;
            XSSFRow row = sheet.createRow(dataRowIndex);
            consumer.accept(row);
        });
        return this;
    }

    /**
     * 写到输出流
     *
     * @param out 输出流
     * @throws IOException Io Exception
     */
    public void write(OutputStream out) throws IOException {
        save();
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 通过http下载
     *
     * @param response Http响应对象
     * @throws IOException Io Exception
     */
    public void writeToHttpResponse(HttpServletResponse response, String fileName) throws IOException {
        if (fileName == null) {
            fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        if (!fileName.endsWith(".xlsx")) {
            fileName += ".xlsx";
        }

        response.reset();
        response.setContentType("application/vnd.ms-struct;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), ISO_8859_1));

        ServletOutputStream out = response.getOutputStream();
        save();
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 写到文件
     *
     * @param fileName 文件名
     * @throws IOException Io Exception
     */
    public void writeToFile(String fileName) throws IOException {
        if (fileName == null) {
            fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        if (!fileName.endsWith(".xlsx")) {
            fileName += ".xlsx";
        }

        FileOutputStream out = new FileOutputStream(fileName);
        save();
        workbook.write(out);
        out.flush();
        out.close();
    }

    public ExcelWriter save() {
        if (!saved) {
            if (sheet == null) {
                createSheet();
            }

            if (title != null && !title.isEmpty()) {
                createTitleArea();
            }
            if (sheetName == null || sheetName.isEmpty()) {
                sheetName = title;
            }
            if (sheetName != null && !sheetName.isEmpty()) {
                workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);
            }

            if (columns != null && !columns.isEmpty()) {
                // 创建列标题
                createColumnArea();
            }
            // 创建数据单元格
            if (data == null) {
                throw new IllegalArgumentException("未设置单元格数据行");
            }
            createDataArea();

            // 执行后置任务
            if (postTask != null) {
                postTask.forEach(Runnable::run);
                postTask.clear();
            }

            workbook.setActiveSheet(0);
            dataRowIndex = 0;
            titleRowIndex = 0;
            title = null;
            sheetName = null;
            columns.clear();
            data = null;
            postTask = null;
            saved = true;
        }
        return this;
    }

    /**
     * 创建标题域
     */
    private void createTitleArea() {
        dataRowIndex++;
        Cell titleCell = sheet.createRow(titleRowIndex).createCell(0);
        titleCell.setCellValue(title);
        setTitleStyle(titleCell);
    }

    /**
     * 创建列域
     */
    private void createColumnArea() {
        if (isAllEmptyColumn(columns)) {
            return;
        }
        int maxColumnNum;
        if (maxColumnRowNum(columns) == 1) {
            // 创建简单列
            createSimpleColumns(columns);
            maxColumnNum = columns.size();
        } else {
            // 创建复合列
            maxColumnNum = createComplexColumns(columns);
        }
        if (title != null && maxColumnNum > 1) {
            // 保证最大列的索引最大值在屏幕右侧，合并后标题保证在当前屏幕中，
            // 不会因为有很多列时标题不在首次打开excel的可视区中
            int visibleMaxIndex = Math.min(maxColumnNum - 1, 10);
            sheet.addMergedRegion(new CellRangeAddress(titleRowIndex, titleRowIndex, 0, visibleMaxIndex));
        }
    }

    /**
     * 创建数据域
     */
    private void createDataArea() {
        // 从数据行索引起点开始 遍历数据源数据
        for (Object line : data) {
            Row row = sheet.createRow(dataRowIndex++);
            for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
                Column column = columns.get(i);
                if (column.isComplex()) {
                    Object childLine = getDataCellValue(line, column);
                    createDataCellsOfComplexColumn(childLine, row, column.getChildColumns(), i);
                } else {
                    createDataCell(line, row, i, column);
                }
            }
        }
    }

    /**
     * 创建简单列
     *
     * @param columns 列集合
     */
    private void createSimpleColumns(List<Column> columns) {
        XSSFRow row = sheet.createRow(dataRowIndex++);
        for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
            Column column = columns.get(i);
            // 创建单列
            createColumn(row, column, i);
        }
    }

    /**
     * 创建复合列
     *
     * @param columns 列集合
     * @return 使用的列数
     */
    private int createComplexColumns(List<Column> columns) {
        // 创建复合列
        int usedColumnNum = doCreateComplexColumns(columns, 0);
        // 此处行索引为创建复合列之前的行索引 + 1,（如：创建列之前是1现在是2）
        // 实际数据写入的行索引应为最末级子列的下一个（如：复合列有3层）
        // 所以增量最大层数 - 1，（即：实际数据行索引为 2 + (3 - 1) = 4）
        dataRowIndex += maxColumnRowNum(columns) - 1;
        // 返回创建列使用的列数
        return usedColumnNum;
    }

    /**
     * 创建复合列
     *
     * @param columns 列集合
     * @param columnOffset 列偏移量 二级列集合从0开始遍历要加上父列列索引为偏移量
     * @return 使用的列数
     */
    private int doCreateComplexColumns(List<Column> columns, int columnOffset) {
        // 起始行 = 行索引
        int startRowIndex = dataRowIndex;
        // 结束行 = 起始行 + 列“额外”（减1）占用的行数
        int stopRowIndex = startRowIndex + maxColumnRowNum(columns) - 1 ;

        // 列大小
        int size = columns.size();
        // 使用的列数量
        int usedColumnNum = size;

        // 不存在才创建（比例上下来回跳跃时创建新行覆盖旧行导致之前写入数据丢失）
        XSSFRow row = sheet.getRow(startRowIndex);
        if (row == null) {
            row = sheet.createRow(startRowIndex);
        }
        // 行索引自增
        dataRowIndex ++;
        // 遍历每一列
        for (int i = 0; i < size; i++) {
            Column column = columns.get(i);
            // 实际列索引（整体偏移量 + 当前列索引）
            int columnIndex = columnOffset + i;

            // 创建复合列
            if (column.isComplex()) {
                // 获取所有的子列
                List<Column> childColumns = column.getChildColumns();
                // 创建“当前”单元格
                createColumn(row, column, columnIndex);
                // 创建子列单元格并返回子单元格使用的列数
                int childUsedColumnNum = doCreateComplexColumns(childColumns, columnIndex);
                // 子列构建完成后行索引自增
                dataRowIndex --;
                // 子列单元格“额外”（减1）使用的列数
                int addChildUsedColumnNum = childUsedColumnNum - 1;
                // 列偏移量 加上子列单元格额外使用的列数
                columnOffset += addChildUsedColumnNum;
                // 使用数统计 加上子列单元格额外使用的列数
                usedColumnNum += addChildUsedColumnNum;

                // 子列集合大于1列时，说明已经扩充了，
                if (childColumns.size() > 1) {
                    // 合并“当前”单元格成为子列单元格的父单元格
                    // 合并列 从 起始列数 至 起始列数 + 子列单元格“额外”使用的列数
                    sheet.addMergedRegionUnsafe(new CellRangeAddress(startRowIndex, startRowIndex,
                            columnIndex, columnIndex + addChildUsedColumnNum));
                }
            } else {
                // 创建简单列
                createColumn(row, column, columnIndex);
                if (startRowIndex < stopRowIndex) {
                    // 合并行 从 起始行数 至 起始行 + 列“额外”占用的行数
                    sheet.addMergedRegion(new CellRangeAddress(startRowIndex, stopRowIndex, columnIndex, columnIndex));
                }
            }
        }

        // 返回使用的列数
        return usedColumnNum;
    }

    /**
     * 创建单列
     *
     * @param row         行
     * @param column      列
     * @param columnIndex 列下标
     */
    private void createColumn(XSSFRow row, Column column, int columnIndex) {
        XSSFCell columnTitle = row.createCell(columnIndex);
        columnTitle.setCellValue(column.getTitle());
        //设置列宽高
        int width = column.getWidth();
        sheet.setColumnWidth(columnIndex, width != 0 ? 256 * width * 2 : 2560 * 2);

        setColumnStyle(columnTitle);

        Consumer<XSSFCellStyle> styleFunc = column.getStyleFunc();
        if (styleFunc != null) {
            XSSFCellStyle cellStyle = columnTitle.getCellStyle();
            styleFunc.accept(cellStyle);
        }
    }

    /**
     * 创建复合单元格有关的数据单元格
     *
     * @param line         行数据
     * @param row          Excel行对象
     * @param columns      列集合
     * @param columnOffset 列偏移量 二级列集合从0开始遍历要加上父列列索引为偏移量
     */
    private void createDataCellsOfComplexColumn(Object line, Row row, List<Column> columns, int columnOffset) {
        for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
            Column column = columns.get(i);
            int columnIndex = columnOffset + i;
            if (column.isComplex()) {
                Object childLine = getDataCellValue(line, column);
                createDataCellsOfComplexColumn(childLine, row, column.getChildColumns(), columnIndex);
            } else {
                createDataCell(line, row, columnIndex, column);
            }
        }
    }

    /**
     * 创建数据单元格
     *
     * @param line        行数据
     * @param row         Excel行对象
     * @param columnIndex 列索引
     * @param column      列对象
     */
    private void createDataCell(Object line, Row row, int columnIndex, Column column) {
        Cell cell = row.createCell(columnIndex);
        Object cellValue = getDataCellValue(line, column);
        setDataCellValue(cell, column, cellValue);
        setDataStyle(cell, column);
    }

    /**
     * 设置数据值
     *
     * @param cell   单元格
     * @param column 列类型
     * @param value  单元格值
     */
    private void setDataCellValue(Cell cell, Column column, Object value) {
        switch (column.getColumnType()) {
            case INT:
                if (value != null) {
                    long l;
                    if (value instanceof Number) {
                        l = ((Number) value).longValue();
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        l = Long.parseLong(str);
                    } else {
                        throw new NumberFormatException("不支持的整数类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(l);
                } else {
                    cell.setCellValue(0);
                }
                break;
            case AMOUNT:
                if (value != null) {
                    BigDecimal amount;
                    if (value instanceof Number) {
                        amount = BigDecimal.valueOf(((Number) value).doubleValue());
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        amount = new BigDecimal(str);
                    } else {
                        throw new NumberFormatException("不支持的金额类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(amount.setScale(2, RoundingMode.HALF_UP).toString());
                } else {
                    cell.setCellValue("0.00");
                }
                break;
            case DOUBLE:
                if (value != null) {
                    double d;
                    if (value instanceof Number) {
                        d = ((Number) value).doubleValue();
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        d = Double.parseDouble(str);
                    } else {
                        throw new NumberFormatException("不支持的浮点数类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(d);
                } else {
                    cell.setCellValue(0d);
                }
                break;
            case TIME:
                if (value != null) {
                    Date t;
                    if (value instanceof Date) {
                        t = (Date) value;
                    } else if (value instanceof LocalDateTime) {
                        t = Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
                    } else if (value instanceof LocalDate) {
                        t = Date.from(((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    } else if (value instanceof Calendar) {
                        t = ((Calendar) value).getTime();
                    } else {
                        throw new RuntimeException("不支持的日期类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(t);
                } else {
                    cell.setCellValue("");
                }
                break;
            case STRING:
            default:
                cell.setCellValue(value == null ? "" : value.toString());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object getDataCellValue(Object item, Column column) {
        String keyName = column.getKey();
        if (item instanceof Map && keyName != null) {
            return ((Map) item).get(keyName);
        } else {
            Function valueFunc = column.getValueFunc();
            if (valueFunc != null) {
                return valueFunc.apply(item);
            }
            if (keyName != null) {
                try {
                    Field field = item.getClass().getDeclaredField(keyName);
                    boolean unaccessible = !Modifier.isPublic(field.getModifiers())
                            || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                            || Modifier.isFinal(field.getModifiers());
                    if (unaccessible && !field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return field.get(item);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            String.format("类[%s]不包含字段[%s]", item.getClass().getName(), keyName));
                }
            } else {
                String msg = String.format("列[%s]未设置取值字段名或方法", column.getTitle());
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * 设置标题样式
     *
     * @param titleCell 标题单元格
     */
    private void setTitleStyle(Cell titleCell) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_TITLE_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        CellStyle cs = workbook.createCellStyle();
        cs.setFont(font);

        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);

        titleCell.getRow().setHeightInPoints(DEFAULT_TITLE_HEIGHT);
        titleCell.setCellStyle(cs);
    }

    /**
     * 设置列样式
     *
     * @param columnTitle 列标题
     */
    private void setColumnStyle(Cell columnTitle) {
        CellStyle cs = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_ROW_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);

        columnTitle.getRow().setHeightInPoints(DEFAULT_COLUMN_HEIGHT);
        columnTitle.setCellStyle(cs);
    }

    /**
     * 设置数据样式
     *
     * @param cell   单元格
     * @param column 列类型
     */
    private void setDataStyle(Cell cell, Column column) {
        CellStyle cs = workbook.createCellStyle();
        switch (column.getColumnType()) {
            case INT:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0"));
                break;
            case DOUBLE:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                break;
            case AMOUNT:
                cs.setDataFormat(workbook.createDataFormat().getFormat("#,###.00"));
                cs.setAlignment(HorizontalAlignment.CENTER);
                break;
            case TIME:
                cs.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy h:mm"));
                break;
            default:
        }

        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_ROW_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);

        ColumnAlign columnAlign = column.getColumnAlign();
        if (columnAlign != null) {
            cs.setAlignment(HorizontalAlignment.forInt(columnAlign.getCode()));
        }
        cell.setCellStyle(cs);
    }


    /**
     * 列占用的最大行数 （单列占一行，复合列没嵌套一层加一行）
     *
     * @param columns 列集合
     * @return 行数
     */
    private boolean isAllEmptyColumn(List<Column> columns) {
        if (columns == null || columns.isEmpty()) {
            return true;
        }
        return columns.stream().allMatch(column -> column == null || column.isEmpty());
    }

    /**
     * 列占用的最大行数 （单列占一行，复合列每嵌套一层加一行）
     *
     * @param columns 列集合
     * @return 行数
     */
    private int maxColumnRowNum(List<Column> columns) {
        if (columns == null || columns.isEmpty()) {
            return 1;
        }
        return columns.stream()
                .map(Column::needRowNum)
                .max(Comparator.comparingInt(Integer::intValue))
                .orElse(1);
    }

    public static class Column {

        /**
         * 列标题
         */
        private String title;

        /**
         * 取值属性名
         */
        private String key;

        /**
         * 取值方法
         */
        private Function<?, ?> valueFunc;

        /**
         * 列宽
         */
        private int width;

        /**
         * 列类型
         */
        private ColumnType columnType;

        /**
         * 列居中类型
         */
        private ColumnAlign columnAlign;

        /**
         * 单元格格式
         */
        private Consumer<XSSFCellStyle> styleFunc;

        /**
         * 子列（多列表头）
         */
        private List<Column> childColumns;

        public Column() {
            this.columnType = ColumnType.STRING;
            this.columnAlign = ColumnAlign.CENTER;
        }

        public Column(String title, String key) {
            this();
            this.title = title;
            this.key = key;
        }

        public Column(String title, Function<?, ?> valueFunc) {
            this();
            this.title = title;
            this.valueFunc = valueFunc;
        }

        public Column(String title, String key, Consumer<XSSFCellStyle> styleFunc) {
            this(title, key);
            this.styleFunc = styleFunc;
        }

        public Column(String title, Function<?, ?> valueFunc, Consumer<XSSFCellStyle> styleFunc) {
            this(title, valueFunc);
            this.styleFunc = styleFunc;
        }

        public String getTitle() {
            return title;
        }

        public Column setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getKey() {
            return key;
        }

        public Column setKey(String key) {
            this.key = key;
            return this;
        }

        public Function<?, ?> getValueFunc() {
            return valueFunc;
        }

        public Column setValueFunc(Function<?, ?> valueFunc) {
            this.valueFunc = valueFunc;
            return this;
        }

        public int getWidth() {
            return width;
        }

        public Column setWidth(int width) {
            this.width = width;
            return this;
        }

        public ColumnType getColumnType() {
            return columnType;
        }

        public Column setColumnType(ColumnType columnType) {
            this.columnType = columnType;
            return this;
        }

        public ColumnAlign getColumnAlign() {
            return columnAlign;
        }

        public Column setColumnAlign(ColumnAlign columnAlign) {
            this.columnAlign = columnAlign;
            return this;
        }

        public Consumer<XSSFCellStyle> getStyleFunc() {
            return styleFunc;
        }

        public Column setStyleFunc(Consumer<XSSFCellStyle> styleFunc) {
            this.styleFunc = styleFunc;
            return this;
        }

        public List<Column> getChildColumns() {
            return childColumns;
        }

        public Column setChildColumns(List<Column> childColumns) {
            this.childColumns = childColumns;
            return this;
        }

        public Column addChildColumn(Column childColumn) {
            if (this.childColumns == null) {
                this.childColumns = new ArrayList<>();
            }
            this.childColumns.add(childColumn);
            return this;
        }

        public boolean isComplex() {
            return childColumns != null && !childColumns.isEmpty();
        }

        public int needRowNum() {
            if (!isComplex()) {
                return 1;
            }
            return 1 + childColumns.stream()
                    // 递归查找
                    .map(Column::needRowNum)
                    // 最大值
                    .max(Comparator.comparingInt(Integer::intValue))
                    .orElse(1);
        }

        public boolean isEmpty() {
            return this.title == null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Column{title='" + title + '\'');
            if (childColumns != null && !childColumns.isEmpty()) {
                sb.append(", childColumns=").append(childColumns);
            }
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * 列的值类型
     */
    public enum ColumnType {
        /**
         * 字符串
         */
        STRING,
        /**
         * 整数
         */
        INT,
        /**
         * 浮点数
         */
        DOUBLE,
        /**
         * 时间
         */
        TIME,
        /**
         * 金额(默认居右)
         */
        AMOUNT
    }

    /**
     * 列的对齐方式
     */
    public enum ColumnAlign {
        /* 顾名思义 */
        GENERAL,
        LEFT,
        CENTER,
        RIGHT,
        FILL,
        JUSTIFY,
        CENTER_SELECTION,
        DISTRIBUTED;

        public short getCode() {
            return (short)this.ordinal();
        }
    }
}

