package com.starter.demo.support.generator.mybatis;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器
 *
 * @author Shoven
 * @date 2019-07-03 17:36
 */
public class CodeGenerator {

    /**
     * 可覆盖
     */
    public static boolean overwrite = true;

    /**
     * classpath 下的配置文件路径
     */
    public static String configPath = "/generatorConfig.xml";

    public static void main(String[] args) throws Exception {
        List<String> warnings = new ArrayList<>();

        // 读取配置文件
        File configFile = new File(CodeGenerator.class.getResource(configPath).getFile());

        // 解析配置
        Configuration config = new ConfigurationParser(warnings).parseConfiguration(configFile);

        // 构造生成器
        MyBatisGenerator generator = new MyBatisGenerator(config, new DefaultShellCallback(overwrite), warnings);

        // 生成代码，设置进度回调（输出当前处理信息）
        generator.generate(new VerboseProgressCallback());

        // 输出警告信息（例如：覆盖）
        warnings.forEach(System.out::println);
    }

}
