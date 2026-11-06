package poc;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubbleExample extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int RADIUS = 45;

    private final List<Bubble> bubbles = new ArrayList<>();
    private final Random random = new Random();

    private final Color[] colors = {
            new Color(120, 190, 255),
            new Color(255, 160, 190),
            new Color(180, 150, 255),
            new Color(130, 230, 200),
            new Color(255, 210, 130)
    };

    public BubbleExample() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(245, 250, 255));

        createBubbles(5);

        Timer timer = new Timer(1, e -> {
            updateBubbles();
            repaint();
        });

        timer.start();
    }

    private void createBubbles(int count) {
        for (int i = 0; i < count; i++) {
            Bubble bubble = null;

            // 기존 비눗방울과 겹치지 않는 위치 찾기
            for (int attempt = 0; attempt < 1000; attempt++) {
                double x = RADIUS
                        + random.nextDouble() * (WIDTH - RADIUS * 2);

                double y = RADIUS
                        + random.nextDouble() * (HEIGHT - RADIUS * 2);

                boolean overlap = false;

                for (Bubble other : bubbles) {
                    double dx = x - other.x;
                    double dy = y - other.y;

                    double minDistance = RADIUS + other.radius;

                    if (dx * dx + dy * dy
                            < minDistance * minDistance) {
                        overlap = true;
                        break;
                    }
                }

                if (!overlap) {
                    double vx = 1 + random.nextDouble() * 3;
                    double vy = 1 + random.nextDouble() * 3;

                    // 이동 방향 랜덤 설정
                    if (random.nextBoolean()) vx *= -1;
                    if (random.nextBoolean()) vy *= -1;

                    bubble = new Bubble(
                            x, y, vx, vy,
                            RADIUS,
                            colors[i % colors.length]
                    );
                    break;
                }
            }

            if (bubble != null) {
                bubbles.add(bubble);
            }
        }
    }

    private void updateBubbles() {

        // 1. 비눗방울 이동
        for (Bubble bubble : bubbles) {
            bubble.x += bubble.vx;
            bubble.y += bubble.vy;
        }

        // 2. 비눗방울끼리 충돌 처리
        for (int i = 0; i < bubbles.size(); i++) {
            for (int j = i + 1; j < bubbles.size(); j++) {
                resolveCollision(
                        bubbles.get(i),
                        bubbles.get(j)
                );
            }
        }

        // 3. 벽 충돌 처리
        for (Bubble bubble : bubbles) {
            resolveWallCollision(bubble);
        }
    }

    private void resolveWallCollision(Bubble bubble) {
        int width = getWidth();
        int height = getHeight();

        if (width <= bubble.radius * 2
                || height <= bubble.radius * 2) {
            return;
        }

        if (bubble.x - bubble.radius < 0) {
            bubble.x = bubble.radius;
            bubble.vx = Math.abs(bubble.vx);
        } else if (bubble.x + bubble.radius > width) {
            bubble.x = width - bubble.radius;
            bubble.vx = -Math.abs(bubble.vx);
        }

        if (bubble.y - bubble.radius < 0) {
            bubble.y = bubble.radius;
            bubble.vy = Math.abs(bubble.vy);
        } else if (bubble.y + bubble.radius > height) {
            bubble.y = height - bubble.radius;
            bubble.vy = -Math.abs(bubble.vy);
        }
    }

    private void resolveCollision(Bubble a, Bubble b) {

        double dx = b.x - a.x;
        double dy = b.y - a.y;

        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = a.radius + b.radius;

        if (distance >= minDistance) {
            return;
        }

        // 중심이 완전히 겹치는 경우
        if (distance == 0) {
            dx = 1;
            dy = 0;
            distance = 1;
        }

        // 충돌 방향의 단위 벡터
        double nx = dx / distance;
        double ny = dy / distance;

        // 겹친 비눗방울 분리
        double overlap = minDistance - distance;

        a.x -= nx * overlap / 2;
        a.y -= ny * overlap / 2;

        b.x += nx * overlap / 2;
        b.y += ny * overlap / 2;

        // 상대 속도
        double rvx = b.vx - a.vx;
        double rvy = b.vy - a.vy;

        // 충돌 방향의 상대 속도
        double velocityAlongNormal = rvx * nx + rvy * ny;

        // 이미 멀어지는 중이면 속도 변경하지 않음
        if (velocityAlongNormal >= 0) {
            return;
        }

        // 동일 질량, 완전 탄성 충돌
        double impulse = -velocityAlongNormal;

        a.vx -= impulse * nx;
        a.vy -= impulse * ny;

        b.vx += impulse * nx;
        b.vy += impulse * ny;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        for (Bubble bubble : bubbles) {
            drawBubble(g2, bubble);
        }

        g2.dispose();
    }

    private void drawBubble(Graphics2D g2, Bubble bubble) {

        float x = (float) bubble.x;
        float y = (float) bubble.y;
        float r = bubble.radius;

        // 1. Radial Gradient
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point(
                        Math.round(x - r * 0.3f),
                        Math.round(y - r * 0.3f)
                ),
                r * 1.5f,
                new float[]{0f, 0.65f, 1f},
                new Color[]{
                        new Color(255, 255, 255, 25),
                        new Color(
                                bubble.color.getRed(),
                                bubble.color.getGreen(),
                                bubble.color.getBlue(),
                                65
                        ),
                        new Color(
                                bubble.color.getRed(),
                                bubble.color.getGreen(),
                                bubble.color.getBlue(),
                                170
                        )
                }
        );

        Shape circle = new Ellipse2D.Double(
                x - r,
                y - r,
                r * 2,
                r * 2
        );

        g2.setPaint(gradient);
        g2.fill(circle);

        // 2. 외곽선
        g2.setColor(new Color(
                bubble.color.getRed(),
                bubble.color.getGreen(),
                bubble.color.getBlue(),
                180
        ));

        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(circle);

        // 3. 빛 반사 하이라이트
        g2.setColor(new Color(255, 255, 255, 180));

        g2.fillOval(
                (int) (x - r * 0.45),
                (int) (y - r * 0.6),
                (int) (r * 0.45),
                (int) (r * 0.2)
        );
    }

    static class Bubble {
        double x, y;
        double vx, vy;
        int radius;
        Color color;

        Bubble(double x, double y,
               double vx, double vy,
               int radius, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bubbling");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new BubbleExample());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
