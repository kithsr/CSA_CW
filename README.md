# Question 01

# In JAX-RS, the default lifecycle of a Resource class (like SensorRoomResource) is per-request. This means the JAX-RS runtime instantiates a brand new object of the class for every single incoming HTTP request, and then destroys it after the response is sent. It is not a singleton by default.

# Because a new instance is created every time, we cannot store out system's data in standard instance variables (like a regular ArrayList inside a class), as the data would be wiped out after every request. To prevent data loss, we must externalize our storage into a static, shared structure (like DatabaseClass). Furthermore, because multiple requests can hit the server at the exact same millisencond, multiple resource instances will try to read/write to tat shared database simultaneously. To prevent race conditions and thread-interference, we must use thread-safe data structures like ConcurrentHashMap rather than a standard Hashmap.


# Question 02

# HATEOAS (Hypermedia As The Engine Of Application State) is the highest level of REST maturity (Level 3 on the Richardson Maturity Model). It is considered a hallmark of advanced design because it transforms an API from a static set of endpoints into a dynamically discoverable web of resources.

# Without HATEOAS, client developers must hardccode specific URLs into their frontend applications and rely entirely on static, out-of-band documentation (like a PDF or Swagger page) to know what actions are available. With HATEOAS, the server embeds navigational links directly inside the JSON responses. This benefits client developers by decoupling their code from the server's specific routing structure; if the backend URLs change in the future, the client application won't break because it dynamically follows the links provided by the server, much like a human navigating a website via hyperlinks.

# Part 01 end

# Question 03

# This decision represents a classic architectural trade-off between network bandwidth and client-side processing:

# Returning Full Objects: This consumes more network bandwidth upfront because the payload is larger. However, it significantly reduces client-side processing and network latency. The client gets all necessary data in a single HTTP request, preventing the infamous "N+1 query problem" where the client would have to make dozens of subsequent GET requests just to render a basic dashboard.

# Returning Only IDs: This minimizes the initial payload size and saves bandwidth. However, it shifts a massive processing burden onto the client. If the client needs to display the names and capacities of those rooms, it must parse the array of IDs and iterate through them, firing off a separate HTTP request for every single room. In mobile or low-latency environments, establishing that many HTTP connections is highly inefficient.

# Question 04

# Yes, the DELETE operation in this implementation is strictly idempotent.

# Justification: In RESTful architecture, an operation is considered idempotent if making multiple identical requests has the exact same effect on the server's state as making a single request.

# In this code, if a client mistakenly sends the exact same DELETE/api/v1/rooms/LTB-301 request three times in a row:

#   1. Request 1: The servers finds LTB-301, deletes it from the ConcurrentHashMap, and returns the 204 No Content status. The server stable has changed (the room is gone)

#   2. Request 2 & 3: The server searches for LTB-301, sees that it evaluates to null, and immediately returns a 404 Not Found status. Crucially, the state of the server's database remains completely unchanged by these subsequent requests. Because the final state of the server is identical whether the client fired the request once or one hundred times, the operation fulfills the strict definition of REST idempotency.

# Part 02 end

# Question 5

# The @Consumes(MediaType.APPLICATION_JSON) annotation acts as a strict gateway filter for the method. If a client attempts to send data with a Content-Type header of text/plain or application/xml, the JAX-RS runtime (Jersey) intercepts the request before it even reaches our Java method execution.

# Recognizing the mismatch between what the client sends and what the endpoint accepts, JAX-RS automatically aborts the request and returns a standard 415 Unsupported Media Type HTTP status code to the client. The technical consequence is highly beneficial: it completely protects our backend logic from attempting to parse incompatible data formats, preventing runtime crashes and enforcing a strict, predictable API contract.

# Question 6

# In RESTful architecture, URIs should represent the resource hierarchy and identity, while query parameters should represent modifiers to the request.

#  1. Semantic Correctness: The path /api/v1/sensors points to the entire collection of sensors. Filtering by type does not create a new hierarchical resource; it simply returns a modified view of the existing collection. Therefore, ?type=C02 is semantically correct.

