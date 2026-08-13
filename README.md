# RAGAgentic Platform
Prometheus Actuator: http://localhost:8080/actuator/prometheus
Prometheus Docker Actuator:http://host.docker.internal:8080/actuator/prometheus -- inside docker only
Prometheus Query: http://localhost:9090/query
Neo4j for graph: http://localhost:7474/browser/
Qdrant for vector database: http://localhost:6333/dashboard#/collections/enterprise-docs
Grafana for dashboards: http://localhost:3000
Prometheus for matrics: http://localhost:9090/
Loki for logs: http://localhost:3100/ready
Tempo for traces: http://localhost:3200/ready

# Docker Services Repo

https://github.com/laxmanrthagan/ragagentic-docker-services

# Insomnia Scripts

https://github.com/laxmanrthagan/ragagentic-insomnia


# 🚀 Enterprise RAG Platform

> **A production-oriented Enterprise Retrieval-Augmented Generation (RAG) platform built with Java, Spring Boot, Spring AI, local LLMs, Vector Search, Graph RAG, Security, Governance, Observability, and Cloud-Native technologies.**

This project is a hands-on engineering and architecture initiative focused on understanding how **enterprise-grade GenAI/RAG platforms are designed, secured, observed, optimized, and scaled**.

The objective is **not to build another chatbot**.

The objective is to understand the architecture, engineering challenges, and production patterns required to build a **secure, scalable, observable, governed, and trustworthy enterprise AI platform**.

---

## 🎯 Project Objectives

The platform explores the complete lifecycle of an enterprise RAG application:

```text
                    ┌──────────────────────────┐
                    │        User / API        │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ Security & Governance    │
                    │ JWT / RBAC / ABAC         │
                    │ PII / Secrets / Policy    │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │      AI Orchestrator     │
                    │   Spring AI / Agents     │
                    └────────────┬─────────────┘
                                 │
             ┌───────────────────┼───────────────────┐
             ▼                   ▼                   ▼
       Semantic Search      Graph RAG          Keyword Search
          Qdrant              Neo4j              Metadata
             │                   │                   │
             └───────────────────┼───────────────────┘
                                 ▼
                    ┌──────────────────────────┐
                    │ Context Aggregation      │
                    │ Filtering / Reranking    │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │         LLM              │
                    │ Ollama / Local Model     │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │ Answer Validation        │
                    │ Grounding / Confidence   │
                    │ Citation / Safety        │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │      Final Response      │
                    └──────────────────────────┘
```

---

# 🏗️ Architecture

The platform is organized into several logical layers.

## 1. Document Ingestion Pipeline

Enterprise knowledge can originate from multiple systems:

* PDF documents
* Microsoft Word documents
* Markdown
* REST APIs
* Databases
* SharePoint
* Cloud storage
* Other enterprise content repositories

### Ingestion Flow

```text
Source
  │
  ▼
File Validation
  │
  ▼
Metadata Extraction
  │
  ▼
Duplicate Detection
(SHA-256)
  │
  ▼
PII / Secret Detection
  │
  ▼
Document Parsing
  │
  ▼
Intelligent Chunking
  │
  ▼
Embedding Generation
  │
  ▼
Vector + Metadata Storage
  │
  ▼
Qdrant
```

The ingestion pipeline is designed to treat documents as **enterprise-controlled knowledge**, rather than simply converting files into embeddings.

### Document Metadata

Each chunk can contain metadata such as:

* Document ID
* Document version
* Page number
* Owner
* Department
* Classification
* Source
* Creation date
* Last modified date
* Access permissions
* Content type

This metadata becomes important during **retrieval, authorization, filtering, auditing, and governance**.

---

# 🔎 2. Enterprise Retrieval Layer

A production RAG system should not depend exclusively on vector similarity.

The retrieval layer supports multiple retrieval strategies.

### Semantic Search

Uses embeddings and vector similarity through:

**Qdrant**

Useful when the user's question and the source document use different terminology but have similar semantic meaning.

### Hybrid Search

Combines:

```text
Vector Search
      +
Keyword Search
      +
Metadata Filtering
```

This helps improve retrieval accuracy for enterprise terminology, identifiers, product names, error codes, and exact phrases.

### Metadata Filtering

Retrieval can be restricted using metadata such as:

```text
department = "Finance"
classification = "Internal"
owner = "user"
document_version = "latest"
```

This prevents irrelevant or unauthorized documents from entering the RAG context.

### Context Aggregation

Retrieved chunks are:

1. Collected
2. Deduplicated
3. Filtered
4. Ranked
5. Aggregated
6. Passed to the LLM

