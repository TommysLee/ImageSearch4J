package com.ty.spring.config;

import com.ty.constant.LuceneFields;
import com.ty.spring.config.properties.TyProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.KnnVectorsFormat;
import org.apache.lucene.codecs.lucene912.Lucene912Codec;
import org.apache.lucene.codecs.lucene99.Lucene99HnswScalarQuantizedVectorsFormat;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Lucene向量配置类
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Configuration
@Slf4j
public class LuceneConfig {

    private FSDirectory directory;
    private IndexWriter writer;
    private SearcherManager searcherManager;

    /**
     * 创建 IndexWriter
     * destroyMethod = "" 表示禁用 Spring 的自动关闭，由本类 shutdown() 统一负责
     */
    @Bean(destroyMethod = "")
    public IndexWriter indexWriter(TyProperties tyProperties) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        config.setRAMBufferSizeMB(tyProperties.getLuceneBufferSize()); // 显式设置缓冲区
        config.setCodec(new Lucene912Codec() { // 自定义编码器，重写向量字段的格式化方法
            @Override
            public KnnVectorsFormat getKnnVectorsFormatForField(String fieldName) {
                // 仅对名为 "embedding" 的向量字段启用量化
                if (LuceneFields.EMBEDDING.equals(fieldName)) {
                    // 使用默认7位标量量化的HNSW格式：内存约为原来的 1/4，召回率 ≥ 95%
                    // 参数: maxConn(图最大连接数), beamWidth(搜索宽度)
                    // 512维中等维度：追求召回率用 (32, 200)，追求速度用 (16, 100)
                    return new Lucene99HnswScalarQuantizedVectorsFormat(32, 200);
                }
                return super.getKnnVectorsFormatForField(fieldName);
            }
        });

        this.directory = FSDirectory.open(Paths.get(tyProperties.getIndexDir()));
        this.writer = new IndexWriter(this.directory, config);
        log.info("初始化 IndexWriter，索引目录: {}", tyProperties.getIndexDir());
        return this.writer;
    }

    /**
     * 创建 SearcherManager
     */
    @Bean(destroyMethod = "")
    public SearcherManager searcherManager(IndexWriter indexWriter) throws IOException {
        this.searcherManager = new SearcherManager(indexWriter, null);
        return this.searcherManager;
    }

    /**
     * 应用关闭时按正确顺序释放资源
     * 顺序：SearcherManager → IndexWriter → FSDirectory
     */
    @PreDestroy
    public void shutdown() {
        IOUtils.closeQuietly(searcherManager, writer, directory);
        log.info("Lucene 资源已关闭");
    }
}