#  2. Optionality and Stacking: Query parameters are inherently optional, making it easy to use the exact same endpoint to fetch all sensors or a filtered list. Furthermore, query parameters stack cleanly  (e.g., ?type=C02&status=ACTIVE). If we used path variables (/api/v1/sensors/type/C02), the routing becomes incredibly rigid, deeply nested, and difficult to maintain if we want to add more optional filters in the future.

# Part 3 end

# Question 7

# The Sub-Resource Locator pattern is essential for managing complexity and adhering to the Single Responsibility Principle as an API scales.

# If we defined every deeply nested path (like /sensors/{id}/readings/{rid}) inside a single SensorResource class, that controller would quickly transform into an unmaintainable "God Object," consisting of thousands of lines of code handling unrelated business logic (e.g., sensor metadata processing alongside massive arrays of historical telemetry data).

# By utilizing a Sub-Resource Locator, we achieve strict modularity. The parent class (SensorResource) acts purely as a routing gateway, capturing the {sensorId} and instantiating the SensorReadingResource. This delegation isolates the telemetry logic into its own encapsulated file. Furthermore, it completely decouples the sub-resource from absolute URIs; the SensorReadingResource operates entirely within its relative context, making the code much cleaner, highly reusable, and significantly easier to write unit tests for.

# Part 4 end

# Question 8

# HTTP 404 (Not Found) implies that the target routing URL itself does not exist. If a client sends a POST request to /api/v1/sensors, that endpoint does exist and is actively listening. Returning a 404 in this scenario would be highly misleading, as the client would assume they have a typo in their URL path or that the server is down.

# HTTP 422 (Unprocessable Entity) is far more semantically accurate. It explicitly tells the client: "The server understands the content type of your request, and the JSON syntaxx you sent is perfectly valid, but the server was unable to process the contained instructions due to semantic errors." In this specific case, the semantic error is a violation of referential integrity (attempting to map a sensor to a roomId that cannot be validated). Using 422 separates payload-logic errors from network-routing errors.

# Question 9

# Exposing internal Java stack traces is a severe "Information Disclosure" vulnerability. From a defensive cybersecurity standpoint, it violates the principle of least privilege regarding system information and provides attackers with free reconnaissance data.

# When an API leaks a stack trace, an attacker can gather highly specific intelligence about the backend architecture, including:

# 1. Framework Versions: The trace often reveals the exact versions of libraries being used (e.g., Jersey 2.35, Jackson, Hibernate). Attackers can cross-reference these specific versions against databases of known CVEs (Common Vulnerabilities and Exposures) to find pre-built exploits.

# 2. Internal Infrastructure: Stack traces expose absolute file paths, directory structures, and package naming conventions (e.g., com.smartcampus.database), giving attackers a map of the server's file system.

# 3. Database Architecture: If a SQL exception is leaked, the trace might expose table names, column names, or even the exact malformed SQL queries, paving the way for targeted SQL Injection attacks.

# By utilizing a generic ExceptionMapper<Throwable>, we sanitize the output, returning a sterile 500 error that acknowledges the failure without handing attackers a blueprint of our internal systems.


# Question 10

# tilizing JAX-RS filters provides three major architectural advantages rooted in the principles of Aspect-Oriented Programming (AOP):

# 1. Separation of Concerns: A resource method (like getRoom) should have one single responsibility: executing the business logic of retrieving a room. By pulling the logging logic out and placing it into a filter, our controller classes remain incredibly clean and highly readable.

# 2. The DRY Principle (Don't Repeat Yourself): If an API has 50 different endpoints, manually inserting Logger.info() into every method is a maintenance nightmare. If the engineering team later decides to change the log format to include timestamps, they would have to modify 50 different files. With a filter, we write the logic exactly once, and it globally applies to the entire application.

# 3. Guaranteed Execution: If we put logging inside a resource method, and the user sends a bad URL that triggers a 404 Not Found before the framework routes the request to our method, that request is never logged. A ContainerRequestFilter catches the traffic at the absolute edge of the application, guaranteeing 100% observability for every incoming ping and outgoing status code, regardless of internal server errors or routing failures.

# Part 5 end