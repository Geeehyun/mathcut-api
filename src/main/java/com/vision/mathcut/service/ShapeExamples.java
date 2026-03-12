package com.vision.mathcut.service;

/**
 * AI 스케치 도형 타입별 예시 JSON (TypeScript aiSketchExamples.ts 포팅)
 * GPT-4o 시스템 프롬프트의 "Reference example" 섹션에 삽입됩니다.
 */
public class ShapeExamples {

    private static final String TRIANGLE_RIGHT = """
            {
              "shapes": [
                {
                  "type": "triangle-right",
                  "points": [
                    {"gridX": 20, "gridY": 23},
                    {"gridX": 26, "gridY": 26},
                    {"gridX": 20, "gridY": 26}
                  ],
                  "pointLabels": ["A", "B", "C"],
                  "guideVisibility": {"pointName": true, "point": false, "length": true, "angle": true, "height": false, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"text": "6cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "3cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": [
                    {"text": "30\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "60\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "90\u00b0", "visible": false, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String TRIANGLE_ISOSCELES = """
            {
              "shapes": [
                {
                  "type": "triangle-isosceles",
                  "points": [
                    {"gridX": 32, "gridY": 18},
                    {"gridX": 30, "gridY": 24},
                    {"gridX": 34, "gridY": 24}
                  ],
                  "pointLabels": ["\u3131", "\u3134", "\u3137"],
                  "guideVisibility": {"pointName": true, "point": false, "length": true, "angle": true, "height": true, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"text": "6cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "4cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "6cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": [
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"text": "70\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "heightItem": {"text": "5.6cm", "textMode": "normal", "textDirection": "auto"},
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String TRIANGLE_EQUILATERAL = """
            {
              "shapes": [
                {
                  "type": "triangle-equilateral",
                  "points": [
                    {"gridX": 32, "gridY": 22},
                    {"gridX": 34, "gridY": 26},
                    {"gridX": 29, "gridY": 26}
                  ],
                  "pointLabels": ["\u3131", "\u3134", "\u3137"],
                  "guideVisibility": {"pointName": true, "point": false, "length": true, "angle": true, "height": false, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"text": "5cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": [
                    {"text": "60\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"visible": true, "textMode": "blank", "textDirection": "auto", "detached": false},
                    {"text": "60\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String CIRCLE = """
            {
              "shapes": [
                {
                  "type": "circle",
                  "points": [
                    {"gridX": 32, "gridY": 18},
                    {"gridX": 35, "gridY": 18}
                  ],
                  "pointLabels": ["\u3147"],
                  "guideVisibility": {"pointName": true, "point": true, "length": true, "angle": false, "height": false, "radius": true},
                  "circleMeasureMode": "radius",
                  "lengthItems": [
                    {"text": "3cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String RECT_RECTANGLE = """
            {
              "shapes": [
                {
                  "type": "rect-rectangle",
                  "points": [
                    {"gridX": 14, "gridY": 10},
                    {"gridX": 50, "gridY": 10},
                    {"gridX": 50, "gridY": 28},
                    {"gridX": 14, "gridY": 28}
                  ],
                  "pointLabels": null,
                  "guideVisibility": {"pointName": false, "point": false, "length": true, "angle": false, "height": false, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"text": "8cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "6cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String SEGMENT = """
            {
              "shapes": [
                {
                  "type": "segment",
                  "points": [
                    {"gridX": 12, "gridY": 18},
                    {"gridX": 52, "gridY": 18}
                  ],
                  "pointLabels": ["A", "B"],
                  "guideVisibility": {"pointName": true, "point": true, "length": true},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"text": "5cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String ANGLE_LINE = """
            {
              "shapes": [
                {
                  "type": "angle-line",
                  "points": [
                    {"gridX": 10, "gridY": 26},
                    {"gridX": 32, "gridY": 18},
                    {"gridX": 54, "gridY": 26}
                  ],
                  "pointLabels": null,
                  "guideVisibility": {"angle": true},
                  "circleMeasureMode": null,
                  "lengthItems": null,
                  "angleItems": [
                    {"text": "60\u00b0", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "heightItem": null,
                  "showUnit": false
                }
              ],
              "guides": []
            }""";

    private static final String FREE_SHAPE = """
            {
              "shapes": [
                {
                  "type": "free-shape",
                  "points": [
                    {"gridX": 16, "gridY": 20},
                    {"gridX": 24, "gridY": 10},
                    {"gridX": 38, "gridY": 8},
                    {"gridX": 48, "gridY": 18},
                    {"gridX": 40, "gridY": 30},
                    {"gridX": 24, "gridY": 28}
                  ],
                  "pointLabels": ["A", "B", "C", "D", "E", "F"],
                  "guideVisibility": {"pointName": true, "point": false, "length": false, "angle": false, "height": false, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": null,
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": false
                }
              ],
              "guides": []
            }""";

    private static final String TRIANGLE_DEFAULT_TEMPLATE = """
            {
              "shapes": [
                {
                  "type": "<TYPE>",
                  "points": [
                    {"gridX": 32, "gridY": 8},
                    {"gridX": 14, "gridY": 28},
                    {"gridX": 50, "gridY": 28}
                  ],
                  "pointLabels": ["A", "B", "C"],
                  "guideVisibility": {"pointName": true, "point": false, "length": true, "angle": false, "height": false, "radius": false},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"text": "5cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "4cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "5cm", "visible": true, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String RECT_DEFAULT_TEMPLATE = """
            {
              "shapes": [
                {
                  "type": "<TYPE>",
                  "points": [
                    {"gridX": 14, "gridY": 10},
                    {"gridX": 50, "gridY": 10},
                    {"gridX": 50, "gridY": 28},
                    {"gridX": 14, "gridY": 28}
                  ],
                  "pointLabels": null,
                  "guideVisibility": {"length": true},
                  "circleMeasureMode": null,
                  "lengthItems": [
                    {"text": "8cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"text": "6cm", "visible": true, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"},
                    {"visible": false, "textMode": "normal", "textDirection": "auto"}
                  ],
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": true
                }
              ],
              "guides": []
            }""";

    private static final String LINE_DEFAULT_TEMPLATE = """
            {
              "shapes": [
                {
                  "type": "<TYPE>",
                  "points": [
                    {"gridX": 12, "gridY": 18},
                    {"gridX": 52, "gridY": 18}
                  ],
                  "pointLabels": null,
                  "guideVisibility": {},
                  "circleMeasureMode": null,
                  "lengthItems": null,
                  "angleItems": null,
                  "heightItem": null,
                  "showUnit": false
                }
              ],
              "guides": []
            }""";

    public static String buildShapeExample(String shapeType) {
        return switch (shapeType) {
            case "triangle-right" -> TRIANGLE_RIGHT;
            case "triangle-isosceles" -> TRIANGLE_ISOSCELES;
            case "triangle-equilateral" -> TRIANGLE_EQUILATERAL;
            case "circle" -> CIRCLE;
            case "rect-rectangle" -> RECT_RECTANGLE;
            case "segment" -> SEGMENT;
            case "angle-line" -> ANGLE_LINE;
            case "free-shape" -> FREE_SHAPE;
            case "triangle" -> TRIANGLE_DEFAULT_TEMPLATE.replace("<TYPE>", shapeType);
            case "rect-square", "rect-rhombus", "rect-parallelogram", "rect-trapezoid" ->
                    RECT_DEFAULT_TEMPLATE.replace("<TYPE>", shapeType);
            case "line", "ray", "arrow", "arrow-curve" ->
                    LINE_DEFAULT_TEMPLATE.replace("<TYPE>", shapeType);
            default -> TRIANGLE_DEFAULT_TEMPLATE.replace("<TYPE>", shapeType);
        };
    }
}
