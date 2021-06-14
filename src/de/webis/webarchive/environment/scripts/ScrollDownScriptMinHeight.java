package de.webis.webarchive.environment.scripts;

import java.io.IOException;
import java.lang.Math;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import org.openqa.selenium.WebDriver;

import org.apache.commons.io.IOUtils;

import de.webis.webarchive.common.Version;
import de.webis.webarchive.environment.browsers.Browser;
import de.webis.webarchive.environment.browsers.Windows;

public class ScrollDownScriptMinHeight extends InteractionScript {
  
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

  public ScrollDownScriptMinHeight(final Path scriptDirectory)
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
      final int scrollPosition = Math.max(Windows.getScrollYPosition(window),663);
      final int scrollHeight = Windows.getScrollHeight(window);
      if (scrollPosition >= scrollHeight) { break; }

      LOG.info("Scrolling down " + (scrollings + 1)
          + " from " + scrollPosition + "/" + scrollHeight);
      Windows.scrollDownOneWindow(window);
      browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);
    }
    
    // use minimal height to standardize and process pages of height 0 correctly
    final int scrollPosition = Math.max(Windows.getScrollYPosition(window),663);
    final int scrollHeight = Math.max(Windows.getScrollHeight(window),663);
    LOG.info("Scrolled down to " + scrollPosition + "/" + scrollHeight);
    
    Windows.scrollToTop(window);
    LOG.info("Resize viewport height to " + scrollHeight);
    Windows.resizeViewportHeight(window, scrollHeight);
    browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);
  }
  
  public static String writeElementsAndCss(
      final WebDriver window, final Path file)
  throws IOException {
    final String elements = getElements(window);
    final String charset = Windows.getCharset(window);
    try (final Writer writer = new OutputStreamWriter(new FileOutputStream(
        file.toFile()), charset)) {
      writer.write(elements);
    }
    return elements;
  }

  private static final String JAVASCRIPT_GET_ELEMENTS_AND_CSS =
      readCode("get-elements-and-css.js");
  
  public static String getElements(final WebDriver window) {
    return Windows.execute(window, JAVASCRIPT_GET_ELEMENTS_AND_CSS).toString();
  }
  
  protected static String readCode(final String filename) {
    try (final InputStream input = Windows.class.getResourceAsStream(filename)) {
      return IOUtils.toString(input, Charset.forName("UTF-8"));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
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
    LOG.info("Writing elements and css");
    writeElementsAndCss(window, outputDirectory.resolve("elements.txt"));
  }

}
