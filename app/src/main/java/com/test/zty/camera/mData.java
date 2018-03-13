package com.test.zty.camera;

/**
 * Created by zhangty1996 on 2017/5/19 0019.
 * E-Mail:zhangty1996@163.com
 */

public class mData {
    private static String IMEI;
    private static String ac_code;
    private static boolean b_status = false;
    private static String v_status = "";
    private static String warning;

    public static String getWarning() {
        return warning;
    }

    public static void setWarning(String warning) {
        mData.warning = warning;
    }

    public static String getIMEI() {
        return IMEI;
    }

    public static void setIMEI(String IMEI) {
        mData.IMEI = IMEI;
    }

    public static boolean isB_status() {
        return b_status;
    }

    public static void setB_status(boolean b_status) {
        mData.b_status = b_status;
    }

    public static String getV_status() {
        return v_status;
    }

    public static void setV_status(String v_status) {
        mData.v_status = v_status;
    }

    public static String getAc_code() {
        return ac_code;
    }

    public static void setAc_code(String ac_code) {
        mData.ac_code = ac_code;
    }

    public static boolean checkStatus() {
        return true;
    }
}
