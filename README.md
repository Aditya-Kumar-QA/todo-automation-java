# 📌 Todo Application – Selenium + TestNG Automation Framework (Java)
This project is a UI automation framework built using Java, Selenium WebDriver, TestNG, and Maven.
It automates functional test scenarios for a sample Todo Application, including add, delete, edit, mark-done, and negative test cases.
# 🚀 Tech Stack
Language: Java
Automation Tool: Selenium WebDriver
Test Framework: TestNG
Build Tool: Maven
Design Pattern: Page Object Model (POM)
Driver Management: WebDriverManager
IDE: IntelliJ IDEA
# 📂 Project Structure
todo-automation-java
 ├── src
 │   └── test
 │       └── java
 │           └── com.example
 │               ├── base
 │               │   └── BaseTest.java
 │               └── tests
 │                   └── InlineTodoTests.java
 ├── pom.xml
 └── README.md
# 🧪 Test Scenarios Covered
✔ Add a new Todo item
✔ Mark Todo item as Done
✔ Unmark a Todo item
✔ Delete a Todo item
✔ Validate blank input (negative test)
✔ Verify UI state changes
✔ Validate element visibility, text & DOM changes
# 🏗 How to Run Tests
1️⃣ Clone the repo
git clone https://github.com/Aditya-Kumar-QA/todo-automation-java.git
2️⃣ Install dependencies
mvn clean install
3️⃣ Run tests
mvn test
# ⚙️ Features
Clean and reusable BaseTest
Explicit waits with WebDriverWait
Uses WebDriverManager for automatic driver setup
Structured, readable tests
Easy to extend
# 📈 Future Enhancements
Add Page Object Model (POM)
Add Allure Reports
Add API tests
Add CI/CD (GitHub Actions)
# 👤 Author
Aditya Kumar
QA Engineer | Learning Selenium + TestNG Automation
