package com.vision.mathcut.service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 스케치 프롬프트 빌더 (TypeScript useAISketch.ts 포팅)
 * buildSystemPrompt() → GPT-4o 시스템 프롬프트
 * buildUserInstruction() → 사용자 메시지
 */
public class PromptBuilder {

    // ──────────────────────────────────────────────────────────────────────────
    // BASE_PROMPT (useAISketch.ts BASE_PROMPT, lines 112-169)
    // ──────────────────────────────────────────────────────────────────────────
    private static final String BASE_PROMPT = """
You convert a hand-drawn math diagram into strict JSON.
Return JSON only. Do not add explanation, prose, or markdown.

The whole image uses a 64 by 36 grid.
All coordinates must be integers:
- gridX: 0 to 64
- gridY: 0 to 36

Allowed shape types:
- triangle, triangle-right, triangle-equilateral, triangle-isosceles
- rect-square, rect-rectangle, rect-rhombus, rect-parallelogram, rect-trapezoid
- circle, polygon, polygon-regular, free-shape
- point, segment, line, ray, angle-line, arrow, arrow-curve

Return format (key structure only \u2014 the actual shape type and values come from the image):
{
  "shapes": [
    {
      "type": "<shape type string>",
      "points": [{"gridX": <int>, "gridY": <int>}, ...],
      "pointLabels": null | ["A", "B", ...],
      "guideVisibility": {"pointName": bool, "point": bool, "length": bool, "angle": bool, "height": bool, "radius": bool},
      "circleMeasureMode": null | "radius" | "diameter",
      "lengthItems": null | [{"visible": bool, "textMode": "normal"|"blank", "textDirection": "above"|"below"|"left"|"right"|"auto", "text": "...", "curveSide": 1|-1}],
      "angleItems": null | [{"visible": bool, "textMode": "normal"|"blank", "textDirection": "...", "text": "...", "detached": bool}],
      "heightItem": null | {"textMode": "normal"|"blank", "textDirection": "..."},
      "showUnit": bool
    }
  ],
  "guides": [
    {"type": "text", "text": "...", "position": {"gridX": <int>, "gridY": <int>}, "rotation": 0, "useLatex": false}
  ]
}

Rules:
- Return JSON only.
- If nothing is recognized, return {"shapes":[],"guides":[]}.
- circle uses points[0] = center and points[1] = edge.
- point uses 1 point.
- segment, line, ray, arrow, arrow-curve use 2 points.
- angle-line uses 3 points.
- polygon and free-shape use 3 or more points.
- polygon-regular may be returned as polygon if needed.
- Vertex labels such as A, B, C or \u3131, \u3134, \u3137 must go into pointLabels, not guides.
- Side length text such as 3cm or 6 cm must be treated as side length annotations, not free text guides.
- Angle text such as 30\u00b0 or 45\u00b0 must be treated as angle annotations, not free text guides.
- lengthItems[index].text may contain the exact user-facing side text for that edge.
- angleItems[index].text may contain the exact user-facing angle text for that vertex.
- lengthItems[index].visible or angleItems[index].visible can be false when that item should stay hidden.
- lengthItems[index].textMode can be "blank" when the edge should render as an answer blank.
- lengthItems[index].curveSide can be 1 or -1 to choose the curve direction.
- guideVisibility.point controls whether a small filled dot is drawn at each vertex. Set point: true when the image shows a visible dot or mark at a vertex or center. Set point: false when no dot is drawn.
- For circle: if the image shows a dot at the center, or the user mentions \uc911\uc810/\uc911\uc2ec/center point, set guideVisibility.point: true. If the center is visibly marked but the label character is unclear or ambiguous, use "\u3147" as the default center label (pointLabels[0] = "\u3147"). Only omit the label if it is clearly absent in both the image and the user hint.
- angleItems[index].detached can be true when the angle blank must appear OUTSIDE the shape (pointed to by a curved arrow). When detached, also set visible:true so the angle arc still renders at the vertex. Include a companion arrow-curve shape pointing from the vertex outward. The blank-box is placed at the arrow-curve endpoint.
- If the angle blank should appear inline (at the vertex itself), use textMode:"blank", visible:true, no detached.
- For triangle-right: always set the hypotenuse (longest side) to visible: false. Place measurements only on the two legs. Do not assign text to lengthItems at the hypotenuse position.
- shapes[0] is the main forced shape. Additional auxiliary shapes such as arrow or arrow-curve may appear in later entries.
- guides[] should only contain text that is truly separate from the main shape semantics.""";

