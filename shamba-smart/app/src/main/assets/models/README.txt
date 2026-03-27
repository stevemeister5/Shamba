ONNX Model for Maarifa Knowledge Engine

Place the all-MiniLM-L6-v2 model in this directory:
- File: all_minilm_l6_v2.onnx
- Size: ~23MB
- Dimensions: 384
- Source: HuggingFace sentence-transformers/all-MiniLM-L6-v2
- Converted to ONNX format using optimum library

To obtain the model:
1. pip install optimum[onnxruntime] sentence-transformers
2. optimum-cli export onnx --model sentence-transformers/all-MiniLM-L6-v2 onnx_model/
3. Copy model.onnx to this directory as all_minilm_l6_v2.onnx

If the model is not available, Maarifa will operate in BM25-only mode (no vector search).
All other features (intent classification, rule engine, context bridge, symptom checker) work without the model.