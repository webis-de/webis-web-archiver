package de.webis.webarchive.environment.scripts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;

import de.webis.webarchive.common.Version;
import de.webis.webarchive.environment.browsers.Browser;
import de.webis.webarchive.environment.browsers.Windows;

public class ScrollDownScript extends InteractionScript {
  
  //////////////////////////////////////////////////////////////////////////////
  // LOGGING
  //////////////////////////////////////////////////////////////////////////////

  private static final Logger LOG =
      Logger.getLogger(ScrollDownScript.class.getName());
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  //////////////////////////////////////////////////////////////////////////////
  
  public static final String NAME = "scroll-down";
  
  public static final Version VERSION = new Version(1, 1, 0);
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  //////////////////////////////////////////////////////////////////////////////

  public ScrollDownScript(final Path scriptDirectory)
  throws IOException {
    super(scriptDirectory);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // FUNCTIONALITY
  //////////////////////////////////////////////////////////////////////////////

  @Override
  protected void executeInteraction(
      final Browser browser, final String startUrl, final Path outputDirectory)
  throws Throwable {
    final WebDriver window = browser.openWindow(startUrl);
    this.scrollDown(browser, window);
    this.saveSnapshot(browser, window, outputDirectory);
  }

  protected void scrollDown(final Browser browser, final WebDriver window) {
    final long quietPeriodInSeconds = 3;
    final long waitTimeoutInSeconds = 10;
    browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);

    // Enough to reach "click for more"-button of google image search
    final int maxScrollings = 25;
    for (int scrollings = 0; scrollings < maxScrollings; ++scrollings) {
      final int scrollPosition = Windows.getScrollYPosition(window);
      final int scrollHeight = Windows.getScrollHeight(window);
      if (scrollPosition >= scrollHeight) { break; }

      LOG.info("Scrolling down " + (scrollings + 1)
          + " from " + scrollPosition + "/" + scrollHeight);
      Windows.scrollDownOneWindow(window);
      browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);
    }

    final int scrollPosition = Windows.getScrollYPosition(window);
    final int scrollHeight = Windows.getScrollHeight(window);
    LOG.info("Scrolled down to " + scrollPosition + "/" + scrollHeight);
    
    Windows.scrollToTop(window);
    LOG.info("Resize viewport height to " + scrollHeight);
    Windows.resizeViewportHeight(window, scrollHeight);
    browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);
  }
  
  protected void saveSnapshot(final Browser browser, final WebDriver window,
      final Path outputDirectory)
  throws IOException {
    LOG.info("Writing source");
    Windows.writeSource(window, outputDirectory.resolve("source.html"));
    LOG.info("Writing DOM");
    Windows.writeDom(window, outputDirectory.resolve("page.html"));
    LOG.info("Taking screenshot");
    Windows.screenshotPng(window, outputDirectory.resolve("page.png"));
    LOG.info("Writing elements");
    Windows.writeElements(window, outputDirectory.resolve("elements.txt"));
  }

}
