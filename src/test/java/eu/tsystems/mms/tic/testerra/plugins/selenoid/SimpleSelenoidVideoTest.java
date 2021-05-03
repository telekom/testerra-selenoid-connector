package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testframework.execution.testng.AssertCollector;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Date: 15.04.2020
 * Time: 10:42
 *
 * @author Eric Kubenka
 */
public class SimpleSelenoidVideoTest extends AbstractSelenoidTest {

    @Test()
    public void testT01_SuccessfulTestCaseWillNotCreateVideo() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
    }

    @Test( dependsOnMethods = "testT01_SuccessfulTestCaseWillNotCreateVideo", alwaysRun = true)
    public void test_VideoIsNotPresent_after_SuccessfulTestCaseWillNotCreateVideo() {
        this.isVideoPresentInMethodContext("testT01_SuccessfulTestCaseWillNotCreateVideo", false);
    }

    @Test
    public void testT02_FailedTestCaseWillCreateVideo() {
        WebDriverManager.setGlobalExtraCapability("sessionTimeout", "5m");
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    @Test(dependsOnMethods = "testT02_FailedTestCaseWillCreateVideo", alwaysRun = true)
    public void test_VideoIsPresent_after_FailedTestCaseWillCreateVideo() {
        this.isVideoPresentInMethodContext("testT02_FailedTestCaseWillCreateVideo", true);
    }

    @Test
    public void testT03_FailedTestWithCollectedAssertions() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        AssertCollector.assertTrue(false);
    }

    @Test(dependsOnMethods = "testT03_FailedTestWithCollectedAssertions", alwaysRun = true)
    public void test_VideoIsPresent_after_FailedTestWithCollectedAssertions() {
        this.isVideoPresentInMethodContext("testT03_FailedTestWithCollectedAssertions",true);
    }
}
