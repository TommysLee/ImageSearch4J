<h1 align="center">ImageSearch4J</h1>

<p align="center">
  <strong>Reverse image search in pure Java.</strong><br/>
  <strong>Put the AI in the foundation. Keep the business logic yours.</strong>
</p>

<p align="center">
  <a href="https://www.azul.com/downloads/#downloads-table-zulu"><img src="https://img.shields.io/badge/JDK-17%2B-orange.svg" alt="JDK17" /></a>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg" alt="Spring Boot" />
  <a href="https://github.com/deepjavalibrary/djl"><img src="https://img.shields.io/badge/deepjavalibrary%2Fdjll-0.38-blue.svg" alt="modbus-serial" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License" /></a>
  <img src="https://img.shields.io/badge/build-passing-brightgreen" alt="PRs Welcome" />
  <a href="CONTRIBUTING.md"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome" /></a>
</p>

---

English | [简体中文](./README_cn.md)

---

Image search on the JVM usually means one of two things. You call out to a Python service over HTTP, or you rewrite your product in Python. This is a third option.

Detection, feature extraction, and vector search all run inside the JVM, in the same process as your Spring Boot application. No Python, no sidecar, and no vector database to keep running.

If you know PP-ShiTu, you already know this project's shape. It's the same idea in Java: the same underlying models (PicoDet-LCNet for detection, PP-LCNetV2 for features), the same positioning, and the same zero-training property. PP-ShiTu runs on Python and PaddlePaddle; this runs on Java and DJL. Same job, two ecosystems.

---

## Contents

