package com.ty.ai.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Paddle 主体检测模型 前置处理与后置处理的Translator
 *
 * @Author Tommy
 * @Date 2026/8/18
 */
@Slf4j
public class PicodetLcnetMainbodyTranslator implements NoBatchifyTranslator<Image, DetectedObjects> {

    private final float[] mean = {0.485f, 0.456f, 0.406f};
    private final float[] std = {0.229f, 0.224f, 0.225f};

    /**
     * 前置处理
     */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
        long begin = System.currentTimeMillis();

        int origH = input.getHeight();
        int origW = input.getWidth();
        NDManager manager = ctx.getNDManager();
        NDArray img = input.toNDArray(manager);

        // 1. Resize 到 640x640，使用 INTER_CUBIC
        int targetSize = 640;
        img = NDImageUtils.resize(img, targetSize, targetSize, Image.Interpolation.BICUBIC);

        // 2. 归一化：缩放至 [0,1] 并 z-score标准化
        img = img.toType(DataType.FLOAT32, false);
        NDArray meanArray = manager.create(mean, new Shape(1, 1, 3));
        NDArray stdArray = manager.create(std, new Shape(1, 1, 3));

        img = img.div(255.0f);
        img = img.sub(meanArray).div(stdArray);

        // 3. 转置为 CHW 并增加 batch 维度
        img = img.transpose(2, 0, 1).expandDims(0);

        // 4. 计算缩放比
        float im_scale_y = (float) targetSize / origH;
        float im_scale_x = (float) targetSize / origW;
        NDArray scale_factor = manager.create(new float[] {im_scale_y, im_scale_x}, new Shape(1, 2));

        long end = System.currentTimeMillis();
        log.debug("主体检测模型-前置处理耗时：{} ms", end - begin);

        // 5.按照 ONNX模型 输入顺序构造 NDList：image 在前，scale_factor 在后
        img.setName("image");
        scale_factor.setName("scale_factor");
        return new NDList(img, scale_factor);
    }

    /**
     * 后置处理
     */
    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray output = list.get(0);
        Shape shape = output.getShape();
        int rows = (int) shape.get(0); // 100
        int cols = (int) shape.get(1); // 6

        // 按行分组，每行6个元素
        float[] data = output.toFloatArray(); // 一次性拷贝 NDArray 全部数据
        float[][] rowsArray = IntStream.range(0, rows)
                .mapToObj(i -> Arrays.copyOfRange(data, (i * cols), ((i + 1) * cols)))
                .toArray(float[][]::new);

        // 封装推理结果
        List<String> classNames = new ArrayList<>(rows);
        List<Double> probabilities = new ArrayList<>(rows);
        List<BoundingBox> boundingBoxes = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            String id = String.valueOf((int)rowsArray[i][0]); // 第一个值：分类ID
            double score = rowsArray[i][1]; // 第二个值：置信度

            // 3~6：矩形框4个坐标
            float x1 = rowsArray[i][2];
            float y1 = rowsArray[i][3];
            float x2 = rowsArray[i][4];
            float y2 = rowsArray[i][5];

            classNames.add(id + "_foreground，" + Math.round(score * 100) / 100f);
            probabilities.add(score);
            boundingBoxes.add(new Rectangle(x1, y1, (x2 - x1), (y2 - y1)));
        }

        return new DetectedObjects(classNames, probabilities, boundingBoxes);
    }
}
