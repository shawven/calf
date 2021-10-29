package com.github.shawven.calf.util.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpTool;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * @author Shoven
 * @date 2019-06-03 9:18
 */
public class PdfUtil {

    private String src;

    private String desc;

    private ByteArrayOutputStream out;

    private PdfDocument pdfDoc;

    private Document document;

    private PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", true);

    public static void main(String[] args) throws IOException {
        // 设置表单域字段
        HashMap<String, String> fields = new HashMap<>();
        fields.put("contractNo", "2019060610010");

        fields.put("jf", "深圳某某计算机系统有限公司");
        fields.put("jfSign", "王某某");
        fields.put("jfPhone", "800-8820-820");
        fields.put("jfAddress", "深圳市南山区高新园地铁站C出口 10 栋 10层");

        fields.put("yf", "深圳微企宝计算机系统有限公司");
        fields.put("yfSign", "微企宝");
        fields.put("yfPhone", "0755-29666320");
        fields.put("yfAddress", "深圳市宝安区西乡街道银田路 4 号华丰宝安智谷科技创新园 H 座 7 楼 720， 726，728");

        List<Cell> header = new ArrayList<>();
        Paragraph title = new Paragraph("店铺名称：微企宝（订单编号：20190530000001；支付流水号：2019.123411011）");
        // 占位1 到 3列（合并所有列）
        header.add(new Cell(1, 3).add(title));
        header.add(new Cell().add(new Paragraph("服务名称")));
        header.add(new Cell().add(new Paragraph("单价")));
        header.add(new Cell().add(new Paragraph("实付金额")));

        List<Cell> rows = new ArrayList<>();
        rows.add(new Cell().add(new Paragraph("云记账 x1")));
        rows.add(new Cell().add(new Paragraph("999.00")));
        rows.add(new Cell().add(new Paragraph("999.00")));

        rows.add(new Cell().add(new Paragraph("云记账 x2")));
        rows.add(new Cell().add(new Paragraph("999.00")));
        rows.add(new Cell().add(new Paragraph("1998.00")));

        rows.add(new Cell().add(new Paragraph("云记账 x2")));
        rows.add(new Cell().add(new Paragraph("999.00")));
        rows.add(new Cell().add(new Paragraph("1998.00")));

        rows.add(new Cell().add(new Paragraph("注册公司少时诵诗书所所所少时诵诗书所（加急注册、不刻章） x2")));
        rows.add(new Cell().add(new Paragraph("1500.00")));
        rows.add(new Cell().add(new Paragraph("3000.00")));

        rows.add(new Cell().add(new Paragraph("合计：")).setBorderRight(Border.NO_BORDER));
        rows.add(new Cell().setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(0.5f)));
        rows.add(new Cell().add(new Paragraph("5997元")).setBorderLeft(Border.NO_BORDER).setFontColor(ColorConstants.RED));

        // 创建有3列的表格，列比例4:1:1
        Table table = new Table(new float[]{4, 1, 1});
        // 添加表头和行数据
        header.forEach(table::addHeaderCell);
        rows.forEach(table::addCell);

        // 表头灰色、padding:0
        table.getHeader().setBackgroundColor(new DeviceRgb(217, 217, 217)).setPadding(0);
        table.setFontSize(9);

        String template =  table.getNumberOfRows() > 4 ? "F:/defaultServiceContract2.com.github.shawven.calf.support.util.pdf" : "F:/defaultServiceContract1.com.github.shawven.calf.support.util.pdf";
        PdfUtil pdfUtil = new PdfUtil(template, "f:/newDefaultServiceContract.com.github.shawven.calf.support.util.pdf");
        TableBlock tableBlock = pdfUtil.new TableBlock(table);
        // 宽度100%
        tableBlock.setWidth(100);

        Document document = pdfUtil.getDocument();
        Position position = pdfUtil.getPositionByText("本次为您所提供的服务如下");
        table = tableBlock.getTable();
        table.setFixedPosition(position.getPageNum(), position.getX(),
                position.getY() - tableBlock.getBox().getHeight() - 30,
                UnitValue.createPercentValue(tableBlock.getWidth()));

