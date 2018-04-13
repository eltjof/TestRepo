package nl.hsac.fitnesse.example;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import org.openqa.selenium.WebElement;

public class ExampleTest extends BrowserTest {

    @Override
    protected void scrollTo(WebElement element) {
        getSeleniumHelper().executeJavascript("window.scrollBy(0,(-window.innerHeight + arguments[0].getBoundingClientRect().top + arguments[0].getBoundingClientRect().bottom) / 2);", element);
        waitAfterScroll(getWaitAfterScroll());
    }
    public void checkPendingRequests() {
        final int timeoutInNumberOfTries = 100;
        try {
            boolean timeout = true;
            for (int i = 0; i < timeoutInNumberOfTries; i++) {
                Thread.sleep(250);
                final Object numberOfAjaxConnections = getSeleniumHelper().executeJavascript("return window.openHTTPs");
                // return should be a number
                if (numberOfAjaxConnections instanceof Long) {
                    final Long n = (Long) numberOfAjaxConnections;
                    if (n.longValue() == 0L) {
                        timeout = false;
                        break;
                    }
                } else {
                    // If it's not a number, the page might have been freshly loaded indicating the monkey
                    // patch is replaced or we haven't yet done the patch.
                    monkeyPatchXMLHttpRequest();
                }
            }
            if (timeout) {
                System.out.println("Pending XHR requests even after 50 times checking (100 msec) for:");
                return;
                //throw new RuntimeException("Pending XHR requests even after 50 times checking (100 msec) for:");
            }
        } catch (final InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void monkeyPatchXMLHttpRequest() {
        try {
            final Object numberOfAjaxConnections = getSeleniumHelper().executeJavascript("return window.openHTTPs");
            if (numberOfAjaxConnections instanceof Long) {
                return;
            }
            final String script = "  (function() {" + "var oldOpen = XMLHttpRequest.prototype.open;" + "window.openHTTPs = 0;" +
                    "XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {" + "window.openHTTPs++;" +
                    "this.addEventListener('readystatechange', function() {" + "if(this.readyState == 4) {" + "window.openHTTPs--;" + "}" +
                    "}, false);" + "oldOpen.call(this, method, url, async, user, pass);" + "}" + "})();";
            getSeleniumHelper().executeJavascript(script);
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
