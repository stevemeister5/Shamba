🧠 Maarifa — Schema-Free Knowledge Architecture

THE CORE INSIGHT
The schemas were solving the wrong problem. They were trying to make knowledge machine-readable at storage time. But the only thing that actually needs to be machine-readable at query time is:

What domain does this chunk belong to? (for scoping searches)
What is the source? (for citation)
What are the key terms in this chunk? (for keyword search)
What does this chunk mean semantically? (for vector search)

Everything else — the internal structure of the knowledge, the fields, the relationships between concepts — should live in the text itself, written in clear English prose. The retrieval system finds relevant prose chunks. The rule engine operates on a small separate layer of explicit operational rules (withdrawal periods, dose calculations, planting date windows). The knowledge content itself is just well-written text.
This means you can add pigs tomorrow. Or rabbits. Or aquaculture. Or coffee farming. Or beekeeping. Without touching a single schema, without a developer, without rebuilding the pipeline. You write the knowledge as text, import it, and it is searchable immediately.

THE REVISED ARCHITECTURE
Replace the rigid domain JSON schemas with three clean layers:
Layer 1 — PROSE KNOWLEDGE STORE
         Plain English text chunks covering any topic.
         No schema. No required fields.
         Organised only by topic tags (free-form, user-defined)
         and source metadata.
         Stored as chunks in SQLite with full-text index
         and vector embeddings.
         Infinitely extensible — add any topic by adding text.

Layer 2 — OPERATIONAL RULES STORE
         A small, separate, explicitly structured store
         for the things that genuinely need to be
         machine-executable:
         - Drug withdrawal period calculations
         - Weight-based dose calculations
         - Planting date windows by crop and season
         - Growth stage day counts from planting date
         - Vaccination due date calculations
         - Kidding/lambing date projections
         - Feed stock depletion rate calculations
         These rules are structured because the app needs
         to compute with them, not just retrieve them.
         New rules can be added as simple key-value or
         if-then entries without changing any schema.

Layer 3 — LIVE FARM DATA
         The app's own SQLite database of animals,
         plots, records, treatments, harvests.
         Unchanged from the original design.
         The context bridge reads this and injects
         it into query responses.

WHAT THE PROSE KNOWLEDGE STORE LOOKS LIKE
Every piece of knowledge — whether about goats, maize, cheese, pigs, rabbits, or Tanzanian tax law for farms — is stored as a text chunk with a small metadata envelope:
json{
  "chunk_id": "chunk_004821",
  "text": "African Swine Fever (ASF) is a highly contagious
           viral disease of pigs caused by a large DNA virus
           of the Asfarviridae family. There is no vaccine
           and no treatment. Clinical signs include high fever
           above 40.5°C, loss of appetite, reddening of the
           skin particularly on the ears and snout, vomiting,
           diarrhoea which may be bloody, and sudden death.
           Mortality can reach 100% in naive pig populations.
           ASF is a notifiable disease in Tanzania — any
           suspected case must be reported immediately to
           TVLA. Affected animals must not be moved or
           slaughtered for food. Depopulation of the affected
           herd is the standard control response.",
  "source_title": "TVLA Swine Disease Reference 2023",
  "source_type": "bundled",
  "topic_tags": ["pigs", "disease", "viral", "notifiable", "ASF"],
  "date_added": "2024-01-15",
  "chunk_index": 3,
  "total_chunks_in_source": 12,
  "vector": [0.021, -0.143, 0.387, ...]
}
That is it. No pig-specific schema. No predefined fields for pig diseases. Just text, tags, source, and a vector. The same structure holds a chunk about maize fall armyworm, a chunk about goat milk fever, a chunk about chèvre cheese defects, a chunk about pig housing ventilation, or a chunk from a coffee farming manual someone imports next year.

WHAT THE OPERATIONAL RULES STORE LOOKS LIKE
This is the only part that remains explicitly structured, because the app genuinely needs to compute with it — not just retrieve it:
json{
  "rule_id": "withdrawal_oxytet_la_goat_milk",
  "rule_type": "withdrawal_period",
  "species": ["goat", "sheep"],
  "drug_generic": "oxytetracycline_long_acting",
  "drug_brands": ["Alamycin LA", "Terramycin LA"],
  "milk_withdrawal_days": 7,
  "meat_withdrawal_days": 28,
  "conservative_buffer_days": 2
}

