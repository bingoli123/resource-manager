package com.inspur.rms.utils;


import com.inspur.rms.rmspojo.DTO.AuthorizationDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * API网关验证工具类
 *
 * @author : gaomeirong
 * @date : 2019年4月1日 上午10:45:02
 * @Copyright : 2019 www.inspur.com Inc. All rights reserved.
 */
public class AuthorizationUtils {

    private static final Log log = LogFactory.getLog(AuthorizationUtils.class);

    /**
     * 将Authorization解析为具体的对象
     *
     * @param authorization 授权认证信息
     * @return AuthorizationDTO
     * @throws
     * @author : gaomeirong
     * @date : 2019年4月1日 上午11:41:34
     */
    public static AuthorizationDTO parseAuthorization(String authorization) {
        AuthorizationDTO authDTO = new AuthorizationDTO();
        String[] authParts = authorization.split(" ");
        if (authParts == null || authParts.length != 2) {
            authDTO.setParseSuccess(false);
            return authDTO;
        }
        authDTO.setProjectTag(authParts[0]);
        String[] authBodyParts = authParts[1].split(":");
        if (authBodyParts == null || authBodyParts.length != 2) {
            authDTO.setParseSuccess(false);
            return authDTO;
        }
        authDTO.setAccountName(authBodyParts[0]);
        authDTO.setSignature(authBodyParts[1]);
        authDTO.setParseSuccess(true);
        return authDTO;
    }

    /**
     * 验证授权认证信息的签名
     * <p>
     * Signature = Base64(HMAC-SHA1(SecretKey, UTF-8-Encoding-Of(StringToSign)))
     * SecretKey = MD5(SaltHash1)
     * StringToSign = VERB + "\n" + Content-MD5 + "\n" +Content-Type + "\n" + Date + "\n"
     *
     * @param signature 签名字符串
     * @return 验证成功或者失败
     * @throws
     * @author : gaomeirong
     * @date : 2019年4月1日 上午11:44:07
     */
//    public static boolean verifySignature(String signature, String salth1, String sign) {
//        String secretKey = DigestUtils.md5Hex(salth1);
//        String hmac = HMACSHA1utils.hmac(secretKey, sign);
//        /*
//        if (log.isInfoEnabled()) {
//            log.info("salth1=" + salth1);
//            log.info("secretKey=" + secretKey);
//            log.info("hmac=" + hmac);
//        }
//        */
//        String signNew = Base64.encodeBase64String(hmac.getBytes(StandardCharsets.UTF_8));
//        if (signNew.equalsIgnoreCase(signature)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * 根据url验证是否有相应的功能权限
     * @author : gaomeirong
     * @date : 2019年4月23日 下午8:17:40     
     * @param perms
     * @return
     * @throws
     */
//    public static boolean verifyPerm(String url, String[] accountPerms) {
//        String perm = getNeedPerm(url);
//        if (perm == null) {
//            return false;
//        } else {
//            for (int i = 0; i < accountPerms.length; i++) {
//                if (perm.equalsIgnoreCase(accountPerms[i])) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    /*
     * private static String getNeedPerm(String url) { url =
     * url.replace(ConfigConst.VERSION + "/", ""); Map<String, String> map =
     * UrlPermConfig.SYS_PERM_MAP; if (map.containsKey(url)) { return (String)
     * map.get(url); } else { Set<String> keySet = new HashSet<>(); for (String key
     * : map.keySet()) {
     *
     * String regex = key + "*"; Matcher m1 = Pattern.compile(regex).matcher(url);
     * boolean isMatch = m1.find(); if (isMatch) { keySet.add(key); } } if
     * (keySet.size() == 0) { return null; } if (keySet.size() == 1) { return
     * keySet.iterator().next(); } float max = 0; String maxKey = null;
     * Iterator<String> it = keySet.iterator(); while (it.hasNext()) { String str =
     * it.next(); float tmp = StringCompareUtils.getSimilarityRatio(url, str); if
     * (tmp > max) { max = tmp; maxKey = str; } }
     *
     * if (maxKey != null) { return (String) map.get(maxKey); } else { return null;
     * } } }
     */

    /**
     * 解析ws鉴权部分
     *
     * @param authorization
     * @return
     * @throws
     * @author : gaomeirong
     * @date : 2019年4月30日 下午3:54:30
     */
//    public static WSAuthorizationDTO parseWSAuthorization(String authorization) {
//        WSAuthorizationDTO authDTO = new WSAuthorizationDTO();
//        String[] authParts = authorization.split("[?]");
//        if (authParts == null || authParts.length != 2) {
//            authDTO.setParseSuccess(false);
//            return authDTO;
//        }
//        authDTO.setProjectTag(authParts[0]);
//        String[] params = authParts[1].split("&");
//        Map<String, String> paramMap = new HashMap<>();
//        for (String param : params) {
//            String[] keyValue = param.split("=");
//            if (keyValue.length != 2) {
//                continue;
//            }
//            String key = keyValue[0];
//            String value = keyValue[1];
//            paramMap.put(key, value);
//        }
//        if (paramMap.containsKey("VSPAccountName")) {
//            authDTO.setAccountName(paramMap.get("VSPAccountName"));
//        } else {
//            authDTO.setParseSuccess(false);
//            return authDTO;
//        }
//        if (paramMap.containsKey("VSPSession")) {
//            authDTO.setSession(paramMap.get("VSPSession"));
//        } else {
//            authDTO.setParseSuccess(false);
//            return authDTO;
//        }
//        if (paramMap.containsKey("VSPExpires")) {
//            authDTO.setExpires(Long.parseLong(paramMap.get("VSPExpires")));
//        } else {
//            authDTO.setParseSuccess(false);
//            return authDTO;
//        }
//        if (paramMap.containsKey("VSPSignature")) {
//            authDTO.setSignature(paramMap.get("VSPSignature"));
//        } else {
//            authDTO.setParseSuccess(false);
//            return authDTO;
//        }
//        authDTO.setParseSuccess(true);
//        return authDTO;
//    }

}
