# Saga end-to-end test checklist

Run these scenarios together against the gateway with the auth, orchestrator, order, and payment services running. Use an ADMIN JWT and a CUSTOMER JWT. For downstream failure cases, fault the named service after creating the relevant order. Assert database state as well as the HTTP response; a `200 OK` alone does not prove that the Saga completed.

## 1. Normal Saga flow

- [ ] **Payment succeeds:** create an order with a payment method that produces `SUCCESS`. Verify Order `CONFIRMED`, Saga `COMPLETED`, Payment `SUCCESS`.
- [ ] **Payment fails:** create an order with a payment method that produces `FAILED`. Verify Order `PAYMENT_FAILED`, Saga `COMPLETED`, Payment `FAILED`.

## 2. Automatic recovery

For every case, verify both Order and Saga state and inspect `retryCount`, `lastRetryAt`, and `nextRetryAt`.

| Scenario | Expected Order | Expected Saga |
| --- | --- | --- |
| Payment Service unavailable during lookup | `PAYMENT_UNKNOWN` | `PAYMENT_UNKNOWN` |
| Existing payment is `PENDING`, gateway lookup resolves `SUCCESS` | `CONFIRMED` | `COMPLETED` |
| Existing payment is `PENDING`, gateway lookup resolves `FAILED` | `PAYMENT_FAILED` | `COMPLETED` |
| Existing payment remains `PENDING` after gateway lookup | `PAYMENT_UNKNOWN` | `PAYMENT_UNKNOWN` and recoverable |
| Payment later becomes `SUCCESS` | `CONFIRMED` | `COMPLETED` |
| Payment later becomes `FAILED` | `PAYMENT_FAILED` | `COMPLETED` |
| Payment record missing during recovery | `PAYMENT_UNKNOWN` | `PAYMENT_UNKNOWN` and recoverable until the retry limit; no payment is created |
| Maximum attempts exhausted | `PAYMENT_UNKNOWN` | `PAYMENT_MANUAL_REVIEW` |

- [ ] A Saga with no `nextRetryAt` is eligible; a future retry time is skipped; a due retry time is eligible.
- [ ] Backoff starts at 30 seconds, grows exponentially, and caps at 300 seconds.
- [ ] Five lookup/create recovery attempts are recorded; when the due Saga is next processed after reaching the limit, it moves to manual review and `nextRetryAt` is cleared.
- [ ] `retryCount` does not increase after the Saga enters `PAYMENT_MANUAL_REVIEW`.
- [ ] A single Saga's downstream or persistence exception does not stop later eligible Sagas in the same scheduler run.
- [ ] Manual-review and completed Sagas are not selected by automatic recovery.
- [ ] Restart the orchestrator while a Saga is `PAYMENT_UNKNOWN`; recovery resumes using persisted Saga state.
- [ ] Simulate a lost payment-create response after Payment Service stores the payment. Recovery looks up by order ID first and uses the existing payment outcome.
- [ ] Make initial payment creation return HTTP 409 for an existing payment. Verify the orchestrator looks up that payment and applies its actual status; if lookup fails, Order and Saga remain unresolved.
- [ ] Return 404 from a recovery lookup. Verify no payment is created, retry metadata is scheduled, and the Saga eventually moves to manual review after the retry limit.
- [ ] Verify manual reconciliation does not create a payment when its lookup returns 404.

## 3. Manual reconciliation

Prepare a Saga in `PAYMENT_MANUAL_REVIEW` for each payment condition. Verify HTTP result and persisted Order/Saga state.

| Payment/Saga condition | Expected API result | Expected state |
| --- | --- | --- |
| Payment `SUCCESS` | HTTP `200`, `COMPLETED` | Order `CONFIRMED`; Saga `COMPLETED` |
| Payment `FAILED` | HTTP `200`, `COMPLETED` | Order `PAYMENT_FAILED`; Saga `COMPLETED` |
| Payment `PENDING`, gateway lookup resolves `SUCCESS` or `FAILED` | HTTP `200`, `COMPLETED` | Order matches payment result; Saga `COMPLETED` |
| Payment remains `PENDING` after gateway lookup | HTTP `200`, `STILL_PENDING` | Order remains `PAYMENT_UNKNOWN`; Saga remains `PAYMENT_MANUAL_REVIEW` |
| Payment record missing | HTTP `404`, `PAYMENT_NOT_FOUND` | Saga remains in manual review; no payment is created |
| Payment Service unavailable/timeout/5xx | HTTP `503`, `SERVICE_UNAVAILABLE` | Saga remains in manual review |
| Saga already `COMPLETED` | HTTP `200`, `COMPLETED` | No repeated business effects |
| Saga in any other state | HTTP `409`, `INVALID_SAGA_STATE` | No state mutation |
| Optimistic-lock conflict | HTTP `409`, `CONCURRENT_UPDATE` | Winning transaction's state is preserved; retry reads the latest Saga |
| Saga does not exist | Existing not-found exception behavior | No state mutation |

- [ ] Reconcile a manual-review Saga while its payment is `PENDING`. Verify the orchestrator calls Payment Service reconciliation for the existing record and completes the Saga if the gateway returns a terminal status; no new payment is created.

## 4. Security

- [ ] Reconciliation without a token returns `401 Unauthorized`.
- [ ] An authenticated CUSTOMER token returns `403 Forbidden`.
- [ ] An ADMIN token with JWT claim `role: ADMIN` can trigger reconciliation.
- [ ] The admin caller's token authorizes the incoming request; the orchestrator's generated internal JWT is used only for downstream Order and Payment calls.
- [ ] The internal service JWT can access the protected Order and Payment endpoints needed by orchestration and recovery.
- [ ] The recovery endpoint `/api/recovery/payments` is also restricted to ADMIN.
- [ ] The scheduler creates its internal service token from configured service credentials; it does not use or hardcode a customer token.

## 5. Resilience and consistency

- [ ] Make Order status update fail after a payment outcome is known. Verify the Saga is not marked `COMPLETED`; after restoring Order Service, a retry resolves it.
- [ ] Make the Order update succeed and the Saga completion save fail. Retry reconciliation and verify it re-reads the same payment, safely repeats the idempotent Order status update, and completes the Saga without creating a payment.
- [ ] Repeat reconciliation for a completed Saga. Verify it returns `COMPLETED` without repeating business effects.
- [ ] Trigger concurrent reconciliation requests for one Saga. Verify the row lock serializes the requests and no conflicting terminal state is written.
- [ ] Run multiple recovery workers against one due Saga. Verify one payment per order and consistent terminal state; this scenario is a gate for a complete concurrency safeguard.
- [ ] Race payment creation for the same order. Verify the payment provider is not charged twice; database uniqueness alone is not sufficient evidence if the external charge can occur before a unique-key conflict.
- [ ] Verify Order status updates are idempotent when the same terminal status is applied more than once.

## Implementation status

The implementation work discussed so far covers orchestration, persisted Saga state, automatic recovery/backoff, manual reconciliation, result responses, and ADMIN authorization. These checklist scenarios have not been run. General concurrent recovery protection and end-to-end verification remain outstanding; the concurrent payment creation scenario also needs confirmation that payment-side/provider idempotency prevents duplicate charges.