{
  "rule_id": "dose_ivermectin_goat",
  "rule_type": "dose_calculation",
  "species": ["goat", "sheep"],
  "drug_generic": "ivermectin",
  "mg_per_kg": 0.2,
  "concentration_mg_per_ml": 10,
  "ml_per_kg": 0.02,
  "route": "SC",
  "max_dose_ml": 5
}

{
  "rule_id": "planting_window_maize_korogwe",
  "rule_type": "planting_window",
  "crop": "maize",
  "location": "korogwe",
  "season": "long_rains",
  "optimal_start_month": 3,
  "optimal_end_month": 4,
  "earliest_month": 3,
  "latest_month": 4
}

{
  "rule_id": "gestation_goat",
  "rule_type": "gestation",
  "species": "goat",
  "gestation_days": 150,
  "pre_kidding_task_days": 7
}
```

These rules are small, simple, and additive. Adding pigs requires adding pig-specific rules — gestation length for sows, dose rules for pig-approved drugs, withdrawal periods. No existing rules are touched. The new rules simply exist alongside the old ones.

Adding a new rule type that has never existed before — say a honey harvest calendar for bees — requires adding one new `rule_type` string and a handler for it in the rule engine. That is a small, isolated code change rather than a schema migration.

---

### HOW THIS CHANGES THE PRE-SHIP BUILD PROCESS

Instead of populating complex nested JSON schemas, the knowledge compiler now does this:

**For every topic**, write clear, accurate, well-organised English prose. Structure it the way a good textbook is structured — short paragraphs, one concept per paragraph, concrete and specific. Each paragraph or pair of paragraphs becomes one chunk.

A knowledge compiler working on pigs would write text like this, organised into a simple document:
```
DOCUMENT: Pig Production Guide — Smallholder Context, Tanzania
TAGS: pigs, livestock, production

--- CHUNK ---
Pig breeds commonly kept by smallholders in Tanzania include the 
local landrace, Large White, Landrace, and Duroc crosses. Local 
pigs are hardy and disease-resistant but grow slowly, reaching 
market weight of 60kg in 12 to 18 months. Exotic and crossbred 
pigs grow faster, reaching 80 to 100kg in 6 to 8 months under 
good management, but require higher quality feed and are more 
susceptible to heat stress and disease.

--- CHUNK ---
Housing for pigs should provide shade, ventilation, and drainage. 
Minimum floor space is 1.5 square metres per adult pig. Floors 
should be concrete with a 2% slope toward a drainage channel. 
Wallowing pools or regular water spraying help pigs regulate 
body temperature in hot climates like Korogwe. Separate pens 
for boars, sows with piglets, weaners, and growers prevent 
fighting and disease spread.

--- CHUNK ---
African Swine Fever (ASF) is a highly contagious viral disease...
[continues as above]
This plain text document is then imported through the same ingestion pipeline as any farmer-added document — chunked, keyword indexed, vectorised, tagged. The only difference between bundled knowledge and farmer-ingested knowledge is that bundled knowledge is processed at build time and shipped with the app, while ingested knowledge is processed at runtime on the tablet.

HOW TO ADD PIGS — CONCRETELY
Knowledge content: Write or compile a pig production guide as a plain text or PDF document covering breeds, housing, feeding, common diseases, vaccination schedule, reproduction, and marketing. Import it through the build pipeline at build time (bundled) or through the Knowledge Inbox at any time (farmer-ingested). Tag it with pigs. Done.
Operational rules: Add pig-specific entries to the rules store:

Gestation length (114 days for sows)
Dose rules for pig-approved drugs (ivermectin, oxytetracycline — note the species field already supports arrays, so just add "pig" to existing drug entries where applicable)
ASF notifiable disease rule so the symptom checker flags it correctly
Pig vaccination schedule rules for diseases like Erysipelas and Parvovirus

