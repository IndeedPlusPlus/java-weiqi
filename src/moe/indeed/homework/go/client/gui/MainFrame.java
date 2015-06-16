package moe.indeed.homework.go.client.gui;

import moe.indeed.homework.go.client.bus.MessageBus;
import moe.indeed.homework.go.client.engine.GoEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MainFrame extends JFrame {
    private GoEngine engine = new GoEngine();
    private final BlockingQueue<Object> OUTGOING = MessageBus.getInstance().getOrCreateChannel("OUTGOING");
    private final BlockingQueue<Object> CHANNEL = MessageBus.getInstance().getOrCreateChannel("GAME");
    private final GoBoard board;
    final Object finishLock = new Object();
    private boolean finishRequested = false;
    private boolean finishMode = false;
    private final JLabel status = new JLabel("LOADING GAME");

    public boolean isOwnTurn() {
        return ownTurn;
    }

    public void setOwnTurn(final boolean ownTurn) {
        this.ownTurn = ownTurn;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText(ownTurn ? "YOUR TURN" : "WAIT");
            }
        });
    }

    private boolean ownTurn = false;

    public boolean isCanDeleteTiles() {
        return canDeleteTiles;
    }

    public void setCanDeleteTiles(boolean canDeleteTiles) {
        this.canDeleteTiles = canDeleteTiles;
    }

    private boolean canDeleteTiles;

    public MainFrame() {
        super("Java Go");
        board = new GoBoard();
        this.setLayout(new BorderLayout());
        this.add(board, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        final JButton end = new JButton("End");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        System.exit(1);
                    }
                });
        end.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (finishMode) {
                    try {
                        status.setText("END");
                        OUTGOING.put("END");
                        engine.get_winner();
                        JOptionPane.showMessageDialog(MainFrame.this, "Winner is " + (engine.getWinner() == 1 ? "black" : "white") + ", score is " + engine.getScore() * 2, "Result", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    finishRequested = true;
                    try {
                        OUTGOING.put("FINISH");
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        buttonPanel.add(status);
        buttonPanel.add(end);
        this.add(buttonPanel, BorderLayout.EAST);
        board.setPutTileListener(new GoBoard.PutTileListener() {
            int color = 1;

            @Override
            public boolean titlePut(int x, int y, Map<Point, Integer> points) {
                if (!finishMode) {
                    if (ownTurn && engine.move(x, y)) {
                        setOwnTurn(false);
                        try {
                            OUTGOING.put("MOVE " + x + " " + y);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        points.clear();
                        for (int i = 0; i < 19; ++i)
                            for (int j = 0; j < 19; ++j)
                                if (engine.map[i][j] != 0) {
                                    points.put(new Point(i, j), engine.map[i][j]);
                                }
                        return true;
                    } else
                        return false;
                } else if (canDeleteTiles) {
                    if (engine.delete(x, y)) {
                        try {
                            OUTGOING.put("REMOVE " + x + " " + y);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        points.clear();
                        for (int i = 0; i < 19; ++i)
                            for (int j = 0; j < 19; ++j)
                                if (engine.map[i][j] != 0) {
                                    points.put(new Point(i, j), engine.map[i][j]);
                                }
                        return true;
                    } else
                        return false;
                }
                return false;
            }

        });
    }

    public void start() {
        Thread eventHandler = new Thread("event-handler") {
            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Object message = CHANNEL.take();
                        System.err.println(message);
                        if (message instanceof String) {
                            String data = (String) message;
                            if (data.startsWith("MOVE")) {
                                final String[] parts = data.split("\\s+");
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        engine.move(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                                        Map<Point, Integer> points = MainFrame.this.board.getPoints();
                                        points.clear();
                                        for (int i = 0; i < 19; ++i)
                                            for (int j = 0; j < 19; ++j)
                                                if (engine.map[i][j] != 0) {
                                                    points.put(new Point(i, j), engine.map[i][j]);
                                                }
                                        board.repaint();
                                        setOwnTurn(true);
                                    }
                                });
                            } else if (data.startsWith("FINISH")) {
                                synchronized (finishLock) {
                                    if (finishRequested) {
                                        finishMode = true;
                                        status.setText("REMOVE");
                                    } else {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "Do you think you have finished playing?", "Ending Request", JOptionPane.YES_NO_OPTION)) {
                                                    finishMode = true;
                                                    status.setText("REMOVE");
                                                    try {
                                                        OUTGOING.put("FINISH");
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    finishRequested = false;
                                                    try {
                                                        OUTGOING.put("NOFINISH");
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            } else if (data.startsWith("REMOVE")) {
                                final String[] parts = data.split("\\s+");
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        engine.delete(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                                        Map<Point, Integer> points = MainFrame.this.board.getPoints();
                                        points.clear();
                                        for (int i = 0; i < 19; ++i)
                                            for (int j = 0; j < 19; ++j)
                                                if (engine.map[i][j] != 0) {
                                                    points.put(new Point(i, j), engine.map[i][j]);
                                                }
                                        board.repaint();
                                    }
                                });
                            } else if (data.startsWith("NOFINISH")) {
                                synchronized (finishLock) {
                                    finishRequested = false;
                                }
                            } else if (data.startsWith("END")) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        status.setText("END");
                                        engine.get_winner();
                                        JOptionPane.showMessageDialog(MainFrame.this, "Winner is " + (engine.getWinner() == 1 ? "black" : "white") + ", score is " + engine.getScore() * 2, "Result", JOptionPane.INFORMATION_MESSAGE);
                                        System.exit(0);
                                    }
                                });
                            }
                        } else if (message instanceof Exception) {
                            final Exception exception = (Exception) message;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(MainFrame.this, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                                    System.exit(2);
                                }
                            });
                        }
                    }
                } catch (InterruptedException ignored) {

                }

            }
        };
        eventHandler.setDaemon(true);
        eventHandler.start();
    }

}