### Reranking

A reranking stage can improve the relevance of the final context before generation.

---

# 🤖 3. Spring AI RAG Layer

Spring AI provides the primary AI application abstraction.

Core capabilities include:

* `ChatClient`
* `VectorStore`
* Advisors
* Prompt construction
* Context injection
* Conversation history
* Streaming responses
* Retrieval orchestration
* LLM invocation

### Prompt Construction

The final prompt can be composed from multiple sources:

```text
System Instructions
        +
Security / Governance Context
        +
Retrieved Documents
        +
Conversation History
        +
User Question
        ↓
     Final Prompt
        ↓
       LLM
```

This allows the application to dynamically construct context-aware prompts rather than relying on a static prompt template.

---

# 🔐 4. AI Security & Governance

Security is treated as a **first-class architecture layer**.

The platform explores multiple security controls before content reaches the LLM.

### Authentication

```text
JWT Authentication
```

### Authorization

Multiple authorization strategies are considered:

* Role-Based Access Control — RBAC
* Attribute-Based Access Control — ABAC
* Document-level authorization
* Metadata-based access filtering

Example:

```text
User
 │
 ├── Role
 ├── Department
 ├── Permissions
 └── Attributes
       │
       ▼
Authorization Policy
       │
       ▼
Allowed Documents
       │
       ▼
RAG Context
```

### AI Security Controls

The platform includes/targets:

* Prompt injection detection
* PII detection
* PII masking
* Secret detection
* Secret redaction
* Input validation
* Output validation
* Rate limiting
* Audit logging
* Document authorization

The principle is:

> **Never send untrusted or unauthorized enterprise data directly to the LLM.**

---

# 🛡️ 5. Hallucination Mitigation

RAG does not automatically eliminate hallucinations.

The platform therefore introduces a validation stage after generation.

### Response Validation

```text
LLM Response
     │
     ▼
Groundedness Check
     │
     ▼
Citation Validation
     │
     ▼
Confidence Evaluation
     │
     ▼
Unsupported Statement Detection
     │
     ├── High Confidence ──► Response
     │
     └── Low Confidence ───► Fallback / Re-Retrieve
```

### Validation Strategies

* Groundedness checks
* Citation generation
* Confidence scoring
* Unsupported statement detection
* Context-to-answer verification
* Safe fallback responses
* Self-healing retrieval

The goal is to make the system prefer:

> **"I don't have enough evidence to answer this."**

over generating an unsupported answer.

---

# 🧠 6. Multi-Agent Architecture

The platform explores a multi-agent architecture instead of placing all responsibility on a single agent.

### Specialized Agents

```text
                 ┌───────────────┐
                 │ Planner Agent │
                 └───────┬───────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
   Retriever Agent  Graph RAG Agent  Reasoning Agent
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                 ┌───────────────┐
                 │  Critic Agent │
                 └───────┬───────┘
                         │
                ┌────────┴────────┐
                ▼                 ▼
             Accept           Re-evaluate
                                  │
                                  ▼
                            Self-Healing
                             Retrieval
```

### Planner Agent

Determines how a request should be handled.

### Retriever Agent

Performs semantic, hybrid, and metadata-filtered retrieval.

### Graph RAG Agent

Uses Neo4j to retrieve relationship-based information.

### Reasoning Agent

Combines retrieved information and performs higher-level reasoning.

### Critic Agent

Evaluates the generated answer for:

* Relevance
* Groundedness
* Completeness
* Unsupported claims
* Confidence

If the response does not meet the required quality threshold, the system can trigger another retrieval/reasoning cycle.

---

# ⚡ 7. Performance & Cost Optimization

Enterprise AI systems can become expensive and slow when every request results in multiple LLM calls.

The platform therefore explores several optimization techniques.

### Redis Caching

Redis is used for caching frequently requested information.

Potential cache layers include:

```text
User Request
     │
     ▼
Response Cache
     │
     ├── Hit ─────► Return Response
     │
     └── Miss
           │
           ▼
       RAG Pipeline
```

### Embedding Cache

Avoids regenerating embeddings for identical content.

### Parallel Retrieval

Independent retrieval operations can execute concurrently:

```text
             Query
               │
       ┌───────┼────────┐
       ▼       ▼        ▼
    Qdrant   Keyword   Neo4j
       │       │        │
       └───────┼────────┘
               ▼
       Context Aggregation
```

### Batch Embedding

Documents can be embedded in batches to improve throughput.

### Streaming Responses

