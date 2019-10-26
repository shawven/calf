package com.starter.demo.support.util.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.font.FontProvider;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-06-05 10:08
 */
public class PdfHtmlUtil {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static void main(String[] args) throws IOException {

        HashMap<String, Object> model = new HashMap<>();
        model.put("address", "深圳宝安区");
        model.put("name", "张某某");
        model.put("phone", "13111111111");
        model.put("date", "2019 年 1 月 1日");


        List<String> header = new ArrayList<>(3);
        header.add("服务名称");
        header.add("单价");
        header.add("实付金额");

        List<String> row = new ArrayList<>();
        List< List<String>> bodies = new ArrayList<>();
        row.add("云记账 x1");
        row.add("999.00");
        row.add("999.00");
        bodies.add(row);

        row = new ArrayList<>();
        row.add("云记账 x2");
        row.add("999.00");
        row.add("1998.00");
        bodies.add(row);

        row = new ArrayList<>();
        row.add("注册公司少时诵诗书所所所少时诵诗书所（加急注册、不刻章） x2");
        row.add("1500.00");
        row.add("3000.00");
        bodies.add(row);

        model.put("header", header);
        model.put("bodies", bodies);
        model.put("total", "1000元");
        model.put("title", "店铺名称：微企宝（订单编号：20190530000001；支付流水号：2019.123411011）");
        String template = FileUtils.readFileToString(new File("f:/order.html"), UTF_8);
        String htmlString = parseTemplate(template, model);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        convertHtmlToPdf(htmlString, outputStream);

        FileUtils.writeByteArrayToFile(new File("f:/html.com.starter.support.util.pdf"), outputStream.toByteArray());
    }

    public static String parseTemplate(String templateString, Object dataModel) throws IOException {
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding(UTF_8.name());

        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringLoader);

        StringWriter writer = new StringWriter();
        Template template = configuration.getTemplate("template");
        try {
            template.process(dataModel, writer);
        } catch (TemplateException ignored) {}

        return writer.toString();
    }

    public static void convertHtmlToPdf(String htmlString, OutputStream outputStream) throws IOException {
        FontProvider fp = new FontProvider();
        fp.addStandardPdfFonts();
        fp.addFont("f:/simsun.ttf");

        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setCharset(UTF_8.name());
        converterProperties.setFontProvider(fp);
        converterProperties.setCreateAcroForm(true);
        converterProperties.setBaseUri("f:/");

        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
        Document document = new Document(pdfDocument, PageSize.A4);
        HtmlConverter.convertToPdf(htmlString, pdfDocument, converterProperties);
        document.close();
    }
}
