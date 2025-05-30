const WebSocket = require('ws');
const { encode: encodeMessagePack, decode: decodeMessagePack } = require('@msgpack/msgpack');

// 생성된 Protobuf JS 파일 가져오기 및 ChatMessage 클래스 할당
// './generated_js/chat_message_pb.js'는 chat_message_pb.js 파일의 실제 상대 경로입니다.
const runintoChatProto = require('./generated_js/chat_message_pb.js');
const ChatMessage = runintoChatProto.ChatMessage; // 내보내진 객체에서 ChatMessage 클래스를 직접 가져옵니다.


// --- 테스트 설정 ---
const SERVER_URL = 'ws://localhost:8080/ws/chat'; // 실제 실행 중인 WebSocket 서버 주소로 변경하세요.
const TOTAL_MESSAGES = 100000; // 보낼 총 메시지 수 (테스트를 위해 1000 정도로 줄여서 시작해도 좋습니다)
const PAYLOAD_FORMAT = process.argv[2] || 'MessagePack'; // 실행 시 인자로 포맷 지정 (MessagePack 또는 Protobuf), 기본값 MessagePack
const REPORT_INTERVAL = 1000; // 몇 메시지마다 중간 보고를 할지
// --- 테스트 설정 끝 ---

let ws;
let sentMessages = 0;
let receivedMessages = 0;
// let roundTripTimes = []; // 개별 RTT 측정은 이 예제에서 제외 (복잡성 증가)
let clientSerializationTimes = [];
let clientDeserializationTimes = [];
let totalSerializedBytes = 0;
let testStartTime;

function generateMessagePayload(index) {
    // 서버에서 기대하는 메시지 구조와 동일하게 작성
    // .proto 파일의 ChatMessage 필드에 맞춰서 데이터를 생성합니다.
    return {
        chatRoomId: 1, // int64 타입
        senderId: 100, // int64 타입
        message: `안녕 반가워 나는 동협이라고해 이건 테스트용 메시지고 난 지금 무슨 형식의 메시지 프로토콜을 사용해서 메시지를 보낼지 고민 중 이야!!` // string 타입
        // 필요시 .proto에 정의된 다른 필드 추가
    };
}

function encodePayload(payload) {
    const serStartTime = performance.now();
    let encoded;
    if (PAYLOAD_FORMAT === 'MessagePack') {
        encoded = encodeMessagePack(payload);
    } else if (PAYLOAD_FORMAT === 'Protobuf') {
        const chatMessage = new ChatMessage();
        chatMessage.setChatRoomId(payload.chatRoomId); // int64 필드 설정
        chatMessage.setSenderId(payload.senderId);     // int64 필드 설정
        chatMessage.setMessage(payload.message);       // string 필드 설정
        // .proto에 정의한 다른 필드가 있다면 여기서 set메서드로 추가

        encoded = chatMessage.serializeBinary(); // Uint8Array로 직렬화
    } else {
        throw new Error('지원하지 않는 포맷입니다: ' + PAYLOAD_FORMAT);
    }
    clientSerializationTimes.push(performance.now() - serStartTime);
    if (encoded) {
        totalSerializedBytes += encoded.byteLength;
    }
    return encoded;
}

function decodePayload(data) {
    const deserStartTime = performance.now();
    let decodedPayloadObject; // 일반 JavaScript 객체 형태로 변환된 페이로드를 저장할 변수
    if (PAYLOAD_FORMAT === 'MessagePack') {
        decodedPayloadObject = decodeMessagePack(new Uint8Array(data)); // data가 ArrayBuffer라고 가정
    } else if (PAYLOAD_FORMAT === 'Protobuf') {
        const byteArray = (data instanceof ArrayBuffer) ? new Uint8Array(data) : data;
        const chatMessageInstance = ChatMessage.deserializeBinary(byteArray); // 역직렬화하여 ChatMessage 인스턴스 얻기

        // Protobuf 인스턴스에서 필드 값을 가져와 일반 JavaScript 객체로 만듭니다.
        decodedPayloadObject = {
            chatRoomId: chatMessageInstance.getChatRoomId(),
            senderId: chatMessageInstance.getSenderId(),
            message: chatMessageInstance.getMessage()
            // .proto에 정의한 다른 필드가 있다면 get메서드로 추가
        };
    } else {
        throw new Error('지원하지 않는 포맷입니다: ' + PAYLOAD_FORMAT);
    }
    clientDeserializationTimes.push(performance.now() - deserStartTime);
    return decodedPayloadObject;
}