Server-Sent Events (SSE) can stream generated tokens to clients instead of waiting for the entire response.

### Intelligent Model Routing

Different models can be selected based on:

* Query complexity
* Latency requirements
* Cost
* Context size
* Task type

### Connection Pooling

Database and service connection pools are used to reduce connection establishment overhead.

---

# 📊 8. Observability

Observability is one of the most important aspects of this project.

The goal is to provide visibility across the complete AI request lifecycle.

```text
User Request
     │
     ▼
Spring Boot
     │
     ├──── Security
     │
     ├──── Redis
     │
     ├──── Qdrant
     │
     ├──── Neo4j
     │
     ├──── RAG
     │
     ├──── LLM
     │
     └──── Response Validation
```

Every major stage can be instrumented and correlated.

## Observability Stack

| Technology    | Purpose                      |
| ------------- | ---------------------------- |
| OpenTelemetry | Distributed instrumentation  |
| Prometheus    | Metrics collection           |
| Grafana       | Dashboards and visualization |
| Loki          | Centralized logs             |
| Tempo         | Distributed tracing          |

### Important Metrics

The platform monitors metrics such as:

* Request latency
* Retrieval latency
* LLM latency
* End-to-end latency
* Cache hit ratio
* Token usage
* Embedding latency
* Vector search latency
* Error rate
* Confidence score
* Retrieval result count
* Agent execution time
* LLM invocation count

### Example Trace

```text
HTTP Request
   │
   ├── Authentication
   │
   ├── Redis Cache
   │
   ├── Query Transformation
   │
   ├── Qdrant Search
   │
   ├── Neo4j Search
   │
   ├── Context Reranking
   │
   ├── LLM Generation
   │
   ├── Critic Validation
   │
   └── HTTP Response
```

This makes it possible to investigate questions such as:

> Why did this request take 4 seconds?

> Was the latency caused by retrieval or the LLM?

> How often is the cache being used?

> How many LLM calls are generated per request?

> Which retrieval strategy provides better results?

---

# 🐳 9. Local Production-Like Environment

The complete environment runs locally using Docker and Docker Compose.

### Current Components

```text
┌─────────────────────────────────────────────┐
│              Application Layer              │
│                                             │
│             Spring Boot API                 │
│             Spring AI                      │
└──────────────────────┬──────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
     Ollama          Qdrant         Neo4j
     LLM             Vector DB      Graph DB
        │
        └──────────────┬──────────────┘
                       │
                      Redis
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Prometheus        Loki           Tempo
        │              │              │
        └──────────────┼──────────────┘
                       ▼
                    Grafana
                       │
                       ▼
              OpenTelemetry
                 Collector
```

### Docker Services

* Spring Boot API
* Ollama
* Qdrant
* Neo4j
* Redis
* Prometheus
* Grafana
* Loki
* Tempo
* OpenTelemetry Collector

Docker provides a local environment that approximates the operational characteristics of a distributed production platform.

---

# ☁️ 10. Cloud-Native & Kubernetes Strategy

The current environment is designed to be Kubernetes-ready.

### Planned Kubernetes Architecture

```text
                   Kubernetes Cluster
                          │
             ┌────────────┴────────────┐
             │                         │
       Spring Boot Pods          Supporting Services
             │                         │
       ┌─────┴─────┐             ┌─────┴─────┐
       ▼           ▼             ▼           ▼
    Pod 1        Pod 2         Redis       Qdrant
       │           │
       └─────┬─────┘
             │
        Load Balancer
             │
             ▼
           Users
```

### Planned Capabilities

* Kubernetes deployment
* Horizontal Pod Autoscaling
* Health checks
* Readiness/liveness probes
* Resource limits
* ConfigMaps
* Secrets
* Service discovery
* Production monitoring
* Distributed tracing
* Centralized logging

---

# 🧰 Technology Stack

| Category             | Technology            |
| -------------------- | --------------------- |
| Language             | Java 17               |
| Framework            | Spring Boot 3.x       |
| AI Framework         | Spring AI             |
| LLM Runtime          | Ollama                |
| LLM                  | Llama 3 family        |
| Embeddings           | nomic-embed-text      |
| Vector Database      | Qdrant                |
| Graph Database       | Neo4j                 |
| Cache                | Redis                 |
| Security             | Spring Security / JWT |
| Containers           | Docker                |
| Local Orchestration  | Docker Compose        |
| Observability        | OpenTelemetry         |
| Metrics              | Prometheus            |
| Dashboards           | Grafana               |
| Logging              | Loki                  |
| Tracing              | Tempo                 |
| CI/CD                | GitHub                |
| Future Orchestration | Kubernetes            |

