package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testframework.events.ExecutionFinishEvent;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for exclusive WebDriver Sessions
 * <p>
 * Date: 16.04.2020
 * Time: 07:39
 *
 * @author Eric Kubenka
 */
public class ExclusiveSessionSelenoidVideoTest extends AbstractSelenoidTest implements Loggable {

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

    @Test(dependsOnMethods = "testT03_FailingTestWillStopVideo", alwaysRun = true)
    public void test_VideoIsPresent_after_FailingTestWillStopVideo() {

        ExclusiveSessionSelenoidVideoTest self = this;

        TesterraListener.getEventBus().register(new ExecutionFinishEvent.Listener() {
            @Override
            @Subscribe
            public void onExecutionFinish(ExecutionFinishEvent event) {
                /**
                 * TODO: Move that into a TestUnderTest
                 */
                self.isVideoPresentInMethodContext("testT03_FailingTestWillStopVideo", true);
            }
        });
    }
}
