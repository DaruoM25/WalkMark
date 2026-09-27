# 00 - Request Normalization

## Objective
Convert a raw request into intent, scope, entities, constraints, ambiguities,
acceptance criteria and required evidence for WalkMark.

## Trigger
Every new request or material scope change.

## Inputs
User request, governance, project map, and architecture sources.

## Steps
1. Extract normalized fields: intent, scope, constraints, ambiguities, criteria, evidence.
2. Separate blocking from non-blocking ambiguity.
3. Require explicit confirmation for blocking ambiguity.
4. Record assumptions for non-blocking ambiguity.

## STOP rules
Stop before implementation when scope, authority, or architectural boundary is ambiguous.
