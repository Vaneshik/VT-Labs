package org.example.remanga;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.example.remanga.pages.AccountPage;
import org.example.remanga.pages.AccountServicesPage;
import org.example.remanga.pages.CatalogPage;
import org.example.remanga.pages.CommunityPage;
import org.example.remanga.pages.ForumPage;
import org.example.remanga.pages.ForumPostPage;
import org.example.remanga.pages.HomePage;
import org.example.remanga.pages.InfoPage;
import org.example.remanga.pages.LoginPage;
import org.example.remanga.pages.SearchPage;
import org.example.remanga.pages.TitlePage;
import org.example.remanga.pages.TopPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected final String baseUrl = System.getProperty("baseUrl", "https://remanga.org/");
    protected final String browser = System.getProperty("browser", "chrome");
    protected final boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
    protected final int timeoutSeconds = Integer.parseInt(System.getProperty("timeout", "25"));

    @BeforeEach
    void setUp() {
        driver = createDriver(browser);
        driver.manage().window().setSize(new Dimension(1366, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebDriver createDriver(String browserName) {
        return switch (browserName.toLowerCase(Locale.ROOT)) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.setAcceptInsecureCerts(true);
                if (headless) {
                    options.addArguments("-headless");
                }
                yield new FirefoxDriver(options);
            }
            case "chrome" -> {
                String chromeVersion = System.getProperty("chrome.version", "");
                if (chromeVersion.isBlank()) {
                    WebDriverManager.chromedriver().setup();
                } else {
                    WebDriverManager.chromedriver().browserVersion(chromeVersion).setup();
                }
                ChromeOptions options = new ChromeOptions();
                String chromeBinary = System.getProperty("chrome.binary", "");
                if (!chromeBinary.isBlank()) {
                    options.setBinary(chromeBinary);
                }
                options.setAcceptInsecureCerts(true);
                options.addArguments("--disable-notifications");
                options.addArguments("--disable-popup-blocking");
                options.addArguments("--lang=ru-RU");
                options.addArguments("--remote-allow-origins=*");
                if (headless) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1366,900");
                }
                yield new ChromeDriver(options);
            }
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };
    }

    public void openHomePage() {
        driver.get(baseUrl);
        waitForPageReady();
        closePossibleOverlays();
    }

    public void openPath(String path) {
        driver.get(baseUrl.replaceAll("/+$", "") + path);
        waitForPageReady();
        closePossibleOverlays();
    }

    protected WebElement visible(String xpath) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    protected WebElement clickable(String xpath) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    public boolean exists(String xpath) {
        try {
            return !driver.findElements(By.xpath(xpath)).isEmpty();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    protected List<WebElement> all(String xpath) {
        return driver.findElements(By.xpath(xpath));
    }

    protected List<WebElement> visibleAll(String xpath) {
        return all(xpath).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                })
                .toList();
    }

    protected Optional<WebElement> firstVisible(String xpath) {
        return all(xpath).stream().filter(WebElement::isDisplayed).findFirst();
    }

    protected WebElement waitFirstVisible(String xpath) {
        return wait.until(driver -> firstVisible(xpath).orElse(null));
    }

    public void click(String xpath) {
        WebElement element = waitFirstVisible(xpath);
        scrollToCenter(element);
        wait.until(driver -> element.isDisplayed() && element.isEnabled());

        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            jsClick(element);
        } catch (ElementNotInteractableException e) {
            jsClick(element);
        } catch (MoveTargetOutOfBoundsException e) {
            jsClick(element);
        }

        waitForPageReady();
    }

    public void type(String xpath, String text) {
        WebElement element = waitFirstVisible(xpath);
        scrollToCenter(element);
        try {
            element.click();
        } catch (ElementNotInteractableException | MoveTargetOutOfBoundsException e) {
            jsClick(element);
        }
        try {
            element.clear();
        } catch (InvalidElementStateException ignored) {
            element.sendKeys(Keys.CONTROL, "a");
            element.sendKeys(Keys.BACK_SPACE);
        }
        try {
            element.sendKeys(text);
        } catch (ElementNotInteractableException e) {
            jsSetText(element, text);
        }
    }

    protected void pressEnter(String xpath) {
        waitFirstVisible(xpath).sendKeys(Keys.ENTER);
        waitForPageReady();
    }

    public void assertVisible(String xpath, String message) {
        assertTrue(waitFirstVisible(xpath).isDisplayed(), message);
    }

    public void assertAnyVisible(String xpath, String message) {
        wait.until(driver -> !visibleAll(xpath).isEmpty());
        assertTrue(!visibleAll(xpath).isEmpty(), message);
    }

    public boolean isVisible(String xpath) {
        try {
            WebElement element = waitFirstVisible(xpath);
            return element != null && element.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public boolean isAnyVisible(String xpath) {
        try {
            wait.until(driver -> !visibleAll(xpath).isEmpty());
            return !visibleAll(xpath).isEmpty();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected void waitForPageReady() {
        wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
        waitForSpaIdle();
    }

    protected void waitForSpaIdle() {
        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void closePossibleOverlays() {
        List<String> closeButtons = List.of(
                "//button[contains(@aria-label, 'Закрыть') or contains(@aria-label, 'Close')]",
                "//*[self::button or self::a][normalize-space(.)='Принять']",
                "//*[self::button or self::a][contains(normalize-space(.), 'Понятно')]",
                "//*[self::button or self::a][contains(normalize-space(.), 'Закрыть')]"
        );

        for (String xpath : closeButtons) {
            firstVisible(xpath).ifPresent(this::tryClickSilently);
        }
    }

    protected String textOfFirstVisible(String xpath) {
        return waitFirstVisible(xpath).getText().trim();
    }

    public String valueOfFirstVisible(String xpath) {
        return waitFirstVisible(xpath).getAttribute("value");
    }

    public String validationMessage(String xpath) {
        WebElement element = waitFirstVisible(xpath);
        return String.valueOf(((JavascriptExecutor) driver).executeScript(
                "return arguments[0].validationMessage || '';",
                element
        ));
    }

    public String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }

    protected void loginWithConfiguredUser() {
        String login = configuredLogin();
        String password = configuredPassword();

        openHomePage();
        openLoginDialog();
        fillLoginForm(login, password);

        String accountMarker = "//*[contains(normalize-space(.), " + xpathLiteral(login) + ")]"
                + " | //*[contains(normalize-space(.), 'Vaneshik')]"
                + " | //a[contains(@href, '/user/bookmarks') and contains(normalize-space(.), 'Закладки')]"
                + " | //button[contains(normalize-space(.), 'Закладки')]";
        assertAnyVisible(accountMarker, "После авторизации должен появиться пользовательский блок");
    }

    protected String configuredLogin() {
        return propertyOrEnv("remanga.login", "REMANGA_LOGIN");
    }

    protected String configuredPassword() {
        return propertyOrEnv("remanga.password", "REMANGA_PASSWORD");
    }

    protected boolean credentialsProvided() {
        return !configuredLogin().isBlank() && !configuredPassword().isBlank();
    }

    private String propertyOrEnv(String propertyName, String envName) {
        String value = System.getProperty(propertyName, "");
        if (!value.isBlank()) {
            return value;
        }
        return System.getenv().getOrDefault(envName, "");
    }

    public void openLoginDialog() {
        String loginButton = "//button[normalize-space(.)='Вход/Регистрация']";
        click(loginButton);
    }

    public void fillLoginForm(String login, String password) {
        String loginInput = "//div[@role='dialog']//input[@name='fields.login.user']";
        String passwordInput = "//div[@role='dialog']//input[@name='fields.login.password']";

        type(loginInput, login);
        type(passwordInput, password);

        String submit = "//div[@role='dialog']//button[normalize-space(.)='Войти']";
        click(submit);
    }

    public void openFirstTitleFromCurrentPage() {
        String titleLink = "(//a[contains(@href, '/manga/') and contains(@href, '/main') and string-length(normalize-space(.)) > 1])[1]";
        click(titleLink);
    }

    private void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'center'});",
                element
        );
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void jsSetText(WebElement element, String text) {
        ((JavascriptExecutor) driver).executeScript("""
                const element = arguments[0];
                const value = arguments[1];
                if (element.isContentEditable) {
                    element.textContent = value;
                } else {
                    element.value = value;
                }
                element.dispatchEvent(new Event('input', { bubbles: true }));
                element.dispatchEvent(new Event('change', { bubbles: true }));
                """, element, text);
    }

    private void tryClickSilently(WebElement element) {
        try {
            scrollToCenter(element);
            element.click();
        } catch (RuntimeException ignored) {
            try {
                jsClick(element);
            } catch (RuntimeException ignoredAgain) {
            }
        }
    }

    public boolean waitUrlContainsAny(String... fragments) {
        try {
            return wait.until(driver -> {
                String url = driver.getCurrentUrl();
                for (String fragment : fragments) {
                    if (url.contains(fragment)) {
                        return true;
                    }
                }
                return false;
            });
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    protected HomePage homePage() {
        return new HomePage(this);
    }

    protected LoginPage loginPage() {
        return new LoginPage(this);
    }

    protected SearchPage searchPage() {
        return new SearchPage(this);
    }

    protected CatalogPage catalogPage() {
        return new CatalogPage(this);
    }

    protected TopPage topPage() {
        return new TopPage(this);
    }

    protected TitlePage titlePage() {
        return new TitlePage(this);
    }

    protected ForumPage forumPage() {
        return new ForumPage(this);
    }

    protected ForumPostPage forumPostPage() {
        return new ForumPostPage(this);
    }

    protected AccountPage accountPage() {
        return new AccountPage(this);
    }

    protected AccountServicesPage accountServicesPage() {
        return new AccountServicesPage(this);
    }

    protected InfoPage infoPage() {
        return new InfoPage(this);
    }

    protected CommunityPage communityPage() {
        return new CommunityPage(this);
    }

    public String browserTitle() {
        return driver.getTitle();
    }
}
