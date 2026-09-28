# RETRY, RCA & VALIDATION FREEZE: WalkMark

1. **Retry & RCA Budget**:
- Maximum 2 identical retries.
- Maximum 3 remediation attempts per root cause.
- On failure: Capture full evidence -> Classify -> Form RCA hypothesis -> Propose minimal remediation.
- If limit exceeded: STOP execution immediately and request human decision.

2. **Validation Freeze**:
- Validation gates marked `FROZEN_PASS` must NEVER be rerun unless:
  1. Relevant source implementation changed.
  2. Relevant runtime environment changed.
  3. Regression evidence is observed.