Symptom checker: The symptom checker does not need a pig-specific schema. The body systems (respiratory, digestive, reproductive, skin, general) apply to pigs exactly as they do to goats and sheep. The diagnostic rules for pig diseases are added as new rule entries in diagnostic_rules.json — same structure as existing rules, just with "species": "pig".
UI: The species selector in the symptom checker adds Pig as an option. The animal register allows Pig as a species. That is the only UI change needed.
Total work to add pigs after shipping: write the knowledge content, add ~10 operational rules, add Pig to the species selector. No schema changes. No pipeline redesign. No breaking changes to any existing feature.

HOW EXTENSIBLE THIS ACTUALLY IS
With this architecture, Maarifa can absorb any of the following without architectural changes — only content and rule additions:
New topicWhat you addPigsProse knowledge + pig rules + pig species optionRabbitsProse knowledge + rabbit rules + rabbit species optionPoultry (chickens, ducks)Prose knowledge + poultry rules + poultry species optionFish farming / aquacultureProse knowledge + tagged fishCoffee farmingProse knowledge + tagged coffee, cash_cropBeekeepingProse knowledge + honey harvest rulesBiogas from manureProse knowledge + tagged energy, biogasFarm business planningProse knowledge + tagged finance, planningTFDA regulatory updatesNew prose chunk replacing old one, same source tagNew drug registered in TanzaniaNew operational rule entry + prose chunkNew crop variety released by TARINew prose chunk in existing maize or bean knowledgeOrganic certification requirementsProse knowledge + tagged certification

Deep Thinking First

PROBLEM 1 — VECTOR SEARCH ALONE IS UNRELIABLE
Vector similarity finds semantically related content but has well-documented failure modes. A question about pig ASF symptoms might return chunks about goat PPR because the semantic similarity between two haemorrhagic fever descriptions is high even though the species are completely different. A dosage question might return a chunk that mentions the drug name in a contraindication context rather than a dosage context. Pure vector search optimises for similarity, not correctness.
Solution: Never use vector search alone. Every query runs three retrieval methods in parallel and the results are merged using a weighted scoring formula:
Final Score = (0.4 × BM25 keyword score)
            + (0.4 × vector cosine similarity)
            + (0.2 × metadata match score)

Metadata match score is boosted by:
- Exact species match in topic_tags: +0.3
- Exact crop match in topic_tags: +0.3
- Intent alignment with chunk domain: +0.2
- Source type bundled vs ingested: +0.1 (bundled slightly preferred
  for medical and dosage content as it has been verified)
- Recency of source: small boost for newer sources
This triple-retrieval with weighted fusion means a chunk has to score well on keyword relevance, semantic meaning, AND metadata alignment to rank highly. A chunk about PPR in goats will score lower than a chunk about ASF in pigs when the query is about pigs, even if the disease descriptions are semantically similar, because the species metadata match pulls the pig chunk ahead.

PROBLEM 2 — CHUNKING DESTROYS CONTEXT
A 400-word chunk from the middle of a disease description might start mid-sentence or omit the disease name entirely because the name appeared in the previous chunk. When retrieved in isolation it is confusing or incomplete. Standard fixed-size chunking is naive and produces poor retrieval quality for structured agricultural knowledge.
Solution: Use semantic chunking rather than fixed word-count chunking. The chunker respects natural document boundaries:
Chunking hierarchy (in order of preference):
1. If document has clear section headers → chunk at section boundaries
2. If section is longer than 600 words → split at paragraph boundaries
3. If paragraph is longer than 300 words → split at sentence boundaries
4. Never split mid-sentence under any circumstances

Every chunk gets a context prefix prepended before embedding
(but not stored in the display text):
"[Source: TVLA Swine Disease Reference 2023]
 [Tags: pigs, disease, ASF, notifiable]
 [Section: African Swine Fever]
 [Content follows:]
 African Swine Fever is a highly contagious..."

The context prefix is used only during embedding generation
so the vector captures the full context of the chunk.
The stored display text is the clean chunk without the prefix.
Additionally every chunk stores a context_window field containing the last 50 words of the previous chunk and the first 50 words of the next chunk. When a chunk is retrieved and displayed, the context window is shown in a collapsed, visually muted section labelled "Context" so the farmer can expand it if the chunk feels incomplete.