---

# 🔄 End-to-End RAG Flow

A typical request follows this lifecycle:

```text
1. User Question
       │
       ▼
2. Authentication
       │
       ▼
3. Authorization
       │
       ▼
4. Prompt Injection Detection
       │
       ▼
5. Query Analysis
       │
       ▼
6. Cache Lookup
       │
       ├──────── Hit ────────► Cached Response
       │
       ▼
7. Retrieval Planning
       │
       ├── Semantic Search
       ├── Keyword Search
       ├── Metadata Filtering
       └── Graph Search
       │
       ▼
8. Context Aggregation
       │
       ▼
9. Reranking
       │
       ▼
10. Prompt Construction
       │
       ▼
11. LLM Generation
       │
       ▼
12. Critic / Validation
       │
       ├── Low Confidence
       │       │
       │       ▼
       │   Re-Retrieve
       │
       └── Valid Response
               │
               ▼
13. Citation / Response
               │
               ▼
14. Audit + Metrics + Trace
```

---

# 📁 Suggested Repository Structure

```text
enterprise-rag-platform/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.rag/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── ingestion/
│   │   │       ├── retrieval/
│   │   │       ├── rag/
│   │   │       ├── agent/
│   │   │       ├── security/
│   │   │       ├── governance/
│   │   │       ├── validation/
│   │   │       ├── cache/
│   │   │       └── observability/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── prompts/
│   │
│   └── test/
│
├── docker/
│   ├── docker-compose.yml
│   ├── prometheus/
│   ├── grafana/
│   ├── loki/
│   ├── tempo/
│   └── otel/
│
├── docs/
│   ├── architecture/
│   ├── security/
│   ├── rag/
│   ├── agents/
│   └── observability/
│
├── scripts/
│
├── README.md
├── pom.xml
└── .gitignore
```

---

# 🧪 Testing Strategy

The platform is intended to evolve beyond basic unit testing.

Testing areas include:

### Unit Testing

* Services
* Retrieval logic
* Security policies
* Prompt construction
* Validators
* Agent logic

### Integration Testing

* Qdrant
* Neo4j
* Redis
* Ollama
* Spring AI

### RAG Evaluation

Potential evaluation dimensions:

* Retrieval relevance
* Context precision
* Context recall
* Answer groundedness
* Citation correctness
* Answer relevance
* Hallucination rate

### Performance Testing

The platform can be evaluated for:

* Concurrent requests
* Retrieval throughput
* LLM latency
* Cache effectiveness
* Embedding throughput
* End-to-end latency

---

# 📈 Engineering Problems Explored

This project focuses on real-world challenges rather than only implementing the happy path.

### RAG

* How should documents be chunked?
* How much context should be sent to the LLM?
* When should semantic search be used?
* When should hybrid search be used?
* How should retrieval results be reranked?

### Security

* How do you prevent unauthorized documents from entering context?
* How do you detect prompt injection?
* How should PII be handled?
* How should secrets be prevented from reaching the model?

### Reliability

* What happens when retrieval returns nothing?
* What happens when the LLM is unavailable?
* What happens when confidence is low?
* How can the system recover automatically?

### Performance

* How can LLM calls be reduced?
* Where should caching be introduced?
* Which operations can execute in parallel?
* How can embeddings be generated efficiently?

### Observability

* Where is latency introduced?
* How many tokens are being consumed?
* Which retrieval strategy performs better?
* How can an individual AI request be traced end-to-end?

### Scalability

* How does the application scale horizontally?
* Which components are stateless?
* Which components require distributed coordination?
* How should the platform run on Kubernetes?

---

# 🗺️ Roadmap

## Phase 1 — Core RAG

* [x] Spring Boot foundation
* [x] Spring AI integration
* [x] Local LLM integration
* [x] Embedding model integration
* [x] Qdrant integration
* [x] Document ingestion
* [x] Basic semantic retrieval
* [ ] Advanced chunking strategies
* [ ] Hybrid retrieval
* [ ] Reranking

## Phase 2 — Enterprise Security

* [x] Spring Security foundation
* [x] JWT authentication
* [ ] RBAC
* [ ] ABAC
* [ ] Document-level authorization
* [ ] Prompt injection detection
* [ ] PII detection and masking
* [ ] Secret detection and redaction
* [ ] Rate limiting
* [ ] Comprehensive audit logging

## Phase 3 — Advanced RAG

