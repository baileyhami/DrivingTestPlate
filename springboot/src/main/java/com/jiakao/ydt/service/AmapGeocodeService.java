package com.jiakao.ydt.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiakao.ydt.config.IntegrationProperties;
import com.jiakao.ydt.dto.GeoPointDto;
import com.jiakao.ydt.util.AmapSigUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 高德地图地理编码（REST），Key 仅在后端使用。
 */
@Service
@RequiredArgsConstructor
public class AmapGeocodeService {

    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";

    private final IntegrationProperties integrationProperties;

    public Optional<GeoPointDto> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        String key = integrationProperties.getAmapKey();
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        param.put("address", address.trim());
        AmapSigUtil.appendSig(param, integrationProperties.getAmapSecret());
        String body = HttpUtil.get(GEOCODE_URL,param);
        JSONObject root = JSONUtil.parseObj(body);
        if (!"1".equals(root.getStr("status"))) {
            return Optional.empty();
        }
        JSONArray geocodes = root.getJSONArray("geocodes");
        if (geocodes == null || geocodes.isEmpty()) {
            return Optional.empty();
        }
        JSONObject first = geocodes.getJSONObject(0);
        String loc = first.getStr("location");
        if (loc == null || !loc.contains(",")) {
            return Optional.empty();
        }
        String[] parts = loc.split(",");
        BigDecimal lng = new BigDecimal(parts[0].trim());
        BigDecimal lat = new BigDecimal(parts[1].trim());
        return Optional.of(new GeoPointDto(lng, lat, loc, first.getStr("formatted_address")));
    }
}
