package moe.indeed.homework.go.client.gui;

import moe.indeed.homework.go.client.bus.MessageBus;
import moe.indeed.homework.go.client.data.Server;
import moe.indeed.homework.go.client.networking.DiscoveryBroadcaster;
import moe.indeed.homework.go.client.networking.DiscoveryListener;
import moe.indeed.homework.go.client.networking.GameClientThread;
import moe.indeed.homework.go.client.networking.GameServerThread;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscoveryFrame extends JFrame {
    private JTextField playerName;
    private JButton createGameButton;
    private JButton joinGameButton;
    private ThreadGroup discoveryGroup;
    private DefaultListModel<Server> model;
    private JLabel joinTargetLabel;
    private Server serverToJoin;

    public DiscoveryFrame() {
        super("Find Your Opponent");
        this.setLayout(new GridBagLayout());
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.PAGE_AXIS));
        actionPanel.add(joinTargetLabel = new JLabel());
        actionPanel.add(createGameButton = new JButton("Create Game"));
        actionPanel.add(joinGameButton = new JButton("Join"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 3;
        c.gridheight = 3;
        final JList<Server> list;
        this.add(new JScrollPane(list = new JList<Server>(model = new DefaultListModel<Server>())), c);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Server serverToJoin = list.getSelectedValue();
                if (serverToJoin != null) {
                    DiscoveryFrame.this.serverToJoin = serverToJoin;
                    joinTargetLabel.setText(serverToJoin.toString());
                }
            }
        });
        c.gridx = 3;
        c.gridy = 1;
        c.gridheight = 3;
        c.gridwidth = 1;
        c.weightx = 0.5;
        this.add(actionPanel, c);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = c.gridwidth = 1;
        this.add(new JLabel("Your name"), c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(playerName = new JTextField(), c);
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playerName.getText().isEmpty())
                    return;
                final PleaseWaitDialog pleaseWait = new PleaseWaitDialog(DiscoveryFrame.this);
                pleaseWait.setModal(true);
                Thread clientThread = new GameClientThread("client-thread", serverToJoin.getAddress());
                clientThread.setDaemon(true);
                clientThread.start();
                Thread joinThread = new Thread("join-thread") {
                    @Override
                    public void run() {
                        try {
                            final Object respond = MessageBus.getInstance().waitForChannel("GAME");
                            System.err.println(respond);
                            if (respond instanceof Exception) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(DiscoveryFrame.this, ((Exception) respond).getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                        pleaseWait.setVisible(false);
                                        pleaseWait.dispose();

                                    }
                                });
                            } else if ("CONNECTED".equals((String) respond)) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (discoveryGroup != null)
                                            discoveryGroup.interrupt();
                                        pleaseWait.setVisible(false);
                                        MainFrame frame = new MainFrame();
                                        DiscoveryFrame.this.setVisible(false);
                                        frame.setOwnTurn(false);
                                        frame.pack();
                                        frame.setLocationRelativeTo(null);
                                        frame.setVisible(true);
                                        frame.start();
                                    }
                                });
                            }
                        } catch (InterruptedException ignored) {
                        }
                    }
                };
                joinThread.setDaemon(true);
                joinThread.start();
                pleaseWait.setVisible(true);
            }


        });
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playerName.getText().isEmpty())
                    return;
                if (discoveryGroup != null) {
                    discoveryGroup.interrupt();
                    discoveryGroup = null;
                }
                Thread serverThread = new GameServerThread();
                serverThread.setDaemon(true);
                serverThread.start();
                final PleaseWaitDialog pleaseWait = new PleaseWaitDialog(DiscoveryFrame.this);
                pleaseWait.setText("Waiting for another player.");
                pleaseWait.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                pleaseWait.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        System.exit(0);
                    }
                });
                Thread handlerThread = (new Thread("broadcast-handler-thread") {
                    @Override
                    public void run() {
                        Thread broadcaster = new DiscoveryBroadcaster("GO " + playerName.getText());
                        broadcaster.setDaemon(true);
                        broadcaster.start();
                        try {
                            Object respond = MessageBus.getInstance().waitForChannel("GAME");
                            if ("PLAYER_JOIN".equals(respond))
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainFrame frame = new MainFrame();
                                        frame.setOwnTurn(true);
                                        frame.setLocationRelativeTo(null);
                                        frame.pack();
                                        frame.setVisible(true);
                                        frame.start();
                                        frame.setCanDeleteTiles(true);
                                        pleaseWait.setVisible(false);
                                        pleaseWait.removeWindowListener(pleaseWait.getWindowListeners()[0]);
                                        pleaseWait.dispose();
                                    }
                                });
                        } catch (InterruptedException ignored) {
                        }
                        broadcaster.interrupt();
                    }
                });
                handlerThread.setDaemon(true);
                handlerThread.start();
                DiscoveryFrame.this.setVisible(false);
                pleaseWait.pack();
                pleaseWait.setVisible(true);
            }
        });
    }

    public void discovery() {
        discoveryGroup = new ThreadGroup("discovery");
        Thread discoveryListener = new DiscoveryListener(discoveryGroup, "discovery-listener");
        discoveryListener.setDaemon(true);
        discoveryListener.start();
        final Map<Server, Long> lastSeen = new LinkedHashMap<>();
        Thread dataHandler = new Thread(discoveryGroup, "discovery-data-handler") {
            @Override
            public void run() {

                while (!this.isInterrupted()) {
                    try {
                        Server server = (Server) MessageBus.getInstance().waitForChannel("DISCOVERY");
                        synchronized (lastSeen) {
                            lastSeen.put(server, (new Date()).getTime());
                        }
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }
        };
        dataHandler.setDaemon(true);
        dataHandler.start();
        Thread dataFetcher = new Thread(discoveryGroup, "discovery-data-fetcher") {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    model.clear();
                    synchronized (lastSeen) {
                        for (Map.Entry<Server, Long> entry : lastSeen.entrySet()) {
                            if ((new Date()).getTime() - entry.getValue() < 20000) {
                                model.addElement(entry.getKey());
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }

        };
        dataFetcher.setDaemon(true);
        dataFetcher.start();
    }
}
