package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testerra.plugins.video.webdriver.VideoDesktopWebDriverFactory;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.hooks.ModuleHook;
import eu.tsystems.mms.tic.testframework.interop.TestEvidenceCollector;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;

/**
 * Date: 15.04.2020
 * Time: 10:44
 *
 * @author Eric Kubenka
 */
public class SelenoidVideoHook implements ModuleHook {

    private static final String[] browsers = {
            Browsers.chrome,
            Browsers.chromeHeadless,
            Browsers.firefox,
    };

    @Override
    public void init() {

        WebDriverManager.registerWebDriverFactory(new VideoDesktopWebDriverFactory(), browsers);

        WebDriverManager.registerWebDriverShutDownHandler(new SelenoidVideoGrabber());
        TestEvidenceCollector.registerVideoCollector(new SelenoidVideoGrabber());

        TesterraListener.registerAfterMethodWorker(SelenoidVideoGrabber.class);
    }

    @Override
    public void terminate() {

    }
}
