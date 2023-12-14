// 클릭시 색상변경 이벤트
$(document).ready(function () {
    $('.choice').on("click", function () {
        $('.choice').removeClass('clicked'); // 1. 모든 '.tag' 요소에서 'clicked' 클래스 제거
        $(this).addClass('clicked'); // 2. 현재 클릭된 요소에 'clicked' 클래스 추가
    });
});

// 현재 위치 좌표 찾기, 찾아서 html에 있는 구글맵 에  지도 중심, 마커 표시 좌표 설정
function getLocation() {
    if (navigator.geolocation) {
        // Geolocation을 지원하는 경우
        navigator.geolocation.getCurrentPosition(showPosition, showError);
    } else {
        alert("Geolocation이 지원되지 않습니다.");
    }
}

function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            // 성공 시 호출되는 콜백 함수
            function (position) {
                let latitude = position.coords.latitude;           //  위도 좌표
                let longitude = position.coords.longitude;         //  경도 좌표

                // Google Maps의 맵과 마커 업데이트
                updateMapAndMarker(latitude, longitude);
            },
            // 오류 시 호출되는 콜백 함수
            function (error) {
                console.error("Error getting location:", error);
            }
        );
    } else {
        //  실패시 호출
        console.error("Geolocation is not supported.");
    }
}

// Google Maps의 맵과 마커를 업데이트하는 함수
function updateMapAndMarker(latitude, longitude) {
    // 맵 객체 가져오기
    let map = document.getElementById("map");

    // 마커 객체 가져오기
    let marker = document.getElementById("userMarker");

    // 맵의 중심 설정
    map.setAttribute("center", latitude + "," + longitude);

    // 마커의 위치 설정
    marker.setAttribute("position", latitude + "," + longitude);
}

// 페이지 로드 시 위치 정보 가져오기 시도
getLocation();


// DB에서 가져온 데이터 슬라이드 로 보는 코드


// foodie-container 클릭시 지도 옆에 디테일 정보 보여주기