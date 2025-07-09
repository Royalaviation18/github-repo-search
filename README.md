
# GitHub Repository Searcher

🎯 Objective

- Developing a Spring Boot application that allows users to search for GitHub repositories using the GitHub REST API.

- The applicaton should store search resultts in a PostgreSQL database and provie an API endpoint to retrieve stored results based on filter criteria.




## 📚 API Reference

####  🔄 Search & Store GitHub Repositories

```http
  POST http://localhost:8080/api/github/search
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `query` | `string` | **Required**. name of the repository|
| `language` | `string` | **Required**. Programming language used in the repository |
| `sort` | `string` | **Optional**. The default value is stars |

#### 📤 Retrieve Stored Repositories with Filters: Fetches repositories from the database with optional filters and sorting.

```http
  GET http://localhost:8080/api/github/repositories?language=Java&minStars=50&sort=forks
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `language`      | `string` | **Optional**. Programming language used in the repository |
| `minStars` | `Integer` | **Optional**. Return repositories with at least this many stars |
| `sort` | `string` | **Optional**. Sort by one of: stars(default),forks, updated |


More Examples:
```http
  GET /api/repositories
  GET /api/repositories?language=Java&minStars=10
  GET /api/repositories?sort=forks
```
## Run Locally

✅ Prerequisites
- Java 17+

- Maven

- PostgreSQL running locally

Clone the project

```bash
  git clone https://github.com/Royalaviation18/github-repo-search
```

Go to the project directory

```bash
  cd github-repo-search
```

Set up PostgreSQL database

- Create a DB named githubdb and update credentials in:
```bash
  src/main/resources/application.properties
```

Run the app

```bash
  ./mvnw spring-boot:run
```
- Or from IntelliJ, run GithubApplication.java.

Access Swagger Docs:

Navigate to:
👉 http://localhost:8080/swagger-ui/index.html



## 🧪  Running Test Cases

This project uses JUnit 5 and Mockito for unit testing the service and controller layers.

#### Prerequisites
- Make sure you have
  
  i) JDK 17 or above
  
  ii) Maven 3.8+

  iii) PostgreSQL Running     

▶️ To run tests via IntelliJ IDEA

- Navigate to src/test
- Right click on RepositoryStorageServiceImplTest.java and Run

✅ What’s Covered
- Layer: Service
- Tool: JUnit + Mockito	
- Purpose: Validates business logic and filters


📝 Sample Output
  ```bash
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```


