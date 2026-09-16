package com.ty.spring.timer;

import com.ty.spring.config.TyConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.AlreadyClosedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Lucene 定时任务
 *
 * <p>两个任务，均绑定到 Lucene 专用线程池 {@code luceneTaskScheduler}：
 * <ul>
 *   <li>每秒刷新：让新写入的数据在 1 秒内可被搜索到</li>
 *   <li>每 5 分钟提交：把数据持久化到磁盘</li>
 * </ul>
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Component
@Slf4j
public class LuceneScheduledTask {

    @Autowired
    private IndexWriter indexWriter;

    @Autowired
    private SearcherManager searcherManager;

    /**
     * 每秒刷新：Lucene 内部会自动把内存数据写入磁盘并刷新 Reader，让新数据可被搜到（NRT）。
     * 使用 fixedDelay 而非 fixedRate，避免任务堆积。
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS, scheduler = TyConfig.LUCENE_TASK_SCHEDULER)
    public void refresh() {
        try {
            searcherManager.maybeRefresh();
        } catch (AlreadyClosedException e) {
            // 优雅处理
            log.warn("SearcherManager already closed, skipping refresh.");
        } catch (Exception e) {
            log.error("刷新索引失败", e);
        }
    }

    /**
     * 每 5 分钟持久化：只有存在未提交的数据时才执行，避免无意义的磁盘写入。
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES, scheduler = TyConfig.LUCENE_TASK_SCHEDULER)
    public void commit() {
        try {
            if (indexWriter.hasUncommittedChanges()) {
                indexWriter.commit();
                log.info("索引已持久化到磁盘");
            }
        } catch (AlreadyClosedException e) {
            // 优雅处理
            log.warn("Writer already closed, skipping commit.");
        } catch (Exception e) {
            log.error("提交索引失败", e);
        }
    }
}
