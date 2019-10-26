package com.starter.demo.support.generator.mybatis;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectAllMethodGenerator;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper接口生成器
 * 增强 selectAll方法
 *
 * @author Shoven
 * @date 2019-07-04 11:14
 */
public class MyJavaClientGenerator extends JavaMapperGenerator {

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        this.progressCallback.startTask(Messages.getString("Progress.17", this.introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = this.context.getCommentGenerator();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(this.introspectedTable.getMyBatis3JavaMapperType());
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);
        String rootInterface = this.introspectedTable.getTableConfigurationProperty("rootInterface");
        if (!StringUtility.stringHasValue(rootInterface)) {
            rootInterface = this.context.getJavaClientGeneratorConfiguration().getProperty("rootInterface");
        }

        if (StringUtility.stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        this.addSelectAllMethod(interfaze);
        this.addSelectByExampleWithBLOBsMethod(interfaze);
        this.addSelectByExampleWithoutBLOBsMethod(interfaze);
        this.addSelectByPrimaryKeyMethod(interfaze);
        this.addCountByExampleMethod(interfaze);
        this.addInsertMethod(interfaze);
        this.addInsertSelectiveMethod(interfaze);
        this.addUpdateByExampleSelectiveMethod(interfaze);
        this.addUpdateByExampleWithBLOBsMethod(interfaze);
        this.addUpdateByExampleWithoutBLOBsMethod(interfaze);
        this.addUpdateByPrimaryKeySelectiveMethod(interfaze);
        this.addUpdateByPrimaryKeyWithBLOBsMethod(interfaze);
        this.addUpdateByPrimaryKeyWithoutBLOBsMethod(interfaze);
        this.addDeleteByExampleMethod(interfaze);
        this.addDeleteByPrimaryKeyMethod(interfaze);
        List<CompilationUnit> answer = new ArrayList<>();
        if (this.context.getPlugins().clientGenerated(interfaze, (TopLevelClass)null, this.introspectedTable)) {
            answer.add(interfaze);
        }

        List<CompilationUnit> extraCompilationUnits = this.getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        return answer;
    }

    private void addSelectAllMethod(Interface interfaze) {
        AbstractJavaMapperMethodGenerator methodGenerator = new SelectAllMethodGenerator();
        this.initializeAndExecuteGenerator(methodGenerator, interfaze);
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new MyXMLMapperGenerator();
    }
}
