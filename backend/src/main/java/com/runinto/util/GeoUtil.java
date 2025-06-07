package com.runinto.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeoUtil {
    // 500m에 해당하는 위도/경도 근사치
    private static final double LATITUDE_STEP = 0.0045;
    private static final double LONGITUDE_STEP = 0.0057;

    /**
     * 좌표를 기반으로 그리드 ID를 생성합니다.
     * 예: "grid_37.5665_126.9780"
     */
    public static String getGridId(double latitude, double longitude) {
        long latIndex = (long) (latitude / LATITUDE_STEP);
        long lngIndex = (long) (longitude / LONGITUDE_STEP);
        return String.format("grid_%d_%d", latIndex, lngIndex);
    }

    /**
     * 주어진 사각형 바운더리에 포함되는 모든 그리드 ID 목록을 반환합니다.
     */
    public static List<String> getGridIdsForBoundingBox(double swLat, double swLng, double neLat, double neLng) {
        List<String> gridIds = new ArrayList<>();
        for (double lat = swLat; lat <= neLat; lat += LATITUDE_STEP) {
            for (double lng = swLng; lng <= neLng; lng += LONGITUDE_STEP) {
                gridIds.add(getGridId(lat, lng));
            }
        }
        // 중복 제거 후 반환
        return gridIds.stream().distinct().collect(Collectors.toList());
    }
}