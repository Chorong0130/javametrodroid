package com.example.javametro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class getData {
    private static final OkHttpClient client = new OkHttpClient();
    private static JsonArray trainArray = new JsonArray();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void getMetroData(String apiUrl, metroTimestamp Timestamps) throws IOException {
        Request request = new Request.Builder() //요청을 보낼 객체 생성
                .url(apiUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("예상하지 못한 오류 발생 :  " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("열차 도착 정보가 비어있어 정보를 제공할 수 없습니다.");
            }

            String jsonData = responseBody.string();
            JsonElement JSElement = JsonParser.parseString(jsonData);
            JsonObject metroobject = JSElement.getAsJsonObject();

            //응답 상태 반환 코드 확인
            JsonObject statusObject = metroobject.getAsJsonObject("errorMessage");
            String statusCode = statusObject.get("code").getAsString();

            JsonArray UPList = new JsonArray();
            JsonArray DOWNList = new JsonArray();


            if(statusCode.equals("INFO-000")){
                //System.out.println("디버깅 >>> 요청 수신에 성공하였습니다.");
                trainArray = metroobject.getAsJsonArray("realtimeArrivalList");

                for(JsonElement tA : trainArray){
                    String Line = tA.getAsJsonObject().get("subwayId").getAsString();
                    if(Timestamps.getStnLine().equals(Line)){
                        String UPDOWN = tA.getAsJsonObject().get("updnLine").getAsString();
                        if(UPDOWN.equals("상행") || UPDOWN.equals("내선")){
                            if(tA.getAsJsonObject().get("bstatnNm").getAsString().equals(tA.getAsJsonObject().get("arvlMsg3").getAsString())){
                                //이 역에 오기 전에 종착하는 열차라면 열차 리스트에 추가하지 않고 그냥 넘어감
                            } else {
                                UPList.add(tA);
                            }
                        } else if(UPDOWN.equals("하행") || UPDOWN.equals("외선")) {
                            if(tA.getAsJsonObject().get("bstatnNm").getAsString().equals(tA.getAsJsonObject().get("arvlMsg3").getAsString())){
                                //이 역에 오기 전에 종착하는 열차라면 열차 리스트에 추가하지 않고 그냥 넘어감
                            } else {
                                DOWNList.add(tA);
                            }
                        }
                    }
                }

                String stnName = Timestamps.getStnName();

                //상행열차 정보 저장
                for(JsonElement up : UPList){
                    String trainID = up.getAsJsonObject().get("btrainNo").getAsString(); //열차번호
                    String destination = up.getAsJsonObject().get("bstatnNm").getAsString(); //행선지
                    String nowLocation = up.getAsJsonObject().get("arvlMsg3").getAsString(); // 현재 위치
                    String arrivalTime = up.getAsJsonObject().get("arvlMsg2").getAsString(); // 도착 예정 시간
                    String Direction = up.getAsJsonObject().get("updnLine").getAsString(); // 상행값 가져오기
                    Timestamps.addTimestamp_UP(trainID, destination, nowLocation, arrivalTime, stnName, Direction);
                }

                //하행열차 정보 저장
                for(JsonElement down : DOWNList){
                    String trainID = down.getAsJsonObject().get("btrainNo").getAsString(); //열차번호
                    String destination = down.getAsJsonObject().get("bstatnNm").getAsString(); //행선지
                    String nowLocation = down.getAsJsonObject().get("arvlMsg3").getAsString(); // 현재 위치
                    String arrivalTime = down.getAsJsonObject().get("arvlMsg2").getAsString(); // 도착 예정 시간
                    String Direction = down.getAsJsonObject().get("updnLine").getAsString();
                    Timestamps.addTimestamp_DOWN(trainID, destination, nowLocation, arrivalTime, stnName, Direction);
                }

                //저장한 상행열차 정보를 출력
                System.out.println("********" + stnName + "역 상행열차 정보를 출력합니다.********");
                Timestamps.showUPStamp();


                //저장한 하행열차 정보를 출력
                System.out.println("********" + stnName + "역 하행열차 정보를 출력합니다.********");
                Timestamps.showDOWNStamp();

            } else {
                System.out.println("디버깅 >>> 요청 수신에 실패하였습니다.");
            }
        } catch (IOException e) {
            System.err.println("API를 요청하는 동안 오류가 발생했습니다. >>> " + e.getMessage());
            throw e;
        }
    }
}