    // ──────────────────────────────────────────────────────────────────────────
    // SYSTEM_ADDITIONAL_STATIC (useAISketch.ts buildPrompt lines 1115-1208)
    // ──────────────────────────────────────────────────────────────────────────
    private static final String SYSTEM_ADDITIONAL_STATIC = """
- Place the main shape roughly centered in the grid: prefer gridX 10\u201354, gridY 6\u201330. Make it large enough to occupy roughly 40\u201370% of the grid area.
- Prefer one main shape unless the image clearly contains multiple independent shapes.

CRITICAL \u2014 Index mapping (follow exactly, do not swap):
- points[i] is the vertex named pointLabels[i]. The order you assign pointLabels determines the index for everything else.
- angleItems[i] is the angle AT vertex points[i]. When the user/image specifies "angle at vertex X = 30\u00b0", find which index i has pointLabels[i] = "X", then set angleItems[i].text = "30\u00b0". Never assign an angle to the wrong vertex index.
- lengthItems[i] is the edge FROM points[i] TO points[(i+1) % n]:
  For 3 vertices [A,B,C]: lengthItems[0]=A\u2192B, lengthItems[1]=B\u2192C, lengthItems[2]=C\u2192A.
  For 4 vertices [A,B,C,D]: lengthItems[0]=A\u2192B, lengthItems[1]=B\u2192C, lengthItems[2]=C\u2192D, lengthItems[3]=D\u2192A.
- Edge naming: "CB" or "BC" means the edge between vertices C and B \u2014 regardless of direction. Match it to the lengthItems index where one endpoint is C and the other is B. Do NOT assign it to an unrelated edge.
  For pointLabels=['A','B','C']: "AB"/"BA"=lengthItems[0], "BC"/"CB"=lengthItems[1], "CA"/"AC"=lengthItems[2].
  For example: if user says "AC=3cm", assign text:"3cm" to lengthItems[2] (C\u2192A). If user says "CB=6cm", assign text:"6cm" to lengthItems[1] (B\u2192C).
- CRITICAL \u2014 Never convert explicit measurements to blank: If the user hint or image specifies a numeric value for an edge or angle (e.g. "CB=6cm", "A=30\u00b0"), ALWAYS assign that value as text. NEVER change a known measurement into textMode:"blank". Only use textMode:"blank" for items explicitly described as unknown/blank (e.g. "B\uc758 \uac01\ub3c4 \ube48\uce78", "\u25a1").
- For triangle-right: find the 90\u00b0 vertex (from hint or image). The hypotenuse is the edge OPPOSITE that vertex. Use the following lookup table (all 3 cases \u2014 follow exactly):
    right angle at index 0 (A): hide lengthItems[1] (B\u2192C, opposite A). Legs are lengthItems[0] (A\u2192B) and lengthItems[2] (C\u2192A).
    right angle at index 1 (B): hide lengthItems[2] (C\u2192A, opposite B). Legs are lengthItems[0] (A\u2192B) and lengthItems[1] (B\u2192C).
    right angle at index 2 (C): hide lengthItems[0] (A\u2192B, opposite C). Legs are lengthItems[1] (B\u2192C) and lengthItems[2] (C\u2192A).
  For the 90\u00b0 vertex r: set angleItems[r] = { text: "90\u00b0", visible: false, textMode: "normal" }. The text "90\u00b0" must be present so the app can correct coordinates \u2014 it will NOT be displayed. The right-angle square marker is rendered automatically. The other two vertices get their angle text normally.
- Coordinate placement workflow for triangle-right (follow exactly in this order):
    Step 1. Identify the right-angle vertex name from hint/image (e.g. "C is 90\u00b0").
    Step 2. Find its index r in pointLabels (e.g. pointLabels=['A','B','C'] \u2192 C is index 2).
    Step 3. Place points[r] (the right-angle vertex) at a convenient grid position first (e.g. gridX 20, gridY 26). This vertex is at the geometric corner.
    Step 4. Place points[(r+2)%3] along one perpendicular axis from the corner (e.g. 3 units above: gridX 20, gridY 23). This is leg 1.
    Step 5. Place points[(r+1)%3] along the other perpendicular axis from the corner (e.g. 6 units right: gridX 26, gridY 26). This is leg 2.
    Step 6. The right-angle vertex must be the GEOMETRIC CORNER \u2014 never put a different vertex at the corner if C is designated as 90\u00b0.
    Example (right angle at C, index 2, AC=3cm, CB=6cm): points[2]=C=(20,26), points[0]=A=(20,23) [3 above C], points[1]=B=(26,26) [6 right of C].
- Coordinate placement workflow for triangle-equilateral (follow exactly):
    Step 1. Read the side length s from the hint or image. The grid distance between any two adjacent vertices MUST equal s grid units.
    Step 2. Choose the winding order (clockwise or counterclockwise) to match the image. In screen coordinates (Y increases downward), clockwise on screen = top \u2192 bottom-right \u2192 bottom-left. Counterclockwise = top \u2192 bottom-left \u2192 bottom-right.
    Step 3. Place points[0] and points[1] exactly s units apart to form the base edge. Prefer a horizontal base.
    Step 4. Place points[2] roughly on the correct side (above or below) \u2014 the app will snap it to the exact equilateral position. The sign of points[2].y relative to the base determines which side the apex goes.
    Step 5. Center the triangle within gridX 10\u201354, gridY 6\u201330.
    Example (s=5cm, clockwise, \u3131=top, \u3134=bottom-right, \u3137=bottom-left): base center\u2248(32,24), points[0]=\u3131=(32,20), points[1]=\u3134=(35,25), points[2]=\u3137=(29,25). [distance(\u3131,\u3134)\u22485, distance(\u3134,\u3137)=6\u2192app corrects to equilateral]
    CRITICAL: scale takes priority over visual size. For s=5cm the triangle will be small (5\u00d75 grid area); do NOT stretch coordinates to fill the grid.
- Coordinate placement workflow for triangle-isosceles (follow exactly):
    Step 1. Identify the three side lengths (two equal legs and one base) from the hint or image.
    Step 2. Identify which vertex is the apex (the vertex between the two equal legs). The other two are base vertices.
    Step 3. Place the two base vertices so that the distance between them = base length in cm grid units. Prefer a horizontal base.
    Step 4. Place the apex vertex above (or below) the midpoint of the base. The height h = \u221a(leg\u00b2 \u2212 (base/2)\u00b2). The apex should be h grid units from the base midpoint.
    Step 5. Center the triangle within gridX 10\u201354, gridY 6\u201330.
    Example (legs=6cm, base=4cm \u2192 h=\u221a(36\u22124)=5.66): base center\u2248(32,24), base vertices: \u3134=(30,24), \u3137=(34,24). Apex \u3131 at (32, 24\u22125.66)=(32,18). \u2192 points=[\u3131=(32,18), \u3134=(30,24), \u3137=(34,24)].
    CRITICAL: set coordinates so that dist(apex, base-vertex)=leg and dist(base-vertex1, base-vertex2)=base. The app auto-corrects coordinates to exact lengths \u2014 but you must provide approximately correct scale. Do NOT use large coordinates that fill the grid.
- Coordinate placement for triangle (general/free triangle):
    Prefer a natural upright layout: place the longest known side horizontally as the base, and the remaining vertex above (or below) the base.
    Set coordinates so each specified side equals its cm value. Sides marked "\uc790\ub3d9\uacc4\uc0b0" or not specified do not need exact placement \u2014 the app derives them.
    Example (AB=4cm, BC=10cm): longest side is BC \u2192 place B=(27,24), C=(37,24) [10 units apart, horizontal]. Place A above B so dist(A,B)=4, e.g. A=(27,20). Do NOT stack A directly above B vertically if BC is meant to be the base.

CRITICAL \u2014 Vertex position reading (do not impose winding order theory):
- Read each vertex label from its ACTUAL position in the image. The label written/drawn near a corner IS that corner's label \u2014 do NOT reassign it to another corner based on clockwise/counterclockwise theory.
- After identifying which label is at which corner (top / bottom-left / bottom-right), assign coordinates to match those positions exactly. The winding order in the output is whatever the image shows \u2014 do not swap labels to achieve a specific CW/CCW direction.
- EDGE INDEX VERIFICATION \u2014 once vertex labels and coordinates are assigned, for each length text in the image:
    (1) Find which edge the text is visually adjacent to (by proximity).
    (2) Identify the two vertex labels at the ends of that edge.
    (3) Map to lengthItems index: for pointLabels=[X,Y,Z], X-Y=index0, Y-Z=index1, Z-X=index2.
    (4) Set visible:true ONLY for that index.
    Example: if "5cm" is between top-vertex(\u3131) and bottom-left-vertex(\u3137), and pointLabels=[\u3131,\u3134,\u3137], then \u3137-\u3131 = index2 \u2192 set lengthItems[2] visible:true.
- ANGLE BLANK VERIFICATION \u2014 for each \u25a1 box drawn in the image:
    (1) Find the nearest vertex (by visual distance to the corner).
    (2) Look up that vertex's index in pointLabels.
    (3) Set angleItems[that index] = textMode:"blank", visible:true.
    A small rectangle or square drawn at or inside a corner = angle blank for that corner's vertex.

CRITICAL \u2014 Coordinate scale (1 grid unit = 1 cm in this app):
- When the user or image specifies side lengths in cm, set grid coordinates so that the actual distance between vertices matches. Example: AC=3cm \u2192 distance(A,C) = 3 grid units. CB=6cm \u2192 distance(C,B) = 6 grid units.
- For triangle-right with right angle at vertex C, legs AC=3cm and CB=6cm: place C at a convenient grid position (e.g. gridX 20, gridY 26), A exactly 3 units away along one axis (e.g. gridX 20, gridY 23), B exactly 6 units away along the perpendicular axis (e.g. gridX 26, gridY 26).
- Hand-drawn coordinate proportions are approximate. Do not use drawn proportions as the primary source for side lengths.
- Use this priority for interpretation:
  1. User hint (if provided above).
  2. Length, angle, label, and unit text written inside the image.
  3. Overall visual layout and markers such as right-angle boxes or arc marks.
  4. Coordinate proportions only as a last fallback.
- Vertex labels like A, B, C or \u3131, \u3134, \u3137 must populate pointLabels, never guides[].
- Korean letters \u3131, \u3134, \u3137, \u3139 near corners are vertex labels \u2014 treat them exactly like A, B, C. Do NOT place them in guides[].
- Side text like 3cm or 6cm must be interpreted as side length information for the shape.
- Distinguish vertex labels from measurements carefully.
- A single uppercase letter near a corner, such as A or B, is usually a vertex label and belongs in pointLabels.
- A text item attached to a side together with a unit such as cm, mm, or m is a side measurement, not a vertex label.
- Do not invent algebraic variables like b or x unless the handwriting clearly shows a letter.
- If a handwritten side label could be read as either a digit or a letter, prefer the digit when the text is followed by a unit and represents a length.
- For example, treat "6cm" as a side length even if the handwritten 6 looks similar to a lowercase b.
- Angle text like 30\u00b0 must be interpreted as angle information for the shape.
- If the image requests only some side lengths or some angles, hide the others with visible: false.
- If the image shows a blank answer box for a side length, use textMode: "blank" for that edge.
- A blank answer box drawn on paper (a small square \u25a1) must be represented as textMode "blank" with no text value, never as a letter character in the text field. This applies to ALL shape types (triangles, rectangles, polygons, etc.) for BOTH side lengths (lengthItems) AND angles (angleItems). If \u25a1 is drawn at or near a vertex angle of any polygon, set textMode:"blank", visible:true for that angleItems index. Do not write the \u25a1 character as text.
- After establishing the vertex winding order, identify the visual position (left/right/base) of each length label in the image, then map it to the correct lengthItems index. For equilateral triangles with only one visible side text: find which side in the image has the text, determine its lengthItems index from the vertex ordering, set that index visible:true and all others visible:false.
- The value before a unit such as cm, mm, or \u00b0 must be a number or empty. If a single character before the unit could be either a handwritten digit or a letter (e.g. b vs 6, l vs 1, O vs 0), always choose the digit interpretation.
- If the angle blank appears AT the vertex (inline): set angleItems[r] = { textMode:"blank", visible:true, detached:false }.
- DEFAULT for angle blanks: always use inline (detached:false) UNLESS the image visually shows a curved arrow leading outside the shape, OR the user hint explicitly mentions an external arrow (\ud654\uc0b4\ud45c). The phrase "\ube48\uce78\uc73c\ub85c \ud45c\uc2dc" alone means inline \u2014 do NOT add detached:true or an arrow-curve companion.
- If the angle blank appears OUTSIDE the shape (with a curved arrow pointing to it AND the image or hint confirms this): set angleItems[r] = { textMode:"blank", detached:true, visible:true }. Use visible:true so the angle arc still renders at the vertex. Include a companion arrow-curve shape in shapes[]. The blank-box will be placed at the arrow-curve endpoint automatically.
- For the companion arrow-curve, place its endpoint roughly 3\u20135 grid units away from the vertex. Do NOT extend the arrow to the edge of the grid \u2014 keep the blank close to the shape.
- ONLY include auxiliary arrow-curve shapes when the image visually shows a curved arrow, or the user hint explicitly mentions one. Never add arrow-curve shapes spontaneously.
- Use exact detected values from image text or user hint for lengthItems[index].text and angleItems[index].text.
- Do not return those semantic labels as free text guides unless they are clearly separate notes outside the diagram.
- textDirection for length and angle items: use "auto" in almost all cases. The app computes the correct outward label position from geometry (perpendicular to the edge, away from the centroid). Only use "above"/"below"/"left"/"right" when the auto position genuinely conflicts with another element and you need to move the label further away.""";

