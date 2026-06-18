package com.twilight.campus.utils;

public class PageUtil {

    private PageUtil() {
    }

    public static int safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    public static int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    public static int offset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }
}