function connect() {
    console.log(`[${PAYLOAD_FORMAT}] 서버에 연결 시도 중: ${SERVER_URL}`);
    ws = new WebSocket(SERVER_URL);
    ws.binaryType = 'arraybuffer'; // 바이너리 메시지를 ArrayBuffer로 받음

    ws.onopen = async () => {
        console.log(`[${PAYLOAD_FORMAT}] WebSocket 연결 성공. 메시지 전송 시작...`);
        testStartTime = performance.now();
        sendMessages();
    };

    ws.onmessage = (event) => {
        // const messageReceiveTime = performance.now(); // 개별 메시지 수신 시간 (RTT 측정 시 사용)

        if (event.data instanceof ArrayBuffer) {
            const decoded = decodePayload(event.data); // 역직렬화 시간 측정 포함
            // console.log("수신 메시지 (decoded):", decoded); // 수신 메시지 내용 확인 (디버깅용)
            receivedMessages++;

            if (receivedMessages % REPORT_INTERVAL === 0) {
                console.log(`[${PAYLOAD_FORMAT}] 메시지 수신: ${receivedMessages} / ${TOTAL_MESSAGES}`);
            }

            if (receivedMessages === TOTAL_MESSAGES) {
                const testEndTime = performance.now();
                const totalTestTimeMs = testEndTime - testStartTime;
                printResults(totalTestTimeMs);
                ws.close();
            }
        } else {
            console.warn("알 수 없는 타입의 메시지 수신:", event.data);
        }
    };

    ws.onerror = (error) => {
        console.error(`[${PAYLOAD_FORMAT}] WebSocket 오류:`, error.message);
        // 테스트가 완전히 끝나지 않은 상태에서 오류 발생 시 부분 결과 출력
        if (testStartTime && receivedMessages < TOTAL_MESSAGES) {
            printResults(performance.now() - testStartTime, true);
        }
    };

    ws.onclose = (event) => {
        console.log(`[${PAYLOAD_FORMAT}] WebSocket 연결 종료. 코드: ${event.code}, 이유: ${event.reason}`);
        if (receivedMessages < TOTAL_MESSAGES && testStartTime) { // testStartTime이 정의되었는지 확인
            console.error(`[${PAYLOAD_FORMAT}] 모든 메시지를 수신하기 전에 연결이 종료되었습니다. (수신: ${receivedMessages}/${TOTAL_MESSAGES})`);
            printResults(performance.now() - testStartTime, true); // 부분 결과 출력
        }
    };
}

function sendMessages() {
    for (let i = 0; i < TOTAL_MESSAGES; i++) {
        if (ws.readyState === WebSocket.OPEN) {
            const payload = generateMessagePayload(i + 1);
            const encoded = encodePayload(payload); // 직렬화 시간 측정 포함
            ws.send(encoded);
            sentMessages++;
        } else {
            console.warn(`[${PAYLOAD_FORMAT}] WebSocket이 열려있지 않아 메시지 ${i + 1} 전송 실패.`);
            break;
        }
    }
    if (sentMessages > 0) {
        console.log(`[${PAYLOAD_FORMAT}] 총 ${sentMessages}개의 메시지 전송 요청 완료.`);
    }
}

function printResults(totalTestTimeMs, partial = false) {
    console.log("\n--- 테스트 결과 ---");
    console.log(`포맷: ${PAYLOAD_FORMAT}`);
    if (partial) {
        console.log("경고: 테스트가 완전히 완료되지 않았습니다. 아래는 부분 결과입니다.");
    }
    console.log(`총 메시지 수: ${TOTAL_MESSAGES} (전송 시도: ${sentMessages}, 수신 완료: ${receivedMessages})`);
    console.log(`총 소요 시간: ${totalTestTimeMs.toFixed(2)} ms`);

    if (receivedMessages > 0) {
        const throughput = (receivedMessages / (totalTestTimeMs / 1000)).toFixed(2);
        console.log(`처리량 (Throughput): ${throughput} msg/sec`);
    }

    if (clientSerializationTimes.length > 0) {
        const avgSerTime = (clientSerializationTimes.reduce((a, b) => a + b, 0) / clientSerializationTimes.length).toFixed(4);
        console.log(`평균 클라이언트 직렬화 시간: ${avgSerTime} ms`);
    }
    if (clientDeserializationTimes.length > 0) {
        const avgDeserTime = (clientDeserializationTimes.reduce((a, b) => a + b, 0) / clientDeserializationTimes.length).toFixed(4);
        console.log(`평균 클라이언트 역직렬화 시간: ${avgDeserTime} ms`);
    }
    if (sentMessages > 0 && totalSerializedBytes > 0) { // sentMessages로 나눠야 정확한 메시지당 크기
        const avgMsgSize = (totalSerializedBytes / sentMessages).toFixed(2);
        console.log(`평균 직렬화 메시지 크기: ${avgMsgSize} bytes`);
    }
    console.log("--- 테스트 종료 ---\n");
}

// 테스트 시작
if (!PAYLOAD_FORMAT || (PAYLOAD_FORMAT !== 'MessagePack' && PAYLOAD_FORMAT !== 'Protobuf')) {
    console.error("사용법: node benchmarkClient.js [MessagePack | Protobuf]");
    process.exit(1);
}
connect();