package MandelbrotSetProject;
/* @(#)Mandelbrot.java
 * @author: Ishwar
 * @version:1.3 
 * started: 2007/4/4, ended: 2007/4/6, man5,v1.2: started 2007/4/3
 * @version desc: -> implement saveToDisk()
 */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;


public class Mandelbrot extends Applet implements ActionListener, MouseListener, MouseMotionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean selecting = false, zOut = false, ready = false, dragged = false;
    int max_i = 600;
    int R, G, B;
    int sFactor = 30;
    float h, s, b;
     int xStart, yStart, xEnd, yEnd;
    int xSize = sFactor * 27, ySize = sFactor * 22;   			// 27,22 = 810X660(x X y)
    double rStart = -2.05, iStart = 1.2;     					// -2.05,  1.2
    double xSpan = 2.65, ySpan = 2.4;							//  2.65,  2.4	
    double rVar = xSpan / (double) xSize;
    double iVar = ySpan / (double) ySize;
    float scaleValue = 1.0f;
    double _alpha = .8;							// belongs to [.5,3.5]
    double rMood = 1.0;
    double gMood = 1.0;
    double bMood = 1.0;
    double hMood = 1.0;
    double sMood = 1.0;
    double brMood = 1.0;
    Image img;
    Graphics imgGraphics, something;
    Button draw, reset, resetAll, Save;
    CheckboxGroup radioGroup;
    Checkbox negative, mono, multi, multi1;
    Scrollbar one, two, three, four;
    Label color, scale, lAbel;
    Cursor Default, Wait, Crosshair;
    File output;
    BufferedImage bufferedImage;

    public void mousePressed(MouseEvent e) {
        e.consume();
        if (e.getX() <= xSize && e.getY() <= xSize && ready) {
            xStart = e.getX();
            yStart = e.getY();
            selecting = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getX() < xSize && e.getY() < ySize) {
            if (e.getButton() == 3) {
                if (dragged) {
                    zoomOut(xStart + (int) .5 * e.getX(), yStart + (int) .5 * e.getY());
                } else {
                    zoomOut(e.getX(), e.getY());
                }
                dragged = false;
            } else {
                if (dragged) {
                    zoomIn();
                } else {
                    xStart = xEnd = e.getX();
                    yStart = yEnd = e.getY();
                    zoomIn();
                }
                dragged = false;
            }
            selecting = false;
        } else {
            showStatus("Invalid selection of zoom area,select again");
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        e.consume();
        if (e.getX() <= xSize && e.getY() <= xSize && ready) {
            dragged = true;
            xEnd = e.getX();
            yEnd = e.getY();
            repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getX() <= xSize && e.getY() <= xSize && ready) {
            setCursor(Crosshair);
        } else {
            setCursor(Default);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == draw) {
            draw.setBounds(850, 20, 80, 30);
            setCursor(Wait);
            draw.setLabel("ReDraw");
            showStatus("Calculating.. Please Wait");
            img = createImage(xSize, ySize);
            imgGraphics = img.getGraphics();
            drawFractal();
            repaint();
            add(negative);
            add(mono);
            add(multi);
            add(multi1);
            add(one);
            add(two);
            add(three);
            add(reset);
            add(color);
            add(resetAll);
            add(Save);
            add(lAbel);
            add(scale);
            add(four);
            ready = true;
        }
        if (evt.getSource() == reset) {
            one.setValue(50);
            two.setValue(50);
            three.setValue(50);
            rMood = bMood = gMood = hMood = sMood = brMood = 1.0;
            if (multi.getState() || multi1.getState()) {
                color.setText("H     S     B");
                negative.setEnabled(false);
            }
        }

        if (evt.getSource() == resetAll) {

            if (multi.getState() || multi.getState()) {
                color.setText("H     S     B");
                negative.setEnabled(false);
            }
            rStart = -2.05;
            iStart = 1.2;
            xSpan = 2.65;
            ySpan = 2.4;
            rVar = (double) xSpan / (double) xSize;
            iVar = (double) ySpan / (double) ySize;
            scaleValue = 1.0f;
        }
        if (evt.getSource() == Save) {
            saveImage();
        }
    }

    public boolean handleEvent(Event evt) {
        if (evt.target == one) {
            hMood = rMood = Math.pow(10.0, -.69897 + .0139794 * one.getValue());
        }

        if (evt.target == two) {
            sMood = gMood = Math.pow(10.0, -.69897 + .0139794 * two.getValue());
        }

        if (evt.target == three) {
            brMood = bMood = Math.pow(10.0, -.69897 + .0139794 * three.getValue());
        }

        if (evt.target == four) {
            _alpha = 3.5 - (3.0 / 100.0) * four.getValue();
        }

        if (multi.getState() || multi1.getState()) {
            color.setText("H      S      B");
            four.setEnabled(false);
        } else {
            color.setText("R      G      B      A");
            four.setEnabled(true);
        }
        if (evt.target == mono) {
            negative.setEnabled(true);
        }
        if (evt.target == multi || evt.target == multi1) {
            negative.setEnabled(false);
        }
        return true;
    }

    public void init() {
        setLayout(null);
        draw = new Button("Draw");
        reset = new Button("Reset Colors");
        resetAll = new Button("Reset Scale");
        Save = new Button("Save");
        radioGroup = new CheckboxGroup();
        negative = new Checkbox("Negative", false);
        mono = new Checkbox("MonoGradient", radioGroup, true);
        multi = new Checkbox("Vibrant", radioGroup, false);
        multi1 = new Checkbox("Vibrant1", radioGroup, false);
        one = new Scrollbar(Scrollbar.VERTICAL, 0, 0, 0, 100);
        two = new Scrollbar(Scrollbar.VERTICAL, 0, 0, 0, 100);
        three = new Scrollbar(Scrollbar.VERTICAL, 0, 0, 0, 100);
        four = new Scrollbar(Scrollbar.VERTICAL, 0, 0, 0, 100);
        color = new Label("R      G      B      A");
        scale = new Label(String.valueOf(scaleValue) + " X");
        lAbel = new Label("MAGNIFICATION: ");
        Wait = new Cursor(Cursor.WAIT_CURSOR);
        Default = new Cursor(Cursor.DEFAULT_CURSOR);
        Crosshair = new Cursor(Cursor.CROSSHAIR_CURSOR);


        draw.setBounds(400, 300, 180, 130);
        negative.setBounds(850, 60, 70, 20);
        mono.setBounds(850, 90, 100, 20);
        multi.setBounds(850, 110, 70, 20);
        multi1.setBounds(850, 130, 70, 20);
        color.setBounds(850, 140, 100, 30);
        one.setBounds(850, 168, 20, 200);
        two.setBounds(875, 168, 20, 200);
        three.setBounds(900, 168, 20, 200);
        four.setBounds(925, 168, 20, 200);
        reset.setBounds(850, 372, 100, 20);
        resetAll.setBounds(850, 392, 100, 20);
        Save.setBounds(850, 412, 100, 20);
        lAbel.setBounds(825, 497, 100, 20);
        scale.setBounds(825, 500, 180, 60);


        one.setUnitIncrement(1);
        two.setUnitIncrement(1);
        three.setUnitIncrement(1);
        four.setUnitIncrement(1);
        one.setValue(25);
        two.setValue(50);
        three.setValue(75);
        four.setValue(33);
        add(draw);
        draw.addActionListener(this);
        reset.addActionListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        resetAll.addActionListener(this);
        Save.addActionListener(this);

        hMood = rMood = Math.pow(10.0, -.69897 + .0139794 * one.getValue());
        sMood = gMood = Math.pow(10.0, -.69897 + .0139794 * two.getValue());
        brMood = bMood = Math.pow(10.0, -.69897 + .0139794 * three.getValue());
    }

    public void start() {

    }

    public void destroy() {
        System.gc();
    }

    public void paint(Graphics g) {
        if (ready) {
            ;
        }
        update(g);
        showStatus("Done,Click/Click-Drag to zoom in, LeftClick/LeftClick-Drag to zoom out");
    }

    public void update(Graphics g) {
        g.drawImage(img, 0, 0, this);
        showStatus(String.valueOf(rVar) + "- " + String.valueOf(iVar));
        if (selecting) {
            g.setColor(Color.RED);
            int xDiff = xEnd - xStart;
            int yDiff = yEnd - yStart;
            if (xDiff > 0 && yDiff > 0) {
                g.drawRect(xStart, yStart, xDiff, yDiff);
            }
            if (xDiff > 0 && yDiff < 0) {
                g.drawRect(xStart, yEnd, xDiff, -yDiff);
            }
            if (xDiff < 0 && yDiff > 0) {
                g.drawRect(xEnd, yStart, -xDiff, yDiff);
            }
            if (xDiff < 0 && yDiff < 0) {
                g.drawRect(xEnd, yEnd, -xDiff, -yDiff);
            }
        }

    }

    void drawFractal() {
        setCursor(Wait);
        double r = rStart, i = iStart;
        int x, y;
        for (x = 0; x <= xSize; x++, r = r + rVar) {
            for (y = 0, i = iStart; y <= ySize; y++, i = i - iVar) {
                calcMandelbrot(r, i);
                Color c = new Color(R, G, B);
                imgGraphics.setColor(c);
                if (multi.getState() || multi1.getState()) {
                    imgGraphics.setColor(Color.getHSBColor(h, s, b));
                }
                imgGraphics.drawLine(x, y, x, y);
            }
        }
        scale.setText(String.valueOf(scaleValue) + " X");
        setCursor(Default);
    }

    void zoomIn() {
        double x = Math.abs(xStart - xEnd), y = Math.abs(yStart - yEnd);
        if (x == 0 || y == 0) {
            setCursor(Wait);
            showStatus("Calculating, please wait..");
            int xt = xStart, yt = yStart;
            rStart = rStart + (double) (xt - 81) * rVar;
            iStart = iStart - (double) (yt - 66) * iVar;
            xSpan = xSpan / 5.0;
            ySpan = ySpan / 5.0;
            iVar = iVar / 5.0;
            rVar = rVar / 5.0;
            scaleValue = scaleValue * 5.0f;
            drawFractal();
            repaint();
            setCursor(Default);
            showStatus("Done,Click/Click-Drag to zoom in, LeftClick/LeftClick-Drag to zoom out");
            scale.setText(String.valueOf(scaleValue) + " X");
            return;
        }
        int xDiff = xEnd - xStart;
        int yDiff = yEnd - yStart;
        int xs, ys;
        if (xDiff > 0 && yDiff < 0) {
            ys = yStart;
            yStart = yEnd;
            yEnd = ys;
        }
        if (xDiff < 0 && yDiff > 0) {
            xs = xStart;
            xStart = xEnd;
            xEnd = xs;
        }
        if (xDiff < 0 && yDiff < 0) {
            ys = yStart;
            yStart = yEnd;
            yEnd = ys;
            xs = xStart;
            xStart = xEnd;
            xEnd = xs;
        }
        double orVar = rVar;
        if (x / y < 27.0 / 22.0) {
            while (x / y < 27.0 / 22.0) {
                x++;
                y--;
            }
        }
        if (x / y > 27.0 / 22.0) {
            while (x / y > 27.0 / 22.0) {
                x--;
                y++;
            }
        }
        showStatus("Calculating, please wait..");
        rStart = rStart + (double) xStart * rVar;
        iStart = iStart - (double) yStart * iVar;
        xSpan = x * rVar;
        ySpan = y * iVar;
        rVar = xSpan / xSize;
        iVar = ySpan / ySize;
        scaleValue *= (float) (orVar / rVar);
        drawFractal();
        repaint();
        showStatus("Done,Click/Click-Drag to zoom in, LeftClick/LeftClick-Drag to zoom out");
    }

    void zoomOut(int xt, int yt) {
        showStatus("Calculating, please wait..");
        double r = rStart + (double) (xt) * rVar;
        double i = iStart - (double) (yt) * iVar;
        setCursor(Wait);
        rStart = r - ((double) xSize * 5.0 * rVar);
        iStart = i + ((double) ySize * 5.0 * iVar);
        xSpan = 2.0 * Math.abs(rStart - r);
        ySpan = 2.0 * Math.abs(iStart - i);
        rVar = (xSpan / (double) xSize);
        iVar = (ySpan / (double) ySize);
        drawFractal();
        repaint();
        scaleValue = scaleValue * .1f;
        showStatus("Done,Click/Click-Drag to zoom in, LeftClick/LeftClick-Drag to zoom out");
        scale.setText(String.valueOf(scaleValue) + " X");
        return;
    }

    void calcMandelbrot(double r, double i) {
        double x = r, y = i;
        double x2 = x * x, y2 = y * y;
        int ite = 0;
        while (ite++ < max_i && x2 + y2 < 5) {
            y = 2.0d * x * y + i;
            x = x2 - y2 + r;
            x2 = x * x;
            y2 = y * y;
        }
        double ratio = (double) ite / (double) max_i;
        if (ratio <= 21.0 / 255.0) {
            ratio *= 10.0;
        }

        if (ratio < 1) {
            R = (int) (ratio * 255 / (rMood / _alpha));
            G = (int) (ratio * 255 / (gMood / _alpha));
            B = (int) (ratio * 255 / (bMood / _alpha));
        } else {
            R = 0;
            G = 0;
            B = 0;
        }

        h = (float) (ratio / hMood);
        s = (float) (.8f / sMood);
        b = (float) ((1 - h * h) / brMood);
//		if(multi1.getState())
//		{	if(ratio==1.0)
//				s=.4f; 
//			else
//				s=(float)(ratio/sMood) ;
//			b=(float)(ratio/brMood);
//		}
        if (multi1.getState()) {
            s = (float) ((2.0 * ratio) / sMood);

        }

        if (h > 1) {
            h = 1;
        }
        if (s > 1) {
            s = 1;
        }
        if (b > 1) {
            b = 1;
        }
        if (h < 0) {
            h = 0;
        }
        if (s < 0) {
            s = 0;
        }
        if (b < 0) {
            b = 0;
        }

        if (R > 255) {
            R = 255;
        }
        if (G > 255) {
            G = 255;
        }
        if (B > 255) {
            B = 255;
        }
        if (negative.getState()) {
            R = 255 - R;
            B = 255 - B;
            G = 255 - G;
        }
    }

    void saveImage() {
        BufferedImage bufferedImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        bufferedImage.createGraphics().drawImage(img, 0, 0, this);
        try {
            ImageIO.write(bufferedImage, "jpeg", new File("Mandelbrot" + String.valueOf(scaleValue) + ".jpg"));
        } catch (Exception e) {
            showStatus(e.getMessage());
        }
    }
}
/*
 <APPLET CODE = "man7.class" HEIGHT = 680 WIDTH = 1015>
 </APPLET>
 */
