package com.ty.ai.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Paddle 特征向量模型 general_PPLCNetV2_base_pretrained 前置处理与后置处理的Translator
 *
 * @Author Tommy
 * @Date 2026/8/20
 */
@Slf4j
public class GeneralPPLCNetTranslator implements NoBatchifyTranslator<Image, float[]> {

    private final float[] mean = {0.485f, 0.456f, 0.406f};
    private final float[] std = {0.229f, 0.224f, 0.225f};

    /**
     * 前置处理
     */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
        long begin = System.currentTimeMillis();

        NDManager manager = ctx.getNDManager();
        NDArray img = input.toNDArray(manager, Image.Flag.COLOR);

        // 1. Resize 到 224x224，使用 INTER_LINEAR
        int targetSize = 224;
        img = NDImageUtils.resize(img, targetSize, targetSize, Image.Interpolation.BILINEAR);

        // 2. 归一化（缩放至 [0,1]） 与 Z-score标准化
        img = img.toType(DataType.FLOAT32, false).div(255.0f);

        NDArray meanArray = manager.create(mean, new Shape(1, 1, 3));
        NDArray stdArray = manager.create(std, new Shape(1, 1, 3));
        img = img.sub(meanArray).div(stdArray);

        // 3. 转置为 CHW 并增加 batch 维度
        img = img.transpose(2, 0, 1).expandDims(0);

        long end = System.currentTimeMillis();
        log.debug("特征向量模型-前置处理耗时：{} ms", end - begin);
        return new NDList(img);
    }

    /**
     * 后置处理
     */
    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
        /*
         * L2归一化（必须做）
         * DJL 相较于 Paddle官方：增加数值稳定性
         *
         * 没有加 eps（极小值）。如果输入向量是全零（[0, 0, ..., 0]），feas_norm 为 0，np.divide 会报除零错误或产生 inf。
         * 所以，加上一个特别小的数，避免产生 inf
         */

        /*
         * L2归一化是为了让特征向量的“方向”代表语义，而“长度”不再干扰相似度计算。
         *
         * 首先，数学层面：为了将“点积”等同于“余弦相似度”
         *  1.核心原理：余弦相似度的公式是 cos(θ) = (A·B) / (||A|| * ||B||)。
         *  2.归一化后的效果：当你执行了 L2 归一化后，向量的模长 ||A|| 和 ||B|| 都变成了 1。
         *  3.带来的便利：此时，A·B (点积) 的数值就等于 cos(θ)。
         *  4.实际意义：向量检索库（如 Milvus、Faiss）在计算距离时，使用“内积（IP）”比计算“余弦”要快得多（少了开根号和除法的步骤）。
         *            归一化后，你可以直接使用高速的 IP 距离 来近似余弦相似度，从而在百万/亿级数据中实现毫秒级检索。
         *
         * 其次，语义层面：消除“亮度”和“对比度”的干扰
         *  深度学习模型提取的特征，其“方向”（即向量在 512 维空间中的指向）代表了图像的语义内容（比如这是一只猫还是一只狗）。
         *  而向量的“长度”（模长）往往容易受到图像对比度、亮度、清晰度等低阶视觉因素的影响。
         *  1. 如果不归一化：两张内容完全一样的猫，如果一张曝光度高（更亮），其向量模长可能就比曝光度低的那张长。在检索时，模长干扰了距离计算，可能导致“较亮”的猫反而匹配到了“较暗但内容不同”的图像。
         *  2. 归一化后：强制将所有向量的模长变成 1，逼迫检索过程只看方向（语义），不看长度（亮度），极大提升了特征的泛化能力和检索准确率。
         */

        return list.get(0).normalize().toFloatArray();
    }
}

