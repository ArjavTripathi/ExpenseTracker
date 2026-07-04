# Backend — CLAUDE.md

Spring Boot backend for Divvy. Java 21, Maven, PostgreSQL, JWT auth, STOMP WebSockets.

---

## Folder Structure

```
src/main/java/com/chat/aj/expensetracker/
├── Algorithm/          — cash flow minimization algorithm, cache, settlement DTOs
├── Auth/               — registration, login, JWT response DTOs
├── Expenses/           — expense CRUD, participant management, recent feed
├── Groups/             — group and membership management, friend settlements
├── Websockets/         — WebSocket config, notification DTO
├── common/
│   ├── Entities/       — JPA entities and repositories (User, Group, GroupMembers, Expenses, ExpenseParticipants)
│   ├── Exceptions/     — custom exceptions, GlobalExceptionHandler, ErrorResponse
│   └── Utility/        — DataSeeder (CommandLineRunner, disabled in production)
└── security/
    ├── Accounts/       — AccountDetails, AccountDetailsService (UserDetailsService impl)
    ├── Config/         — WebSecurityConfig, WebConfig (CORS)
    └── JWT/            — JWTService, JWTAuthenticationFilter, JWTAuthenticationResponse
```

---

## Endpoint Map

### Auth — `/api/auth`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user. Body: `{ name, email, password }`. Returns 200 string. |
| POST | `/api/auth/login` | Authenticate user. Body: `{ email, password }`. Returns raw JWT token string. |

### Groups — `/api/group`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/group` | Create a new group. Body: `{ name }`. Caller becomes owner. |
| GET | `/api/group?groupId=` | Get single group detail with member list. |
| GET | `/api/group/me` | Get all groups the authenticated user owns or belongs to. |
| PUT | `/api/group` | Add a member to a group. Body: `{ groupId, email }`. Owner only. |
| DELETE | `/api/group?groupId=` | Delete a group and all its members. Owner only. |
| DELETE | `/api/group/member?groupId=&email=` | Remove a member from a group. Owner only. |
| GET | `/api/group/friends` | Get net balances per person across all groups for the authenticated user. |

### Expenses — `/api/expenses`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/expenses` | Create a new expense. Body: `{ groupId, totalAmount, description, participants: [{ userId, shareAmount }] }`. Caller is payer. Participant shares must sum to totalAmount. |
| GET | `/api/expenses/all?groupId=` | Get all expenses for a group. Caller must be a group member. |
| GET | `/api/expenses?groupId=&expenseId=` | Get single expense with participant breakdown. Caller must be a group member. |
| PUT | `/api/expenses` | Update an expense amount, description, and participant shares. Expense creator only. |
| DELETE | `/api/expenses?groupId=&expenseId=` | Delete an expense. Expense creator or group owner only. |
| GET | `/api/expenses/recent` | Get recent expenses across all groups the user belongs to. |

### Algorithm — `/api/algorithm`
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/algorithm?groupId=` | Get optimized settlement list for a group. Result is cached in ConcurrentHashMap. |
| GET | `/api/algorithm/preprocessed?groupId=` | Debug endpoint — returns raw net balance map (name → amount) before algorithm runs. |
| GET | `/api/algorithm/friends` | Get friend-level settlement view for the authenticated user across all groups. |

---

## Security Configuration

- All `/api/auth/**` endpoints are public.
- All `/ws/**` WebSocket endpoints are public (STOMP handles its own session lifecycle).
- All other endpoints require a valid JWT Bearer token.
- CSRF is disabled — stateless JWT auth does not require it.
- CORS is configured for `http://localhost:3000` and `http://localhost:5173`.
- `JWTAuthenticationFilter` extends `OncePerRequestFilter` — runs before `UsernamePasswordAuthenticationFilter` on every request.
- Authentication identity is extracted from the JWT subject (email) via `principal.getName()` in controllers.

---

## Service Responsibilities

**AuthService** — user registration (BCrypt password hashing, duplicate email check) and login (Spring Security authentication, JWT generation). Exposes `findUserByEmail(email)` and `findUserById(id)` as shared utilities used across services.

**GroupService** — group lifecycle (create, delete), membership management (add, remove), authorization checks (owner-only operations), group membership queries, friend settlement aggregation across groups.

**ExpenseService** — expense and participant persistence, share validation (participant shares must sum to total), authorization (member check on reads, creator/owner check on writes), recent activity feed, WebSocket notification publishing, and cache invalidation after every write.

**Algorithm** — net balance preprocessing (iterates expenses and participants to compute per-user net balance), greedy two-heap settlement algorithm, ConcurrentHashMap cache keyed by groupId, friend settlement aggregation.

---

## WebSocket Behaviour

- STOMP endpoint: `/ws` (with SockJS fallback)
- Topic prefix: `/topic`
- App destination prefix: `/app`
- Events published by the backend after writes:
    - `EXPENSE_ADDED` → `/topic/group/{groupId}`
    - `EXPENSE_UPDATED` → `/topic/group/{groupId}`
    - `EXPENSE_DELETED` → `/topic/group/{groupId}`
    - `MEMBER_ADDED` → `/topic/group/{groupId}`
    - `MEMBER_REMOVED` → `/topic/group/{groupId}`
    - `GROUP_DELETED` → `/topic/group/{groupId}`
- The backend never tracks subscriptions — that is the broker's responsibility.

---

## Critical Rules

- **Do not modify `Algorithm.java` algorithm logic.** The two-heap greedy approach and `BigDecimal.min()` settlement calculation are correct and intentional.
- **Do not add `CascadeType.ALL` to `@ManyToOne` relationships.** Cascades flow parent → child only. Adding it to child-to-parent relationships will cause data loss.
- **Do not change monetary fields from `BigDecimal` to float or double.**
- **Do not add `@Transactional` to DTO classes.** Transaction boundaries belong in the service layer only.
- **Do not call `algorithm.getOrComputeCache()` from `GroupService`.** This causes a circular dependency. All algorithm calls originate from `Algorithm` service or `AlgorithmController`.
- **Cache invalidation must happen after every expense write.** If you add a new write operation to `ExpenseService`, call `algorithm.invalidateCache(groupId)` at the end of that method.
- **`DataSeeder` is a `@Component`.** Remove the annotation or guard with `if (userRepository.count() > 0) return;` before running in any environment with existing data.