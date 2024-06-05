package com.example.javametro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText stationInputEditText;
    private EditText lineInputEditText;
    private Button searchButton;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity); // 레이아웃 파일 이름 확인

        textView = findViewById(R.id.textView);
        stationInputEditText = findViewById(R.id.stationInputEditText);
        lineInputEditText = findViewById(R.id.lineInputEditText);
        searchButton = findViewById(R.id.searchButton);
        handler = new Handler(Looper.getMainLooper());

        // System.out을 TextViewPrintStream으로 대체
        PrintStream printStream = new TextViewPrintStream(System.out, textView);
        System.setOut(printStream);

        Main.setStnCode();

        // 검색 버튼 클릭 이벤트
        searchButton.setOnClickListener(v -> {
            String stationInput = stationInputEditText.getText().toString();
            String lineInput = lineInputEditText.getText().toString();

            // 종료 조건을 여기서 처리하지 않고, Main.main의 로직 내에서 처리하도록 하여 사용자가 "종료"를 입력할 때까지 노선과 역 이름 입력을 반복할 수 있도록 합니다.
            // 텍스트뷰를 초기화하는 부분을 버튼 클릭시 마다 실행하도록 수정
            handler.post(() -> textView.setText(""));

            // 새로운 스레드에서 Main.main 호출
            new Thread(() -> {
                try {
                    Main.main(stationInput, lineInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    public Handler getHandler() {
        return handler;
    }

    private class TextViewPrintStream extends PrintStream {
        private TextView textView;

        public TextViewPrintStream(OutputStream out, TextView textView) {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                    // do nothing
                }
            });
            this.textView = textView;
        }

        @Override
        public void println(String x) {
            runOnUiThread(() -> textView.append(x + "\n"));
        }
    }

    public static class Main {
        private static Map<String, String> stationCode = new HashMap<>(); // 키와 값 쌍을 저장할 맵 생성

        public static void setStnCode() {
            stationCode.put("1", "1001");
            stationCode.put("2", "1002");
            stationCode.put("3", "1003");
            stationCode.put("4", "1004");
            stationCode.put("5", "1005");
            stationCode.put("6", "1006");
            stationCode.put("7", "1007");
            stationCode.put("8", "1008");
            stationCode.put("9", "1009");
            stationCode.put("경의중앙선", "1063");
            stationCode.put("공항철도", "1065");
            stationCode.put("경춘선", "1067");
            stationCode.put("수인분당선", "1075");
            stationCode.put("신분당선", "1077");
            stationCode.put("우이신설선", "1092");
            stationCode.put("서해선", "1093");
            stationCode.put("경강선", "1081");
            stationCode.put("GTX-A", "1032");
        }

        public static boolean isInvalidStn(String stnName) {
            String[] Jinjeop = {"진접", "오남", "별내별가람"};
            String[] Sinlim = {"관악산", "서울대벤처타운", "서원", "신림", "당곡", "보라매병원", "보라매공원", "서울지방병무청"}; // 신림선 역 중 환승역(보라매, 대방) 제외
            String[] Uijeongbu = {"탑석", "송산", "어룡", "곤제", "효자", "경기도청북부청사", "새말", "동오", "의정부중앙", "흥선", "의정부시청", "경전철의정부", "범골", "발곡"}; // 의정부경전철 역 중 환승역(회룡) 제외
            boolean isInvalid = false;

            // 검증 로직을 작성할 부분

            return isInvalid;
        }

        public static String getStnCode(String Type, String Code) {
            // 키에 해당하는 값을 받아올건지, 값에 해당하는 키를 받아올건지 Type으로 입력받고,
            // 타입에서 코드에 해당하는 값/키를 반환.
            if (Type.equals("노선")) {
                for (Map.Entry<String, String> entry : stationCode.entrySet()) { // 호선명 : 호선 코드를 입력해놓은 역 코드 맵을 탐색
                    if (entry.getKey().equals(Code)) { // 찾고 있는 호선 이름과 일치하다면
                        return entry.getKey(); // 해당 키 반환
                    }
                }
            } else if (Type.equals("코드")) {
                for (Map.Entry<String, String> entry : stationCode.entrySet()) {
                    if (entry.getValue().equals(Code)) { // 찾고 있는 호선 코드와 일치하다면
                        return entry.getValue(); // 해당 키 반환
                    }
                }
            }
            return null;
        }

        public static void main(String stationInput, String lineInput) throws IOException {
            setStnCode(); // 해쉬맵에 호선명:호선 코드 키 쌍을 추가.

            if (stationInput.equals("종료")) {
                return;
            }

            String apiUrl = "http://swopenapi.seoul.go.kr/api/subway/APIKEY/json/realtimeStationArrival/0/16/" + stationInput;
            // -> 요청을 보낼 API


            metroTimestamp stnTimestamp = new metroTimestamp(stationInput, stationCode.get(lineInput)); // 찾고자 하는 역의 근처에 있는 열차들의 정보를 가진 Train 클래스의 모음인 metroTimestamp 객체 생성

                try {
                    getData.getMetroData(apiUrl, stnTimestamp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

