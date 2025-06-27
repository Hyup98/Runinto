import { useEffect, useState } from "react";

const DEFAULT_LOCATION = {
    latitude: 37.5665,
    longitude: 126.9780, // 서울 시청
};

function useUserLocation() {
    const [location, setLocation] = useState(DEFAULT_LOCATION);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!navigator.geolocation) {
            console.warn("Geolocation is not supported");
            setLoading(false);
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                setLocation({ latitude, longitude });
                setLoading(false);
            },
            (error) => {
                console.warn("위치 권한 거부 또는 오류:", error.message);
                setLoading(false);
            },
            {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0,
            }
        );
    }, []);

    return { location, loading };
}

export default useUserLocation;
