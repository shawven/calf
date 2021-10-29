package com.github.shawven.calf.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Shoven
 * @date 2019-11-04
 */
public class IndustryUtils {
    private static SoftReference<List<Industry>> reference;

    public static void main(String[] args) {

    }

    /**
     * 获取行业列表
     *
     * @return
     */
    public static List<Industry> getIndustries() {
        List<Industry> industries;
        if (reference == null || (industries = reference.get()) == null) {
            String data;
            try {
                data = IOUtils.resourceToString("/industry.json", UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            industries = new Gson().fromJson(data, new TypeToken<List<Industry>>() {
            }.getType());
//            List<Industry> industries = transform(data);
            reference = new SoftReference<>(industries);
        }
        return industries.parallelStream().map(SerializationUtils::clone).collect(Collectors.toList());
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

    public static Industry getIndustryByName(String name) {
        Objects.requireNonNull(name, "行业名称不能为空");
        final String findName = name.trim();
        List<Industry> industries = new ArrayList<>(getIndustries());
        return NodeTree.findNode(industries, industry -> industry.getName().equals(findName));
    }

    private static List<Industry> transform(String data) {
        List<IndustryRaw> raws = new Gson().fromJson(data, new TypeToken<List<IndustryRaw>>() {
        }.getType());

        List<Industry> industries =NodeTree.<IndustryRaw, Industry>from(raws)
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
                .build();
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

        /**
         * 展示名称
         */
        public String displayName;

        private List<Industry> children;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public List<Industry> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<Industry> children) {
            this.children = children;
        }
    }

    public static class IndustryRaw {

        public String id;

        public String code;

        public String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
