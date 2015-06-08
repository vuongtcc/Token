package com.thetratruoc.vn.token;

import android.content.Context;
import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Vuong on 16/07/2013.
 */
public class Common {
    public static String postCtx(String endpoint, Map<String, String> params, Context ctx) {
        String msgResponse = "ERROR";
        HttpsURLConnection conn = null;
        int i = 1;
        while (i <= 3) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = ctx.getResources().openRawResource(R.raw.ssl);
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }

                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

                URL url = new URL(endpoint);
                StringBuilder bodyBuilder = new StringBuilder();
                Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
                // constructs the POST body using the parameters
                while (iterator.hasNext()) {
                    Map.Entry<String, String> param = iterator.next();
                    bodyBuilder.append(param.getKey()).append('=')
                            .append(param.getValue());
                    if (iterator.hasNext()) {
                        bodyBuilder.append('&');
                    }
                }
                String body = bodyBuilder.toString();
                byte[] bytes = body.getBytes();
                conn = (HttpsURLConnection) url.openConnection();
                conn.setSSLSocketFactory(context.getSocketFactory());
                conn.setConnectTimeout(120000);
                conn.setReadTimeout(120000);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                conn.setRequestProperty("Accept-Encoding", "identity");
                // post the request
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();
                // handle the response
                msgResponse = readStream(conn.getInputStream());
                i = 4;
            } catch (Exception e) {
                Log.e("Error Post to Server:", String.valueOf(i));
                i = i + 1;
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return msgResponse;
    }
//    public static String post(String endpoint, Map<String, String> params) {
//        String messageResponse = "ERROR";
//        try {
//            URL url = new URL(endpoint);
//            StringBuilder bodyBuilder = new StringBuilder();
//            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
//            // constructs the POST body using the parameters
//            while (iterator.hasNext()) {
//                Map.Entry<String, String> param = iterator.next();
//                bodyBuilder.append(param.getKey()).append('=')
//                        .append(param.getValue());
//                if (iterator.hasNext()) {
//                    bodyBuilder.append('&');
//                }
//            }
//            String body = bodyBuilder.toString();
//            byte[] bytes = body.getBytes();
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setUseCaches(true);
//            conn.setFixedLengthStreamingMode(bytes.length);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type",
//                    "application/x-www-form-urlencoded;charset=UTF-8");
//            conn.setRequestProperty("Accept-Encoding", "identity");
//            // post the request
//            OutputStream out = conn.getOutputStream();
//            out.write(bytes);
//            out.close();
//            // handle the response
//            messageResponse = readStream(conn.getInputStream());
//            conn.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return messageResponse;
//    }


    public static String readStream(InputStream in) {
        String str = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                str += line;
            }
            str = URLDecoder.decode(str, "UTF-8");
            Log.i("RETURN POST", str);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    in.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }

    public static String dateToString(java.util.Date date, String pattern, String defaultValue) {
        try {
            java.text.Format formater = new java.text.SimpleDateFormat(pattern);
            return formater.format(date);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String dateToString(java.util.Date date, java.text.Format formater, String defaultValue) {
        try {
            return formater.format(date);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static java.util.Date stringToDate(String sDate, String pattern) {
        return stringToDate(sDate, pattern, null);
    }

    public static java.util.Date stringToDate(String sDate, String pattern, java.util.Date defaultValue) {
        try {
            java.text.SimpleDateFormat converttodt = new java.text.SimpleDateFormat(pattern);
            return converttodt.parse(sDate);
        } catch (Exception e) {
            //e.printStackTrace();
            // /common.debug("?Loi ham common.getUtilDate(String sDate, String pattern):" + sDate);
            return defaultValue;
        }
    }

    //HmacSHA256 implementation
    public static String hmacSHA256(String data, String key) {
        String strHash = "";
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] doFinal = mac.doFinal(data.getBytes("UTF-8"));
            strHash = hex(doFinal);
        } catch (Exception e) {

        }
        return strHash;
    }

    public static String zenToken(String data, String key) {
        return hmacSHA256(data, key).substring(0, 8);
    }

    public static String hex(byte[] input) {
        StringBuffer sb = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            sb.append(HEX_TABLE[(input[i] >> 4) & 0xf]);
            sb.append(HEX_TABLE[input[i] & 0xf]);
        }
        return sb.toString();
    }

    static final char[] HEX_TABLE = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
