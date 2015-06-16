package moe.indeed.homework.go.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

public class GoBoard extends JComponent {

    static final Point[] BOLD_POINTS = {
            new Point(3, 3),
            new Point(3, 9),
            new Point(3, 15),
            new Point(9, 3),
            new Point(9, 9),
            new Point(9, 15),
            new Point(15, 3),
            new Point(15, 9),
            new Point(15, 15)
    };
    public final int STATUS_BLACK = 1;
    public final int STATUS_WHITE = 2;
    private final int CELL_SIZE = 30;
    private final int CIRCLE_RADIUS = 12;
    private PutTileListener putTileListener;
    private Map<Point, Integer> points = new HashMap<>();

    public GoBoard() {
        this.setPreferredSize(new Dimension(CELL_SIZE * 19, CELL_SIZE * 19));
        this.setMaximumSize(this.getPreferredSize());
        this.setMinimumSize(this.getPreferredSize());
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int px = e.getX() - CELL_SIZE / 2;
                int py = e.getY() - CELL_SIZE / 2;
                int x = (px + CELL_SIZE / 2) / CELL_SIZE;
                int y = (py + CELL_SIZE / 2) / CELL_SIZE;
                if (x >= 0 && x < 19 && y >= 0 && y < 19 && x * CELL_SIZE - CIRCLE_RADIUS <= px && x * CELL_SIZE + CIRCLE_RADIUS >= px &&
                        y * CELL_SIZE - CIRCLE_RADIUS <= py && y * CELL_SIZE + CIRCLE_RADIUS >= py)
                    if (GoBoard.this.putTileListener != null) {
                        if (GoBoard.this.putTileListener.titlePut(x, y, GoBoard.this.points))
                            GoBoard.this.repaint();
                    }

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public Map<Point, Integer> getPoints() {
        return points;
    }

    public PutTileListener getPutTileListener() {
        return putTileListener;
    }

    public void setPutTileListener(PutTileListener putTileListener) {
        this.putTileListener = putTileListener;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        for (int i = 0; i < 19; ++i)
            g.drawLine(CELL_SIZE / 2 + i * CELL_SIZE, CELL_SIZE / 2, CELL_SIZE / 2 + i * CELL_SIZE, CELL_SIZE / 2 + CELL_SIZE * 18);
        for (int j = 0; j < 19; ++j)
            g.drawLine(CELL_SIZE / 2, CELL_SIZE / 2 + j * CELL_SIZE, CELL_SIZE / 2 + CELL_SIZE * 18, CELL_SIZE / 2 + j * CELL_SIZE);
        for (Point point : BOLD_POINTS) {
            final int R = 5;
            g.fillArc(CELL_SIZE / 2 + CELL_SIZE * point.x - R, CELL_SIZE / 2 + CELL_SIZE * point.y - R, R * 2, R * 2, 0, 360);
        }
        for (Map.Entry<Point, Integer> entry : points.entrySet()) {
            Point point = entry.getKey();
            if (entry.getValue().equals(STATUS_WHITE)) {
                g.setColor(Color.WHITE);
            } else g.setColor(Color.BLACK);
            g.fillArc(CELL_SIZE / 2 + CELL_SIZE * point.x - CIRCLE_RADIUS, CELL_SIZE / 2 + CELL_SIZE * point.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2, 0, 360);
            g.setColor(Color.BLACK);
            g.drawArc(CELL_SIZE / 2 + CELL_SIZE * point.x - CIRCLE_RADIUS, CELL_SIZE / 2 + CELL_SIZE * point.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2, 0, 360);
        }
    }

    public interface PutTileListener {
        boolean titlePut(int x, int y, Map<Point, Integer> points);
    }


}
