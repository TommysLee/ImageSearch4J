package com.ty.utils;

import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.modality.cv.output.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 目标检测后处理 - 非极大值抑制（NMS）工具类
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
public class NMSUtils {

    /**
     * 结构感知NMS（SANMS）—— 专用于主体检测 / 以图搜图场景。
     *
     * <p>逻辑概述：
     * 按面积降序处理，剔除完全被大框包含的小框，并将被剔除框的最高置信度继承给大框。
     * </p>
     *
     * <p>核心逻辑：
     * <ul>
     *  <li>1. 按面积从大到小遍历矩形框；</li>
     *  <li>2. 若小框被大框完全包含（几何子集关系），则剔除小框；</li>
     *  <li>3. 若被剔除的小框置信度高于大框，则大框继承该最高置信度。</li>
     * </ul>
     *
     * <p>适用场景：解决标准IoU-NMS无法处理"大框吞小框"的问题，
     * 例如：模型同时检测出"整个瓶子（低分）"和"瓶身标签（高分）"时，
     * 该算法会保留瓶子大框，并让其获得标签的高分。
     *
     *  @param list 目标检测结果集合
     *  @return 处理后的目标检测结果集合
     */
    public static List<DetectedObject> sanms(List<DetectedObject> list) {
        if (null == list || list.isEmpty()) {
            return list;
        }

        int size = list.size();
        List<String> classNames = new ArrayList<>(size);
        float[][] boxes = new float[size][4];
        float[] scores = new float[size];
        for (int i = 0; i < size; i++) {
            DetectedObject r = list.get(i);
            classNames.add(r.getClassName());
            scores[i] = (float) r.getProbability();

            // 坐标
            Rectangle box = r.getBoundingBox().getBounds();
            float x1 = (float) box.getX();
            float y1 = (float) box.getY();
            float x2 = x1 + (float) box.getWidth();
            float y2 = y1 + (float) box.getHeight();
            boxes[i] = new float[]{ x1, y1, x2, y2 };
        }

        // SANMS
        NmsResult nmsResult = sanms(boxes, scores, classNames);

        // 构建结果
        classNames = nmsResult.classNames;
        boxes = nmsResult.boxes;
        scores = nmsResult.scores;
        int n = nmsResult.size();
        List<DetectedObject> resultList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            float[] box = boxes[i];
            float x1 = box[0];
            float y1 = box[1];
            float x2 = box[2];
            float y2 = box[3];
            resultList.add(new DetectedObject(classNames.get(i), scores[i], new Rectangle(x1, y1, (x2 - x1), (y2 - y1))));
        }
        return resultList;
    }

    /**
     * 结构感知NMS（SANMS）—— 专用于主体检测 / 以图搜图场景。
     *
     * <p>逻辑概述：
     * 按面积降序处理，剔除完全被大框包含的小框，并将被剔除框的最高置信度继承给大框。
     * </p>
     *
     * <p>核心逻辑：
     * <ul>
     *  <li>1. 按面积从大到小遍历矩形框；</li>
     *  <li>2. 若小框被大框完全包含（几何子集关系），则剔除小框；</li>
     *  <li>3. 若被剔除的小框置信度高于大框，则大框继承该最高置信度。</li>
     * </ul>
     *
     * <p>适用场景：解决标准IoU-NMS无法处理"大框吞小框"的问题，
     * 例如：模型同时检测出"整个瓶子（低分）"和"瓶身标签（高分）"时，
     * 该算法会保留瓶子大框，并让其获得标签的高分。
     *
     * @param boxes       原始检测框，N x 4，坐标格式 [x1, y1, x2, y2]（需确保 x2>x1, y2>y1）【建议传入副本数据】
     * @param scores      原始置信度数组，长度必须与 boxes 的行数一致 【建议传入副本数据】
     * @param classNames  分类ID集合
     * @return 处理后的 NmsResult 对象（已按置信度降序排列）
     * @throws IllegalArgumentException 如果 boxes 和 scores 长度不匹配
     */
    public static NmsResult sanms(float[][] boxes, float[] scores, List<String> classNames) {
        if (null == boxes || null == scores || null == classNames) {
            return new NmsResult(new float[0][0], new float[0], new ArrayList<>(0));
        }
        if (boxes.length != scores.length || scores.length != classNames.size()) {
            throw new IllegalArgumentException("boxes, scores and classNames length mismatch");
        }

        final int n = boxes.length;
        if (n <= 1) {
            return new NmsResult(boxes, scores, classNames);
        }

        // 1. 计算面积并获取按面积从大到小的索引
        Float[] areas = new Float[n];
        for (int i = 0; i < n; i++) {
            float w = Math.max(0, boxes[i][2] - boxes[i][0]);
            float h = Math.max(0, boxes[i][3] - boxes[i][1]);
            areas[i] = w * h;
        }

        Integer[] sortedIdx = new Integer[n];
        for (int i = 0; i < n; i++) sortedIdx[i] = i;
        Arrays.sort(sortedIdx, (a, b) -> Float.compare(areas[b], areas[a])); // 降序

        // 2. 核心逻辑：大框优先保留，小框被包含则吞并并继承分数
        List<Integer> keepList = new ArrayList<>(); // 存储最终保留下来的原始索引
        final float EPS = 1e-3f; // 归一化时，建议：1e-6f;
        for (int i = 0; i < n; i++) {
            int curIdx = sortedIdx[i];
            float[] curBox = boxes[curIdx];
            boolean isContained = false;

            // 遍历已保留的框（它们都是比当前框面积更大的框）
            for (int kidx : keepList) {
                float[] kbox = boxes[kidx];

                // 判定：大框(kbox) 是否完全包含 小框(curBox)
                // 条件：大框的左上角 <= 小框左上角，且大框的右下角 >= 小框右下角
                if (kbox[0] <= curBox[0] + EPS && kbox[1] <= curBox[1] + EPS &&
                        kbox[2] >= curBox[2] - EPS && kbox[3] >= curBox[3] - EPS) {

                    // ★ 分数继承（点对点取最大值），发生在吞并瞬间
                    if (scores[curIdx] > scores[kidx]) {
                        scores[kidx] = scores[curIdx];
                    }

                    isContained = true;
                    break; // 已被一个大框包含，无需继续检查其他大框
                }
            }

            // 如果未被任何已保留的大框包含，则将该框加入保留列表
            if (!isContained) {
                keepList.add(curIdx);
            }
        }

        // 3. 提取结果
        int m = keepList.size();
        float[][] resultBoxes = new float[m][4];
        float[] resultScores = new float[m];
        List<String> resultClassNames = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            int idx = keepList.get(i);
            resultBoxes[i] = boxes[idx];
            resultScores[i] = scores[idx];
            resultClassNames.add(classNames.get(idx));
        }

        // 4. 按置信度降序排序
        Integer[] finalOrder = new Integer[m];
        for (int i = 0; i < m; i++) finalOrder[i] = i;
        Arrays.sort(finalOrder, (a, b) -> Float.compare(resultScores[b], resultScores[a]));

        float[][] finalBoxes = new float[m][4];
        float[] finalScores = new float[m];
        List<String> finalClassNames = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            int idx = finalOrder[i];
            finalBoxes[i] = resultBoxes[idx];
            finalScores[i] = resultScores[idx];
            finalClassNames.add(resultClassNames.get(idx));
        }

        // 5. 返回最终结果
        return new NmsResult(finalBoxes, finalScores, finalClassNames);
    }

    /**
     * 结构感知NMS（SANMS）的处理结果类
     */
    public static class NmsResult {
        public final float[][] boxes; // N x 4, 格式 [x1, y1, x2, y2]
        public final float[] scores;  // N
        public final List<String> classNames;

        public NmsResult(float[][] boxes, float[] scores, List<String> classNames) {
            this.boxes = boxes;
            this.scores = scores;
            this.classNames = classNames;
        }

        public int size() {
            return null != boxes? boxes.length : 0;
        }
    }
}
