package com.starter.demo.support.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.starter.demo.common.NodeTree;
import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Shoven
 * @date 2019-11-04
 */
public class RegionUtils {

    private static SoftReference<List<Region>> reference;

    /**
     * 解析行政地址
     *
     * @param name 住址名称
     * @return Address
     */
    public static Address parseAddress(String name) {
        String regex = "(?<province>[^省]+省|.+自治区)(?<city>[^自治州]+自治州|[^市]+市|[^盟]+盟|[^地区]+地区|.+区划)(?<county>[^市]+市|[^县]+县|[^旗]+旗|.+区)?(?<town>[^区]+区|.+镇)?(?<village>.*)";
        Matcher m = Pattern.compile(regex).matcher(name);
        Address address = new Address();
        while (m.find()) {
            String province = m.group("province");
            if (province != null) {
                address.setProvince(province);
            }
            String city = m.group("city");
            if (city != null) {
                address.setCity(city);
            }
            String county = m.group("county");
            if (county != null) {
                address.setCountry(county);
            }
            String town = m.group("town");
            if (town != null) {
                address.setTown(town);
            }
            String village = m.group("village");
            if (village != null) {
                address.setVillage(village);
            }
        }
        return address;
    }

    /**
     * 获取省份区域信息
     *
     * @param province 省份：广东
     * @return Province
     */
    public static Province provinceOf(String province) {
        Objects.requireNonNull(province, "省份不能为空");
        return (Province)getRegions().parallelStream()
                .filter(provinceRegion -> provinceRegion.getName().startsWith(province))
                .peek(provinceRegion -> provinceRegion.setFullName(provinceRegion.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取城市区域信息
     *
     * @param province 省份：广东
     * @param city 城市：深圳
     * @return City
     */
    public static City cityOf(String province, String city) {
        Province provinceRegion = provinceOf(province);
        Objects.requireNonNull(city, "城市不能为空");
        if (provinceRegion != null) {
            return provinceRegion.getChildren().parallelStream()
                    .filter(cityRegion -> cityRegion.getName().startsWith(city))
                    .peek(cityRegion -> cityRegion.setFullName(provinceRegion.getFullName() + cityRegion.getName()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 获取县区域信息
     *
     * @param province 省份：广东
     * @param city 城市：深圳
     * @param country 城市：宝安
     * @return Country
     */
    public static Country countryOf(String province, String city, String country) {
        City cityRegion = cityOf(province, city);
        Objects.requireNonNull(city, "县区不能为空");
        if (cityRegion != null) {
            return cityRegion.getChildren().parallelStream()
                    .filter(countryRegion -> countryRegion.getName().startsWith(country))
                    .peek(countryRegion -> countryRegion.setFullName(cityRegion.getFullName() + countryRegion.getName()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 获取区域列表
     *
     * @return 区域集合
     */
    public static List<Region> getRegions() {
        if (reference == null || reference.get() == null) {
            String data;
            try {
                data = IOUtils.resourceToString("/china_region.json", UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<Region> regions = new Gson().fromJson(data, new TypeToken<List<Province>>() {}.getType());
            // 不可变List
            regions = ImmutableList.copyOf(regions);
            reference = new SoftReference<>(regions);
            return regions;
        }
        return reference.get();
    }

    @Data
    public static class Region {
        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 全称
         */
        private String fullName;

    }

    public static class Province extends Region implements NodeTree.Node<City> {
        /**
         * 下属城市
         */
        private List<City> cityList;

        @Override
        public List<City> getChildren() {
            return cityList;
        }

        @Override
        public void setChildren(List<City> children) {
            this.cityList = children;
        }
    }

    public static class City extends Region implements NodeTree.Node<Country>{
        /**
         * 下属地区
         */
        private List<Country> areaList;

        @Override
        public List<Country> getChildren() {
            return areaList;
        }

        @Override
        public void setChildren(List<Country> children) {
            this.areaList = children;
        }
    }

    public static class Country extends Region implements NodeTree.Node {

        @Override
        public List getChildren() {
            return null;
        }

        @Override
        public void setChildren(List children) {

        }
    }

    @Data
    public static class Address {

        /**
         * 省级
         */
        private String province;

        /**
         * 市级
         */
        private String city;

        /**
         * 区级
         */
        private String country;

        /**
         * 镇级
         */
        private String town;

        /**
         * 村级街道
         */
        private String village;
    }
}
