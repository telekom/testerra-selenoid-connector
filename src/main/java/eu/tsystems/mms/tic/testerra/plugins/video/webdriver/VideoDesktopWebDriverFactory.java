package eu.tsystems.mms.tic.testerra.plugins.video.webdriver;

import eu.tsystems.mms.tic.testerra.plugins.video.SelenoidHelper;
import eu.tsystems.mms.tic.testerra.plugins.video.VideoRequest;
import eu.tsystems.mms.tic.testerra.plugins.video.VideoRequestStorage;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverFactory;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Extends DesktopWebDriverFactory by using selenoid video capabilities.
 * <p>
 * Date: 15.04.2020
 * Time: 11:49
 *
 * @author Eric Kubenka
 */
public class VideoDesktopWebDriverFactory extends DesktopWebDriverFactory {

    private SelenoidCapabilityProvider selenoidCapabilityProvider = SelenoidCapabilityProvider.get();
    private SelenoidHelper selenoidHelper = SelenoidHelper.get();

    private VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();

    @Override
    protected DesktopWebDriverRequest buildRequest(WebDriverRequest request) {
        return super.buildRequest(request);
    }

    @Override
    public WebDriver getRawWebDriver(DesktopWebDriverRequest request, DesiredCapabilities desiredCapabilities) {

        // determine everything for selenoid... incl. video name on remote.
        final Capabilities videoCaps = selenoidCapabilityProvider.provide(request);
        desiredCapabilities = desiredCapabilities.merge(videoCaps);

        // start webdriver with selenoid caps.
        final WebDriver rawWebDriver = super.getRawWebDriver(request, desiredCapabilities);

        // create a VideoRequest with request and videoName
        final VideoRequest videoRequest = new VideoRequest();
        videoRequest.webDriverRequest = request;
        videoRequest.selenoidVideoName = videoCaps.asMap().get(SelenoidCapabilityProvider.Caps.videoName.toString()).toString();

        // store it.
        videoRequestStorage.store(videoRequest);

        // get vnc path and log
        final String remoteVncPath = selenoidHelper.getRemoteVncUrl(videoRequest);
        log().info("VNC Streaming URL: " + remoteVncPath);

        return rawWebDriver;
    }
}
