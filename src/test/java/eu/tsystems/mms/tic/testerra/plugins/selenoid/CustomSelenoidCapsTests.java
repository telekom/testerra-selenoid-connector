package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.webdriver.SelenoidCapabilities;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.useragents.ChromeConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created on 2023-07-19
 *
 * @author mgn
 */
public class CustomSelenoidCapsTests extends AbstractSelenoidTest {

    @Test
    public void testT01_EnvCapsInUserAgent() {
        final String[] localeArray = {
                "LANG=de_DE.UTF-8",
                "LANGUAGE=de:en",
                "LC_ALL=de_DE.UTF-8"};
        Map<String, String[]> envMap = Map.of("env", localeArray);

        WEB_DRIVER_MANAGER.setUserAgentConfig(Browsers.chrome, new ChromeConfig() {
            @Override
            public void configure(ChromeOptions chromeOptions) {
                chromeOptions.setCapability(SelenoidCapabilities.SELENOID_OPTIONS, envMap);
            }
        });

        WebDriver webDriver = WEB_DRIVER_MANAGER.getWebDriver();
        SessionContext sessionContext = WEB_DRIVER_MANAGER.getSessionContext(webDriver).get();

        Map<String, Object> sessionCapabilities = (Map<String, Object>) sessionContext.getWebDriverRequest().getCapabilities().asMap().get(SelenoidCapabilities.SELENOID_OPTIONS);
        Object o = sessionCapabilities.get("env");
        Assert.assertNotNull(o, "Env caps should be set");
    }

}
