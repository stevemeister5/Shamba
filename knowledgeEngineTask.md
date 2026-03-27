# Knowledge Engine Task Board

> **Last Updated:** 27/03/2026
> **Workspace:** `C:\Users\CyberDuck\Documents\Android Tools\Shamba`
> **Branch:** `master`

---

## Board Summary

| Column       | Count |
|--------------|-------|
| In Progress  | 1     |
| Backlog      | 21    |
| Review       | 0     |
| Trash        | 1     |

---

## In Progress

### `239cf` — ProseChunkDataModelAndSemanticChunker
- **Status:** `in_progress` (session idle — agent not running)
- **Auto-review:** enabled → `commit`
- **Created:** 2025-09-24
- **Notes:** This is the foundational data model task. A previous attempt (`72899` with the same prompt) was trashed. This is the current active iteration. 14 backlog tasks are linked waiting on this.

---

## Backlog — Knowledge Content Layer

| ID | Prompt | Waits On |
|----|--------|----------|
| `a242d` | ProseKnowledgeDiseaseDiagnosticsAndSymptoms | `79c1d` |
| `d421f` | OperationalRulesNutritionAndFeedAndBreeding | `79c1d` |
| `90618` | ProseKnowledgeMedicinesFormularyAndDrugs | `79c1d` |
| `47722` | ProseKnowledgeMedicinesAndCheese | `79c1d` |
| `bb62a` | OperationalRulesPlantingAndGrowthAndGestation | `79c1d` |
| `efa99` | ProseKnowledgeCropsAndWeather | `79c1d` |
| `53d86` | ProseKnowledgeLivestockGoatsAndSheep | `79c1d` |
| `f2580` | OperationalRulesVaccinationAndNotifiable | `79c1d` |
| `ac7d4` | OperationalRulesWithdrawalAndDose | `79c1d` |

## Backlog — Retrieval & Processing Layer

| ID | Prompt | Waits On |
|----|--------|----------|
| `79c1d` | TripleRetrievalMiniSearchBM25VectorMetadata | all 9 knowledge tasks |
| `af443` | ContextBridgeAndConflictDetection | `79c1d` |
| `a6ba9` | IntentClassifierAndConsistencyChecker | `79c1d` |

## Backlog — UI Layer

| ID | Prompt | Waits On |
|----|--------|----------|
| `4f632` | UISidePanelAskBrowseSaved | `af443`, `a6ba9` |
| `c4507` | UIDynamicSymptomCheckerAndAnswerDisplay | `a6ba9`, `af443` |

## Backlog — Module Integration Layer

| ID | Prompt | Waits On |
|----|--------|----------|
| `22ffd` | LivestockModuleMaarifaIntegration | `4f632`, `c4507`, `a6ba9`, `af443` |
| `827e2` | CropModuleMaarifaIntegration | `af443`, `4f632`, `c4507`, `a6ba9` |
| `7f1bd` | CheeseFeedModuleMaarifaIntegration | `a6ba9`, `c4507`, `af443`, `4f632` |
| `8fc8a` | BuildPipelineProseKnowledgeCompiler | `79c1d` |

## Backlog — App & Validation Layer

| ID | Prompt | Waits On |
|----|--------|----------|
| `f0f7b` | AppBundlingAndFirstLaunchInit | `8fc8a` |
| `c1c86` | ValidationAndFourTierConfidenceTesting | `22ffd`, `827e2`, `7f1bd`, `f0f7b`, `ac7d4`, `f2580`, `d421f`, `bb62a`, `a242d` |
| `9328d` | UIKnowledgeInboxQualityGatePipeline | `ac7d4`, `79c1d` |

---

## Trash

| ID | Prompt | Notes |
|----|--------|-------|
| `72899` | ProseChunkDataModelAndSemanticChunker | First attempt, replaced by `239cf` |

---

## Dependency Graph

