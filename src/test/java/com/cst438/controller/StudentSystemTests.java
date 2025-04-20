package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * System tests pertaining to STUDENT role
 */
public class StudentSystemTests {
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
     * System test to enroll into a section
     * --- The test uses Selenium to navigate from the home page for
     * --- an student to the page to enroll into a section.
     * --- A section is selected from the list of open sections.
     * --- The student view schedule page is selected, and the year
     * --- and semester are entered. There are assert statements that
     * --- verify the new section was successfully added to the
     * --- student's schedule.
     * @throws Exception
     */
    private void Login(String username, String password) {
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.id("submit")).click();
    }
    @Test
    public void systemTestEnroll () throws Exception {
        Login("user@csumb.edu","user");
        Thread.sleep(SLEEP_DURATION);
        String title = "Software Design";
        String section = "6";

        driver.findElement(By.cssSelector("a[href= '/schedule']")).click();
        driver.findElement(By.xpath("//tr[td[contains(text(), 'Year:')]]/td/input")).sendKeys("2025");     //2025
        driver.findElement(By.xpath("//tr[td[contains(text(), 'Semester:')]]/td/input")).sendKeys("Spring");       //Spring
        driver.findElement(By.xpath("//button[contains(text(), 'Show Schedule')]")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement scheduleTable = driver.findElement(By.xpath("//table[@class='Center' and @border='1']"));
        List<WebElement> rows = scheduleTable.findElements(By.tagName("tr"));
        int beforeSize = rows.size();

        WebElement we = driver.findElement(By.cssSelector("a[href= '/addCourse']"));
        we.click();
        Thread.sleep(SLEEP_DURATION);
        WebElement courseRow = driver.findElement(By.xpath("//tr[td[contains(text(), '" + title + "')] and td[contains(text(), '" + section + "')]]"));
        Thread.sleep(SLEEP_DURATION);

        WebElement enrollButton = courseRow.findElement(By.xpath(".//button[contains(text(), 'Enroll')]"));
        enrollButton.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement enrollButton2 = driver.findElement(By.cssSelector("div.MuiDialogActions-root button:nth-of-type(1)"));
        enrollButton2.click();
        Thread.sleep(SLEEP_DURATION);
        WebElement courseRow2 = driver.findElement(By.xpath("//tr[td[contains(text(), '" + title + "')] and td[contains(text(), '" + section + "')]]"));
        WebElement dropButton = courseRow2.findElement(By.xpath(".//button[contains(text(), 'Drop')]"));
        assertNotNull(dropButton);
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.cssSelector("a[href= '/schedule']")).click();
        driver.findElement(By.xpath("//tr[td[contains(text(), 'Year:')]]/td/input")).sendKeys("2025");     //2025
        driver.findElement(By.xpath("//tr[td[contains(text(), 'Semester:')]]/td/input")).sendKeys("Spring");       //Spring
        driver.findElement(By.xpath("//button[contains(text(), 'Show Schedule')]")).click();
        Thread.sleep(SLEEP_DURATION);

        scheduleTable = driver.findElement(By.xpath("//table[@class='Center' and @border='1']"));
        rows = scheduleTable.findElements(By.tagName("tr"));
        int afterSize = rows.size();
        assertEquals(beforeSize + 1, afterSize);
    }

}
