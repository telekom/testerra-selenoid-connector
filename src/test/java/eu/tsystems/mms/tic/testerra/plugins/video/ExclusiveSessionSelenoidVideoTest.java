package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * Tests for exclusive WebDriver Sessions
 * <p>
 * Date: 16.04.2020
 * Time: 07:39
 *
 * @author Eric Kubenka
 */
public class ExclusiveSessionSelenoidVideoTest extends TesterraTest {

    private static String WEBDRIVER_SESSION = "";

    @Test
    public void testT01_SuccessfulTestCaseWillStartVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver();
        WEBDRIVER_SESSION = WebDriverManager.makeSessionExclusive(driver);
        driver.get("https://heise.de");
    }

    @Test(dependsOnMethods = "testT01_SuccessfulTestCaseWillStartVideo")
    public void testT02_SuccessfulTestCaseWillNotStopVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver(WEBDRIVER_SESSION);
        driver.get("https://google.de");
    }

    @Test(dependsOnMethods = "testT02_SuccessfulTestCaseWillNotStopVideo")
    public void testT03_FailingTestWillStopVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver(WEBDRIVER_SESSION);
        driver.get("https://golem.de");
        Assert.fail("This should fail.");
    }
}
