package com.jiakao.ydt.util;

import cn.hutool.crypto.digest.DigestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 高德 Web 服务请求数字签名（与地理编码等接口一致）。
 */
public final class AmapSigUtil {

    private AmapSigUtil() {
    }

    /**
     * 向参数 Map 写入 sig（请勿在调用前自行放入 sig）。
     */
    public static void appendSig(Map<String, Object> params, String secret) {
        if (secret == null || secret.isBlank()) {
            return;
        }
        params.put("sig", sign(params, secret.trim()));
    }

    /**
     * MD5(按参数名升序拼接的 key=value&… + 私钥)，小写十六进制。
     */
    public static String sign(Map<String, Object> paramsWithoutSig, String secret) {
        List<String> names = new ArrayList<>(paramsWithoutSig.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if ("sig".equals(name)) {
                continue;
            }
            Object v = paramsWithoutSig.get(name);
            if (v == null) {
                continue;
            }
            String s = v.toString();
            if (s.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(name).append('=').append(s);
        }
        return DigestUtil.md5Hex(sb + secret).toLowerCase();
    }
}
