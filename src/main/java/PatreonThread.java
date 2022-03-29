import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.*;
import java.util.*;
import org.json.simple.*;


public class PatreonThread extends Thread {

	String patreonUrl;
	String webhookUrl;
	DiscordBot bot;
	String discordChannel;

	public PatreonThread(String patreonUrl, String webhookUrl, DiscordBot bot, String discordChannel) {
		this.patreonUrl = patreonUrl;
		this.webhookUrl = webhookUrl;
		this.bot = bot;
		this.discordChannel = discordChannel;
	}

	@Override
	public void run() {

		JSONParser parser = new JSONParser();
		int drivernum = 0; // add driver type here 0-Chrome, 1-Firefox, 2-Edge

		try {
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("config.json"));
			JSONArray checkconfig = (JSONArray) jsonObject.get("drivers");
			JSONObject drivers = (JSONObject) checkconfig.get(drivernum);
			String drivername = drivers.values().toString();
			drivername = drivername.replaceAll("[\\[\\](){}]", "");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}

		System.out.println("Loading the driver...");

		WebDriver driver;
		if (drivernum == 0)
		{
			ChromeOptions options = new ChromeOptions();
			options.addArguments(/*"--headless"*/);
			driver = WebDriverManager.chromedriver().capabilities(options).create();
		}
		else if (drivernum == 1)
		{
			FirefoxOptions options = new FirefoxOptions();
			options.addArguments(/*"--headless"*/);
			driver = WebDriverManager.firefoxdriver().create();
		}
		else
		{
			EdgeOptions options = new EdgeOptions();
			options.addArguments(/*"--headless"*/);
			driver = WebDriverManager.edgedriver().capabilities(options).create();
		}

		// Create and initialize the browser

		goToLoginPage(driver);

		System.out.printf("Loading patreon page '%s'...", patreonUrl);
		driver.get(patreonUrl);
		waitForPageLoad(driver);
//		driver.get("https://www.patreon.com/supermega");

//		// Sleep so we can ensure the page loads even if the connection is slow
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		System.out.println("Finding all posts on the front page...");
		// Get any public posts
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<WebElement> publicPosts = driver.findElements(By.cssSelector("[data-tag='post-card']"));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Display every post found on the front page
		// TODO: will need to get the parts of the post (like image and whatnot) so we can give it to the discord webhook client
		// DiscordWebhook client = new DiscordWebhook(webhookUrl);
		bot.setChannel(discordChannel); // will either get channel from user or from config file in the future

		for (WebElement currentPost : publicPosts) {
			System.out.println("\n\n---------- Post ----------\n" + currentPost.getText());

			// bot start
			bot.setTitle(currentPost.getText());
			bot.setDescription(currentPost.getText());
			bot.send();
			// bot end

			// webhook start
//			client.setTitle(currentPost.getText());
//			client.setDescription(currentPost.getText());
//			client.send();
			// webhook end
		}
//		client.close();
		driver.close();
	}

	private void goToLoginPage(WebDriver driver) {
		// Load the login page to pass Geetest, ensuring we're allowed to see post
		System.out.println("Loading the login page for Geetest");

		int loadCount = 0;

		// Keep reload to get the GeeTest.  Sometimes it doesn't appear on the first load.
		while (loadCount++ < 5) {
			System.out.println("Loading login page...");
			driver.get("https://www.patreon.com/login");

			System.out.println("Waiting...");
			waitForPageLoad(driver);

			/*
			 * Check for the login page on the 2nd or greater successful page reload
			 *
			 * This is required as sometimes loading the page for the first time
			 * will not show the bot check
			 */
			if (loadCount > 1) if (!driver.getPageSource().contains("New to Patreon?")) break;
		}

		System.out.println("Broken out!");
		if (!driver.getPageSource().contains("New to Patreon?")) {
			System.out.println("Pass the test on the screen, then press enter in this console to continue...");

			// geeTest(driver); TODO: captcha not currently working

			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		driver.get(patreonUrl);
	}

	private void geeTest(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

		try {
			// Wait until the GeeTest iframe is loaded
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("iframe")));

			// Store the GeeTest iframe
			WebElement iFrame = driver.findElement(By.tagName("iframe"));

			// Switch the web driver context to the iframe
			driver.switchTo().frame(iFrame);

			// Wait until the GeeTest clickable verification button is loaded
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("geetest_radar_btn")));

			// Store the GeeTest verification button
			WebElement geeTestVerify = driver.findElement(By.className("geetest_radar_btn"));

			// Click the GeeTest verification button
			geeTestVerify.click();

			while (1 == 1) {

				// Wait until both the original and the puzzle image are present
				wait.until(ExpectedConditions.presenceOfElementLocated(By.className("geetest_canvas_fullbg")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.className("geetest_canvas_bg")));

				// Save both the original and the puzzle image elements
				WebElement originalImageElement = driver.findElement(By.className("geetest_canvas_fullbg"));
				WebElement puzzleImageElement = driver.findElement(By.className("geetest_canvas_bg"));

				// Convert both images to base64 image strings
				String originalImageString = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].toDataURL('image/png').substring(22);", originalImageElement);
				String puzzleImageString = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].toDataURL('image/png').substring(22);", puzzleImageElement);

				byte[] originalImageBytes = DatatypeConverter.parseBase64Binary(originalImageString);
				byte[] puzzleImageBytes = DatatypeConverter.parseBase64Binary(puzzleImageString);

				// Actual image
				BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
				BufferedImage puzzleImage = ImageIO.read(new ByteArrayInputStream(puzzleImageBytes));

				// Store WxH of each image
				int originalWidth = originalImage.getWidth();
				int originalHeight = originalImage.getHeight();
				int puzzleWidth = puzzleImage.getWidth();
				int puzzleHeight = puzzleImage.getHeight();

				// Dimensions must be the same
				if (originalWidth != puzzleWidth || originalHeight != puzzleHeight) {
					System.out.println("The width/height don't match for the images.");
					System.out.printf("Original: %dx%d\nPuzzle: %dx%d", originalWidth, originalHeight, puzzleWidth, puzzleHeight);
					System.exit(1);
				}

				int[][] differenceMatrix = new int[originalWidth][originalHeight];

				// Calculate the differences between the original image and the puzzle image
				System.out.println("Difference matrix:");
				for (int y = 0; y < originalHeight; y++) {
					for (int x = 0; x < originalWidth; x++) {
						// Get current RGB values
						int rgbA = originalImage.getRGB(x, y);
						int rgbB = puzzleImage.getRGB(x, y);

						// Something from geeksforgeeks...
						int redA = (rgbA >> 16) & 0xff;
						int greenA = (rgbA >> 8) & 0xff;
						int blueA = (rgbA) & 0xff;
						int redB = (rgbB >> 16) & 0xff;
						int greenB = (rgbB >> 8) & 0xff;
						int blueB = (rgbB) & 0xff;

						// Store the difference values
						differenceMatrix[x][y] += Math.abs(redA - redB);
						differenceMatrix[x][y] += Math.abs(greenA - greenB);
						differenceMatrix[x][y] += Math.abs(blueA - blueB);

						// If the number is less than 130 (a threshold I chose), set it to 0 to signify no change in current pixel.
						if (differenceMatrix[x][y] < 130) differenceMatrix[x][y] = 0;

						System.out.print(differenceMatrix[x][y] + ",");
					}

					System.out.println();
				}

				int dragAmount = 0;

				// Find the first change in the difference matrix
				for (int x = 0; x < originalWidth && dragAmount == 0; x++) {
					for (int y = 0; y < originalHeight; y++) {
						if (differenceMatrix[x][y] != 0) {
							System.out.println("First change is #: " + differenceMatrix[x][y] + " with x = " + x);
							dragAmount = x - 6;
							break;
						}
					}
				}

				// Write image files to current folder
//			File outputOriginal = new File("original.png");
//			File outputPuzzle = new File("puzzle.png");
//
//			ImageIO.write(originalImage, "png", outputOriginal);
//			ImageIO.write(puzzleImage, "png", outputPuzzle);

				System.out.println("Geetest found!  Attempting to slide by " + dragAmount);


				WebElement dragButton = driver.findElement(By.className("geetest_slider_button"));
				Actions move = new Actions(driver);//;.clickAndHold(dragButton);

				// Slowly move the slider with varying cursor height adjustments
//				for (int i = 0; i < dragAmount; i++) {
//					move.moveByOffset(1, new Random().nextInt(2 - (-2)) + (-2));
//				}
//				move.release().perform();
//
//				System.out.println("Press any key...");
//				System.in.read();

				// Let the page load
				Thread.sleep(2000);

				// Two different ways of instantly moving the slider to the correct position
//				move.clickAndHold(dragButton).moveByOffset(dragAmount, 0).release().build().perform();
				move.dragAndDropBy(dragButton, dragAmount, 0).build().perform();

				// Check for retry button
				if (wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("geetest_reset_tip_content"))) != null) {
					driver.findElement(By.className("geetest_reset_tip_content")).click();

					// Let the page load
					Thread.sleep(3000);
				}
			}


		} catch (Exception ex) {
			System.out.println("An error has occurred:\n");
			ex.printStackTrace();
			driver.quit();
			System.exit(1);
		}
	}

	private void waitForPageLoad(WebDriver driver) {
		System.out.println("Waiting for the page to finish loading...");

		new WebDriverWait(driver, Duration.ofSeconds(30)).until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

		System.out.println("Done..!");
	}
}