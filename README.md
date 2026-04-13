# Question 01

# In JAX-RS, the default lifecyce of a Resource class (like SensorRoomResource) is per-request. This means the JAX-RS runtime instatiates a brand new object of the class for every single incoming HTTP request, and then destroys it after the response is sent. It is not a singleton by default.

# Because a new instance is created every time, we cannot store out system's data in statndard instance variables (like a regular ArrayList inside a class), as the data would be wiped out after every request. To prevent data loss, we must externalize our storage into a static, shared structure (like DatabaseClass). Furthermore, because multiple requests can hit the server at the exact same millisencond, multiple resource instances will try to read/write to tat shared database simultaneously. To prevent race conditions and thread-interference, we must use thread-safe data structures like ConcurrentHashMap rather than a standard Hashmap.


# Question 02

# HATEOAS (Hypermedia As The Enginer Of Application State) is the highest level of REST maturity (Level 3 on the Richardson Maturity Model). It is considered a hallmark of advanced design because it transforms an API from a static set of endpoints into a dynamically discoverable web of resources.

# Without HATEOAS, client developers must hardccode specific URLs into their frontend applications and rely entirely on static, out-of-band documentation (like a PDF or Swagger page) to know what actions are available. With HATEOAS, the server embeds navigational links directly inside the JSON responses. This benefits client developers by decoupling their code from the server's specific routing structure; if the backend URLs change in the future, the client application won't break because it dynamically follows the links provided by the server, much like a human navigating a website via hyperlinks.

# Part 01 end

# Question 03

# This decision represents a classic architectural trade-off between network bandwidth and client-side processing:

# Returning Full Objects: This consumes more network bandwidth upfront because the payload is larger. However, it significantly reduces client-side processing and network latency. The client gets all necessary data in a single HTTP request, preventing the infamous "N+1 query problem" where the client would have to make dozenns of subsequent GET requests just to render a basic dashboard.

# Returning Only IDs: This minimizes the initial payload size and saves bandwidth. However, it shifts a massic processing burden onto the client. If the client needs to display the names and capacities of those rooms, it must parse the array of IDs and iterate through them, firing off a separate HTTP request for every single room. In mobile or low-latency environments, estabilishing that many HTTP connections is hightly inefficient.

# Question 04

# Yes, the DELETE operation in this implementation is strictly idempotent.

# Justification: In RESTful architecture, an operation is considered idempotent if making multiple identical requests has the exact same effect on the server's state as making a single request.

# In this code, if a client mistakenly sends the exact same DELETE/api/v1/rooms/LTB-301 request three times in a row:

#   1. Request 1: The servers finds LTB-301, deletes it from the ConcurrentHashMap, and returns the 204 No Content status. The server stable has changed (the room is gone)

#   2. Request 2 & 3: The server searches for LTB-301, sees that it evalates to null, and immediately returns a 404 Not Found status. Crucially, the state of the server's database remains completely unchanged by these subsequest requests. Because the final state of the server is identical whether the client fired the request once or one hundred times, the operation fulfills the strict definition of REST indempotency.

# Part 02 end