PROBLEM 3 — QUERY AMBIGUITY
Short queries are ambiguous. "My goat is not eating" could be a digestive question, a respiratory question (secondary anorexia), a reproductive question (post-kidding), a toxicity question, or a nutritional question. A single intent classification produces the wrong retrieval scope for ambiguous queries and returns confidently wrong answers.
Solution: The intent classifier produces a ranked list of intents with confidence scores, not a single intent. If the top intent confidence is below 0.7, the engine runs retrieval for the top two intents in parallel, merges results, and presents the answer with a clear disambiguation note. Additionally, the entity extractor always checks whether any named animal or plot exists in the live farm database — if it does, that animal's full context is injected before retrieval, which often resolves the ambiguity automatically.
Query: "my goat is not eating"

Intent classification result:
  symptom_query: 0.82 (dominant — proceed with this)
  nutrition_query: 0.31 (secondary — run in parallel)
  
Entity extraction:
  species: goat (explicit)
  animal_id: none mentioned
  
Context injection:
  No specific animal named.
  Herd-level context injected:
  - Current season: late dry season (potential feed stress)
  - Any animals currently on treatment: yes, G-14 on oxytetracycline
  - Recent health events in herd: respiratory case 5 days ago
  
This context shifts the retrieval weighting toward
respiratory-secondary-anorexia and feed-stress chunks
before a single retrieval call is made.

PROBLEM 4 — ANSWER HALLUCINATION FROM RETRIEVED CHUNKS
Even without a language model, a naive system can assemble misleading answers by concatenating retrieved chunks that are individually accurate but contradict each other or apply to different species or conditions. Two chunks retrieved together might give conflicting drug dosages for the same drug because one is for cattle and one is for goats.
Solution: Before assembling the final answer, run a consistency checker across all retrieved chunks:
Consistency checks applied to retrieved chunk set:
1. Species consistency — if query is about goats, flag any chunk
   tagged for a different species and demote it or exclude it
2. Contradiction detection — if two chunks contain dose figures
   for the same drug that differ by more than 20%, do not
   merge them into one answer. Display them separately with
   their sources labelled and add a note: "Dosage figures
   differ between sources — verify with a veterinarian"
3. Temporal consistency — if two chunks address the same topic
   but have different last_verified dates, prefer the more
   recent chunk and note the older one as superseded
4. Notifiable disease check — scan all retrieved chunks for
   any of the 12 Tanzania notifiable diseases regardless of
   query intent. If found, always surface the TVLA alert
   even if the farmer did not ask about notifiability

PROBLEM 5 — RETRIEVAL DEGRADES AS KNOWLEDGE BASE GROWS
When the farmer has imported 50 documents over two years, the vector database contains tens of thousands of chunks. A naive cosine similarity search across all chunks becomes slow and noisy — the signal-to-noise ratio drops as more tangentially related chunks are retrieved.
Solution: Two-stage retrieval with a fast pre-filter stage:
Stage 1 — PRE-FILTER (fast, eliminates 90%+ of chunks)
  Apply metadata filters before any vector computation:
  - If species extracted → keep only chunks tagged with that species
    or tagged as general/multi-species
  - If crop extracted → keep only chunks tagged with that crop
  - If intent is dosage_lookup → keep only chunks tagged
    with medicines or the specific drug name
  - If intent is planting_advice → keep only chunks tagged
    with crops or the specific crop
  This reduces the search space from 50,000 chunks to
  typically 200 to 800 chunks before vector search runs.

Stage 2 — RANKED RETRIEVAL (precise, runs on filtered set only)
  Run BM25 + vector cosine on the filtered set.
  Return top 8 chunks by weighted fusion score.
  Apply consistency checker.
  Assemble answer.
This keeps retrieval fast (under 200ms on mid-range tablet hardware) regardless of how large the knowledge base grows.

