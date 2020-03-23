package com.starter.demo.support.generator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.AbstractTemplateEngine;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.starter.demo.mapper.base.BaseMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * 代码生成器
 *
 * @author Shoven
 * @date 2019-08-06
 */
public class CodeGenerator {

    /**
     * 输出目录
     */
    private String outputDir = System.getProperty("user.dir");

    /**
     * 模板包路径（classpath下）
     */
    private String templatePath = "/templates";

    /**
     * 父包和子包设置
     */
    private String parentPackage = "com.wqb.jz";
    private String servicePackage = "service";
    private String serviceImplPackage = "service.impl";
    private String mapperPackage = "mapper";
    private String mapperXmlPackage = "mapper.xml";
    private String entityPackage = "domain";

    /**
     * 需要继承的基类设置
     */
    private Class superMapper = BaseMapper.class;

    private List<FileWhiteList> whiteList = new ArrayList<>();

    /**
     * 数据源设置
     */
    private String dataSourceUrl = "jdbc:mysql://192.168.1.10:3306/wqb_jz?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8";
    private String driverName = com.mysql.cj.jdbc.Driver.class.getName();
    private String username = "jzdev";
    private String password = "Jzdev1130.";

    /**
     * 存在时是否覆盖，建议设置为false
     */
    private boolean overwrite = true;


    public static void main(String[] args) {
        new CodeGenerator()
                .setOutputPath("generator/src/test/java");
    }

    /**
     * @param entity 实体名称
     * @param table  表名
     */
    private CodeGenerator generate(String entity, String table, IdType idType) {
        if (StringUtils.isBlank(table)) {
            throw new RuntimeException("请填写表名");
        }

        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig()
                .setOutputDir(outputDir)
                .setServiceName(entity + "Service")
                .setServiceImplName(entity + "ServiceImpl")
                .setMapperName(entity + "Mapper")
                .setXmlName(entity + "Mapper")
                .setEntityName(entity)
                .setAuthor("Generator")
                .setFileOverride(overwrite)
                .setBaseResultMap(true)
                .setBaseColumnList(true)
                .setIdType(idType)
                .setDateType(DateType.ONLY_DATE)
                .setOpen(false);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig()
                .setUrl(dataSourceUrl)
                .setSchemaName("public")
                .setDriverName(driverName)
                .setUsername(username)
                .setPassword(password);

        // 包配置
        PackageConfig pc = new PackageConfig()
                .setParent(parentPackage)
                .setService(servicePackage)
                .setServiceImpl(serviceImplPackage)
                .setMapper(mapperPackage)
                .setXml(mapperXmlPackage)
                .setEntity(entityPackage);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig()
                .setInclude(table)
                .setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                .setEntityLombokModel(true)
                .setRestControllerStyle(true)
                .setSuperMapperClass(superMapper == null ? null : superMapper.getName());

        // 模板配置
        TemplateConfig templateConfig = new TemplateConfig()
                .setService(templatePath + "/service.java")
                .setServiceImpl(templatePath + "/serviceImpl.java")
                .setEntity(templatePath + "/entity.java")
                .setMapper(templatePath + "/mapper.java")
                .setXml(templatePath + "/mapper.xml");

        // 代码生成器
        new AutoGenerator()
                .setDataSource(dsc)
                .setStrategy(strategy)
                .setPackageInfo(pc)
                .setGlobalConfig(globalConfig)
                .setTemplate(templateConfig)
                .setTemplateEngine(new CustomFreemarkerTemplateEngine())
                .execute();

        whiteList = new ArrayList<>();
        return this;
    }

    /**
     * 前缀分组生成，生成的代码在配置的（组名 + 包）构成在二级包下
     *
     * @param name
     * @param generatorFunction
     * @return
     */
    public CodeGenerator prefixGroup(String name, Consumer<CodeGenerator> generatorFunction) {
        setPrefixGroupName(name);
        generatorFunction.accept(this);
        // 删除组名不影响下一个生成
        unsetPrefixGroupName(name);
        return this;
    }