* [ ] Graph RAG
* [ ] Neo4j integration
* [ ] Multi-agent orchestration
* [ ] Planner Agent
* [ ] Retriever Agent
* [ ] Reasoning Agent
* [ ] Critic Agent
* [ ] Self-healing retrieval
* [ ] Advanced citation generation

## Phase 4 — Performance

* [ ] Redis response caching
* [ ] Embedding caching
* [ ] Parallel retrieval
* [ ] Batch embeddings
* [ ] Streaming responses
* [ ] Intelligent model routing
* [ ] Connection pooling
* [ ] Performance benchmarking

## Phase 5 — Observability

* [x] OpenTelemetry
* [x] Prometheus
* [x] Grafana
* [x] Loki
* [x] Tempo
* [ ] End-to-end trace correlation
* [ ] AI-specific dashboards
* [ ] Token/cost dashboards
* [ ] RAG quality dashboards
* [ ] Alerting

## Phase 6 — Cloud Native

* [x] Docker
* [x] Docker Compose
* [ ] Kubernetes
* [ ] Helm
* [ ] Horizontal Pod Autoscaling
* [ ] Kubernetes secrets
* [ ] Production-grade monitoring
* [ ] Cloud deployment

---

# 🎓 Learning Outcomes

This project provides practical experience across several areas of modern software engineering and AI architecture.

### Enterprise Architecture

* Distributed systems
* Microservices
* Cloud-native architecture
* Scalability
* Resilience
* Security architecture

### Generative AI

* LLM integration
* RAG
* Prompt engineering
* Embeddings
* Vector search
* Graph RAG
* Multi-agent systems
* Hallucination mitigation

### AI Governance

* Authentication
* Authorization
* Data protection
* Prompt security
* PII handling
* Secret protection
* Auditability
* Responsible AI controls

### Platform Engineering

* Docker
* Kubernetes
* CI/CD
* Configuration management
* Service integration
* Infrastructure automation

### Observability

* Metrics
* Logs
* Distributed tracing
* AI-specific telemetry
* Performance analysis
* Operational dashboards

---

# 💡 Key Architectural Principles

This project follows several principles:

### 1. Security Before Generation

Unauthorized or sensitive data should never reach the LLM.

### 2. Retrieval Before Generation

The model should use enterprise knowledge rather than relying exclusively on its pretrained knowledge.

### 3. Evidence Before Confidence

Generated answers should be supported by retrieved evidence.

### 4. Observe Everything

Every important stage of the AI pipeline should be measurable and traceable.

### 5. Cache Where It Matters

Repeated expensive operations should not unnecessarily invoke the LLM or embedding model.

### 6. Design for Failure

The platform should gracefully handle:

* LLM failures
* Retrieval failures
* Database failures
* Cache failures
* Invalid documents
* Low-confidence answers
* External service failures

### 7. Separate Concerns

Security, retrieval, orchestration, generation, validation, caching, and observability should remain independently manageable components.

---

# 🚀 Why This Project?

Many GenAI demonstrations follow:

```text
User → Prompt → LLM → Response
```

That architecture is useful for experimentation, but it is insufficient for enterprise production environments.

A real enterprise AI platform must answer additional questions:

```text
Who is the user?

What data is the user allowed to access?

Where did the answer come from?

Can the answer be trusted?

Was sensitive information exposed?

Was the prompt malicious?

How long did retrieval take?

How many tokens were consumed?

Why did the model generate this response?

What happens if the LLM fails?

How does the system scale?

How is the entire request audited?
```

This project explores those questions through implementation rather than theory alone.

---

# 🏁 Final Goal

The ultimate objective is to build a **production-style Enterprise AI/RAG platform** that demonstrates how modern organizations can architect:

```text
Secure
   +
Governed
   +
Observable
   +
Scalable
   +
Performant
   +
Reliable
   +
Trustworthy
        ↓
Enterprise GenAI Platform
```

This repository is continuously evolving as new architecture patterns, technologies, experiments, benchmarks, and production engineering practices are explored.

> **This is a learning project focused on understanding enterprise AI architecture by building the platform end-to-end — from document ingestion and retrieval to security, governance, multi-agent reasoning, observability, optimization, and cloud-native deployment.**

---

## ⭐ Areas of Focus

`Spring AI` · `RAG` · `LLM` · `Vector Database` · `Graph RAG` · `Multi-Agent AI` · `AI Security` · `AI Governance` · `Redis` · `Qdrant` · `Neo4j` · `Docker` · `Kubernetes` · `OpenTelemetry` · `Prometheus` · `Grafana` · `Loki` · `Tempo` · `Cloud Native Architecture` · `Distributed Systems`
