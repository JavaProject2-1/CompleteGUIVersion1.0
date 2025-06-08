package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

/**
 * CrawlerExample 방식을 사용한 상세 과목 크롤러
 */
public class DetailedSubjectCrawler {

    /**
     * 셀 내용을 텍스트로 추출
     */
    public static String getCellText(WebElement cell) {
        try {
            List<WebElement> nobrList = cell.findElements(By.tagName("nobr"));
            if (!nobrList.isEmpty()) {
                String innerHtml = nobrList.get(0).getDomProperty("innerHTML");
                String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
                return textWithNewlines.replaceAll("<[^>]+>", "").trim();
            }
            return cell.getText().trim();
        } catch (NoSuchElementException e) {
            return cell.getText().trim();
        }
    }

    /**
     * 강의 고유 키 생성 (중복 제거용)
     */
    private static String getCourseKey(List<WebElement> cells) {
        String code = getCellText(cells.get(7));
        String name = getCellText(cells.get(8));
        String professor = getCellText(cells.get(12));
        String time = getCellText(cells.get(13));
        return code + "|" + name + "|" + professor + "|" + time;
    }

    /**
     * 크롬 드라이버 설정 (운영체제별 자동 감지)
     */
    private static void setupChromeDriver() {
        String os = System.getProperty("os.name").toLowerCase();
        String driverPath;

        // 운영체제별 드라이버 경로 설정
        if (os.contains("win")) driverPath = "chromedriver.exe";
        else if (os.contains("mac")) driverPath = "chromedriver_mac";
        else if (os.contains("nux") || os.contains("nix")) driverPath = "chromedriver_linux";
        else throw new RuntimeException("지원하지 않는 운영체제입니다: " + os);

        // 절대 경로로 변환
        String absolutePath = Paths.get(driverPath).toAbsolutePath().toString();
        File driverFile = new File(absolutePath);

        // 파일 존재 여부 확인
        if (!driverFile.exists()) {
            throw new RuntimeException("크롬 드라이버 파일을 찾을 수 없습니다: " + absolutePath);
        }

        // Unix 계열 시스템에서 실행 권한 설정
        if (!os.contains("win")) {
            if (!driverFile.canExecute()) {
                driverFile.setExecutable(true);
            }
        }

        // 시스템 속성 설정
        System.setProperty("webdriver.chrome.driver", absolutePath);
    }

    /**
     * 과목명으로 상세 강의 정보 검색 (CrawlerExample 방식)
     */
    public static List<DetailedSubject> searchDetailedSubjects(String inputYearFull, String inputSemester, String subjectName) {
        setupChromeDriver();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
        options.addArguments("--window-size=1280,1024");
        WebDriver driver = new ChromeDriver(options);

        List<DetailedSubject> detailedSubjects = new ArrayList<>();
        Set<String> uniqueCourses = new HashSet<>();

        try {
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            WebElement yearInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));
            js.executeScript("arguments[0].value=arguments[1];", yearInput, inputYearFull);

            WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
            new Select(semesterSelect).selectByVisibleText(inputSemester);

            js.executeScript("document.getElementById('schSbjetCd1').value = '';");

            WebElement detailSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
            new Select(detailSelect).selectByVisibleText("교과목명");

            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
            inputBox.clear();
            inputBox.sendKeys(subjectName);

            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
            searchBtn.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr[1]")));
            WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
            long lastScrollTop = -1;

            while (true) {
            	Thread.sleep(200);
            	
                List<WebElement> rows = driver.findElements(By.xpath("//tbody[@id='grid01_body_tbody']/tr"));
                int rowCount = rows.size();
                boolean isLastScroll = false;

                js.executeScript("arguments[0].scrollTop += 320;", scrollDiv);
                Thread.sleep(200);
                long newScrollTop = (Long) js.executeScript("return arguments[0].scrollTop;", scrollDiv);
                if (newScrollTop == lastScrollTop) {
                    isLastScroll = true;
                }
                lastScrollTop = newScrollTop;

                // 마지막 스크롤이면 전체 행 처리, 아니면 마지막 행 제외
                int limit = isLastScroll ? rowCount : rowCount - 1;

                for (int i = 0; i < limit; i++) {
                    WebElement row = rows.get(i);
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() < 17) continue;

                    String key = getCourseKey(cells);
                    if (!uniqueCourses.contains(key)) {
                        uniqueCourses.add(key);

                        try {
                            String year = getCellText(cells.get(3));
                            String code = getCellText(cells.get(7));
                            String name = getCellText(cells.get(8));
                            String credit = getCellText(cells.get(9));
                            String professor = getCellText(cells.get(12));
                            String lectureTime = getCellText(cells.get(13));
                            String classroom = getCellText(cells.get(15));
                            String roomNumber = getCellText(cells.get(16));
                            String division = getCellText(cells.get(4));

                            if (name.isEmpty() || name.equals("-") || code.isEmpty() || code.equals("-")) {
                                continue;
                            }

                            boolean isRequired = division.contains("전공필수") || division.contains("교양필수");
                            boolean isDesign = division.contains("설계");

                            DetailedSubject detailedSubject = new DetailedSubject(
                                year, inputSemester, division,
                                code, name, isRequired, isDesign, credit,
                                professor, lectureTime, classroom, roomNumber
                            );

                            detailedSubjects.add(detailedSubject);
                        } catch (Exception e) {
                            System.err.println("행 파싱 중 오류: " + e.getMessage());
                        }
                    }
                }

                if (isLastScroll) break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        
        RatingUpdater.applyRatings(detailedSubjects, "course_rating.txt");

        return detailedSubjects;
    }
}