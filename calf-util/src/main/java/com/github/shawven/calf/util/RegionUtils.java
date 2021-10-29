package com.github.shawven.calf.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 行政地区工具
 *
 * @author Shoven
 * @date 2019-11-04
 */
public class RegionUtils {

    private static SoftReference<List<Province>> reference;

    public static void main(String[] args) {

    }

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
        return getRegions().parallelStream()
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
    public static List<Province> getRegions() {
        List<Province> regions;
        if (reference == null || (regions = reference.get()) == null) {
            String data;
            try {
                data = IOUtils.resourceToString("/china_region.json", UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            regions = new Gson().fromJson(data, new TypeToken<List<Province>>() {}.getType());
            reference = new SoftReference<>(regions);
        }
        return regions.parallelStream().map(SerializationUtils::clone).collect(Collectors.toList());
    }

    public static class Region implements Serializable {

        private static final long serialVersionUID = 3487962100670057757L;

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

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class Province extends Region {

        private static final long serialVersionUID = 2436389774406543258L;

        /**
         * 下属城市
         */
        private List<City> cityList;

        public List<City> getChildren() {
            return cityList;
        }

        public void setChildren(List<City> children) {
            this.cityList = children;
        }
    }

    public static class City extends Region {

        private static final long serialVersionUID = 9085363875009465890L;

        /**
         * 下属地区
         */
        private List<Country> areaList;

        public List<Country> getChildren() {
            return areaList;
        }

        public void setChildren(List<Country> children) {
            this.areaList = children;
        }

    }

    public static class Country extends Region {

        private static final long serialVersionUID = 5344567900154338252L;
    }

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

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTown() {
            return town;
        }

        public void setTown(String town) {
            this.town = town;
        }

        public String getVillage() {
            return village;
        }

        public void setVillage(String village) {
            this.village = village;
        }
    }
}
