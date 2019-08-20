package com.test.support.generator.mybatis;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * 注释生成器
 *
 * @author Shoven
 * @date 2019-07-04 10:04
 */
public class MyCommentGenerator extends DefaultCommentGenerator {

    private Properties properties = new Properties();
    private boolean suppressDate = false;
    private boolean suppressAllComments = false;
    private boolean addRemarkComments = false;
    private SimpleDateFormat dateFormat;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.properties.putAll(properties);
        this.suppressDate = StringUtility.isTrue(properties.getProperty("suppressDate"));
        this.suppressAllComments = StringUtility.isTrue(properties.getProperty("suppressAllComments"));
        this.addRemarkComments = StringUtility.isTrue(properties.getProperty("addRemarkComments"));
        String dateFormatString = properties.getProperty("dateFormat");
        if (StringUtility.stringHasValue(dateFormatString)) {
            this.dateFormat = new SimpleDateFormat(dateFormatString);
        }

    }

    @Override
    public void addComment(XmlElement xmlElement) { }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) { }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!this.suppressAllComments) {
            field.addJavaDocLine("/**");
            String[] remarkLines = getRemarkLines(introspectedColumn);
            for (String remarkLine : remarkLines) {
                field.addJavaDocLine(" *  " + remarkLine);
            }
            field.addJavaDocLine(" */");
        }
    }


    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!this.suppressAllComments) {
            StringBuilder sb = new StringBuilder();
            method.addJavaDocLine("/**");
            String[] remarkLines = getRemarkLines(introspectedColumn);
            if (remarkLines.length > 0) {
                for (int i = 0; i < remarkLines.length; i++) {
                    if (i == 0) {
                        sb.append(" * @return ");
                    } else {
                        sb.append(" *         ");
                    }
                    sb.append(remarkLines[i]);
                }
            } else {
                sb.append(" * @return");
            }

            method.addJavaDocLine(sb.toString());
            method.addJavaDocLine(" */");
        }
    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!this.suppressAllComments) {
            StringBuilder sb = new StringBuilder();
            method.addJavaDocLine("/**");
            Parameter param = (Parameter)method.getParameters().get(0);
            sb.setLength(0);

            String[] remarkLines = getRemarkLines(introspectedColumn);
            if (remarkLines.length > 0) {
                for (int i = 0; i < remarkLines.length; i++) {
                    if (i == 0) {
                        sb.append(" * @param ").append(param.getName()).append(" ");
                    } else {
                        sb.append(" *        ");
                    }
                    sb.append(remarkLines[i]);
                }
            } else {
                sb.append(" * @param ").append(param.getName());
            }


            method.addJavaDocLine(sb.toString());
            method.addJavaDocLine(" */");
        }
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!this.suppressAllComments && this.addRemarkComments) {
            topLevelClass.addJavaDocLine("/**");
            String remarks = introspectedTable.getRemarks();
            if (this.addRemarkComments && StringUtility.stringHasValue(remarks)) {
                String[] remarkLines = remarks.split(System.getProperty("line.separator"));
                for (String remarkLine : remarkLines) {
                    topLevelClass.addJavaDocLine(" *   " + remarkLine);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(" * 对应表：");
            sb.append(introspectedTable.getFullyQualifiedTable());
            topLevelClass.addJavaDocLine(sb.toString());
            topLevelClass.addJavaDocLine(" */");
        }
    }

    private String[] getRemarkLines(IntrospectedColumn introspectedColumn) {
        String remarks = introspectedColumn.getRemarks();
        if (this.addRemarkComments && StringUtility.stringHasValue(remarks)) {
            return remarks.split(System.getProperty("line.separator"));
        }

        return new String[]{};
    }
}
