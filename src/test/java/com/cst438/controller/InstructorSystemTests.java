package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * System tests pertaining to INSTRUCTOR role
 */

public class InstructorSystemTests {

    static String osName = System.getProperty("os.name").toLowerCase();
    Keys myKey = osName.contains("windows") || osName.contains("linux")?Keys.CONTROL:Keys.COMMAND;

    public static final String CHROME_DRIVER_FILE_LOCATION = osName.contains("windows") || osName.contains("linux")?
            "C:\\chromedriver-win64\\chromedriver.exe": "/Users/henry/chromedriver-mac-x64/chromedriver";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    /**
     * System test to add an assignment
     * --- The test uses Selenium to navigate
     * --- from the home page for an instructor,
     * --- to view a list of Sections, enter year and semester
     * --- and view the list of sections, then select the link
     * --- to view assignments, and finally to create a new assignment.
     * --- The fields assignment title and due date are entered,
     * --- and the assignment is successfully created. There are assert
     * --- statements that verify the assignment was successfully created.
     * @throws Exception
     */
    @Test
    public void systemTestAddAssignment() throws Exception {
        String assignmentName = "Visit Edmund's Planet";
        String dateDue = "03122025";
        String dueDateTable = "2025-03-12";


        driver.findElement(By.id("year")).sendKeys("2025");     //2025
        driver.findElement(By.id("semester")).sendKeys("Spring");       //Spring
        driver.findElement(By.cssSelector("a[href='/sections']")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.cssSelector("a[href='/assignments']")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement table = driver.findElement(By.tagName("table"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        int beforeSize = rows.size();

        driver.findElement(By.xpath("//button[contains(text(), ' Add Assignment + ')]")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.xpath("//input[@name='title']")).sendKeys(assignmentName);

        driver.findElement(By.name("credits")).sendKeys(dateDue);
        driver.findElement(By.xpath("//button[contains(text(), 'Save')]")).click();
        Thread.sleep(SLEEP_DURATION);

        table = driver.findElement(By.tagName("table"));
        rows = table.findElements(By.tagName("tr"));
        int afterSize = rows.size();
        assertEquals(beforeSize + 1, afterSize);

        WebElement row = driver.findElement(By.xpath("//tr[td[contains(text(), '" + dueDateTable + "')] and td[contains(text(), \"" + assignmentName + "\")]]"));
        assertNotNull(row);
        Thread.sleep(SLEEP_DURATION);
    }

    /**
     * System test to grade an assignment
     * --- The test uses Selenium to navigate from the home page for an \
     * --- instructor, to view a list of Sections, enter year and semester
     * --- and view the list of sections, then select the link to
     * --- grade an assignment. Scores are entered for all students and
     * --- then saved. There are assert statements to check that the
     * --- save was successful.
     * @throws Exception
     */
    @Test
    public void systemTestGradeAssignment() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2025");     //2025
        driver.findElement(By.id("semester")).sendKeys("Spring");       //Spring
        driver.findElement(By.cssSelector("a[href='/sections']")).click();
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.cssSelector(("a[href='/assignments']"))).click();
        Thread.sleep(SLEEP_DURATION);
        // assignment of interest
        WebElement we = driver.findElement(By.xpath("//tr[td[contains(text(), \"db homework 1\")]]"));
        assertNotNull(we);
        we.findElement(By.xpath("//button[contains(text(), 'Grade')]")).click();
        Thread.sleep(SLEEP_DURATION);
        Thread.sleep(SLEEP_DURATION);

        // get the table to see how many students there are
        WebElement table = driver.findElement(By.xpath("//table[tr/th[contains(text(), 'GradeId')] and tr/th[contains(text(), 'Student')]]"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        if (rows.size() == 2){
            assertEquals("95", driver.findElement(By.name("Score")).getAttribute("value"));
            Thread.sleep(SLEEP_DURATION);

            driver.findElement(By.name("Score")).sendKeys(Keys.chord(myKey,"a", Keys.DELETE));
            // regrade
            driver.findElement(By.name("Score")).sendKeys("100");
            Thread.sleep(SLEEP_DURATION);
            // reopen and confirm regrade success
            driver.findElement(By.xpath("//button[contains(text(), 'Save Grades')]")).click();
            Thread.sleep(SLEEP_DURATION);
            we.findElement(By.xpath("//button[contains(text(), 'Grade')]")).click();
            Thread.sleep(SLEEP_DURATION);
            assertEquals("100", driver.findElement(By.name("Score")).getAttribute("value"));
        }else if (rows.size() >= 3){
            for (int i = 1; i < rows.size(); i++) {
                WebElement scoreInputs = rows.get(i).findElement(By.name("Score"));
                if (scoreInputs != null) {
                    rows.get(i).findElement(By.name("Score")).sendKeys(Keys.chord(myKey,"a", Keys.DELETE));
                    rows.get(i).findElement(By.name("Score")).sendKeys("100");
                    Thread.sleep(SLEEP_DURATION);
                }

            }
            driver.findElement(By.xpath("//button[contains(text(), 'Save Grades')]")).click();
            Thread.sleep(SLEEP_DURATION);
            we.findElement(By.xpath("//button[contains(text(), 'Grade')]")).click();
            Thread.sleep(SLEEP_DURATION);
            table = driver.findElement(By.xpath("//table[tr/th[contains(text(), 'GradeId')] and tr/th[contains(text(), 'Student')]]"));
            rows = table.findElements(By.tagName("tr"));
            for (int i = 1; i < rows.size(); i++) {
                String scoreInputs = rows.get(i).findElement(By.name("Score")).getAttribute("value");
                assertEquals("100", scoreInputs);
            }
        }
    }

    /**
     * System test to enter enrollment grades for enrolled students
     * --- The test uses Selenium to navigate from the home page for an
     * --- instructor, to view a list of Sections, enter year and semester
     * --- and view the list of sections, then select the link to view enrollments,
     * --- and the grade field for each enrolled student is updated with a final
     * --- letter grade value. Then the grades are saved.
     * --- There are assert statements to verify that the grades were saved.
     * @throws Exception
     */
    @Test
    public void systemTestEnrollmentGrades() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2025");     //2025
        driver.findElement(By.id("semester")).sendKeys("Spring");       //Spring
        driver.findElement(By.cssSelector("a[href='/sections']")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement sectionsTable = driver.findElement(By.tagName("table"));
        List<WebElement> rows = sectionsTable.findElements(By.tagName("tr"));
        // update the grades
        for (int i = 1; i < rows.size(); i++) {
            // get the current row
            WebElement row = rows.get(i);

            row.findElement(By.xpath(".//a[contains(text(), 'ENROLLMENTS')]")).click();
            Thread.sleep(SLEEP_DURATION);

            // getting a new table and giving everyone an A
            WebElement enrollmentsTable = driver.findElement(By.xpath("//table[thead/tr/th[contains(text(), 'Enrollment ID')] and thead/tr/th[contains(text(), 'Student ID')]]"));
            List<WebElement> enrollments = enrollmentsTable.findElements(By.tagName("tr"));
            for (int j = 1; j < enrollments.size(); j++) {
                WebElement enrollment = enrollments.get(j);
                enrollment.findElement(By.name("grade")).sendKeys(Keys.chord(myKey,"a", Keys.DELETE));
                enrollment.findElement(By.name("grade")).sendKeys("A");
            }
            driver.findElement(By.xpath("//button[contains(text(), 'Update')]")).click();
            Thread.sleep(SLEEP_DURATION);
            Alert alert = driver.switchTo().alert();
            alert.accept();
            driver.navigate().back();
            Thread.sleep(SLEEP_DURATION);
            rows = driver.findElement(By.tagName("table")).findElements(By.tagName("tr"));
        }

        // refreshing the page to check persistence
        driver.navigate().refresh();
        Thread.sleep(SLEEP_DURATION);
        // checking that the grades are all set to A's
        sectionsTable = driver.findElement(By.tagName("table"));
        rows = sectionsTable.findElements(By.tagName("tr"));
        // checking every sections grade
        for (int i = 1; i < rows.size(); i++) {
            WebElement row = rows.get(i);

            row.findElement(By.xpath(".//a[contains(text(), 'ENROLLMENTS')]")).click();
            Thread.sleep(SLEEP_DURATION);
            WebElement enrollmentsTable = driver.findElement(By.xpath("//table[thead/tr/th[contains(text(), 'Enrollment ID')] and thead/tr/th[contains(text(), 'Student ID')]]"));
            List<WebElement> enrollments = enrollmentsTable.findElements(By.tagName("tr"));
            for (int j = 1; j < enrollments.size(); j++) {
                WebElement enrollment = enrollments.get(j);
                assertEquals("A", enrollment.findElement(By.name("grade")).getAttribute("value"));
            }
            driver.navigate().back();
            Thread.sleep(SLEEP_DURATION);
            rows = driver.findElement(By.tagName("table")).findElements(By.tagName("tr"));
        }

    }
}
