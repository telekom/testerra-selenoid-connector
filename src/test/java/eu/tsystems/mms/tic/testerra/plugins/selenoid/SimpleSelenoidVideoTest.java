package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import eu.tsystems.mms.tic.testframework.useragents.ChromeConfig;
import eu.tsystems.mms.tic.testframework.useragents.FirefoxConfig;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverProxyUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 15.04.2020
 * Time: 10:42
 *
 * @author Eric Kubenka
 */
public class SimpleSelenoidVideoTest extends TesterraTest {

    @BeforeSuite
    public void configureChromeOptions() {
        Proxy proxy = new WebDriverProxyUtils().getDefaultHttpProxy();

        WebDriverManager.setUserAgentConfig(Browsers.chrome, new ChromeConfig() {
            @Override
            public void configure(ChromeOptions options) {
                options.setProxy(proxy);
            }
        });

        WebDriverManager.setUserAgentConfig(Browsers.firefox, new FirefoxConfig() {
            @Override
            public void configure(FirefoxOptions firefoxOptions) {
                firefoxOptions.setProxy(proxy);
            }
        });

    }
    @Test
    public void testT01_SuccessfulTestCaseWillNotCreateVideo() {

        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
    }

    @Test
    public void testT02_FailedTestCaseWillCreateVideo() {
        WebDriverManager.setGlobalExtraCapability("sessionTimeout", "5m");
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

}