    /**
     * 后缀分组生成，生成的代码在配置的（包 + 组名）构成在二级包下
     *
     * @param name
     * @param generatorFunction
     * @return
     */
    public CodeGenerator suffixGroup(String name, Consumer<CodeGenerator> generatorFunction) {
        setSuffixGroupName(name);
        generatorFunction.accept(this);
        // 删除组名不影响下一个生成
        unsetSuffixGroupName(name);
        return this;
    }

    /**
     * 包含的文件
     *
     * @param whiteList
     * @return
     */
    public CodeGenerator include(FileWhiteList... whiteList) {
        if (whiteList != null) {
            Collections.addAll(this.whiteList, whiteList);
        }
        return this;
    }

    /**
     * 排除的文件
     *
     * @param blacklist
     * @return
     */
    public CodeGenerator exclude(FileWhiteList... blacklist) {
        Collections.addAll(this.whiteList, FileWhiteList.CONTROLLER, FileWhiteList.SERVICE,
                FileWhiteList.SERVICE_IMPL, FileWhiteList.ENTITY, FileWhiteList.MAPPER, FileWhiteList.XML);
        if (blacklist != null) {
            List<FileWhiteList> exclusion = Arrays.asList(blacklist);
            this.whiteList.removeIf(exclusion::contains);
        }
        return this;
    }

    public CodeGenerator includeEntityAndMapper() {
        Collections.addAll(this.whiteList, FileWhiteList.ENTITY, FileWhiteList.MAPPER, FileWhiteList.XML);
        return this;
    }

    private CodeGenerator setOutputPath(String path) {
        outputDir = outputDir + "/" + path;
        return this;
    }

    /**
     * 设置前缀组名
     *
     * @param name
     * @return
     */
    private CodeGenerator setPrefixGroupName(String name) {
        parentPackage = parentPackage + "." + name;
        return this;
    }

    /**
     * 删除前缀组名
     *
     * @param name
     * @return
     */
    private CodeGenerator unsetPrefixGroupName(String name) {
        parentPackage = parentPackage.substring(0, parentPackage.length() - ("." + name).length());
        return this;
    }

    /**
     * 设置后缀组名
     *
     * @param name
     * @return
     */
    private CodeGenerator setSuffixGroupName(String name) {
        servicePackage = addSuffix(servicePackage, name);
        serviceImplPackage = addSuffix(serviceImplPackage, name);
        mapperPackage = addSuffix(mapperPackage, name);
        mapperXmlPackage = addSuffix(mapperXmlPackage, name);
        entityPackage = addSuffix(entityPackage, name);
        return this;
    }

    /**
     * 删除后缀组名
     *
     * @param name
     * @return
     */
    private CodeGenerator unsetSuffixGroupName(String name) {
        servicePackage = removeSuffix(servicePackage, name);
        serviceImplPackage = removeSuffix(serviceImplPackage, name);
        mapperPackage = removeSuffix(mapperPackage, name);
        mapperXmlPackage = removeSuffix(mapperXmlPackage, name);
        entityPackage = removeSuffix(entityPackage, name);
        return this;
    }

    private String addSuffix(String packageName, String suffix) {
        return packageName + (StringUtils.isNotBlank(suffix) ? "." + suffix : "");
    }

    private String removeSuffix(String packageName, String suffix) {
        return packageName.endsWith(suffix)
                ? packageName.substring(0, packageName.length() - ("." + suffix).length())
                : packageName;
    }

