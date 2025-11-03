package com.danawa.webservice.service;

import com.danawa.webservice.domain.Part;
import com.danawa.webservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // API 키 주입 위해 추가
import org.springframework.data.domain.PageRequest; // DB 조회 위해 추가
import org.springframework.data.domain.Sort; // DB 조회 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.danawa.webservice.domain.PartSpec;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final PartRepository partRepository; // DB 접근 위해 PartRepository 주입

    @Value("${gemini.api.key}") // application.properties에서 API 키 가져오기
    private String apiKey;

    public String getAiResponse(String userQuery) {
        // 1. 사용자 쿼리 분석 (간단 버전: 카테고리만 추출 시도)
        String category = extractCategory(userQuery); // 예: "CPU", "그래픽카드" 등
        if (category == null) {
            return "어떤 종류의 부품을 찾으시는지 명확하지 않아요. (예: CPU 추천해줘)";
        }

        // 2. DB에서 관련 데이터 검색 (예: 해당 카테고리 가격 낮은 순 5개)
        List<Part> relevantParts = partRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("category"), category), // 카테고리 필터
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "price")) // 0페이지, 5개, 가격 오름차순
        ).getContent();

        if (relevantParts.isEmpty()) {
            return category + " 카테고리의 부품 정보를 찾을 수 없어요.";
        }

        // 3. 참고 자료(Context) 문자열 만들기
        String context = relevantParts.stream()
                .map(part -> String.format("제품명: %s, 가격: %d원, 스펙: %s",
                        part.getName(), part.getPrice(), buildSpecString(part))) // buildSpecString은 부품 스펙 요약 함수 (아래 예시)
                .collect(Collectors.joining("\n"));

        // 4. 프롬프트 구성
        String prompt = String.format(
                """
                # 페르소나
                너는 PC 부품 전문가 '컴박사'야. 사용자의 질문에 대해 아래 '참고 자료'만을 바탕으로 답변해야 해.
    
                # 지시사항
                1. 반드시 '참고 자료' 안의 정보만 사용해서 답변해. 없는 내용은 말하지 마.
                2. 사용자의 질문에 가장 적합한 부품을 추천하고, 그 이유를 가격과 스펙을 근거로 설명해줘.
                3. 답변은 "컴박사입니다! 🤖" 로 시작해줘.
    
                ---
                ## 참고 자료 ##
                %s
                ---
    
                # 사용자 질문
                %s
                """, context, userQuery
        );

        // 5. Gemini API 호출 (실제 SDK 사용법에 맞게 수정 필요)
        String aiResponse = callGeminiApi(prompt); // 아래 callGeminiApi 함수 예시 참고

        return aiResponse;
    }

    // 사용자 쿼리에서 카테고리 추출
    private String extractCategory(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("cpu")) return "CPU";
        if (lowerQuery.contains("그래픽카드") || lowerQuery.contains("vga") || lowerQuery.contains("gpu")) return "그래픽카드";
        if (lowerQuery.contains("메인보드") || lowerQuery.contains("보드")) return "메인보드";
        if (lowerQuery.contains("ram") || lowerQuery.contains("램") || lowerQuery.contains("메모리")) return "RAM";
        if (lowerQuery.contains("ssd")) return "SSD";
        if (lowerQuery.contains("hdd") || lowerQuery.contains("하드")) return "HDD";
        if (lowerQuery.contains("파워") || lowerQuery.contains("전원")) return "파워";
        if (lowerQuery.contains("케이스") || lowerQuery.contains("컴퓨터케이스")) return "케이스";
        if (lowerQuery.contains("쿨러") || lowerQuery.contains("냉각")) return "쿨러";
        return null;
    }

    // 부품 스펙 요약 문자열 만들기 (간단 예시)
    // 부품 스펙 요약 문자열 만들기 (JSON 파싱 방식으로 수정)
    private String buildSpecString(Part part) {
        // 1. PartSpec 엔티티를 가져옵니다.
        PartSpec partSpec = part.getPartSpec();
        if (partSpec == null || partSpec.getSpecs() == null) {
            return "상세 스펙 정보 없음";
        }

        try {
            // 2. specs 컬럼의 JSON 문자열을 파싱합니다.
            JSONObject specs = new JSONObject(partSpec.getSpecs());

            // 3. 카테고리별로 JSON에서 스펙을 꺼내 씁니다.
            if ("CPU".equals(part.getCategory())) {
                return String.format("%s / %s / %s",
                        specs.optString("cores", ""), // optString은 키가 없어도 오류 대신 빈 문자열 반환
                        specs.optString("threads", ""),
                        specs.optString("socket", ""));
            }
            if ("그래픽카드".equals(part.getCategory())) {
                String chipset = specs.optString("nvidia_chipset", specs.optString("amd_chipset"));
                return String.format("%s / %s",
                        chipset,
                        specs.optString("gpu_memory_capacity", ""));
            }
            if ("RAM".equals(part.getCategory())) {
                return String.format("%s / %s / %s",
                        specs.optString("capacity", ""),
                        specs.optString("clock_speed", ""),
                        specs.optString("product_class", ""));
            }
            // ... (필요한 다른 카테고리들도 위와 같은 방식으로 추가) ...

        } catch (Exception e) {
            // JSON 파싱 중 오류 발생 시
            return "스펙 처리 중 오류";
        }
        
        return "상세 스펙 확인 필요";
    }

    // Gemini API 호출 함수 (REST API 사용)
    private String callGeminiApi(String prompt) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            // Gemini API 요청 본문 구성
            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            content.add("parts", new com.google.gson.JsonArray());
            content.getAsJsonArray("parts").add(part);
            requestBody.add("contents", new com.google.gson.JsonArray());
            requestBody.getAsJsonArray("contents").add(content);
            
            String requestBodyJson = new Gson().toJson(requestBody);
            
            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            // 요청 전송 및 응답 처리
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
                if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject contentObj = candidate.getAsJsonObject("content");
                        if (contentObj.has("parts") && contentObj.getAsJsonArray("parts").size() > 0) {
                            JsonObject partObj = contentObj.getAsJsonArray("parts").get(0).getAsJsonObject();
                            if (partObj.has("text")) {
                                String result = partObj.get("text").getAsString();
                                return result != null && !result.isEmpty() ? result : "응답을 생성할 수 없습니다.";
                            }
                        }
                    }
                }
                return "응답을 생성할 수 없습니다.";
            } else {
                return "AI 응답 생성 중 오류가 발생했습니다: HTTP " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    // 전체 PC 견적 추천 기능
    public String getFullPcEstimate(String budget, String purpose) {
        // 주요 부품 카테고리 목록
        String[] mainCategories = {"CPU", "메인보드", "RAM", "그래픽카드", "SSD", "파워", "케이스"};
        
        StringBuilder context = new StringBuilder();
        int totalPrice = 0;
        
        // 각 카테고리에 대해 인기 상품 3개씩 조회
        for (String category : mainCategories) {
            List<Part> parts = partRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("category"), category),
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "reviewCount")) // 인기순
            ).getContent();
            
            if (!parts.isEmpty()) {
                context.append("\n[").append(category).append("]\n");
                for (Part part : parts) {
                    context.append(String.format("- %s: %d원 (리뷰: %d개, 별점: %.1f)\n",
                        part.getName(), part.getPrice(),
                        part.getReviewCount() != null ? part.getReviewCount() : 0,
                        part.getStarRating() != null ? part.getStarRating() : 0.0f));
                }
                totalPrice += parts.get(0).getPrice(); // 첫 번째 상품 가격 합산
            }
        }
        
        String prompt = String.format(
            """
            # 페르소나
            너는 PC 부품 전문가 '컴박사'야. 사용자의 예산과 용도에 맞는 완전한 PC 견적을 추천해야 해.
            
            # 지시사항
            1. 아래 '부품 정보'를 참고하여 견적을 작성해줘.
            2. 사용자 예산: %s원, 용도: %s
            3. 각 부품 카테고리별로 1개씩 선택해서 견적을 구성해줘.
            4. 총 예산 내에서 최적의 조합을 추천해줘.
            5. 답변은 다음 형식으로 작성:
               - CPU: [제품명] ([가격]원)
               - 메인보드: [제품명] ([가격]원)
               - RAM: [제품명] ([가격]원)
               - 그래픽카드: [제품명] ([가격]원)
               - SSD: [제품명] ([가격]원)
               - 파워: [제품명] ([가격]원)
               - 케이스: [제품명] ([가격]원)
               - 총 예상 가격: [합계]원
            6. 각 부품 선택 이유를 간단히 설명해줘.
            7. 답변은 "컴박사입니다! 🤖"로 시작해줘.
            
            ---
            ## 부품 정보 ##
            %s
            ---
            """, 
            budget.isEmpty() ? "지정 안함" : budget,
            purpose.isEmpty() ? "일반용" : purpose,
            context.toString()
        );
        
        return callGeminiApi(prompt);
    }
    
    // --- [추가] Gemini Test 형식의 견적 추천 메서드 ---
    public Map<String, Object> getPcEstimateGeminiStyle(String mode, int budget, String cpuBrand, String gpuBrand, String storage, String monitor) {
        // DB에서 부품 정보 조회
        String[] mainCategories = {"CPU", "메인보드", "RAM", "그래픽카드", "SSD", "파워", "케이스"};
        StringBuilder context = new StringBuilder();
        
        for (String category : mainCategories) {
            List<Part> parts = partRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("category"), category),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "reviewCount"))
            ).getContent();
            
            if (!parts.isEmpty()) {
                context.append("\n[").append(category).append("]\n");
                for (Part part : parts) {
                    int priceInManwon = part.getPrice() / 10000; // 원을 만원 단위로 변환
                    context.append(String.format("- %s: %d만원\n", part.getName(), priceInManwon));
                }
            }
        }
        
        // Gemini API에 JSON 형식으로 응답 요청
        String prompt = String.format(
            """
            역할: 당신은 예산 내에서 PC 부품을 추천하는 전문가입니다.
            한국어로 답하고, 반드시 **순수 JSON만** 반환하세요. 마크다운/코드블록 금지.
            
            요구사항:
            - 총합이 예산(%d만원)을 넘지 않게
            - 용도: %s
            - CPU 선호: %s
            - GPU 선호: %s
            - 저장장치: %s
            - 모니터 포함: %s
            - 참고용 부품 정보: %s
            
            반드시 아래 스키마로만 응답:
            {
              "items":[{"cat":"CPU","name":"예: Ryzen 5 7600","price":22}],
              "total":0,
              "reasoning":"선정 이유를 간단히"
            }
            가격 단위는 '만원'.
            """, budget, mode, cpuBrand, gpuBrand, storage, monitor, context.toString()
        );
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            content.add("parts", new com.google.gson.JsonArray());
            content.getAsJsonArray("parts").add(part);
            requestBody.add("contents", new com.google.gson.JsonArray());
            requestBody.getAsJsonArray("contents").add(content);
            
            String requestBodyJson = new Gson().toJson(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
                if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject contentObj = candidate.getAsJsonObject("content");
                        if (contentObj.has("parts") && contentObj.getAsJsonArray("parts").size() > 0) {
                            JsonObject partObj = contentObj.getAsJsonArray("parts").get(0).getAsJsonObject();
                            if (partObj.has("text")) {
                                String resultText = partObj.get("text").getAsString().trim();
                                // JSON 코드블록 제거
                                if (resultText.startsWith("```")) {
                                    resultText = resultText.replaceAll("^```json\\s*|\\s*```$", "");
                                }
                                
                                JsonObject resultJson = new Gson().fromJson(resultText, JsonObject.class);
                                
                                // Map으로 변환
                                Map<String, Object> result = new java.util.HashMap<>();
                                result.put("summary", Map.of(
                                    "mode", mode,
                                    "budget", budget,
                                    "cpuBrand", cpuBrand,
                                    "gpuBrand", gpuBrand,
                                    "storage", storage,
                                    "monitor", monitor
                                ));
                                
                                if (resultJson.has("items")) {
                                    result.put("items", new Gson().fromJson(resultJson.get("items"), java.util.List.class));
                                }
                                
                                if (resultJson.has("total")) {
                                    result.put("total", resultJson.get("total").getAsDouble());
                                } else {
                                    // items의 가격 합계 계산
                                    double total = 0;
                                    if (resultJson.has("items")) {
                                        for (Object item : new Gson().fromJson(resultJson.get("items"), java.util.List.class)) {
                                            JsonObject itemObj = new Gson().toJsonTree(item).getAsJsonObject();
                                            if (itemObj.has("price")) {
                                                total += itemObj.get("price").getAsDouble();
                                            }
                                        }
                                    }
                                    result.put("total", total);
                                }
                                
                                if (resultJson.has("reasoning")) {
                                    result.put("reasoning", resultJson.get("reasoning").getAsString());
                                }
                                
                                result.put("note", "가격은 만원 단위 예시이며 실제 시세와 다를 수 있습니다.");
                                
                                return result;
                            }
                        }
                    }
                }
            }
            
            // 실패 시 빈 결과 반환
            Map<String, Object> fallback = new java.util.HashMap<>();
            fallback.put("summary", Map.of(
                "mode", mode,
                "budget", budget,
                "cpuBrand", cpuBrand,
                "gpuBrand", gpuBrand,
                "storage", storage,
                "monitor", monitor
            ));
            fallback.put("items", new java.util.ArrayList<>());
            fallback.put("total", 0);
            fallback.put("reasoning", "AI 응답 생성에 실패했습니다.");
            fallback.put("note", "가격은 만원 단위 예시이며 실제 시세와 다를 수 있습니다.");
            return fallback;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("summary", Map.of(
                "mode", mode,
                "budget", budget,
                "cpuBrand", cpuBrand,
                "gpuBrand", gpuBrand,
                "storage", storage,
                "monitor", monitor
            ));
            errorResult.put("items", new java.util.ArrayList<>());
            errorResult.put("total", 0);
            errorResult.put("reasoning", "오류 발생: " + e.getMessage());
            errorResult.put("note", "가격은 만원 단위 예시이며 실제 시세와 다를 수 있습니다.");
            return errorResult;
        }
    }
}