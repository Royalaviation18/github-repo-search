# GitHub Repository Searcher

Spring Boot 3.5.3 / Java 21 application that searches GitHub repositories, stores
results in PostgreSQL, and returns stored results with optional filters.

## API

Both routes are rooted at `/api/github`. Responses are JSON. No application-level
authentication or CORS policy is configured; configure Function URL access and
CORS separately in AWS if required.

### Search and store repositories

- **Method/path:** `POST /api/github/search`
- **Headers:** `Content-Type: application/json`, `Accept: application/json`
- **Body:** `query` is required and must not be blank; `language` and `sort` are
  optional by validation. `sort` defaults to `stars`.

```http
POST https://<lambda-function-url>/api/github/search
Content-Type: application/json
Accept: application/json
```

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

Success is HTTP 200:

```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "example",
      "description": "Example repository",
      "owner": "example-owner",
      "language": "Java",
      "stars": 100,
      "forks": 20,
      "lastUpdated": "2025-01-01T00:00:00Z"
    }
  ]
}
```

Validation errors return HTTP 400, for example `{"query":"must not be blank"}`.
GitHub API failures return HTTP 502; GitHub rate limiting returns HTTP 503 with
`{"message":"GitHub API rate limit exceeded"}` and may include `Retry-After`.
Database and unexpected server errors return HTTP 500 with a generic message.

### List stored repositories

- **Method/path:** `GET /api/github/repositories`
- **Query parameters:** `language` (optional), `minStars` (optional integer),
  `sort` (optional; `stars` by default, with `forks` and `updated` also supported).
- **Headers:** `Accept: application/json`

```http
GET https://<lambda-function-url>/api/github/repositories?language=Java&minStars=50&sort=forks
Accept: application/json
```

Success is HTTP 200 with an envelope:

```json
{
  "repositories": [
    {
      "id": 123456,
      "name": "example",
      "description": "Example repository",
      "owner": "example-owner",
      "language": "Java",
      "stars": 100,
      "forks": 20,
      "lastUpdated": "2025-01-01T00:00:00Z"
    }
  ]
}
```

Invalid `minStars` values return HTTP 400. Database failures return HTTP 500.
The deployed URL format is `https://<lambda-function-url>/<existing-api-path>`.
The examples above use placeholders, not a deployed endpoint.

Swagger UI and the OpenAPI document remain at `/swagger-ui/index.html` and
`/v3/api-docs`.

## Local development

Prerequisites: Java 21, PostgreSQL, and the Maven wrapper. The default local
configuration uses PostgreSQL at `jdbc:postgresql://localhost:5432/githubdb`.
Set credentials as environment variables; no database credential is stored in
the repository.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/githubdb'
export DB_USERNAME='postgres'
export DB_PASSWORD='<local-password>'
./mvnw spring-boot:run
```

The default (non-Lambda) configuration retains Hibernate `update` behavior for
local development. The Lambda handler always activates the `lambda` profile,
which uses Hibernate `validate` and does not create, alter, or reset the
deployed schema.

## AWS Lambda / Function URL

The application uses the AWS-maintained `aws-serverless-java-container` Spring
Boot 3 adapter. Its HTTP API payload-v2 handler translates Lambda Function URL
events to Spring MVC requests and Spring responses back to Lambda responses.
The Spring context and adapter are initialized once per execution environment
and reused for warm invocations; the embedded web server is not started by the
handler.

Build from the repository root:

```bash
./mvnw clean test
./mvnw package
```

Deploy **`target/github-0.0.1-SNAPSHOT-lambda.zip`**. It contains application
classes/resources at the archive root and runtime dependencies in `lib/`; the
normal Spring Boot executable JAR remains available as
`target/github-0.0.1-SNAPSHOT.jar`.
The ZIP is approximately 60 MB in this build, above Lambda's 50 MB direct
upload limit. The deployment agent must upload it through Amazon S3 (or another
supported path); the unzipped package must remain within Lambda's 250 MB limit.
Confirm DevopsArk can handle this artifact size and upload flow.

Configure the Lambda function as follows:

- Runtime: **Java 21** (`java21`).
- Handler: **`com.royalaviation.github.LambdaStreamHandler`**
  (the Java `RequestStreamHandler` class).
- Architecture: the ZIP has no architecture-specific native dependencies.
  `x86_64` is the conservative initial setting; validate `arm64` with the
  selected DevopsArk build/runtime before choosing it.
- Function URL payload: HTTP API payload format 2.0, which is the Function URL
  event format.
- Start with at least 1024 MB memory and a 30-second timeout, then tune using
  cold-start and database/GitHub latency measurements. Function URL auth and
  any Function URL CORS policy must be configured in AWS.

Set these environment variables in Lambda/DevopsArk, using a secret store or
secure environment-variable injection for credentials:

| Variable | Required | Example/meaning |
| --- | --- | --- |
| `DB_URL` | Yes | `jdbc:postgresql://<host>:5432/<database>?sslmode=require` |
| `DB_USERNAME` | Yes | `<database-user>` |
| `DB_PASSWORD` | Yes | `<database-password>` |
| `GITHUB_TOKEN` | No | `<github-token>`; optional token for authenticated GitHub REST API requests |
| `SPRING_PROFILES_ACTIVE` | Optional | `lambda`; the handler also activates this profile |
| `DB_POOL_MAX_SIZE` | Optional | `2`; conservatively caps each execution environment's Hikari pool |

The DB hostname must be reachable from the Lambda VPC/subnets. Configure routing,
DNS, outbound rules, and the database security group to permit PostgreSQL
connections from the Lambda security group. Use TLS for remote PostgreSQL (for
example, `sslmode=require`, with certificate verification configured to match
your provider). Estimate maximum possible connections as Lambda concurrency
times `DB_POOL_MAX_SIZE`; coordinate that limit with the database.

No migration tool or migration scripts exist in this project. Before deployment,
create/update the PostgreSQL schema to match `GitHubRepository`; the Lambda
profile validates the schema at startup and will fail rather than change it.
Use a reviewed, non-destructive database migration process for schema changes.

## DevopsArk FAAS deployment assessment

No DevopsArk FAAS implementation or deployment configuration is included in this
repository, so Java deployment support cannot be verified here. Confirm in the
separate DevopsArk FAAS backend and deployment agent:

1. Java 21 Lambda runtime availability.
2. Configuration of the handler class above as `RequestStreamHandler`.
3. Acceptance of a ZIP with classes at the root and dependency JARs under
   `lib/` (or equivalent correct dependency classpath assembly).
4. Java 21 runtime bootstrap/invocation support (not a web-server bootstrap).
5. Secure propagation of the environment variables above, especially database
   credentials and the optional GitHub token.
6. Lambda VPC/database connectivity, Function URL payload-v2 events, and
   Function URL auth/CORS settings.
7. Memory, timeout, concurrency, and artifact-size limits.

No AWS deployment or Postman invocation is claimed by this repository change.

## Tests

```bash
./mvnw clean test
./mvnw package
```

`GithubApplicationTests.contextLoads` uses the configured PostgreSQL database;
the Lambda event translation test and repository storage unit tests do not
require PostgreSQL.
