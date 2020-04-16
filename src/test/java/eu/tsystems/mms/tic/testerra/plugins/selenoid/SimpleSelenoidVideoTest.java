package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
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
public class SimpleSelenoidVideoTest extends TesterraTest {

    @Test
    public void testT01_SuccessfulTestCaseWillNotCreateVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://heise.de");
    }

    @Test
    public void testT02_FailedTestCaseWillCreateVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://google.de");
        Assert.fail("must fail");
    }

}