```
                        ┌─────────────────────────────────┐
                        │  Knowledge Content (9 tasks)    │
                        │  a242d  d421f  90618  47722     │
                        │  bb62a  efa99  53d86  f2580     │
                        │  ac7d4                          │
                        └───────────────┬─────────────────┘
                                        │ (all wait on)
                                        ▼
                        ┌─────────────────────────────────┐
                        │  79c1d TripleRetrievalMiniSearch │
                        │  BM25VectorMetadata             │
                        └──┬──────────┬──────────┬────────┘
                           │          │          │
              ┌────────────┘          │          └────────────┐
              ▼                       ▼                       ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│  af443 ContextBridge │  │  a6ba9 IntentClassif │  │  8fc8a BuildPipeline │
└──────────┬───────────┘  └──────────┬───────────┘  └──────────┬───────────┘
           │                         │                         │
           └──────────┬──────────────┘                         │
                      ▼                                        ▼
      ┌────────────────────────────────┐          ┌────────────────────────┐
      │  4f632 UISidePanel             │          │  f0f7b AppBundling     │
      │  c4507 UIDynamicSymptomChecker │          └───────────┬────────────┘
      └───────────────┬────────────────┘                      │
                      ▼                                       │
      ┌────────────────────────────────┐                      │
      │  22ffd LivestockModule         │                      │
      │  827e2 CropModule              │                      │
      │  7f1bd CheeseFeedModule        │                      │
      └───────────────┬────────────────┘                      │
                      │                                       │
                      └───────────────┬───────────────────────┘
                                      ▼
                        ┌─────────────────────────────────┐
                        │  c1c86 ValidationAndFourTier    │
                        │  9328d UIKnowledgeInbox          │
                        └─────────────────────────────────┘

★ 239cf (IN PROGRESS) — 14 backlog tasks link to it directly
```

---

## Parallelization Plan

### Phase 0 (Current)
- `239cf` — **ProseChunkDataModelAndSemanticChunker** ← foundation

### Phase 1 (Ready after `239cf`) — 9 parallel tasks
- `a242d` DiseaseDiagnosticsAndSymptoms
- `d421f` NutritionAndFeedAndBreeding
- `90618` MedicinesFormularyAndDrugs
- `47722` MedicinesAndCheese
- `bb62a` PlantingAndGrowthAndGestation
- `efa99` CropsAndWeather
- `53d86` LivestockGoatsAndSheep
- `f2580` VaccinationAndNotifiable
- `ac7d4` WithdrawalAndDose

### Phase 2 (After Phase 1)
- `79c1d` **TripleRetrievalMiniSearchBM25VectorMetadata**
- `8fc8a` **BuildPipelineProseKnowledgeCompiler**

### Phase 3 (After `79c1d`) — 3 parallel tasks
- `af443` ContextBridgeAndConflictDetection
- `a6ba9` IntentClassifierAndConsistencyChecker
- `f0f7b` AppBundlingAndFirstLaunchInit

### Phase 4 (After Phase 3) — 2 parallel tasks
- `4f632` UISidePanelAskBrowseSaved
- `c4507` UIDynamicSymptomCheckerAndAnswerDisplay

### Phase 5 (After Phase 4) — 3 parallel tasks
- `22ffd` LivestockModuleMaarifaIntegration
- `827e2` CropModuleMaarifaIntegration
- `7f1bd` CheeseFeedModuleMaarifaIntegration

### Phase 6 (Final)
- `c1c86` ValidationAndFourTierConfidenceTesting
- `9328d` UIKnowledgeInboxQualityGatePipeline

---

## Linked Dependencies to `239cf` (In Progress)

These 14 backlog tasks depend on `239cf`:

| Dep ID | Task | Prompt |
|--------|------|--------|
| `5d514644` | `bb62a` | PlantingAndGrowthAndGestation |
| `1de1eae6` | `f2580` | VaccinationAndNotifiable |
| `e97d7103` | `ac7d4` | WithdrawalAndDose |
| `b5bd66b7` | `efa99` | CropsAndWeather |
| `1d09f8b5` | `53d86` | LivestockGoatsAndSheep |
| `19ab3674` | `47722` | MedicinesAndCheese |
| `5d58cf2a` | `d421f` | NutritionAndFeedAndBreeding |
| `45de4c99` | `af443` | ContextBridgeAndConflictDetection |
| `da1dbade` | `a242d` | DiseaseDiagnosticsAndSymptoms |
| `fdfdeb6f` | `9328d` | UIKnowledgeInboxQualityGatePipeline |
| `bf4401d5` | `8fc8a` | BuildPipelineProseKnowledgeCompiler |
| `d6e3df0c` | `a6ba9` | IntentClassifierAndConsistencyChecker |
| `ed130855` | `90618` | MedicinesFormularyAndDrugs |

