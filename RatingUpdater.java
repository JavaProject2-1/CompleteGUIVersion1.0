package basicWeb;

import java.io.*;
import java.util.*;

public class RatingUpdater {

    public static void applyRatings(List<DetailedSubject> subjectList, String resourceName) {
        Map<String, Double> ratingMap = new HashMap<>();

        try (InputStream input = RatingUpdater.class.getResourceAsStream(resourceName)) {
            if (input == null) {
                System.err.println("리소스 파일을 찾을 수 없습니다: " + resourceName);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                int lastSpace = line.lastIndexOf(" ");
                if (lastSpace == -1) continue;

                String keyPart = line.substring(0, lastSpace).trim();
                String[] parts = keyPart.split("\\s+");
                if (parts.length < 2) continue;

                String professor = parts[parts.length - 1];
                String subjectName = keyPart.substring(0, keyPart.length() - professor.length()).trim();

                double rating;
                try {
                    rating = Double.parseDouble(line.substring(lastSpace + 1).trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                String key = normalizeSubjectName(subjectName) + "|" + professor.trim();
                ratingMap.put(key, rating);
            }
        } catch (IOException e) {
            System.err.println("파일 읽기 실패: " + e.getMessage());
            return;
        }

        for (DetailedSubject ds : subjectList) {
            String key = normalizeSubjectName(ds.getName()) + "|" + ds.getProfessor().trim();
            if (ratingMap.containsKey(key)) {
                ds.setRating(ratingMap.get(key));
            }
        }
    }

    private static String normalizeSubjectName(String name) {
        return name.replaceAll("\\s+", "")         // 모든 공백 제거
                   .replaceAll("Ⅰ", "I")           // 로마 숫자 Ⅰ → I
                   .replaceAll("Ⅱ", "II")          // 로마 숫자 Ⅱ → II
                   .replaceAll("Ⅲ", "III")         // 로마 숫자 Ⅲ → III
                   .replaceAll("Ⅳ", "IV")          // 필요 시 추가
                   .replaceAll("1", "I")           // 숫자 1 → I (선택적)
                   .trim();
    }
}