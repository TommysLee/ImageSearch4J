package com.ty.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.training.util.ProgressBar;
import com.google.common.collect.Lists;
import com.ty.ai.service.FeatureExtractionService;
import com.ty.model.VectorDocument;
import com.ty.spring.config.properties.TyProperties;
import com.ty.utils.ImageUtils;
import com.ty.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.ty.constant.LuceneFields.EMBEDDING;
import static com.ty.constant.LuceneFields.MD5;
import static com.ty.constant.LuceneFields.NAME;
import static com.ty.constant.LuceneFields.PATH;

/**
 * 向量服务（存储+检索）
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Service
@Slf4j
public class VectorService {

    @Autowired
    private IndexWriter writer;

    @Autowired
    private SearcherManager searcherManager;

    @Autowired
    private FeatureExtractionService featureExtractionService;

    @Autowired
    private TyProperties tyProperties;

    private static final int EF_SEARCH = 10;

    /**
     * 构建向量数据库
     *
     * @throws Exception
     */
    public void build() throws Exception {
        long begin = System.currentTimeMillis();
        ProgressBar bar = new ProgressBar("构建向量库", 100);

        // 读取图片数据
        ImageFactory imageFactory = ImageFactory.getInstance();
        List<String> lines = Files.readAllLines(Paths.get(tyProperties.getImageDataFile()), StandardCharsets.UTF_8).stream().filter(line -> !line.isBlank()).map(String::trim).toList();
        int tick = 0;
        for (String line : lines) {
            String[] lineArr = line.split("\t");
            String path = lineArr[0];
            String name = lineArr[1];

            try {
                // 转换为 DJL 图像数据
                Path imgPath = Paths.get(tyProperties.getImageRoot(), path);
                Image image = imageFactory.fromFile(imgPath);

                // 计算图像MD5
                String md5 = MD5Utils.calc(imgPath.toString());

                // 添加向量
                this.add(image, name, path, md5);
            } catch (Exception e) {
                log.error("处理失败 (跳过)：" + path, e);
            }

            // 更新构建进度条
            tick++;
            bar.update(Math.floorDiv(tick * 100, lines.size()));
        }
        // 提交索引，持久化到磁盘
        writer.commit();
        long end = System.currentTimeMillis();
        log.info("向量数据库构建完毕，耗时：{} seconds.", Math.round((end - begin)/1000f));
    }

    /**
     * 添加向量
     *
     * @param imageBase64 图片的 Base64 编码字符串
     * @param name 图像名称
     * @param path 图像路径（可选）
     * @return int 返回受影响的数量
     * @throws Exception
     */
    public int add(String imageBase64, String name, String path) throws Exception {
        if (StringUtils.isBlank(imageBase64)) {
            return 0;
        }

        // 计算图像MD5
        byte[] imageBytes = ImageUtils.base64ToBytes(imageBase64);
        String md5 = MD5Utils.calc(imageBytes);

        // 转换为 DJL 图像数据
        BufferedImage bufferedImage = ImageUtils.bytesToBufferedImage(imageBytes);
        Image image = ImageFactory.getInstance().fromImage(bufferedImage);

        // 添加向量
        return this.add(image, name, path, md5);
    }

    /**
     * 添加向量
     *
     * @param image 图像数据
     * @param name  图像名称
     * @param path  图像路径（可选）
     * @param md5   图像MD5值
     * @return int  返回受影响的数量
     * @throws Exception
     */
    public int add(Image image, String name, String path, String md5) throws Exception {
        // 判断图像向量是否已存在
        if (existsByMd5(md5)) {
            log.warn("图片已存在，跳过: {} / {}", path);
            return 0;
        }

        // 特征提取
        float[] vector = featureExtractionService.predict(image);

        // 构建Document
        Document doc = new Document();
        doc.add(new StringField(MD5, md5, Field.Store.YES)); // 整体作为一个Term，用于精确匹配
        doc.add(new StoredField(NAME, name)); // 仅存储
        if (StringUtils.isNotBlank(path)) {
            doc.add(new StoredField(PATH, path)); // 仅存储
        }
        // 向量字段
        // 指定相似度函数为IP点积（可选值：COSINE 或 DOT_PRODUCT）
        // 因为AI模型对结果进行了 L2 归一化，所以这里用 DOT_PRODUCT
        doc.add(new KnnFloatVectorField(EMBEDDING, vector, VectorSimilarityFunction.DOT_PRODUCT));

        // 添加索引/向量
        writer.addDocument(doc);
        log.debug("已添加图片: {} ({})", name, md5);
        return 1;
    }

    /**
     * 向量检索 Vector Retrieval：根据图片找最相似的 TopK 条记录。
     *
     * @param bufferedImage 待搜索的图像
     * @param topK          返回的最相似图片数量
     * @return List<VectorDocument> 按相似度倒序排列的匹配结果；无匹配时返回空列表
     * @throws Exception
     */
    public List<VectorDocument> search(BufferedImage bufferedImage, int topK) throws Exception {
        List<VectorDocument> vdocList = Lists.newArrayList();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            // 提取图片特征向量
            Image image = ImageFactory.getInstance().fromImage(bufferedImage);
            float[] queryVector = featureExtractionService.predict(image);

            // 执行 k-NN 搜索，efSearch=10：搜索时扩大搜索范围以提升召回率
            // efSearch 控制每个索引段（Segment）内部 HNSW 图的搜索广度（即 HNSW 的 efSearch 参数）
            // [合理设置该值，可提高召回率（Recall）。对于百万级数据、512 维向量，建议将 EF_SEARCH 设为 10 ~ 100 之间]
            KnnFloatVectorQuery query = new KnnFloatVectorQuery(EMBEDDING, queryVector, EF_SEARCH);
            TopDocs topDocs = searcher.search(query, topK);

            // 处理并封装结果
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                vdocList.add(new VectorDocument(
                        doc.get(NAME),
                        doc.get(PATH),
                        doc.get(MD5),
                        scoreDoc.score
                ));
            }
        } finally {
            searcherManager.release(searcher);
        }
        return vdocList;
    }

    /**
     * 根据 MD5 查询文档
     *
     * @param md5 图像MD5值
     * @return Document 返回匹配的文档；若不存在，返回 null
     * @throws IOException
     */
    public Document findByMd5(String md5) throws IOException {
        if (StringUtils.isBlank(md5)) {
            return null;
        }

        IndexSearcher searcher = searcherManager.acquire();
        try {
            TopDocs topDocs = searcher.search(new TermQuery(new Term(MD5, md5)), 1);
            if (topDocs.scoreDocs.length == 0) {
                return null;
            }
            return searcher.storedFields().document(topDocs.scoreDocs[0].doc);
        } finally {
            searcherManager.release(searcher);
        }
    }

    /**
     * 判断指定 MD5文档 是否已存在
     *
     * @param md5 图像MD5值
     * @return boolean
     * @throws IOException
     */
    public boolean existsByMd5(String md5) throws IOException {
        if (StringUtils.isBlank(md5)) {
            return false;
        }

        IndexSearcher searcher = searcherManager.acquire();
        try {
            TopDocs topDocs = searcher.search(new TermQuery(new Term(MD5, md5)), 1);
            return topDocs.scoreDocs.length > 0;
        } finally {
            searcherManager.release(searcher);
        }
    }

    /**
     * 判断索引库是否为空
     *
     * @return true 表示索引库中没有任何文档
     * @throws IOException
     */
    public boolean isEmpty() throws IOException {
        IndexSearcher searcher = searcherManager.acquire();
        try {
            int count = searcher.getIndexReader().numDocs();
            log.debug("索引库数量：{}", count);
            return count == 0;
        } finally {
            searcherManager.release(searcher);
        }
    }
}
