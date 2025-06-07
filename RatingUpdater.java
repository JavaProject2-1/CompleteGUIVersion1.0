package basicWeb;

import java.io.*;
import java.util.*;

public class RatingUpdater {

    public static void applyRatings(List<DetailedSubject> subjectList, String resourceName) {
        Map<String, Double> ratingMap = new HashMap<>();

        // 1. 클래스패스 내 리소스 파일 읽기
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

                String keyPart = line.substring(0, lastSpace).trim(); // 과목명 + 교수명
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

                String key = subjectName + "|" + professor;
                ratingMap.put(key, rating);
            }
        } catch (IOException e) {
            System.err.println("파일 읽기 실패: " + e.getMessage());
            return;
        }

        // 2. subjectList의 각 항목에 대해 rating 설정
        for (DetailedSubject ds : subjectList) {
            String key = ds.getName().trim() + "|" + ds.getProfessor().trim();
            if (ratingMap.containsKey(key)) {
                ds.setRating(ratingMap.get(key));
            }
        }
    }
}
