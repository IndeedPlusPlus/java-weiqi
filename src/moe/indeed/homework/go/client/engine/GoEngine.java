package moe.indeed.homework.go.client.engine;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class GoEngine {
    final int[][] d = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    public int[][] map = new int[19][19];        //1黑棋，2白棋
    boolean[][][] control = new boolean[3][19][19];        //true表示可能被控制
    boolean[][][] vis = new boolean[3][19][19];
    private int size;
    private int winner;
    private double score, tiezi;
    private int num;

    public GoEngine() {
        size = 19;
        winner = 0;
        score = 0;
        tiezi = 3.75;
        num = 0;
        //dead(2, 3);
    }

    public static void main(String[] args) {
        //System.out.println("haha");
        GoEngine alpha = new GoEngine();
        int i, j;
        for (i = 0; i < 4; i++)
            for (j = 0; j < 4; j++)
                alpha.map[i][j] = 1;
        alpha.map[0][0] = 0;
        alpha.map[2][2] = 2;
        alpha.map[1][2] = 2;
        alpha.delete(2, 2);
        alpha.map[9][9] = 2;
        alpha.get_winner();
        System.out.println(alpha.winner);
        System.out.println(alpha.score);
    }

    public int getWinner() {
        return winner;
    }

    public double getScore() {
        return score;
    }

    public int getNum() {
        return num;
    }

    public boolean move(int x, int y)    //(x,y)处落子，成功则返回true
    {
        if (!inmap(x, y) || map[x][y] != 0) return false;
        int color;
        if (num % 2 == 0) color = 1;
        else color = 2;
        map[x][y] = color;
        num++;
        Point pcz = new Point(x, y);
        for (int i = 0; i < 4; i++) {
            Point tmp = new Point(x + d[i][0], y + d[i][1]);
            if (inmap(tmp.x, tmp.y) && map[tmp.x][tmp.y] != color && dead(tmp.x, tmp.y)) delete(tmp.x, tmp.y);
        }
        if (dead(x, y)) {
            map[x][y] = 0;
            num--;
            return false;
        }
        return true;
    }

    private boolean inmap(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) return false;
        return true;
    }

    private boolean dead(int x, int y)    //(x,y)所在块死亡返回true，活着或者越界或无棋子则返回false
    {
        if (!inmap(x, y) || map[x][y] == 0) return false;
        boolean[][] inq = new boolean[19][19];        //inq为true表示已经访问过，不再访问
        Point pcz = new Point(x, y);
        int color = map[x][y];    //(x,y)所在块的颜色
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(pcz);
        inq[x][y] = true;
        while (!queue.isEmpty()) {
            pcz = queue.remove();
            if (map[pcz.x][pcz.y] == 0) return false;
            for (int i = 0; i < 4; i++) {
                Point tmp = new Point(pcz.x + d[i][0], pcz.y + d[i][1]);
                if (inmap(tmp.x, tmp.y) && (map[tmp.x][tmp.y] == color || map[tmp.x][tmp.y] == 0) && !inq[tmp.x][tmp.y]) {
                    inq[tmp.x][tmp.y] = true;
                    queue.add(tmp);
                }
            }
        }
        return true;
    }

    public boolean delete(int x, int y)    //删除(x,y)所在块，如果越界或无棋子则返回false
    {
        if (!inmap(x, y) || map[x][y] == 0) return false;
        boolean[][] inq = new boolean[19][19];        //inq为true表示已经访问过，不再访问
        Point pcz = new Point(x, y);
        int color = map[x][y];    //(x,y)所在块的颜色
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(pcz);
        inq[x][y] = true;
        while (!queue.isEmpty()) {
            pcz = queue.remove();
            map[pcz.x][pcz.y] = 0;
            for (int i = 0; i < 4; i++) {
                Point tmp = new Point(pcz.x + d[i][0], pcz.y + d[i][1]);
                if (inmap(tmp.x, tmp.y) && map[tmp.x][tmp.y] == color && !inq[tmp.x][tmp.y]) {
                    inq[tmp.x][tmp.y] = true;
                    queue.add(tmp);
                }
            }
        }
        return true;
    }

    public void get_winner() {
        int i, j, k;
        int[] ans = new int[3];
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                control[map[i][j]][i][j] = true;
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                if (map[i][j] > 0 && vis[map[i][j]][i][j] == false)
                    dfs(i, j, map[i][j]);
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                if (control[1][i][j] && control[2][i][j])
                    control[1][i][j] = control[2][i][j] = false;
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                for (k = 1; k <= 2; k++)
                    if (control[k][i][j])
                        ans[k]++;
        score = ans[2] + tiezi - ans[1];
        if (score > 0) winner = 2;
        else {
            winner = 1;
            score = -score;
        }
    }

    private void dfs(int x, int y, int c) {
        if (!inmap(x, y)) return;
        vis[c][x][y] = true;
        control[c][x][y] = true;
        for (int i = 0; i < 4; i++) {
            Point tmp = new Point(x + d[i][0], y + d[i][1]);
            if (inmap(tmp.x, tmp.y) && (map[tmp.x][tmp.y] == c || map[tmp.x][tmp.y] == 0) && !vis[c][tmp.x][tmp.y])
                dfs(tmp.x, tmp.y, c);
        }
    }

}
