package be.webtechie.vaadin.pi4j.views.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import in.virit.color.NamedColor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.vaadin.firitin.components.VSvg;
import org.vaadin.firitin.element.svg.AnimateElement;
import org.vaadin.firitin.element.svg.DefsElement;
import org.vaadin.firitin.element.svg.MarkerElement;
import org.vaadin.firitin.element.svg.PathElement;
import org.vaadin.firitin.element.svg.RectElement;
import org.vaadin.firitin.element.svg.SvgGraphicsElement.LineCap;
import org.vaadin.firitin.element.svg.SvgGraphicsElement.LineJoin;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.vaadin.firitin.element.svg.CircleElement;
import org.vaadin.firitin.element.svg.GElement;
import org.vaadin.firitin.element.svg.LineElement;
import org.vaadin.firitin.element.svg.PathBuilder;

/**
 * A simple snake visualization component using SVG.
 * The snake moves continuously and its direction can be changed externally.
 * This component can be reused with any directional input (joystick, keyboard, etc.).
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SnakeGame extends VSvg {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private static final int GRID_SIZE = 12;
    private static final int CELL_SIZE = 18;
    private static final int INITIAL_LENGTH = 4;
    private static final int MOVE_INTERVAL_MS = 400;

    private final LinkedList<Point> snake = new LinkedList<>();
    private PathElement snakeBody;
    private MarkerElement snakeHead;
    private PathElement snakeHeadShape;
    private RectElement background;
    private AnimateElement warningAnimation;

    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private boolean crashed = false;
    private boolean gameStarted = false;
    private List<Point> previousPositions = List.of();
    private final LinkedList<AnimateElement> animationQueue = new LinkedList<>();
    private static final int MAX_QUEUED_ANIMATIONS = 5;

    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> gameLoop;
    private UI ui;

    private record Point(int x, int y) {}

    public SnakeGame(TaskScheduler taskScheduler) {
        super(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        this.taskScheduler = taskScheduler;
        setWidth(GRID_SIZE * CELL_SIZE + "px");
        setHeight(GRID_SIZE * CELL_SIZE + "px");
    }

    /**
     * Builds all SVG elements. Called from onAttach to fix Safari timing issues.
     */
    private void buildSvgElements() {
        // Draw background
        background = new RectElement();
        background.bounds(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE)
                .fill(NamedColor.DARKSLATEGRAY)
                .stroke(NamedColor.GRAY)
                .strokeWidth(2);
        getElement().appendChild(background);

        // Draw grid lines for visual guidance
        drawGrid();

        // Snake head marker (artistic snake head pointing right)
        // Head shape - rounded with a pointed snout
        snakeHeadShape = new PathElement(p -> p
                .moveTo(0, 5)                           // left middle (back of head)
                .quadraticBezierTo(0, 0, 5, 0)          // top-left curve
                .quadraticBezierTo(12, 0, 14, 5)       // top curve to snout
                .quadraticBezierTo(12, 10, 5, 10)      // bottom curve from snout
                .quadraticBezierTo(0, 10, 0, 5)        // bottom-left curve back
                .closePath());
        snakeHeadShape.fill(NamedColor.LIMEGREEN)
                .stroke(NamedColor.DARKGREEN)
                .strokeWidth(0.5);

        // Forked tongue
        var tongue = new PathElement(p -> p
                .moveTo(13, 5)
                .lineTo(18, 5)
                .lineTo(20, 3)
                .moveTo(18, 5)
                .lineTo(20, 7));
        tongue.fill("none")
                .stroke(NamedColor.RED)
                .strokeWidth(0.8)
                .strokeLinecap(LineCap.ROUND);

        // Eyes - white background
        var leftEye = new CircleElement().center(6, 3).r(1.5)
                .fill(NamedColor.WHITE).stroke(NamedColor.DARKGREEN).strokeWidth(0.3);
        var rightEye = new CircleElement().center(6, 7).r(1.5)
                .fill(NamedColor.WHITE).stroke(NamedColor.DARKGREEN).strokeWidth(0.3);

        // Pupils with rolling animation
        var leftPupil = new CircleElement().center(7, 3).r(0.7)
                .fill(NamedColor.BLACK);
        var rightPupil = new CircleElement().center(7, 7).r(0.7)
                .fill(NamedColor.BLACK);

        // Animate pupils with subtle "drunken" effect (alcohol level 0.4)
        // Left pupil - slight roll with minor vertical drift
        var leftPupilRollX = new AnimateElement()
                .attributeName("cx")
                .values("7;6.2;6.6;7.3;7.1;7")
                .keyTimes(0, 0.12, 0.28, 0.42, 0.6, 1)
                .dur("5s")
                .repeatIndefinitely();
        leftPupil.appendChild(leftPupilRollX);

        var leftPupilRollY = new AnimateElement()
                .attributeName("cy")
                .values("3;2.8;3.1;2.9;3;3")
                .keyTimes(0, 0.15, 0.3, 0.45, 0.6, 1)
                .dur("5s")
                .repeatIndefinitely();
        leftPupil.appendChild(leftPupilRollY);

        // Right pupil - slightly different timing, subtle opposite drift
        var rightPupilRollX = new AnimateElement()
                .attributeName("cx")
                .values("7;7.3;6.7;6.3;6.9;7")
                .keyTimes(0, 0.1, 0.25, 0.4, 0.6, 1)
                .dur("5s")
                .repeatIndefinitely();
        rightPupil.appendChild(rightPupilRollX);

        var rightPupilRollY = new AnimateElement()
                .attributeName("cy")
                .values("7;7.2;6.9;7.1;7;7")
                .keyTimes(0, 0.18, 0.32, 0.48, 0.6, 1)
                .dur("5s")
                .repeatIndefinitely();
        rightPupil.appendChild(rightPupilRollY);

        // Group all head elements
        var headGroup = new GElement();
        headGroup.add(snakeHeadShape, tongue, leftEye, rightEye, leftPupil, rightPupil);

        snakeHead = new MarkerElement("snakehead")
                .viewBox(-2, -2, 24, 14)
                .ref(0, 5)
                .markerSize(56, 36)
                .markerUnits(MarkerElement.MarkerUnits.USER_SPACE_ON_USE)
                .orientAuto()
                .add(headGroup);

        getElement().appendChild(new DefsElement(snakeHead));

        // Snake body as a single path
        snakeBody = new PathElement();
        snakeBody.fill("none")
                .stroke(NamedColor.FORESTGREEN)
                .strokeWidth(CELL_SIZE - 2)
                .strokeLinecap(LineCap.ROUND)
                .strokeLinejoin(LineJoin.ROUND)
                .markerEnd(snakeHead);
        getElement().appendChild(snakeBody);

        // Initialize snake in the middle
        initializeSnake();
    }

    private void drawGrid() {
        for (int i = 0; i <= GRID_SIZE; i++) {
            // Vertical lines
            var vLine = new LineElement()
                    .points(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE)
                    .stroke(NamedColor.DIMGRAY)
                    .strokeOpacity(0.3);
            getElement().appendChild(vLine);

            // Horizontal lines
            var hLine = new LineElement()
                    .points(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE)
                    .stroke(NamedColor.DIMGRAY)
                    .strokeOpacity(0.3);
            getElement().appendChild(hLine);
        }
    }

    private void initializeSnake() {
        snake.clear();
        crashed = false;
        removeWarningAnimation();

        int startX = GRID_SIZE / 2;
        int startY = GRID_SIZE / 2;

        for (int i = 0; i < INITIAL_LENGTH; i++) {
            snake.add(new Point(startX - i, startY));
        }

        updateSnakeGraphics();
    }

    private void updateSnakeGraphics() {
        // Update body path - draw from tail to head (no animation for initial draw)
        if (snake.size() > 1) {
            snakeBody.d(pb -> buildPath(pb, snake));
            previousPositions = List.copyOf(snake);
        }
    }

    private void animateSnakeMovement() {
        // Remove oldest animations when queue is full (keeps some to avoid Safari flashing)
        while (animationQueue.size() >= MAX_QUEUED_ANIMATIONS) {
            AnimateElement oldest = animationQueue.removeFirst();
            oldest.removeFromParent();
        }

        // Animate from previous positions to current positions
        AnimateElement animation = snakeBody.animateD(
                from -> buildPath(from, previousPositions),
                to -> buildPath(to, snake),
                Duration.ofMillis(MOVE_INTERVAL_MS)
        );
        animationQueue.addLast(animation);
        previousPositions = List.copyOf(snake);
    }

    private void buildPath(PathBuilder pb, List<Point> positions) {
        if (positions.size() < 2) {
            return;
        }

        // Start from tail (last element) and go towards head (first element)
        for (int i = positions.size() - 1; i >= 0; i--) {
            Point p = positions.get(i);
            double cx = p.x * CELL_SIZE + CELL_SIZE / 2.0;
            double cy = p.y * CELL_SIZE + CELL_SIZE / 2.0;

            if (i == positions.size() - 1) {
                pb.moveTo(cx, cy);
            } else {
                pb.lineTo(cx, cy);
            }
        }
    }

    /**
     * Sets the direction for the snake to move.
     * The direction change will take effect on the next move.
     * Prevents 180-degree turns (can't go directly backwards).
     * If the snake has crashed, a valid direction input will resume the game from current position.
     *
     * @param direction the new direction
     */
    public void setDirection(Direction direction) {
        if (direction == null) {
            return;
        }

        // If crashed, resume the game from current position with the new direction
        if (crashed) {
            resume(direction);
            return;
        }

        // Start game on first joystick input (only if SVG is initialized)
        if (!gameStarted && ui != null && initialized) {
            gameStarted = true;
            currentDirection = direction;
            nextDirection = direction;
            startGameLoop();
            return;
        }

        // Prevent 180-degree turns
        if ((currentDirection == Direction.UP && direction == Direction.DOWN) ||
            (currentDirection == Direction.DOWN && direction == Direction.UP) ||
            (currentDirection == Direction.LEFT && direction == Direction.RIGHT) ||
            (currentDirection == Direction.RIGHT && direction == Direction.LEFT)) {
            return;
        }
        nextDirection = direction;
    }

    /**
     * Resumes the game from current position with a new direction.
     */
    private void resume(Direction direction) {
        crashed = false;
        removeWarningAnimation();
        currentDirection = direction;
        nextDirection = direction;
        updateSnakeGraphics();
        if (ui != null) {
            startGameLoop();
        }
    }

    /**
     * Gets the current direction of the snake.
     *
     * @return the current direction
     */
    public Direction getDirection() {
        return currentDirection;
    }

    /**
     * Returns whether the snake has crashed into a wall.
     *
     * @return true if crashed
     */
    public boolean isCrashed() {
        return crashed;
    }

    private void move() {
        if (crashed) {
            return;
        }

        currentDirection = nextDirection;

        Point head = snake.getFirst();
        int newX = head.x;
        int newY = head.y;

        switch (currentDirection) {
            case UP -> newY--;
            case DOWN -> newY++;
            case LEFT -> newX--;
            case RIGHT -> newX++;
        }

        // Check for wall collision
        if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE) {
            crash();
            return;
        }

        Point newHead = new Point(newX, newY);

        // Add new head, remove tail
        snake.addFirst(newHead);
        snake.removeLast();

        // Animate the movement
        animateSnakeMovement();
    }

    private void crash() {
        crashed = true;
        stopGameLoop();

        // Add warning animation - flash between red and original color
        warningAnimation = new AnimateElement();
        warningAnimation.attributeName("stroke")
                .values(NamedColor.FORESTGREEN.toString() + ";" +
                        NamedColor.RED.toString() + ";" +
                        NamedColor.FORESTGREEN.toString())
                .dur("3s")
                .repeatIndefinitely();
        snakeBody.appendChild(warningAnimation);

        // Also animate the head marker shape
        AnimateElement headAnimation = new AnimateElement();
        headAnimation.attributeName("fill")
                .values(NamedColor.LIMEGREEN.toString() + ";" +
                        NamedColor.RED.toString() + ";" +
                        NamedColor.LIMEGREEN.toString())
                .dur("3s")
                .repeatIndefinitely();
        snakeHeadShape.appendChild(headAnimation);
    }

    private void removeWarningAnimation() {
        if (warningAnimation != null) {
            snakeBody.removeAllChildren();
            snakeHeadShape.removeAllChildren();
            warningAnimation = null;
            animationQueue.clear();
        }
        // Restore colors
        snakeBody.stroke(NamedColor.FORESTGREEN);
        snakeHeadShape.fill(NamedColor.LIMEGREEN);
    }

    private boolean initStarted = false;
    private boolean initialized = false;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = attachEvent.getUI();

        // Defer SVG building until after the view is fully attached (fixes Safari timing issue)
        if (!initStarted) {
            initStarted = true; // Prevent double scheduling
            getElement().executeJs("").then(v -> {
                buildSvgElements();
                initialized = true; // Now ready for game to start
            });
        }
        // Game starts on first joystick input, not automatically
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        stopGameLoop();
        super.onDetach(detachEvent);
    }

    private void startGameLoop() {
        if (gameLoop != null) {
            return; // Already running
        }
        gameLoop = taskScheduler.scheduleAtFixedRate(() -> {
            if (ui != null) {
                ui.access(this::move);
            }
        }, Instant.now().plusMillis(MOVE_INTERVAL_MS), Duration.ofMillis(MOVE_INTERVAL_MS));
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.cancel(false);
            gameLoop = null;
        }
    }

    /**
     * Resets the snake to its initial state. Snake waits for first joystick input to start moving.
     */
    public void reset() {
        stopGameLoop();
        currentDirection = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameStarted = false;
        initializeSnake();
    }
}
