package com.gitee.dbquery.influxdbgui.sdk.util;

/**
 * @author chenpi
 * @since 2024/3/20
 **/
public class VersionUtils {
    public static int compareVersion(String version1, String version2) {

        String[] s1 = version1.split("\\.");
        String[] s2 = version2.split("\\.");
        int len1 = s1.length;
        int len2 = s2.length;

        for(int i = 0; i < len1 || i < len2; i ++) {
            int xx = 0, yy = 0;
            if(i < len1) {
                xx = Integer.parseInt(s1[i]);
            }
            if(i < len2) {
                yy = Integer.parseInt(s2[i]);
            }
            if(xx > yy) {
                return 1;
            }
            if(xx < yy) {
                return -1;
            }
        }
        return 0;

    }
}