- [Why this exists](#why-this-exists)
- [What you get](#what-you-get)
- [No training required](#no-training-required)
- [Where it fits](#where-it-fits)
- [Screenshots](#screenshots)
- [How it works](#how-it-works)
- [Getting started](#getting-started)
- [API](#api)
- [Response format](#response-format)
- [Why ONNX, DJL, and Lucene](#why-onnx-djl-and-lucene)
- [Building on top](#building-on-top)
- [Configuration](#configuration)
- [Project layout](#project-layout)
- [FAQ](#faq)
- [Roadmap](#roadmap)
- [License and credits](#license-and-credits)

---

## Why this exists

Training a model and serving one are two different jobs, and they tend to want two different languages.

| Stage | What matters | What usually wins |
|---|---|---|
| Training | fast iteration, a large ecosystem, room to experiment | Python (PyTorch, TensorFlow, PaddlePaddle) |
| Serving | low latency, high concurrency, stability, easy integration | Java and the JVM |

"Train in Python, serve in Java" is already the default for plenty of teams, and for good reason. You get the strengths of both sides.

The problem is the Java half of that split. It usually means standing up a Python microservice and paying for it with cross-language serialization, two tech stacks, two deploy pipelines, and two sets of dashboards. The alternative is moving the whole product to Python, which is rarely what anyone actually wants.

ImageSearch4J is the third option. The AI gets folded into a single Java service. You keep the language and toolchain you already know, you don't have to think about how models load or how vectors get indexed, and you spend your time on the product instead.

---

## What you get

- **The whole pipeline runs in the JVM.** Detection, feature extraction, vector search. No Python, no sidecar, no cross-language calls.
- **No training required.** The models are general-purpose and your knowledge lives in the gallery. Adding a category means adding images, not retraining anything.
- **Models are bundled.** Both ONNX files (28 MB + 18 MB) ship inside the JAR and get unpacked to `~/models/` the first time you start.
- **Lucene is the vector store.** HNSW with scalar quantization, embedded in your app, backed by a local folder. There is no separate vector database in the picture.
- **SANMS**, a second pass over the detected boxes that fixes the case where a big box swallows a small one. More on this below.
- **One JAR and nothing to operate.** The index is a directory, the models are files, and there's no service to keep alive.
- **Designed to be extended.** The AI complexity sits behind a handful of services. You inject one, you call a method.
- **Two front-end pages included.** Static HTML and plain JavaScript, no framework. One for searching, one for managing the vector index.

---

## No training required

Image search is a retrieval task, not a classification task, and that distinction is where everything else follows from.

|                           | Classification                 | Retrieval (image search)                |
| ------------------------- | ------------------------------ | --------------------------------------- |
| Where the knowledge lives | in the model weights           | in the gallery                          |
| Adding a new category     | retrain: data, labels, compute | add a few images                        |
| The model's job           | be a classifier                | be a general "image to vector" function |

Because the knowledge sits in the gallery and not in the model, the model only has to do one thing: turn any image into a stable vector. A general pretrained model does that well, with no tuning against your data. That's why zero-shot works here.

Three things follow:

- **A new category means new images, not a new model.** No training, no labeling, no AI engineer in the loop. It's also what the vector update step replaces here, compared to the retraining you'd have to do elsewhere.
- **Whoever integrates it needs no ML background.** No loss functions, no learning rates, no GPU cluster. You bring images.
- **The Java side only ever does inference.** Training is heavy machinery: datasets, distributed jobs, hyperparameter search, GPU scheduling. Inference is light. Because there's no training to do, that whole class of complexity is gone, which is a large part of why the rest stays small.

This is also where the project lines up with PP-ShiTu: same models, same positioning, same zero-training property. Moving to a new domain usually means a new gallery, and nothing else.

---

## Where it fits

- **Product search.** A user photographs an item and you find the same item, or something close, in your catalog. Also handy for shelf audits and counterfeit checks.
- **Asset libraries.** Find similar or duplicate images in a design library, or catch content that's been re-uploaded.
- **Manufacturing.** Compare parts against a reference image to spot outliers on the line.
- **Content moderation.** Catch cropped, framed, or recombined variants of images you've already banned.
- **Edge and appliances.** Run business logic and image search in a single JVM service on a box you control.

---

## Screenshots

### Search

![Search Page](https://raw.githubusercontent.com/TommysLee/images-bed/refs/heads/main/ImageSearch4J/search-index.jpg)

![Search Page](https://raw.githubusercontent.com/TommysLee/images-bed/refs/heads/main/ImageSearch4J/search-results.jpg)

There are three ways to hand it an image: upload a file, paste a URL, or paste a screenshot from the clipboard. All three end up as a `File` on the wire, so the backend only ever deals with one field.

Results come back with the preview annotated. The best match gets a solid green box; other candidates get dashed orange boxes, so you can see what the system looked at and what it decided to ignore. The match card and the similar-image list sit underneath.

### Vector management

![Vector management page](https://raw.githubusercontent.com/TommysLee/images-bed/refs/heads/main/ImageSearch4J/vector-update.jpg)

Batch upload, incremental updates, index statistics, and a processing log. This one is a demo page.

---

## How it works

```
┌──────────────────────────────────────────────────────────────────────────┐
│  Spring Boot 3 application (single process)                              │
│                                                                          │
│  POST /api/search                                                        │
│       │   image (File), topk, threshold                                  │
│       ▼                                                                  │
│    ┌──────────────────────────────────────────────────────────────────┐  │
│    │  @Async("aiInferExecutor")                                       │  │
│    │                                                                  │  │
│    │  1. PipelineService.process()  ->  locate the main subject       │  │
│    │       MainBodyDetectionService.predict()                         │  │
│    │         PicoDet-LCNet -> top-5 -> threshold -> SANMS             │  │
│    │       for each candidate box:                                    │  │
│    │         crop -> PP-LCNetV2 features -> vector search (top 1)     │  │
│    │         on a tie, the larger box wins                            │  │
│    │                                                                  │  │
│    │  2. search the index with the match vector  ->  similarList      │  │
│    └──────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  Inference: AWS DJL 0.38 | ONNX Runtime 1.29 | PicoDet + PP-LCNetV2      │
│  Retrieval: Apache Lucene 9.12 | HNSW + scalar quantization              │
└──────────────────────────────────────────────────────────────────────────┘
```

### The three stages

**1. Detection (PicoDet-LCNet)**

The model returns every plausible subject box it can find. Those boxes then go through a funnel:

```
raw model output  →  top-5  →  threshold filter  →  SANMS
                     (keep recall)  (drop noise)   (drop contained boxes)
```

**2. Feature extraction (PP-LCNetV2)**

Each candidate box is cropped, resized to 224×224, normalized with the ImageNet mean and standard deviation, and turned into a 512-dimensional vector. The vector comes out L2-normalized.

**3. Vector search (Lucene HNSW)**

Candidates are searched against the index one at a time. Their scores are then compared, and when two of them tie, the larger box wins. That box becomes the match, and its vector is what pulls the full list of similar images.

### SANMS, briefly

> **Structure-Aware NMS: one more pass after NMS.**

Detection models in the PP-ShiTu family are end-to-end, so their output has already been through a built-in NMS pass. Traditional NMS uses IoU to decide what's redundant, which handles overlap well (high IoU) and handles containment badly. When a large box fully contains a small one, their IoU is tiny, so NMS keeps both.

That's exactly the failure mode that matters for subject detection. Search for a bottle of cola and the model might return:

- the whole bottle, as a large box with a low score, and
- the label on the bottle, as a small box with a high score.

NMS keeps both. The high-scoring small box can still win, so the system goes off and searches with the label. The thing it should be comparing is the whole bottle. It picked the wrong object on the very first step.

SANMS sorts boxes by area, descending. If a smaller box is geometrically contained inside a larger one, the smaller box is dropped and its score is handed to the larger one. What survives is the whole bottle, still carrying the label's confidence.

It complements NMS rather than replacing it. The built-in NMS handles ordinary overlap; SANMS covers the containment blind spot. Between the two, "what did we detect" and "what should we search with" line up again.

---

## Getting started

### What you need

| | |
|---|---|
| **JDK** | 17 or newer (Spring Boot 3 requires it) |
| **OS** | Windows, Linux, or macOS, matching the platform in your `ty.onnx-engine-path` setting |
| **Memory** | 2 GB free is a reasonable floor |
| **Build** | Maven 3.8+ |

### Step 1: get a gallery

The project doesn't ship any image data. You bring your own directory. The PP-ShiTu sample dataset **`drink_dataset_v2.0`** (drink products) is a good one to start with.

If no gallery is found at startup, the app prints the download link and exits. It won't dump a stack trace on you.

Expected layout:

```
${user.home}/image_gallery/
├── gallery/                     # the images (subdirectories are fine)
│   ├── drink_label_all.txt      # labels: one "relative/path<TAB>name" per line
│   ├── 142/2.jpg
│   └── ...
└── test_images/                 # sample images for the front-end's "examples" feature
```

Download the dataset, unzip it, rename the root folder to `image_gallery`, and drop it in your home directory.

### Step 2: run it

```bash
git clone <this-repo>
cd ImageSearch4J
mvn spring-boot:run
```

The first run does three things on its own:

1. Unpacks both ONNX models from the JAR into `${user.home}/models/`.
2. Checks that the gallery is there.
3. If the index is empty, scans the gallery and builds it (controlled by `ty.auto-build-index-on-empty`).

> **Port note:** the default port is `80`. On Linux and macOS you often won't have permission to bind it, so pass another one:
>
> ```bash
> mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
> ```

### Step 3: open a page

| Page | URL |
|---|---|
| Search | `http://localhost:8080/index.html` |
| Vector management | `http://localhost:8080/vector-update.html` |

---

## API

### `POST /api/search`

| Parameter | In | Type | Required | Default | Notes |
|---|---|---|---|---|---|
| `image` | form-data | File | yes | — | the image to search with |
| `topk` | form-data | int | no | 10 | how many similar results to return; falls back to 10 if less than 1 |
| `threshold` | form-data | float | no | 0.4 | similarity threshold for the vector search; falls back to 0.4 if outside 0–1 |

```bash
curl -X POST http://localhost:8080/api/search \
  -F "image=@./cola.jpg" \
  -F "topk=10" \
  -F "threshold=0.4"
```

### `GET /data/examples`

Returns up to 8 random relative paths from the `test_images` directory, for the front end's "try an example" button.

---

## Response format

```json
{
    "state": 1,
    "code": 200,
    "message": "Success",
    "uuid": null,
    "data": {
        "match": {
            "name": "康师傅冰红茶",
            "path": "142/2.jpg",
            "md5": "96b9f53217b116af6b767ba033e74b9f",
            "score": 0.8343468
        },
        "similarList": [
            { "name": "康师傅冰红茶", "path": "142/2.jpg", "md5": "96b9f53217b116af6b767ba033e74b9f", "score": 0.8343468 },
            { "name": "康师傅冰红茶", "path": "142/1.jpg", "md5": "39bd18f36f6aa2b23b19b65bb3acc1ab", "score": 0.81112576 },
            { "name": "康师傅冰绿茶", "path": "164/5.jpg", "md5": "5f55b3b3c66c901597e69af8a79543c6", "score": 0.7852762 }
        ],
        "candis": [
            {
                "className": "0_foreground，0.36",
                "probability": 0.3613264560699463,
                "rect": [194.1317901611328, 6.474399566650391, 493.5636749267578, 982.0707130432129]
            }
        ],
        "rect": [499.8619079589844, 9.447002410888672, 801.0286254882812, 945.4377250671387]
    }
}
```

| Field | Type | Notes |
|---|---|---|
| `state` | int | 1 = success, 0 = failure |
| `code` | int | 200 success, 400 bad request from the caller, 500 server error, 999 other |
| `message` | string | human-readable message, shown to the user on failure |
| `data.match` | object | the best match: name, path, MD5, score |
| `data.similarList` | array | the full list of similar results, sorted by score |
| `data.candis` | array | detected subject boxes other than the best match, with detection confidence |
| `data.rect` | array | the best match's box, in original image coordinates as `[x1, y1, x2, y2]` |

A few notes on why it's split up this way:

- **`match` and `similarList` answer different questions.** `match` is "what is this, most likely," a conclusion. `similarList` is "what in the library looks like it," the supporting evidence. Different concerns, so the front end can present them differently.
- **`rect` and `candis` are split for the same reason.** `rect` is what the system settled on; `candis` is what it passed over. Drawing them in two colors lets a user see why the system made the call it did.
- **Detection succeeding without a match still returns something.** Even when nothing in the library matches, `candis` and `rect` still come back, so the UI can say "we found an object, there's just nothing like it in the index" instead of showing a blank page.

---

## Why ONNX, DJL, and Lucene

These three choices aren't three separate decisions. They're the same idea applied three times: couple as little as you have to, and leave the rest free.

### ONNX

![ONNX: an open standard for interoperable deep learning models](https://aisearch.cdn.bcebos.com/fileAsset/7C2xQ1WdlE14BnTXwjVVGA/1790957880427WGGz5d.jpg?authorization=bce-auth-v1%2F7e22d8caf5af46cc9310f1e3021709f3%2F2026-10-02T16%3A17%3A58Z%2F86400%2Fhost%2F1195cf01c3da03401f0ccff80258ac9a41c12a8e635ec54aad323355988cc1f9)

ONNX is a neutral, open format that sits between training frameworks and deployment targets. It belongs to the Linux Foundation, not to any one vendor, and that's the whole point.

Two things follow from that:

- **Training stays free.** Your AI engineers pick whatever framework they like (PyTorch, TensorFlow, PaddlePaddle), as long as it can export ONNX.
- **Serving stays stable.** The runtime doesn't care which framework produced the file. If a new framework shows up next year with an ONNX exporter, it plugs into this pipeline without a single change on the Java side.

The handoff between training and serving stops being "framework to framework" and becomes "framework to standard." For anything you plan to maintain for years, that matters a lot.

### DJL

[AWS Deep Java Library](https://djl.ai) is AWS's open-source deep learning framework for Java, maintained for years now. It's engine-agnostic: the same Java API sits on top of ONNX Runtime, PyTorch, TensorFlow, or whatever else you point it at.

It's also battle-tested. DJL Serving is the official model-serving path on SageMaker, with dynamic batching, autoscaling, and multi-engine hosting. There's no good reason to write an inference engine from scratch when this one already exists.

### Lucene

Apache Lucene is the search engine core underneath Elasticsearch and Solr. Two properties made it the right fit here.

It's embedded. Lucene is a JAR that runs in your process. No separate daemon, no network hop, and no extra thing to monitor. An entire component disappears from your deployment.

It also does vector search natively. HNSW landed in 9.0. This project goes a step further and turns on scalar quantization, which compresses float32 to int8 and cuts index memory to roughly a quarter while keeping recall above 95%.

There's a nice side effect, too. A Lucene document can hold both vector fields and ordinary inverted-index text fields, so the architecture already supports hybrid vector-plus-keyword search without pulling in another component.

> **If you want that:** the default path is pure vector search. To add keyword matching, write the `name` into a text field at index time and combine a KNN subquery with a term subquery using `BooleanQuery`. The merge happens inside a single query, and nothing in the main path has to change.

---

## Building on top

The whole point of this project is to be a foundation. The AI complexity sits behind the service layer, and your code talks to a handful of Java interfaces.

| Service | Key methods |
|---|---|
| `ImageSearchService` | `search(BufferedImage, int topK, float scoreThres)` → `CompletableFuture<SearchResult>` |
| `PipelineService` | `process(BufferedImage)` → `SearchResult` |
| `MainBodyDetectionService` | `predict(Image / BufferedImage / String base64)` → `List<DetectedObject>` |
| `FeatureExtractionService` | `predict(Image / BufferedImage)` → `float[]` |
| `VectorService` | `add(...)`, `search(...)`, `build()`, `existsByMd5(...)`, `isEmpty()` |

```java
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ImageSearchService imageSearchService;

    public SearchResult search(byte[] imageBytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        // search is annotated @Async("aiInferExecutor") and returns a CompletableFuture
        return imageSearchService.search(image, 10, 0.4f).get();
    }
}
```

One thing that trips people up: `search` is `@Async("aiInferExecutor")`, so it runs on a dedicated pool and hands you a `CompletableFuture`. Call `.get()` or `.join()` on it. It is not a synchronous method.

---

## Configuration

Everything lives under the `ty` prefix in `src/main/resources/application.yml`.

```yaml
server:
  port: 80                          # service port

ty:
  # -- models --
  models-dir: "${user.home}/models"
  onnx-engine-path: "${user.home}/.djl.ai/onnx/win-x64"
  main-body-model-name: "picodet_lcnet_x2_5_640_mainbody.onnx"
  general-p-pLCNet-model-name: "general_PPLCNetV2.onnx"

  # -- index and gallery --
  lucene-buffer-size: 256
  index-dir: "${user.home}/image_gallery/vector_index"
  image-root: "${user.home}/image_gallery/gallery"
  image-data-file: "${user.home}/image_gallery/gallery/drink_label_all.txt"
  auto-build-index-on-empty: true

  # -- AI inference thread pool --
  ai-infer:
    core-pool-size: 4
    max-pool-size: 8
    queue-capacity: 100

  # -- Predictor object pool --
  predictor:
    max-total: 8
    min-idle: 2
    block-when-exhausted: true
    max-wait: 1
```

### About `onnx-engine-path`

Every time ONNX Runtime starts, it unpacks its own native libraries (`onnxruntime.dll` and friends) into a temporary directory. Cleanup relies on `File.deleteOnExit`, which only removes empty directories, so a long-running process slowly accumulates temp files.

Pointing this setting at a fixed directory avoids the repeated unpacking and keeps the environment stable.

> **Note:** the path has to match the platform you're running on (Windows x64, Linux x64, and so on). **Change it to the right directory when you deploy somewhere else.**

---

## Project layout

```
ImageSearch4J
├── src/main/java/com/ty/
│   ├── BootApplication.java              # entry point (@EnableAsync + @EnableScheduling)
│   ├── ai/
│   │   ├── service/                      # detection / feature extraction / pipeline
│   │   └── translator/                   # ONNX pre- and post-processing (one per model)
│   ├── config/                           # model, Lucene, object pool, thread pool config
│   ├── controller/                       # REST endpoints (/api/search, /data/examples)
│   ├── listener/                         # startup bootstrap: gallery check + auto build
│   ├── model/                            # unified response and domain objects
│   ├── task/                             # Lucene scheduled tasks (NRT refresh / commit)
│   ├── service/                          # ImageSearchService / VectorService
│   ├── init/                             # model resource extraction
│   └── utils/                            # SANMS, MD5, image helpers
├── src/main/resources/
│   ├── models/                           # bundled ONNX models (unpacked at startup)
│   ├── public/                           # front-end pages (index.html / vector-update.html)
│   ├── application.yml
│   └── banner.txt
└── pom.xml
```

---

## FAQ

**Do I need Python installed?**
No. The whole pipeline runs in the JVM. The only thing it needs from the outside world is the two ONNX files, and those ship inside the JAR.

**Do I need to deploy a vector database?**
No. The vector index is Lucene, embedded in the application, stored in a local directory. There's no separate service to maintain.

**How many images can the gallery hold?**
Lucene is a mature search engine core and handles large vector collections fine. The practical limit is your machine's memory. A single 512-dimensional float32 vector is about 2 KB, and scalar quantization brings the index down further.

**Search feels slow. What do I tune?**
HNSW parameters and the quantization strategy, mostly. The index layer already widens its search based on `efSearch = max(topk, 10)`. For bigger datasets, try adjusting `efSearch` somewhere between 10 and 100.

**Why doesn't my `topK` parameter do anything?**
The parameter is `topk`, all lowercase. Spring is case-sensitive with `@RequestParam` names.

**Can I use this with Elasticsearch?**
It's a different tradeoff. This project deliberately goes embedded, which is what makes it a single JAR with nothing to operate. If you already run an ES cluster, you can keep this project's pipeline design and swap the retrieval layer for ES KNN queries.

**Does it support GPU?**
DJL supports multiple engines and hardware backends. This project runs on CPU by default. For GPU, adjust DJL's engine and device settings.

---

## Roadmap

- [x] Detection + feature extraction + vector search, working end to end
- [x] SANMS subject refinement
- [x] Bundled models, works out of the box
- [x] Auto index build + NRT refresh (new vectors searchable within seconds)
- [x] Two framework-free front-end pages
- [ ] Hybrid search (vector + keyword)
- [ ] A more complete vector management API
- [ ] More prebuilt models and scenario examples

---

## License and credits

- Licensed under the **Apache License 2.0**. See [LICENSE](LICENSE).
- The detection and feature models come from **[PaddlePaddle / PP-ShiTu](https://github.com/PaddlePaddle/PaddleClas)**, converted to ONNX and running on the JVM. Thanks to the PaddlePaddle team.
- Thanks to **[AWS Deep Java Library](https://djl.ai)** and **[Apache Lucene](https://lucene.apache.org)** for the solid ground this stands on.
- And thanks to everyone who keeps the "train in Python, serve in Java" path getting wider.

---

<div align="center">

**If this project is useful to you, a star is appreciated. Contributions are welcome.**

*Technology should serve the scenario, not the other way around.*