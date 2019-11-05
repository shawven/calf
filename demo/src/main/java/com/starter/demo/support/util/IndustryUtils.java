package com.starter.demo.support.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.starter.demo.common.NodeTree;
import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Shoven
 * @date 2019-11-04
 */
public class IndustryUtils {
    private static SoftReference<List<Industry>> reference;

    /**
     * 获取行业列表
     *
     * @return
     */
    public static List<Industry> getIndustries() {
        if (reference == null || reference.get() == null) {
            String data;
            try {
                data = IOUtils.resourceToString("/industry.json", UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<Industry> industries = new Gson().fromJson(data, new TypeToken<List<Industry>>() {
            }.getType());
//            List<Industry> industries = transform(data);

            // 不可变List
            industries = ImmutableList.copyOf(industries);
            reference = new SoftReference<>(industries);
            return industries;
        }
        return reference.get();
    }

    public static Industry getIndustryByCode(String code) {
        Objects.requireNonNull(code, "行业编码不能为空");
        code = code.trim();
        List<Industry> industries = new ArrayList<>(getIndustries());
        while (!industries.isEmpty()) {
            for (Industry industry : industries) {
                String nextCode = industry.getCode();
                if (code.startsWith(nextCode)) {
                    if (nextCode.equals(code)) {
                        return industry;
                    }
                    List<Industry> children = industry.getChildren();
                    if (children != null && !children.isEmpty()) {
                        industries = children;
                        break;
                    }
                }
            }
        }
        return null;
    }

    private static List<Industry> transform(String data) {
        List<IndustryRaw> raws = new Gson().fromJson(data, new TypeToken<List<IndustryRaw>>() {
        }.getType());

        List<Industry> industries = new NodeTree<>(raws)
                .rootFilter(industry -> industry.getCode().length() == 1)
                .childFilter((parent, child) -> {
                    String parentId= parent.getId();
                    String childId = child.getId();
                    String parentCode = parent.getCode();
                    String childCode = child.getCode();
                    if (parent.getCode().length() == 1 && childCode.length() == 2) {
                        return parentId.substring(0, parentId.length() - 2)
                                .equals(childId.substring(0, childId.length() - 2));
                    } else {
                        return childCode.startsWith(parentCode) && childCode.length() == parentCode.length() + 1;
                    }

                })
                .map(industryRaw -> {
                    Industry industry = new Industry();
                    industry.setCategory(industryRaw.getCode());
                    industry.setCode(industryRaw.getCode());
                    industry.setName(industryRaw.getName().trim());
                    return industry;
                })
                .generate();
        mergeCode(industries, null);
        return industries;
    }

    private static void mergeCode(List<Industry> industries, String parentCode) {
        if (industries == null) {
            return;
        }
        for (Industry industry : industries) {
            String selfCode = industry.getCode();
            industry.setCode(parentCode != null ? parentCode + selfCode : selfCode);
            List<Industry> children = industry.getChildren();
            if (children != null) {
                mergeCode(children, parentCode == null ? selfCode : parentCode);
            }
        }
    }


    @Data
    public static class Industry implements NodeTree.Node<Industry> {

        /**
         * 行业分类
         */
        public String category;

        /**
         * 行业编码
         */
        public String code;

        /**
         * 行业名称
         */
        public String name;

        private List<Industry> children;
    }

    @Data
    public static class IndustryRaw {

        public String id;

        public String code;

        public String name;
    }

}