        pdfUtil.setFormFields(fields);
        document.add(table);
        pdfUtil.close();
    }


    /**
     * 写到文件 (只读)
     *
     * @param src
     * @param desc
     * @throws IOException
     */
    public PdfUtil(String src, String desc) throws IOException {
        this(src, desc, false);
    }

    /**
     * 写到文件
     *
     * @param src
     * @param desc
     * @param readOnly
     * @throws IOException
     */
    public PdfUtil(String src, String desc, boolean readOnly) throws IOException {
        this.src = src;
        this.desc = desc;
        this.pdfDoc = new PdfDocument(new PdfReader(src), getPdfWriter(new FileOutputStream(desc), readOnly));
        this.document = new Document(pdfDoc);
    }

    /**
     * 写入字节输出流 (只读)
     *
     * @param src
     * @param outputStream
     * @throws IOException
     */
    public PdfUtil(String src, ByteArrayOutputStream outputStream) throws IOException {
        this(src, outputStream, false);
    }


    /**
     * 写入字节输出流
     *
     * @param src
     * @param outputStream
     * @throws IOException
     */
    public PdfUtil(String src, ByteArrayOutputStream outputStream, boolean readOnly) throws IOException {
        this.src = src;
        this.out = outputStream;
        this.pdfDoc = new PdfDocument(new PdfReader(src), getPdfWriter(outputStream, readOnly));
        this.document = new Document(pdfDoc);
    }

    /**
     * 获取PdfWriter 是否需要只读
     *
     * @param outputStream
     * @param readOnly
     * @return
     * @throws FileNotFoundException
     */
    private PdfWriter getPdfWriter(OutputStream outputStream, boolean readOnly) {
        PdfWriter pdfWriter;
        if (readOnly) {
            WriterProperties props = new WriterProperties();
            props.setStandardEncryption(null, null,
                    EncryptionConstants.ALLOW_PRINTING | EncryptionConstants.ALLOW_COPY,
                    EncryptionConstants.STANDARD_ENCRYPTION_128);
            pdfWriter = new PdfWriter(outputStream, props);
        } else {
            pdfWriter = new PdfWriter(outputStream);
        }
        return pdfWriter;
    }

    /**
     * 生成新的pdf
     */
    public void close() {
        document.close();
    }


    /**
     * pdf转图片
     * itext生成pdf pdfbox转图片
     *
     * @param fileName
     * @throws IOException
     */
    public void pdfToImage(String fileName) throws IOException {
        // 填充pdf信息
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        //设置表单域不可编辑
        form.flattenFields();
        // 关闭生成pdf
        document.close();

        boolean isOutputStream = out != null;
        // 调用apache pdfBoc读取pdf
        PDDocument pdDocument = isOutputStream
                ? PDDocument.load(new ByteArrayInputStream(out.toByteArray()))
                : PDDocument.load(new File(desc));

        PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 90, ImageType.RGB);

        // 写入图片
        ImageIO.write(bim,  "png", new File(fileName));
        bim.flush();

        if (!isOutputStream) {
            // 删除pdf
            new File(desc).deleteOnExit();
        }

    }

    /**
     * 设置表单字段
     *
     * @param fields
     */
    public void setFormFields(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        Map<String, PdfFormField> formFields = form.getFormFields();

        if (formFields.isEmpty()) {
            return;
        }

        fields.forEach((k, v) -> {
            if (formFields.containsKey(k)) {
                PdfFormField formField = formFields.get(k);
                formField.setValue(v);
            }
        });

        form.setNeedAppearances(true);
    }

    /**
     * 腾出空间（把指定区域的往下挪）
     *
     * @param position 开始坐标定位
     * @param tableBox
     * @throws IOException
     */
    public void makeAndMoveArea(Position position, Rectangle tableBox) throws IOException {
        float tableHeight = tableBox.getHeight();
        float tableWidth = tableBox.getWidth();

        PdfPage pdfPage = pdfDoc.getPage(position.getPageNum());
        Rectangle pageSize = pdfPage.getPageSize();
        Rectangle toMove = new Rectangle(position.getX(), position.getY() - tableHeight, tableWidth, tableHeight);

        PdfFormXObject pageXObject = pdfPage.copyAsFormXObject(pdfDoc);

        PdfFormXObject xObject1 = new PdfFormXObject(pageSize);
        PdfCanvas canvas1 = new PdfCanvas(xObject1, pdfDoc);
        canvas1.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
        canvas1.rectangle(toMove.getLeft(), toMove.getBottom(), toMove.getWidth(), toMove.getHeight());
        canvas1.eoClip();
        canvas1.newPath();
        canvas1.addXObject(pageXObject, 0, 0);

        PdfFormXObject xObject2 = new PdfFormXObject(pageSize);
        PdfCanvas canvas2 = new PdfCanvas(xObject2, pdfDoc);
        canvas2.rectangle(toMove.getLeft(), toMove.getBottom(), toMove.getWidth(), toMove.getHeight());
        canvas2.clip();
        canvas2.newPath();
        canvas2.addXObject(pageXObject, 0, 0);

        //  清除待复制区的内容
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        Rectangle rectangle = new Rectangle(position.getX(), position.getY() - tableHeight, tableWidth, tableHeight);
        cleanUpLocations.add(new PdfCleanUpLocation(position.getPageNum(), rectangle, ColorConstants.WHITE));
        PdfCleanUpTool cleaner = new PdfCleanUpTool(pdfDoc, cleanUpLocations);
        cleaner.cleanUp();

        PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(position.getPageNum()));
        canvas.addXObject(xObject1, 0, 0);
        canvas.addXObject(xObject2, 0, -tableHeight);
    }

    /**
     * 获取文本定位（只获取第一个找到的）
     *
     * @param keyword
     * @return
     */
    public Position getPositionByText(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("无效的文本：" + keyword);
        }
        PdfDocumentContentParser pdfReaderContentParser = new PdfDocumentContentParser(pdfDoc);
        int numberOfPages = pdfDoc.getNumberOfPages();
        final Position[] container = new Position[1];

        for (int i = 1; i <= numberOfPages; i++) {
            int finalI = i;
            pdfReaderContentParser.processContent(i, new IEventListener() {
                @Override
                public void eventOccurred(IEventData iEventData, EventType eventType) {
                    if (iEventData instanceof TextRenderInfo) {
                        TextRenderInfo textRenderInfo = (TextRenderInfo) iEventData;
                        String text = textRenderInfo.getText();
                        if (null != text && text.contains(keyword)) {
                            Rectangle boundingRectangle = textRenderInfo.getBaseline().getBoundingRectangle();
                            container[0] = new Position(boundingRectangle.getX(), boundingRectangle.getY(), finalI);
                        }
                    }
                }

                @Override
                public Set<EventType> getSupportedEvents() {
                    HashSet<EventType> eventTypes = new HashSet<>();
                    eventTypes.add(EventType.RENDER_TEXT);
                    return eventTypes;
                }
            });
            if (container[0] != null) {
                break;
            }
        }

        if (container[0] == null) {
            throw new IllegalArgumentException("找不到此定位文本：" + keyword);
        }
        return container[0];
    }

    public PdfFont getFont() {
        return font;
    }

    public void setFont(PdfFont font) {
        this.font = font;
    }

    public PdfDocument getPdfDoc() {
        return pdfDoc;
    }

    public void setPdfDoc(PdfDocument pdfDoc) {
        this.pdfDoc = pdfDoc;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public class TableBlock {

        private com.itextpdf.layout.element.Table table;

        /**
         * 加粗头部
         */
        private boolean boldHeader;

        /**
         * 百分比宽度
         */
        private int width;


        public TableBlock(Table table) {
            table.setFont(font);
            table.setTextAlignment(TextAlignment.CENTER);
            table.setHorizontalAlignment(HorizontalAlignment.CENTER);
            table.setFixedLayout();
            this.table = table;
        }

        public Rectangle getBox() {
            PageSize ps = pdfDoc.getDefaultPageSize();
            IRenderer tableRenderer = table.createRendererSubTree().setParent(document.getRenderer());
            LayoutResult tableLayoutResult =
                    tableRenderer.layout(new LayoutContext(new LayoutArea(0, new Rectangle(ps.getWidth(), ps.getHeight()))));
            return tableLayoutResult.getOccupiedArea().getBBox();
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        public boolean isBoldHeader() {
            return boldHeader;
        }

        public void setBoldHeader(boolean boldHeader) {
            this.boldHeader = boldHeader;
        }

        public int getWidth() {
            return Math.min(width, 100);
        }

        public void setWidth(int width) {
            this.width = width;
        }

    }

    public class Position {

        /**
         * x轴坐标
         */
        private float x;

        /**
         * y轴坐标
         */
        private float y;

        /**
         * 第几页
         */
        private int pageNum;

        public Position(float x, float y, int pageNum) {
            this.x = x;
            this.y = y;
            this.pageNum = pageNum;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }
    }
}
