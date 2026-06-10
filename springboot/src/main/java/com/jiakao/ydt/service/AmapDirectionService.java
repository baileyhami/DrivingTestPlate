package com.jiakao.ydt.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiakao.ydt.config.IntegrationProperties;
import com.jiakao.ydt.dto.RoutePlanningResultDto;
import com.jiakao.ydt.util.AmapSigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德 Web 服务路径规划（驾车 / 步行），用于考场线路展示。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmapDirectionService {

    private static final String DRIVING_URL = "https://restapi.amap.com/v3/direction/driving";
    private static final String WALKING_URL = "https://restapi.amap.com/v3/direction/walking";
    /** 驾车途经点过多时截断，避免超出平台限制 */
    private static final int MAX_ANCHOR_POINTS = 18;

    private final IntegrationProperties integrationProperties;

    public RoutePlanningResultDto planFromRoutePathJson(String routePathJson) {
        List<double[]> anchors = parseAnchors(routePathJson);
        if (anchors.size() < 2) {
            return fail("至少需要两个有效的 [lng,lat] 锚点用于路径规划");
        }
        if (anchors.size() > MAX_ANCHOR_POINTS) {
            anchors = resampleAnchors(anchors, MAX_ANCHOR_POINTS);
        }

        String origin = formatLngLat(anchors.get(0));
        String destination = formatLngLat(anchors.get(anchors.size() - 1));
        String waypoints = buildWaypoints(anchors);

        JSONObject driving = requestDriving(origin, destination, waypoints);
        if (driving != null && "1".equals(driving.getStr("status"))) {
            List<List<Double>> path = toDtoPath(extractPolyline(driving));
            if (path.size() >= 2) {
                int[] distDur = extractDistanceDuration(driving);
                return ok(path, "DRIVING", distDur[0], distDur[1], driving.getStr("info"));
            }
        } else if (driving != null) {
            log.debug("驾车路径规划未成功: {}", driving.getStr("info"));
        }

        List<List<Double>> walkPath = toDtoPath(chainWalkingSegments(anchors));
        if (walkPath.size() >= 2) {
            return ok(walkPath, "WALKING_SEGMENT", -1, -1, "步行分段拼接");
        }

        List<List<Double>> straight = toDtoPath(anchors);
        return ok(straight, "ANCHOR_STRAIGHT", -1, -1,
                driving != null ? driving.getStr("info") : "路径规划不可用，已按锚点直连展示");
    }

    private RoutePlanningResultDto ok(List<List<Double>> path, String mode, int distMeters, int durSeconds, String info) {
        Integer dm = distMeters >= 0 ? distMeters : null;
        Integer ds = durSeconds >= 0 ? durSeconds : null;
        return new RoutePlanningResultDto(path, mode, dm, ds, info);
    }

    private RoutePlanningResultDto fail(String msg) {
        return new RoutePlanningResultDto(List.of(), "ERROR", null, null, msg);
    }

    private List<double[]> parseAnchors(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JSONArray arr = JSONUtil.parseArray(json);
            List<double[]> out = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JSONArray pt = arr.getJSONArray(i);
                if (pt == null || pt.size() < 2) {
                    continue;
                }
                double lng = pt.getDouble(0);
                double lat = pt.getDouble(1);
                if (Double.isFinite(lng) && Double.isFinite(lat)) {
                    out.add(new double[]{lng, lat});
                }
            }
            return out;
        } catch (Exception e) {
            log.warn("解析 routePathJson 失败: {}", e.getMessage());
            return List.of();
        }
    }

    private List<double[]> resampleAnchors(List<double[]> anchors, int max) {
        if (anchors.size() <= max) {
            return anchors;
        }
        List<double[]> out = new ArrayList<>(max);
        out.add(anchors.get(0));
        double step = (anchors.size() - 1.0) / (max - 1);
        for (int i = 1; i < max - 1; i++) {
            int idx = (int) Math.round(i * step);
            idx = Math.min(idx, anchors.size() - 2);
            out.add(anchors.get(idx));
        }
        out.add(anchors.get(anchors.size() - 1));
        return out;
    }

    private static String formatLngLat(double[] p) {
        return p[0] + "," + p[1];
    }

    private static String buildWaypoints(List<double[]> anchors) {
        if (anchors.size() <= 2) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < anchors.size() - 1; i++) {
            if (i > 1) {
                sb.append('|');
            }
            sb.append(formatLngLat(anchors.get(i)));
        }
        return sb.toString();
    }

    private JSONObject requestDriving(String origin, String destination, String waypoints) {
        String key = integrationProperties.getAmapKey();
        if (key == null || key.isBlank()) {
            return null;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        param.put("origin", origin);
        param.put("destination", destination);
        param.put("extensions", "all");
        if (waypoints != null && !waypoints.isBlank()) {
            param.put("waypoints", waypoints);
        }
        AmapSigUtil.appendSig(param, integrationProperties.getAmapSecret());
        try {
            String body = HttpUtil.get(DRIVING_URL);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            log.warn("驾车路径请求异常: {}", e.getMessage());
            return null;
        }
    }

    private JSONObject requestWalking(String origin, String destination) {
        String key = integrationProperties.getAmapKey();
        if (key == null || key.isBlank()) {
            return null;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        param.put("origin", origin);
        param.put("destination", destination);
        AmapSigUtil.appendSig(param, integrationProperties.getAmapSecret());
        try {
            String body = HttpUtil.get(WALKING_URL);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            log.warn("步行路径请求异常: {}", e.getMessage());
            return null;
        }
    }

    private List<double[]> extractPolyline(JSONObject root) {
        JSONObject routeObj = root.getJSONObject("route");
        if (routeObj == null) {
            return List.of();
        }
        JSONArray paths = routeObj.getJSONArray("paths");
        if (paths == null || paths.isEmpty()) {
            return List.of();
        }
        JSONObject path0 = paths.getJSONObject(0);
        JSONArray steps = path0.getJSONArray("steps");
        if (steps == null) {
            return List.of();
        }
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            JSONObject step = steps.getJSONObject(i);
            String poly = step.getStr("polyline");
            if (poly == null || poly.isBlank()) {
                continue;
            }
            for (String seg : poly.split(";")) {
                String[] ll = seg.split(",");
                if (ll.length >= 2) {
                    try {
                        out.add(new double[]{Double.parseDouble(ll[0].trim()), Double.parseDouble(ll[1].trim())});
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return dedupeConsecutive(out);
    }

    private int[] extractDistanceDuration(JSONObject root) {
        try {
            JSONObject routeObj = root.getJSONObject("route");
            if (routeObj == null) {
                return new int[]{-1, -1};
            }
            JSONArray paths = routeObj.getJSONArray("paths");
            if (paths == null || paths.isEmpty()) {
                return new int[]{-1, -1};
            }
            JSONObject path0 = paths.getJSONObject(0);
            int dist = parseIntSafe(path0.get("distance"));
            int dur = parseIntSafe(path0.get("duration"));
            return new int[]{dist, dur};
        } catch (Exception e) {
            return new int[]{-1, -1};
        }
    }

    private static int parseIntSafe(Object o) {
        if (o == null) {
            return -1;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        String s = o.toString();
        if (s.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean nearSame(double[] a, double[] b) {
        return Math.abs(a[0] - b[0]) < 1e-5 && Math.abs(a[1] - b[1]) < 1e-5;
    }

    /**
     * 多段锚点：逐段步行规划后拼接。
     */
    private List<double[]> chainWalkingSegments(List<double[]> anchors) {
        List<double[]> merged = new ArrayList<>();
        for (int i = 0; i < anchors.size() - 1; i++) {
            String o = formatLngLat(anchors.get(i));
            String d = formatLngLat(anchors.get(i + 1));
            JSONObject resp = requestWalking(o, d);
            if (resp == null || !"1".equals(resp.getStr("status"))) {
                continue;
            }
            List<double[]> seg = extractPolyline(resp);
            if (seg.isEmpty()) {
                continue;
            }
            if (merged.isEmpty()) {
                merged.addAll(seg);
            } else {
                double[] last = merged.get(merged.size() - 1);
                double[] first = seg.get(0);
                if (nearSame(last, first)) {
                    for (int k = 1; k < seg.size(); k++) {
                        merged.add(seg.get(k));
                    }
                } else {
                    merged.addAll(seg);
                }
            }
        }
        return merged;
    }

    private static List<double[]> dedupeConsecutive(List<double[]> pts) {
        if (pts.isEmpty()) {
            return pts;
        }
        List<double[]> out = new ArrayList<>();
        double[] prev = null;
        for (double[] p : pts) {
            if (prev != null && prev[0] == p[0] && prev[1] == p[1]) {
                continue;
            }
            out.add(p);
            prev = p;
        }
        return out;
    }

    private static List<List<Double>> toDtoPath(List<double[]> pts) {
        List<List<Double>> out = new ArrayList<>();
        for (double[] p : pts) {
            out.add(List.of(p[0], p[1]));
        }
        return out;
    }
}
