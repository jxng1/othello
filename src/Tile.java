//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class Tile extends JButton {
    private TileState state;

    public Tile(int width, int height, TileState state) {
        this.state = state;
        this.setContentAreaFilled(false);
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.GREEN);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public void setState(TileState state) {
        this.state = state;
    }

    protected void paintComponent(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        if (this.state == TileState.BLACK) {
            g.setColor(Color.BLACK);
            g.fillOval(5, 5, this.getSize().width - 10, this.getSize().height - 10);
            g.setColor(Color.WHITE);
            g.drawOval(5, 5, this.getSize().width - 10, this.getSize().height - 10);
        } else if (this.state == TileState.WHITE) {
            g.setColor(Color.WHITE);
            g.fillOval(5, 5, this.getSize().width - 10, this.getSize().height - 10);
            g.setColor(Color.BLACK);
            g.drawOval(5, 5, this.getSize().width - 10, this.getSize().height - 10);
        }
    }

    public enum TileState {
        EMPTY, WHITE, BLACK
    }
}
