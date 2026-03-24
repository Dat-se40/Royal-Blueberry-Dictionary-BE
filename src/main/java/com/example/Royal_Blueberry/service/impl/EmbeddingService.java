package com.example.Royal_Blueberry.service.impl;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {
    private static final String MODEL_DIR_ENV = "EMBEDDING_MODEL_DIR";
    private static final Path DEFAULT_MODEL_DIR = Paths.get("/app/models");

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;
    private boolean isInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws Exception {
        try {
            log.info("[EmbeddingService] Loading ONNX model...");
            long startTime = System.currentTimeMillis();

            log.info("[EmbeddingService] Creating OrtEnvironment...");
            env = OrtEnvironment.getEnvironment();

            Path modelPath = resolveModelPath("model.onnx");
            if (modelPath != null) {
                log.info("[EmbeddingService] Loading model.onnx from filesystem: {}", modelPath);
                session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
            } else {
                log.info("[EmbeddingService] Reading model.onnx from classpath...");
                try (InputStream is = getClass().getResourceAsStream("/models/model.onnx")) {
                    if (is == null) {
                        throw new RuntimeException("model.onnx not found in filesystem or classpath!");
                    }
                    byte[] modelBytes = is.readAllBytes();
                    log.info("[EmbeddingService] model.onnx size: {} bytes", modelBytes.length);
                    session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                }
            }

            log.info("[EmbeddingService] Loading tokenizer.json...");
            Path tokenizerPath = resolveModelPath("tokenizer.json");
            if (tokenizerPath == null) {
                tokenizerPath = copyClasspathResourceToTempFile("/models/tokenizer.json", "tokenizer", ".json");
            }
            log.info("[EmbeddingService] Tokenizer path: {}", tokenizerPath);
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);

            isInitialized = true;
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[EmbeddingService] ONNX model loaded successfully in {}ms", elapsed);

        } catch (Exception e) {
            log.error("[EmbeddingService] Failed to initialize ONNX model", e);
            throw new RuntimeException("Failed to initialize EmbeddingService: " + e.getMessage(), e);
        }
    }

    private Path resolveModelPath(String fileName) {
        String configuredDir = System.getenv(MODEL_DIR_ENV);
        if (configuredDir != null && !configuredDir.isBlank()) {
            Path configuredPath = Paths.get(configuredDir).resolve(fileName);
            if (Files.isRegularFile(configuredPath)) {
                return configuredPath;
            }
        }

        Path defaultPath = DEFAULT_MODEL_DIR.resolve(fileName);
        if (Files.isRegularFile(defaultPath)) {
            return defaultPath;
        }

        return null;
    }

    private Path copyClasspathResourceToTempFile(String resourcePath, String prefix, String suffix) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException(resourcePath + " not found in filesystem or classpath!");
            }
            Path tempFile = Files.createTempFile(prefix, suffix);
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
    }

    public float[] embed(String text) {
        if (!isInitialized) {
            log.warn("[EmbeddingService] Not initialized, returning zero vector");
            return new float[384];
        }

        try {
            Encoding encoding = tokenizer.encode(text);

            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds = encoding.getTypeIds();

            long[] shape = {1, inputIds.length};

            Map<String, OnnxTensor> inputs = Map.of(
                    "input_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape),
                    "attention_mask", OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape),
                    "token_type_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape)
            );

            try (OrtSession.Result result = session.run(inputs)) {
                float[][][] tokenEmbeddings = (float[][][]) result.get(0).getValue();
                return meanPooling(tokenEmbeddings[0], attentionMask);
            }

        } catch (Exception e) {
            log.error("[EmbeddingService] Embed error: {}", e.getMessage(), e);
            return new float[384];
        }
    }

    private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
        int seqLen = tokenEmbeddings.length;
        int dims = tokenEmbeddings[0].length;
        float[] result = new float[dims];
        float maskSum = 0;

        for (int i = 0; i < seqLen; i++) {
            float m = attentionMask[i];
            maskSum += m;
            for (int j = 0; j < dims; j++) {
                result[j] += tokenEmbeddings[i][j] * m;
            }
        }

        float norm = 0;
        for (int j = 0; j < dims; j++) {
            result[j] /= Math.max(maskSum, 1e-9f);
            norm += result[j] * result[j];
        }
        norm = (float) Math.sqrt(norm);
        for (int j = 0; j < dims; j++) {
            result[j] /= norm;
        }

        return result;
    }
}
