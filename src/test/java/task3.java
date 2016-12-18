import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zapalskyi Volodymyr on 13.12.2016.
 */

public class task3 {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor jse;
    private Actions builder;

    @BeforeClass
    public void beforeTest(){
        System.setProperty("webdriver.chrome.driver",
                System.getProperty("user.dir") + "/drivers/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 10);
        jse = ((JavascriptExecutor) driver);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        builder  = new Actions(driver);

        Reporter.setEscapeHtml(false);
    }

    @Test
    public void openSite(){
        log("Open main page 'Bing.com'");
        driver.navigate().to("http://www.bing.com");
        Assert.assertEquals(driver.getTitle(),
                "Bing",
                "Unexpected page title!");
    }

    @Test(dependsOnMethods = "openSite")
    public void goToPicture() {
        log("Go to the search picture page");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@id='scpl1']")));
        driver.findElement(By.xpath("//a[@id='scpl1']")).click();
        log("Page title is: " + driver.getTitle());
        Assert.assertEquals(driver.getTitle(),
                "Лента изображений Bing",
                "Unexpected page title!");
    }

    @Test(dependsOnMethods = "goToPicture")
    public  void scrollDown() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            log("Scroll down - " + (i + 1));
            int numPict1 = driver.findElements(By.xpath("//li[@data-idx]")).size();
            int numPict2 = 0;
            Thread.sleep(1000);
            jse.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.xpath("//li[@data-idx]"), numPict1));
            numPict2 = driver.findElements(By.xpath("//li[@data-idx]")).size();
            log("Pictures: before " +
                    numPict1 +
                    " / after " +
                    numPict2);
            Assert.assertTrue((numPict1 < numPict2),
                    "Pictures not loading!");
        }
    }

    @Test(dependsOnMethods = "scrollDown", dataProvider = "toFind")
    public void searchPicture(ArrayList list){
        jse.executeScript("window.scrollTo(0, 0)");
        for (int i = 0; i< list.size(); i++ ){
            String str = (String) list.get(i);
            log("Search '" + str +"' pictures");
            driver.findElement(By.xpath("//input[@id='sb_form_q']")).sendKeys(Keys.CONTROL + "a");
            //Thread.sleep(200);
            driver.findElement(By.xpath("//input[@id='sb_form_q']")).sendKeys(Keys.BACK_SPACE);
            driver.findElement(By.xpath("//input[@id='sb_form_q']")).sendKeys(str);
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@id='sb_form_go']")));
            driver.findElement(By.xpath("//input[@id='sb_form_go']")).click();
            Assert.assertEquals(driver.getTitle(),
                    str + " - Bing images",
                    "Unexpected page title!");
        }
    }

    @Test(dependsOnMethods = "searchPicture")
    public void zoomPicture() throws InterruptedException {
        log("Cursor hovers over the picture");
        WebElement somePicture = driver.findElement(By.xpath("//div[@class ='dg_u'][7]"));
        builder.moveToElement(somePicture).build().perform();
        log("Check the picture in zoom frame");
        Assert.assertTrue(checkElement("//div[@class ='irhc']"),
                "Unexpected result: Picture is not in new frame");
        Thread.sleep(200);
        log("Check the 'Add in collection' button");
        Assert.assertTrue(checkElement("//*[name()='svg' and @class = 'collicon gen favSav']"),
                "Unexpected result: button is not available!");
        log("Check the 'Image match' button");
        Assert.assertTrue(checkElement("//img[@class ='ovrf ovrfIconMS']"),
                "Unexpected result: button is not available!");
        log("Check the 'Mark as Adult' button");
        Assert.assertTrue(checkElement("//img[@class ='ovrf ovrfIconFA']"),
                "Unexpected result: button is not available!");
    }

    @Test(dependsOnMethods = "zoomPicture")
    public void openSlideshow(){
        log("Press the 'Image match' button");
        WebElement icon1 = driver.findElement(
                By.xpath("//img[@class ='ovrf ovrfIconMS']"));
        builder.moveToElement(icon1).click().build().perform();
        log("Waiting load slideshow");
        Assert.assertTrue(checkElement("//div[@id = 'detail_canvas']"),
                "Unexpected result: slideshow mode is not load!");
    }

    @Test(dependsOnMethods = "openSlideshow")
    @Parameters({"numRelat"})
    public void numPicture(int numRelat){
        log("Click the 'See more image' button");
        driver.findElement(By.xpath("//div[@class = 'expandButton clickable active']")).click();
        log("Check number of related pictures");
        Assert.assertTrue(numRelat < driver.findElements(By.xpath("//li[@data-row]")).size(),
                "Unexpected result: number of related pictures less "+ numRelat + "!");
    }

    @AfterClass
    public void afterTest(){
        driver.quit();
    }

    @DataProvider(name = "toFind")
    public Object[][] testData() throws FileNotFoundException {
        ArrayList list = new ArrayList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    System.getProperty("user.dir") + "/Data/toFind.txt"));
            try {
                String str;
                while ((str = reader.readLine()) != null){
                    list.add(str);
                }
            }finally {
                reader.close();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return new Object[][]{
                {list}
        };
    }

    private void log(String message) {
        Reporter.log(message + "<br>");
    }

    private boolean checkElement(String str){
        try {
            wait.until(ExpectedConditions.numberOfElementsToBe(
                    By.xpath(str), 1));
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
