# TASs ( Request Flow ⏳)
User clicks "Buy"⬇️

Load Balancer
⬇️

TSAS receives request
⬇️

Classify request
⬇️

Check rate limits
⬇️

Check backend capacity
⬇️

(🧠Logic)
If free → dispatch immediately🔀️
Else → enqueue 🔄


Worker sends request to backend
⬇️️

Backend processes request
⬇️️

Response flows back to user 👆🏼⬆️

#______________________After Inplemetation___________________

# TAS – Traffic Acquisition System

A high-performance backend system built with **Spring Boot** that handles traffic spikes using rate limiting, async request queuing, and real-time monitoring. Designed to simulate how large-scale platforms (like Flipkart or Amazon) protect their backend during flash sales.

---

## How It Works

Incoming requests pass through three layers of protection:

```
Request → TrafficMonitor → RateLimiter → RequestQueue → BuyService
              ↓                 ↓               ↓
          Count RPS         Block if >10     Queue if busy
```

1. **TrafficMonitor** — counts requests per second using thread-safe `AtomicInteger` with CAS-based window reset
2. **RateLimiter** — rejects requests that exceed the configured threshold (HTTP 429)
3. **RequestQueue** — queues accepted requests into a `BlockingQueue`, processed async by a fixed thread pool

---

## API Endpoints

| Method | Endpoint | Description | Success Code |
|--------|----------|-------------|--------------|
| `POST` | `/api/v1/buy` | Submit a buy request | `202 Accepted` |
| `GET` | `/api/v1/queue/size` | Current queue depth | `200 OK` |
| `GET` | `/api/v1/metrics` | Full system metrics snapshot | `200 OK` |
| `GET` | `/api/v1/health` | Health status (UP / DEGRADED) | `200 OK` |

### Response Codes
| Code | Meaning |
|------|---------|
| `202` | Request accepted and queued |
| `429` | Rate limit exceeded — too many requests/sec |
| `503` | Queue full — server busy, try again |
| `500` | Internal server error |

### Sample `/metrics` Response
```json
{
  "success": true,
  "message": "Metrics retrieved",
  "data": {
    "currentRps": 4,
    "currentRpm": 52,
    "totalRequests": 200,
    "totalRejected": 12,
    "rejectionRatePercent": 6.0,
    "queueSize": 8,
    "maxQueueSize": 50,
    "queueProcessed": 188,
    "queueDropped": 0,
    "rateLimitMaxRps": 10,
    "status": "HEALTHY"
  }
}
```

---

## Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/your-username/tas-system.git
cd tas-system

# 2. Build
mvn clean install

# 3. Run
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

### Test it
```bash
# Submit a buy request
curl -X POST http://localhost:8080/api/v1/buy

# Check metrics
curl http://localhost:8080/api/v1/metrics

# Check health
curl http://localhost:8080/api/v1/health
```

---

## Configuration

All values are tunable in `application.properties` — no code changes needed:

```properties
# Max requests allowed per second before throttling
tas.rate-limit.max-rps=10

# Max requests that can wait in queue
tas.queue.max-size=50

# Number of parallel worker threads
tas.queue.thread-pool-size=5
```

---

## Project Structure

```
com.Tas.TAS/
├── controller/
│   ├── BuyController.java       # POST /buy, GET /queue/size
│   └── MetricsController.java   # GET /metrics, GET /health
├── limiter/
│   └── RateLimiter.java         # Per-second rate throttling
├── monitor/
│   └── TrafficMonitor.java      # Thread-safe RPS/RPM tracking
├── queue/
│   └── RequestQueue.java        # Async bounded queue + thread pool
├── service/
│   └── BuyService.java          # Business logic (purchase processing)
├── model/
│   ├── ApiResponse.java         # Unified JSON response wrapper
│   └── MetricsSnapshot.java     # Metrics data model
└── exception/
    └── GlobalExceptionHandler.java  # Centralized error handling
```

---

### What problem does this solve?
During a flash sale, thousands of requests hit `/buy` simultaneously. Without protection, the server crashes. This system handles that with three layers: count traffic, reject excess, queue the rest.

### Key technical concepts used
- **`AtomicInteger` / `AtomicLong`** — thread-safe counters without synchronized blocks
- **CAS (Compare-And-Swap)** — used in `compareAndSet()` to safely reset the per-second window with no race condition
- **`BlockingQueue`** — producer-consumer pattern; HTTP thread produces tasks, worker pool consumes them
- **`ExecutorService`** with named threads — parallel async processing, easier to debug in logs
- **Graceful shutdown** — `@PreDestroy` drains the queue before the app stops

### Why not just use Kubernetes?
Kubernetes autoscaling **reacts** — it sees a CPU spike, then spins up new pods. That takes 30–60 seconds. A flash sale crushes your server in the first 5 seconds. This rate limiter and queue respond in **microseconds**. You need both: K8s for horizontal scaling, this for application-level protection while scaling happens.

### Design patterns used
- **Producer-Consumer** — HTTP thread produces, worker pool consumes
- **Chain of Responsibility** — Monitor → RateLimiter → Queue, each layer can reject
- **Facade** — Controller hides all complexity behind a single `/buy` endpoint

### we will add next? (coming up)
- Replace in-memory queue with **Kafka** for distributed scaling across pods
- Add **Resilience4j** circuit breaker for downstream failures
- **Prometheus + Grafana** dashboard off the `/metrics` endpoint
- **JUnit load tests** using `CountDownLatch` to simulate concurrent traffic
