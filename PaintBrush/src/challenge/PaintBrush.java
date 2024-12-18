package challenge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class PaintBrush extends JFrame {
    private final Canvas canvas;
    private final JButton clearButton, undoButton, lineButton, rectButton, ovalButton, pencilButton, eraserButton;
    private final JRadioButton solidButton, dottedButton, fillButton, noFillButton;
    private final Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE};
    private Color currentColor = Color.BLACK;

    public PaintBrush() {
        setTitle("Paint Brush");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        clearButton = new JButton("Clear");
        undoButton = new JButton("Undo");
        lineButton = new JButton("Line");
        rectButton = new JButton("Rectangle");
        ovalButton = new JButton("Oval");
        pencilButton = new JButton("Pencil");
        eraserButton = new JButton("Eraser");

        solidButton = new JRadioButton("Solid", true);
        dottedButton = new JRadioButton("Dotted");
        fillButton = new JRadioButton("Fill", false);
        noFillButton = new JRadioButton("No Fill", true);

        ButtonGroup group = new ButtonGroup();
        group.add(solidButton);
        group.add(dottedButton);

        ButtonGroup fillGroup = new ButtonGroup();
        fillGroup.add(fillButton);
        fillGroup.add(noFillButton);

        buttonPanel.add(new JLabel("Functions:"));
        buttonPanel.add(clearButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(new JLabel("Paint Mode:"));
        buttonPanel.add(lineButton);
        buttonPanel.add(rectButton);
        buttonPanel.add(ovalButton);
        buttonPanel.add(pencilButton);
        buttonPanel.add(eraserButton);
        buttonPanel.add(solidButton);
        buttonPanel.add(dottedButton);

        buttonPanel.add(new JLabel("Fill Mode:"));
        buttonPanel.add(fillButton);
        buttonPanel.add(noFillButton);

        buttonPanel.add(new JLabel("Colors:"));
        for (Color c : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(c);
            colorButton.setPreferredSize(new Dimension(30, 30));
            colorButton.addActionListener(e -> currentColor = c);
            buttonPanel.add(colorButton);
        }

        add(buttonPanel, BorderLayout.NORTH);

        canvas = new Canvas();
        add(canvas, BorderLayout.CENTER);

        clearButton.addActionListener(e -> canvas.clear());
        undoButton.addActionListener(e -> canvas.undo());
        lineButton.addActionListener(e -> canvas.setMode("Line"));
        rectButton.addActionListener(e -> canvas.setMode("Rectangle"));
        ovalButton.addActionListener(e -> canvas.setMode("Oval"));
        pencilButton.addActionListener(e -> canvas.setMode("Pencil"));
        eraserButton.addActionListener(e -> canvas.setMode("Eraser"));
        solidButton.addActionListener(e -> canvas.setStrokeStyle("Solid"));
        dottedButton.addActionListener(e -> canvas.setStrokeStyle("Dotted"));
        fillButton.addActionListener(e -> canvas.setFill(true));
        noFillButton.addActionListener(e -> canvas.setFill(false));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaintBrush().setVisible(true));
    }

    class Canvas extends JPanel {
        private ArrayList<Shape> shapes = new ArrayList<>();
        private String mode = "Line";
        private String strokeStyle = "Solid";
        private boolean fillShape = false;
        private int startX, startY, endX, endY;
        private Shape tempShape;

        public Canvas() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startX = e.getX();
                    startY = e.getY();

                    if (mode.equals("Eraser")) {
                        eraseShapeAtPoint(startX, startY);
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    endX = e.getX();
                    endY = e.getY();

                    if (mode.equals("Line")) {
                        shapes.add(new Line(startX, startY, endX, endY, currentColor, getStroke(), fillShape));
                    } else if (mode.equals("Rectangle")) {
                        shapes.add(new RectangleShape(startX, startY, endX - startX, endY - startY, currentColor, getStroke(), fillShape));
                    } else if (mode.equals("Oval")) {
                        shapes.add(new OvalShape(startX, startY, endX - startX, endY - startY, currentColor, getStroke(), fillShape));
                    }
                    tempShape = null;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    endX = e.getX();
                    endY = e.getY();

                    if (mode.equals("Pencil")) {
                        shapes.add(new Line(startX, startY, endX, endY, currentColor, getStroke(), fillShape));
                        startX = endX;
                        startY = endY;
                        repaint();
                    } else if (mode.equals("Eraser")) {
                        eraseShapeAtPoint(endX, endY);
                        repaint();
                    }
                }
            });
        }

        public void clear() {
            shapes.clear();
            repaint();
        }

        public void undo() {
            if (!shapes.isEmpty()) {
                shapes.remove(shapes.size() - 1);
                repaint();
            }
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setStrokeStyle(String style) {
            this.strokeStyle = style;
        }

        public void setFill(boolean fill) {
            this.fillShape = fill;
        }

        private Stroke getStroke() {
            return strokeStyle.equals("Dotted") ? new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{5}, 0)
                                                : new BasicStroke(2);
        }

        private void eraseShapeAtPoint(int x, int y) {
            shapes.removeIf(shape -> shape.containsPoint(x, y));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            for (Shape shape : shapes) {
                shape.draw(g2);
            }
        }
    }

    interface Shape {
        void draw(Graphics2D g);
        boolean containsPoint(int x, int y);
    }

    class Line implements Shape {
        int x1, y1, x2, y2;
        Color color;
        Stroke stroke;
        boolean fill;

        public Line(int x1, int y1, int x2, int y2, Color color, Stroke stroke, boolean fill) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.stroke = stroke;
            this.fill = fill;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.setStroke(stroke);
            g.drawLine(x1, y1, x2, y2);
        }

        public boolean containsPoint(int x, int y) {
            return Line2D.ptLineDist(x1, y1, x2, y2, x, y) < 5;
        }
    }

    class RectangleShape implements Shape {
        int x, y, width, height;
        Color color;
        Stroke stroke;
        boolean fill;

        public RectangleShape(int x, int y, int width, int height, Color color, Stroke stroke, boolean fill) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.stroke = stroke;
            this.fill = fill;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.setStroke(stroke);
            if (fill) {
                g.fillRect(x, y, width, height);
            } else {
                g.drawRect(x, y, width, height);
            }
        }

        public boolean containsPoint(int px, int py) {
            return (px >= x && px <= x + width && py >= y && py <= y + height);
        }
    }

    class OvalShape implements Shape {
        int x, y, width, height;
        Color color;
        Stroke stroke;
        boolean fill;

        public OvalShape(int x, int y, int width, int height, Color color, Stroke stroke, boolean fill) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.stroke = stroke;
            this.fill = fill;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.setStroke(stroke);
            if (fill) {
                g.fillOval(x, y, width, height);
            } else {
                g.drawOval(x, y, width, height);
            }
        }

        public boolean containsPoint(int px, int py) {
            double dx = (double)(px - x - width / 2) / (width / 2);
            double dy = (double)(py - y - height / 2) / (height / 2);
            return dx * dx + dy * dy <= 1;
        }
    }
}