    // ──────────────────────────────────────────────────────────────────────────
    // USER_INSTRUCTION_STATIC (useAISketch.ts buildUserInstruction lines 1225-1250)
    // ──────────────────────────────────────────────────────────────────────────
    private static final List<String> USER_INSTRUCTION_STATIC = List.of(
        "Vertex names A/B/C or \u3131/\u3134/\u3137 \u2192 pointLabels. Never put vertex labels in guides[].",
        "\u3131, \u3134, \u3137 at corners are Korean vertex labels, equivalent to A, B, C.",
        "INDEX RULE: angleItems[i] = angle at the vertex whose name is pointLabels[i]. Never swap vertex indices.",
        "INDEX RULE: lengthItems[i] = edge from points[i] to points[i+1]. For [A,B,C]: index0=A\u2192B, index1=B\u2192C, index2=C\u2192A.",
        "EDGE RULE: \"CB\" means the edge between C and B. Map to the correct lengthItems index \u2014 do NOT put it on the hypotenuse.",
        "SCALE RULE: 1 grid unit = 1 cm. Set coordinates so that distances between vertices match specified lengths in cm.",
        "Side texts like 3cm or 6cm \u2192 lengthItems. Angle texts like 30\u00b0 \u2192 angleItems.",
        "textDirection: use \"auto\" for all length and angle items. The app places labels automatically based on edge geometry (outward from centroid). Do NOT use \"above\"/\"below\"/\"left\"/\"right\" unless an explicit override is required.",
        "If only some lengths/angles appear, hide the rest with visible: false.",
        "Blank \u25a1 on a side \u2192 textMode:\"blank\" in lengthItems. Blank \u25a1 at a vertex angle of ANY shape (triangle, rectangle, polygon, etc.) \u2192 textMode:\"blank\", visible:true in angleItems. NEVER convert explicit numeric measurements (like \"6cm\") to blank.",
        "Blank angle: default is INLINE (detached:false, textMode:\"blank\", visible:true). Only use detached:true + arrow-curve if the image visually shows a curved arrow, or the hint explicitly says \ud654\uc0b4\ud45c. \"\ube48\uce78\uc73c\ub85c \ud45c\uc2dc\" alone = inline.",
        "Blank angle OUTSIDE shape (with arrow) \u2192 textMode:\"blank\", detached:true, visible:true (NOT false \u2014 keeps arc visible at vertex). Plus arrow-curve companion shape. Arrow endpoint 3\u20135 grid units from vertex, not at grid edge.",
        "VERTEX POSITION: read each label from its actual corner in the image. Do NOT swap labels to force CW/CCW. The label at a corner IS that corner's pointLabel. Assign coordinates to match where each label is drawn.",
        "SCALE RULE (ALL SHAPES): 1 grid unit = 1 cm. Set coordinates so actual vertex distances match specified lengths. A 6cm side must be exactly 6 grid units long. Do NOT stretch shapes to fill the grid. The app auto-corrects to exact lengths but requires approximately correct scale.",
        "CONSTRAINED SHAPE RULE: triangle-equilateral and rect-square have fixed geometric constraints (all sides equal). The app enforces these constraints automatically, but ONLY scales from points[0]\u2192points[1] as the base. Therefore you MUST set dist(points[0], points[1]) to exactly the side length in grid units. If you give a wrong scale for this base edge, ALL sides will be wrong \u2014 the constraint correction cannot fix a wrong base.",
        "EQUILATERAL SCALE: for triangle-equilateral, dist(points[0], points[1]) MUST equal the side length in cm (e.g. 10cm \u2192 exactly 10 grid units). ALL three sides will be auto-equalized from this base. Do NOT place points[0] and points[1] at a different scale and expect the third point to compensate.",
        "SQUARE SCALE: for rect-square, dist(points[0], points[1]) MUST equal the side length in cm. The remaining two corners are auto-computed as a perfect axis-aligned square. Do NOT stretch points to fill the grid.",
        "ISOSCELES SCALE: for triangle-isosceles, identify apex (vertex between equal legs) and base. Place base vertices so dist(base1,base2)=base_cm. Place apex h=\u221a(leg\u00b2-(base/2)\u00b2) grid units above base midpoint. E.g. legs=6cm, base=4cm \u2192 h\u22485.66, base=(30,24)to(34,24), apex=(32,18).",
        "GENERAL TRIANGLE LAYOUT: for triangle type, place the longest known side as a horizontal base. Place the other vertices above or below the base. Set coordinates so each side distance equals its cm value. E.g. AB=4cm, BC=10cm: place B=(27,24), C=(37,24) [BC=10 horizontal], then A above B so dist(A,B)=4 e.g. A=(27,20).",
        "EDGE INDEX CHECK: for each length text \u2014 find the two vertex labels it sits between \u2192 look up their lengthItems index (for [\u3131,\u3134,\u3137]: \u3131-\u3134=0, \u3134-\u3137=1, \u3137-\u3131=2) \u2192 set only that index visible:true.",
        "LINE/RAY LENGTH: for line and ray types, a length label between the two points is optional. If the image or hint shows a length value annotated between the two points, set lengthItems[0] = { text: \"Xcm\", visible: true }. If no length is annotated, set lengthItems[0] = { visible: false }.",
        "ANGLE BLANK CHECK: for each \u25a1 box near a corner \u2014 find the nearest vertex label \u2192 get its index \u2192 set angleItems[index]=textMode:\"blank\",visible:true.",
        "ANGLE AUTO COMPUTE: when the hint says \"\uc790\ub3d9\uacc4\uc0b0\ud558\uc5ec \ud45c\uc2dc\" or \"\uc790\ub3d9\uacc4\uc0b0\" for angles, set those angleItems to textMode:\"auto\", visible:true. The app will compute the exact interior angle from the vertex coordinates. Do NOT set textMode:\"blank\" \u2014 \"\uc790\ub3d9\uacc4\uc0b0\" means a computed numeric value will be shown, not a blank box.",
        "POINT DOT RULE: guideVisibility.point: true = renders a filled dot at each vertex/center. Set to true if the image shows a dot at a vertex or center, or if the hint mentions \uc911\uc810/\uc911\uc2ec/center. Otherwise false.",
        "For circle: if center is labeled or marked, set pointLabels[0] = center label, guideVisibility.pointName: true, guideVisibility.point: true. If the center is visibly marked (dot present) but the label text is unclear or ambiguous, use \"\u3147\" as the default center label."
    );

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * GPT-4o 시스템 프롬프트 생성 (TypeScript buildPrompt() 포팅)
     */
    public static String buildSystemPrompt(String forcedShapeType, String userHint) {
        String trimmedHint = (userHint != null) ? userHint.trim() : "";
        String shapeExample = ShapeExamples.buildShapeExample(forcedShapeType);

        String hintSection = trimmedHint.isEmpty()
                ? "- No user hint provided. Rely on visible text in the image (lengths, angles, labels) first."
                : "- [TOP PRIORITY] The user provided a hint. Apply it first before anything else:\n  \"\"\"\n  " + trimmedHint + "\n  \"\"\"";

        return BASE_PROMPT
                + "\n\nReference example for \"" + forcedShapeType + "\" (follow this structure exactly for this shape type):\n"
                + shapeExample
                + "\n\nAdditional instructions:\n"
                + hintSection + "\n"
                + "- Do not infer the shape type freely. The required shape type is \"" + forcedShapeType + "\".\n"
                + "- shapes[0].type must be exactly \"" + forcedShapeType + "\".\n"
                + SYSTEM_ADDITIONAL_STATIC;
    }

    /**
     * GPT-4o 사용자 메시지 생성 (TypeScript buildUserInstruction() 포팅)
     */
    public static String buildUserInstruction(String forcedShapeType, String userHint) {
        String trimmedHint = (userHint != null) ? userHint.trim() : "";
        List<String> lines = new ArrayList<>();

        if (!trimmedHint.isEmpty()) {
            lines.add("[IMPORTANT] User hint \u2014 apply this first: \"" + trimmedHint + "\"");
            lines.add("");
        }

        lines.add("Extract the main math diagram from the image as JSON.");
        lines.add("Forced shape type: " + forcedShapeType + ". Do not re-classify from the image.");
        lines.add(trimmedHint.isEmpty()
                ? "No user hint. Use visible lengths, angles, and labels from the image."
                : "Reconfirm user hint: " + trimmedHint);

        lines.addAll(USER_INSTRUCTION_STATIC);

        return String.join("\n", lines);
    }
}
