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
    public void test_Video_is_not_present_in_SessionContext_on_passed_test() {
        this.Video_is_present_in_SessionContext("testT01_SuccessfulTestCaseWillNotCreateVideo", false);
    }

    @Test
    public void testT02_FailedTestCaseWillCreateVideo() {
        WebDriverManager.setGlobalExtraCapability("sessionTimeout", "5m");
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    @Test(dependsOnMethods = "testT02_FailedTestCaseWillCreateVideo", alwaysRun = true)
    public void test_Video_is_present_in_SessionContext_on_failed_test() {
        this.Video_is_present_in_SessionContext("testT02_FailedTestCaseWillCreateVideo", true);
    }

    @Test
    public void test_collect_Video_on_collected_assertion() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        AssertCollector.assertTrue(false);
    }

    @Test(dependsOnMethods = "test_collect_Video_on_collected_assertion", alwaysRun = true)
    public void test_Video_is_present_in_SessionContext_on_collected_assertion() {
        this.Video_is_present_in_SessionContext("test_collect_Video_on_collected_assertion",true);
    }
}