PROBLEM 6 — OPERATIONAL RULES DRIFT FROM KNOWLEDGE CONTENT
If the prose knowledge store says oxytetracycline milk withdrawal is 7 days but someone later imports a newer FAO document that mentions 5 days, the operational rules store still says 7 days. The rules store and the prose store can diverge, producing contradictory outputs where the withdrawal tracker says one thing and the retrieved text says another.
Solution: Every operational rule entry has a content_reference field that points to the chunk IDs of the prose chunks that define that rule. When a new document is ingested that contains a conflicting value for a rule-governed parameter (withdrawal period, dose, gestation length), the ingestion pipeline detects the conflict and flags it for review rather than silently diverging:
Conflict detection at ingestion time:
1. New chunk ingested containing "oxytetracycline" + "milk" + a number
2. System checks: does an operational rule exist for this drug + parameter?
3. Yes — rule says 7 days. New chunk says 5 days.
4. System flags: "Conflict detected — new document suggests a different
   milk withdrawal period for oxytetracycline than the current rule.
   Review in Knowledge Management before this takes effect on
   withdrawal calculations."
5. Farmer or administrator reviews and either updates the rule
   or dismisses the conflict.
6. Until resolved, the operational rule (7 days) governs calculations.
   The new chunk is still searchable and will appear in answers
   with its source labelled, but the withdrawal tracker uses
   the verified rule value.

PROBLEM 7 — THE SYMPTOM CHECKER IS BRITTLE FOR NOVEL SPECIES
The symptom checker wizard was designed around the body systems and symptom vocabulary of goats and sheep. Adding pigs or poultry means some symptoms are the same (reduced appetite, lethargy) but many are species-specific (pigs: prolapsed rectum, wasting disease; poultry: dropped wings, swollen sinuses). A single fixed symptom list fails both by showing irrelevant symptoms and by missing relevant ones.
Solution: The symptom checker is not a hardcoded wizard. It is dynamically built from the knowledge base at runtime. When a species is selected, the engine queries the diagnostic rules store for all rules tagged with that species and builds the symptom list dynamically from the trigger_symptoms fields of those rules. If no diagnostic rules exist for a species yet, the symptom checker shows a graceful fallback: general body system selection only, with retrieval-based (not rule-based) differential generation. This means the symptom checker works for any species from the moment knowledge is added — no code changes required.

PROBLEM 8 — TRUST AND TRANSPARENCY
A farmer making a treatment decision based on Maarifa's output needs to know where that information came from, how confident the system is, and what to do when the system is uncertain. A black box that returns confident-sounding answers without provenance is dangerous in a veterinary and agronomic context.
Solution: Every answer component is source-tagged at the chunk level, not just at the answer level. The answer display shows exactly which part of the answer came from which source:
Answer display structure:

SUMMARY
"Possible causes of reduced appetite in your goat include
respiratory infection, early digestive upset, or feed-related
stress given the current dry season conditions on your farm."
[Source: herd context — dry season flag, G-14 health record]

MOST LIKELY CAUSE
"Contagious Caprine Pleuropneumonia (CCPP) — High confidence
match based on species, symptom combination, and current season."
[Source: ILRI Small Ruminant Health Manual 2021, Chapter 4]

TREATMENT GUIDANCE
"First-line treatment: Oxytetracycline Long Acting,
20mg/kg body weight by IM injection..."
[Source: Operational Rules Store — verified Jan 2024]
[Withdrawal: milk 7 days, meat 28 days from last injection]

FARM-SPECIFIC NOTE
"G-14 is currently within the oxytetracycline withdrawal
period from her treatment on March 20. Her milk is not
safe for cheese until March 28."
[Source: live treatment record — G-14]

⚠ NOTIFIABLE DISEASE ALERT
"CCPP is a notifiable disease in Tanzania.
Contact TVLA or your district veterinary officer immediately."
[Source: TVLA Notifiable Disease List 2023]

CONFIDENCE: High — 3 of 3 retrieval methods agree on top result
SOURCES USED: 3 bundled, 0 ingested
RELATED TOPICS: [Vaccination schedule for CCPP] [Isolating sick animals] [Herd biosecurity after CCPP]
Every confidence level is computed, not asserted. High means all three retrieval methods returned the same top result. Medium means two of three agreed. Low means the methods diverged and the top result won by margin only.