    class CustomFreemarkerTemplateEngine extends FreemarkerTemplateEngine {
        @Override
        public AbstractTemplateEngine batchOutput() {
            try {
                List<TableInfo> tableInfoList = this.getConfigBuilder().getTableInfoList();

                for (TableInfo tableInfo : tableInfoList) {
                    Map<String, Object> objectMap = this.getObjectMap(tableInfo);
                    Map<String, String> pathInfo = this.getConfigBuilder().getPathInfo();
                    TemplateConfig template = this.getConfigBuilder().getTemplate();
                    InjectionConfig injectionConfig = this.getConfigBuilder().getInjectionConfig();
                    if (null != injectionConfig) {
                        injectionConfig.initMap();
                        objectMap.put("cfg", injectionConfig.getMap());
                        List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                        if (CollectionUtils.isNotEmpty(focList)) {

                            for (FileOutConfig foc : focList) {
                                if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.OTHER, foc.outputFile(tableInfo))) {
                                    this.writer(objectMap, foc.getTemplatePath(), foc.outputFile(tableInfo));
                                }
                            }
                        }
                    }

                    String entityName = tableInfo.getEntityName();
                    String controllerFile;


                    if (canWrite(FileWhiteList.ENTITY) && null != entityName && null != pathInfo.get("entity_path")) {
                        controllerFile = String.format(pathInfo.get("entity_path") + File.separator + "%s" + this.suffixJavaOrKt(), entityName);
                        if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.ENTITY, controllerFile)) {
                            this.writer(objectMap, this.templateFilePath(template.getEntity(this.getConfigBuilder().getGlobalConfig().isKotlin())), controllerFile);
                        }
                    }

                    if (canWrite(FileWhiteList.MAPPER) && null != tableInfo.getMapperName() && null != pathInfo.get("mapper_path")) {
                        controllerFile = String.format(pathInfo.get("mapper_path") + File.separator + tableInfo.getMapperName() + this.suffixJavaOrKt(), entityName);
                        if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.MAPPER, controllerFile)) {
                            this.writer(objectMap, this.templateFilePath(template.getMapper()), controllerFile);
                        }
                    }

                    if (canWrite(FileWhiteList.XML) && null != tableInfo.getXmlName() && null != pathInfo.get("xml_path")) {
                        controllerFile = String.format(pathInfo.get("xml_path") + File.separator + tableInfo.getXmlName() + ".xml", entityName);
                        if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.XML, controllerFile)) {
                            this.writer(objectMap, this.templateFilePath(template.getXml()), controllerFile);
                        }
                    }

                    if (canWrite(FileWhiteList.SERVICE) && null != tableInfo.getServiceName() && null != pathInfo.get("service_path")) {
                        controllerFile = String.format(pathInfo.get("service_path") + File.separator + tableInfo.getServiceName() + this.suffixJavaOrKt(), entityName);
                        if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.SERVICE, controllerFile)) {
                            this.writer(objectMap, this.templateFilePath(template.getService()), controllerFile);
                        }
                    }

                    if (canWrite(FileWhiteList.SERVICE_IMPL) && null != tableInfo.getServiceImplName() && null != pathInfo.get("service_impl_path")) {
                        controllerFile = String.format(pathInfo.get("service_impl_path") + File.separator + tableInfo.getServiceImplName() + this.suffixJavaOrKt(), entityName);
                        if (this.isCreate(com.baomidou.mybatisplus.generator.config.rules.FileType.SERVICE_IMPL, controllerFile)) {
                            this.writer(objectMap, this.templateFilePath(template.getServiceImpl()), controllerFile);
                        }
                    }
                }
            } catch (Exception var11) {
                AbstractTemplateEngine.logger.error("无法创建文件，请检查配置信息！", var11);
            }

            return this;
        }

        private boolean canWrite(FileWhiteList item) {
            if (CodeGenerator.this.whiteList == null || CodeGenerator.this.whiteList.isEmpty()) {
                return true;
            }
            return whiteList.contains(item);
        }
    }

    public enum FileWhiteList {
        // 顾名思义
        CONTROLLER,
        SERVICE,
        SERVICE_IMPL,
        MAPPER,
        XML,
        ENTITY;
    }
}
