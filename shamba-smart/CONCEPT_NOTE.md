# Farming Without the Cloud: On-Device AI for Resilient Smallholder Agriculture

**Concept Note — M.Sc. in Data Science and Artificial Intelligence**
Candidate: [Your Name] &nbsp;|&nbsp; Programme &amp; University: [e.g., M.Sc. Data Science and AI, *University*]

---

## 1. Problem Statement

Smallholder farmers across East Africa face compounding pressures: invasive fall armyworm and desert-locust outbreaks, erratic rainfall, and endemic livestock disease. Digital decision-support tools could help, yet nearly all agricultural AI today is **cloud-based**: it assumes stable internet connectivity and recurring subscriptions [6]. Support breaks precisely when it matters most — in the field, during an outbreak, at harvest — in the very areas where connectivity is weakest and affordability lowest. It also forces farm and herd data off the farmer's own device. Earlier research confirmed both the promise and the fragility of this model: deep learning reached **99.35% accuracy** on controlled images of plant disease but fell to **31.4%** on realistic field images [2] — evidence that deployment conditions, not merely model accuracy, decide real-world value. A gap therefore remains: an integrated, **fully on-device and offline** study of agricultural AI that matches a cloud service in functionality while exceeding it in reliability, privacy, and operating cost.

## 2. Objectives

1. **Design and evaluate lightweight edge-first models** (computer vision and natural-language retrieval) that run entirely on a low-cost Android tablet — **wholly offline, subscription-free, and cloud-independent.**
2. **Deliver accurate, field-ready support** for pest detection (YOLOv8), crop-quality grading, and livestock and agronomy knowledge retrieval, at the point of need.
3. **Demonstrate reliability and privacy advantages** over a cloud baseline under realistic offline conditions (no signal, packet loss, high latency).
4. **Optimize for constrained hardware** — quantization (INT4/INT8), pruning, NNAPI acceleration — balancing accuracy against latency and memory.
5. **Publish benchmark results and a reproducible methodology** usable by researchers and deployers of edge AI in low-income regions.

## 3. Research Context and Related Work

- **Mobile diagnostics.** The PlantVillage vision (Hughes &amp; Salathé [1]) and its deep-learning realisation (Mohanty et al. [2]) established smartphone-assisted plant-health diagnosis, but exposed a large **lab-to-field accuracy gap** (99.35% → 31.4%) — the central problem this research addresses.
- **Efficient architectures.** MobileNet [3], MobileNetV2 [4] and EfficientNet [5] supply the efficiency toolkit for constrained devices; recent TinyML work (Lin et al. [9]) charts the compression and hardware trends that make full edge deployment practical.
- **Edge intelligence.** Xu et al. [6] situate inference at the network edge to cut latency, bandwidth, cost and privacy exposure — the systems argument this project operationalises.
- **On-device retrieval.** The retrieval-augmented paradigm (Lewis et al. [7]) motivates grounding answers in a trusted local knowledge base; this project runs an all-MiniLM-L6-v2 embedding model entirely on-device with a BM25 fallback.
- **Data realism.** The PlantDoc dataset (Singh et al. [8]) shows realistic field imagery is decisive for deployable accuracy, guiding the data-collection strategy.

**Research gap.** Existing edge-agriculture work targets a single task in isolation and frequently still depends on a network link. This project advances the state of the art by combining pest detection, produce grading and knowledge retrieval in one **integrated, offline-only** stack, benchmarked end-to-end under realistic rural conditions.

## 4. Proposed Methodology

The research is grounded in *Shamba Smart*, a working Kotlin/Android codebase that already merges the three on-device AI pipelines above. Four stages:

1. **Model engineering and compression.** Adapt the existing ONNX Runtime pipelines for embedded deployment: a YOLOv8 pest detector (640&times;640, INT4/INT8 quantised, NNAPI/GPU-accelerated, nine East-African pest classes), an HSV-based produce grader, and an embedding-based retriever. Baseline against MobileNetV2/EfficientNet backbones [4,5]; record accuracy–latency–memory trade-offs.
2. **Edge benchmarking.** Measure inference latency, memory footprint and accuracy on real ARM tablets; compare reliability against a cloud-API baseline under simulated offline and poor-connectivity scenarios (no signal, packet loss, high round-trip time) [6].
3. **Retrieval and reasoning quality.** Quantify top-k retrieval recall, mean average precision, BM25-fallback robustness, and rule-engine correctness over the bundled FAO/ILRI knowledge base (crops, livestock, medicines, weather) [7].
4. **Field evaluation.** Collect field-condition imagery (PlantDoc-style) and run a smallholder-farmer pilot; measure diagnostic agreement against agronomists (Cohen's kappa), plus interpretability, trust and usability [8].

**Intended outcome:** an empirically validated, reproducible design for subscription-free, offline agricultural AI, demonstrating that the expertise of agro-scientists and veterinarians can run wholly in the farmer's pocket — reliable, private, and permanently free.

## References

**[1]** D. P. Hughes and M. Salathé, "An open access repository of images on plant health to enable the development of mobile disease diagnostics," arXiv:1511.08060, 2015. *PlantVillage mobile-diagnostics dataset.*

**[2]** S. P. Mohanty, D. P. Hughes and M. Salathé, "Using Deep Learning for Image-Based Plant Disease Detection," *Frontiers in Plant Science*, vol. 7, 2016 (arXiv:1604.03169). *99.35% held-out accuracy on 54,306 controlled images; 31.4% on field-condition images.*

**[3]** A. G. Howard et al., "MobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications," arXiv:1704.04861, 2017.

**[4]** M. Sandler, A. Howard, M. Zhu, A. Zhmoginov and L.-C. Chen, "MobileNetV2: Inverted Residuals and Linear Bottlenecks," *CVPR 2018* (arXiv:1801.04381).

**[5]** M. Tan and Q. V. Le, "EfficientNet: Rethinking Model Scaling for Convolutional Neural Networks," *ICML 2019* (arXiv:1905.11946).

**[6]** D. Xu, T. Li, Y. Li, X. Su, S. Tarkoma and T. Jiang, "Edge Intelligence: Architectures, Challenges, and Applications," arXiv:2003.12172, 2020; *Proceedings of the IEEE*.

**[7]** P. Lewis et al., "Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks," *NeurIPS 2020* (arXiv:2005.11401).

**[8]** D. Singh, N. Jain, P. Jain, P. Kayal, S. Kumawat and N. Batra, "PlantDoc: A Dataset for Visual Plant Disease Detection," *CODS-COMAD 2020* (arXiv:1911.10317). *Field-condition dataset with the diagnostics-gap measurements this proposal builds on.*

**[9]** J. Lin, L. Zhu, W.-M. Chen, W.-C. Wang and S. Han, "Tiny Machine Learning: Progress and Futures," arXiv:2403.19076, 2024.