PROBLEM 9 — INGESTED DOCUMENT QUALITY CONTROL
If the farmer imports a poorly written document, a document in Swahili, an Excel file disguised as a PDF, or a document with completely wrong information (incorrect drug doses from an unreliable internet printout), that bad content enters the knowledge base and potentially surfaces in answers alongside verified bundled knowledge.
Solution: A multi-stage quality gate at ingestion time:
Quality gates applied to every ingested document:

1. FORMAT CHECK
   Confirm text was successfully extracted (minimum 100 words).
   If extraction yields less than 100 words, flag as
   "Document appears to be image-based or empty — cannot index."

2. LANGUAGE CHECK
   Run a lightweight language detection check (langdetect
   library, ~500KB). If document is not English, flag:
   "Document appears to be in [language]. Maarifa is English
   only. Non-English content will produce unreliable results."
   Allow the user to proceed anyway or cancel.

3. DOMAIN RELEVANCE CHECK
   After chunking, run a quick similarity check between
   the document's first 3 chunks and the centroid vectors
   of each knowledge domain. If the document scores below
   0.2 similarity to all domains, flag:
   "This document does not appear to contain agricultural
   content. It will still be indexed but may not surface
   in farm-related queries."

4. CONFLICT DETECTION
   As described in Problem 6 — flag any numerical conflicts
   with existing operational rules.

5. SOURCE CREDIBILITY LABEL
   Ask the user to select the source type:
   - Government / research institution (TARI, FAO, ILRI, TVLA)
   - Veterinary / agricultural supplier (product data sheet)
   - Academic publication (journal, thesis)
   - Extension bulletin (district, NGO)
   - General reference (book, manual)
   - Unknown
   This label is stored with every chunk and displayed
   in answers. Bundled knowledge is always labelled as
   "Verified bundled source." Ingested knowledge always
   shows its source type so the farmer can judge credibility.

6. CRITICAL PARAMETER PROMINENCE
   After ingestion, the system scans all new chunks for
   drug names, dose figures, and withdrawal periods.
   Any chunk containing these is tagged with a
   medical_content: true flag and answers drawn from
   these chunks always append the veterinarian confirmation
   disclaimer regardless of query type.

PROBLEM 10 — THE RULE ENGINE CANNOT COVER NOVEL SITUATIONS
The operational rules store handles known, computable situations. But a first-time pig farmer asking about optimal pig fattening rations has no rules to invoke — only retrieved prose. The system must degrade gracefully from rule-governed precision to retrieval-based guidance without the user experiencing a cliff edge in answer quality.
Solution: Define three answer tiers and let the system communicate which tier it is operating in:
Tier 1 — RULE-GOVERNED (highest reliability)
  Operational rule exists for this query.
  Answer is computed precisely.
  Example: drug withdrawal date, kidding date projection,
  fertiliser quantity for plot size.
  Display indicator: green — "Calculated from verified rule"

Tier 2 — RETRIEVAL-CONFIRMED (high reliability)
  No operational rule, but 3+ high-scoring chunks
  retrieved with consistent content.
  Answer assembled from retrieved prose.
  Example: disease treatment protocol, cheese process guide,
  pig housing standards.
  Display indicator: blue — "Based on [N] sources"

Tier 3 — RETRIEVAL-PARTIAL (moderate reliability)
  1 or 2 chunks retrieved, or chunks have low scores,
  or chunks are from ingested (unverified) sources only.
  Answer presented as guidance only with strong caveat.
  Example: novel topic with limited coverage, very specific
  local variety question with no matching knowledge.
  Display indicator: amber — "Limited sources — verify
  with an extension officer or specialist"

Tier 4 — NOT FOUND (honest failure)
  No relevant chunks retrieved above minimum threshold.
  System does not guess. Returns:
  "Maarifa does not have reliable information on this topic.
  Consider importing a relevant document via the Knowledge
  Inbox, or consult your district agricultural extension
  officer."
  Display indicator: grey — "Not in knowledge base"

Now, with all ten problems solved, here is the complete final prompt: