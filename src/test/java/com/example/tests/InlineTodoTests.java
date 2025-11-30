package com.example.tests;

import com.example.base.BaseTest;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class InlineTodoTests extends BaseTest {

    // Change this to your app URL
    private static final String BASE = "http://localhost:3000";

    // Helper to add a unique todo and return its numeric id (as String)
    private String addTodoAndGetId(String title) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.id("newTitle")));
        input.clear();
        input.sendKeys(title);
        driver.findElement(By.id("addBtn")).click();

        By itemLocator = By.xpath("//ul[@id='list']/li[.//span[contains(text(), \"" + title + "\")]]");
        WebElement li = wait.until(ExpectedConditions.visibilityOfElementLocated(itemLocator));
        String liId = li.getAttribute("id"); // e.g., todo-7
        Assert.assertTrue(liId != null && liId.startsWith("todo-"), "List item id must start with 'todo-'");
        return liId.replace("todo-", "");
    }

    // ---------- existing tests (TC001 - TC004) ----------

    @Test(description = "TC001 - Add a todo and verify it appears")
    public void testAddTodo() {
        driver.get(BASE);
        String title = "TC001 Add item " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);
        WebElement titleSpan = driver.findElement(By.cssSelector("#todo-" + id + " .title"));
        Assert.assertEquals(titleSpan.getText().trim(), title);
    }

    @Test(description = "TC002 - Delete a todo and verify it is removed")
    public void testDeleteTodo() {
        driver.get(BASE);
        String title = "TC002 Delete item " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        // Click Delete
        By delBtn = By.cssSelector(".del[data-id='" + id + "']");
        WebElement del = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(delBtn));
        del.click();

        // Wait until the item is no longer present
        By liLocator = By.id("todo-" + id);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(liLocator));
        Assert.assertTrue(driver.findElements(liLocator).isEmpty(), "Todo should be removed after delete");
    }

    @Test(description = "TC003 - Inline edit a todo and verify persistence")
    public void testInlineEditTodo() {
        driver.get(BASE);
        String title = "TC003 Edit item " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        // Click Edit
        By editBtn = By.cssSelector(".edit[data-id='" + id + "']");
        WebElement edit = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(editBtn));
        edit.click();

        // Type new title and save
        By editInput = By.id("edit-input-" + id);
        WebElement input = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(editInput));
        String edited = title + " - updated";
        input.clear();
        input.sendKeys(edited);

        By saveBtn = By.cssSelector(".save[data-id='" + id + "']");
        WebElement save = driver.findElement(saveBtn);
        save.click();

        // Verify UI shows new title
        By titleLocator = By.cssSelector("#todo-" + id + " .title");
        WebElement titleSpan = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(titleLocator));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.textToBePresentInElement(titleSpan, edited));
        Assert.assertEquals(titleSpan.getText().trim(), edited);

        // Refresh to confirm persistence
        driver.navigate().refresh();
        WebElement titleAfterRefresh = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(titleLocator));
        Assert.assertEquals(titleAfterRefresh.getText().trim(), edited);
    }

    @Test(description = "TC004 - Mark todo done/undone")
    public void testMarkDoneTodo() {
        driver.get(BASE);
        String title = "TC004 Done item " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        By chk = By.cssSelector(".chk[data-id='" + id + "']");
        WebElement checkbox = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(chk));
        // mark done
        checkbox.click();
        // verify the li has class 'done'
        By liLocator = By.cssSelector("#todo-" + id + ".done");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(liLocator));
        Assert.assertFalse(driver.findElements(liLocator).isEmpty());

        // uncheck to undo
        checkbox = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(chk));
        checkbox.click();
        // verify class removed
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(liLocator));
        Assert.assertTrue(driver.findElements(By.cssSelector("#todo-" + id + ".done")).isEmpty());
    }

    // ---------- NEW tests (TC005 - TC009) ----------

    @Test(description = "TC005 - Add blank title should show alert and not add item")
    public void testAddBlankTitleShowsAlert() {
        driver.get(BASE);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Ensure field is empty and click add
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.id("newTitle")));
        input.clear();
        driver.findElement(By.id("addBtn")).click();

        // Wait for alert (the UI uses alert('Failed to add todo') on error)
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        alert.accept(); // close it

        // Verify alert mentions failure (basic check) and no new empty item added
        Assert.assertTrue(text.toLowerCase().contains("failed") || !text.isEmpty(),
                "Expected an alert when adding blank todo");
        List<WebElement> items = driver.findElements(By.cssSelector("#list li"));
        // none of the items should have empty title text
        boolean anyEmpty = items.stream().map(e -> {
            WebElement span = e.findElement(By.cssSelector(".title"));
            return span.getText().trim();
        }).anyMatch(String::isEmpty);
        Assert.assertFalse(anyEmpty, "No todo with empty title should exist");
    }

    @Test(description = "TC006 - Edit then Cancel should keep original title")
    public void testEditThenCancelKeepsTitle() {
        driver.get(BASE);
        String title = "TC006 Cancel edit " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        // Open edit area
        By editBtn = By.cssSelector(".edit[data-id='" + id + "']");
        WebElement edit = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(editBtn));
        edit.click();

        // Change input value but click Cancel
        By editInput = By.id("edit-input-" + id);
        WebElement input = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(editInput));
        input.clear();
        input.sendKeys("THIS SHOULD NOT SAVE");

        By cancelBtn = By.cssSelector(".cancel[data-id='" + id + "']");
        WebElement cancel = driver.findElement(cancelBtn);
        cancel.click();

        // Verify title remains unchanged
        By titleLocator = By.cssSelector("#todo-" + id + " .title");
        WebElement titleSpan = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(titleLocator));
        Assert.assertEquals(titleSpan.getText().trim(), title, "Title should remain the original after Cancel");
    }

    @Test(description = "TC007 - Add multiple todos and verify order and count")
    public void testAddMultipleAndVerifyOrder() {
        driver.get(BASE);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String t1 = "TC007 item1 " + System.currentTimeMillis();
        String t2 = "TC007 item2 " + System.currentTimeMillis();
        String t3 = "TC007 item3 " + System.currentTimeMillis();

        addTodoAndGetId(t1);
        addTodoAndGetId(t2);
        addTodoAndGetId(t3);

        // Wait until at least 3 items exist
        wait.until(d -> driver.findElements(By.cssSelector("#list li")).size() >= 3);

        List<WebElement> items = driver.findElements(By.cssSelector("#list li"));
        // get titles in visible order
        List<String> titles = items.stream()
                .map(li -> li.findElement(By.cssSelector(".title")).getText().trim())
                .toList();

        // Ensure the three titles appear and in the order they were added (they should be present somewhere)
        Assert.assertTrue(titles.contains(t1), "List should contain t1");
        Assert.assertTrue(titles.contains(t2), "List should contain t2");
        Assert.assertTrue(titles.contains(t3), "List should contain t3");
        // Optionally assert relative order: find indices
        int i1 = titles.indexOf(t1);
        int i2 = titles.indexOf(t2);
        int i3 = titles.indexOf(t3);
        Assert.assertTrue(i1 >= 0 && i2 >= 0 && i3 >= 0, "All titles found");
        // It's acceptable if order equals insertion order; assert monotonic increasing indices
        Assert.assertTrue(i1 <= i2 && i2 <= i3, "Items appear in insertion order or stable order");
    }

    @Test(description = "TC008 - Mark done then delete the item")
    public void testMarkDoneThenDelete() {
        driver.get(BASE);
        String title = "TC008 Done+Delete " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        By chk = By.cssSelector(".chk[data-id='" + id + "']");
        WebElement checkbox = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(chk));
        checkbox.click();

        // verify done class present
        By doneLocator = By.cssSelector("#todo-" + id + ".done");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(doneLocator));

        // Now delete
        By delBtn = By.cssSelector(".del[data-id='" + id + "']");
        WebElement del = driver.findElement(delBtn);
        del.click();

        // wait for invisibility
        By liLocator = By.id("todo-" + id);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(liLocator));
        Assert.assertTrue(driver.findElements(liLocator).isEmpty(), "Item should be deleted after marking done and delete");
    }

    @Test(description = "TC009 - Add todo then refresh and verify it persists")
    public void testAddThenRefreshPersists() {
        driver.get(BASE);
        String title = "TC009 Persist " + System.currentTimeMillis();
        String id = addTodoAndGetId(title);

        // Refresh and verify
        driver.navigate().refresh();
        By titleLocator = By.cssSelector("#todo-" + id + " .title");
        WebElement titleSpan = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(titleLocator));
        Assert.assertEquals(titleSpan.getText().trim(), title, "Added todo should persist after refresh");
    }
}
