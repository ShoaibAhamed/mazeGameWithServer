package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class MazeCreator implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
    
   
	// for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();
    

    // set of key codes currently pressed down
    private static TreeSet<Integer> keysDown = new TreeSet<Integer>();
    

    // queue of typed key characters
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();
    
    
    // mouse state
    private static boolean mousePressed = false;
    private static double mouseX = 0;
    private static double mouseY = 0;
    
    // boundary of drawing canvas, 0% border
    // private static final double BORDER = 0.05;
    private static final double BORDER = 0.00;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;
    
    
    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 1024;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;
    
    // default colors
    private static final Color DEFAULT_PEN_COLOR   = Color.BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = Color.WHITE;
    

    // current pen color
    private static Color penColor;
    
    // double buffered graphics
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // singleton for callbacks: avoids generation of extra .class files
    private static MazeCreator std = new MazeCreator();

    // the frame for drawing to the screen
    private static JFrame frame;
    

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;
    
    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;
    

    // show we draw immediately or wait until next show?
    private static boolean defer = false;

    // time in milliseconds (from currentTimeMillis()) when we can draw again
    // used to control the frame rate
    private static long nextDraw = -1; 
    
    // static initializer
    static {
        init();
    }

    private static void init() {
        if (frame != null) frame.setVisible(false);
        String className = getLookAndFeelClassName("Nimbus");
        try {
			UIManager.setLookAndFeel(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} 
        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                  RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        //frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }
    
    /**
     * Returns the class name of the installed LookAndFeel with a name
     * containing the name snippet or null if none found.
     * 
     * @param nameSnippet a snippet contained in the Laf's name
     * @return the class name if installed, or null
     */
    public static String getLookAndFeelClassName(String nameSnippet) {
        LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo info : plafs) {
            if (info.getName().contains(nameSnippet)) {
                return info.getClassName();
            }
        }
        return null;
    }
    
    /**
     * Write the given text string in the current font, centered at (<em>x</em>, <em>y</em>).
     *
     * @param  x the center <em>x</em>-coordinate of the text
     * @param  y the center <em>y</em>-coordinate of the text
     * @param  text the text to write
     */
    public static void text(double x, double y, String text) {
        if (text == null) throw new NullPointerException();
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(text);
        int hs = metrics.getDescent();
        offscreen.drawString(text, (float) (xs - ws/2.0), (float) (ys + hs));
        draw();
    }
    
    /**
     * Sets the canvas (drawing area) to be <em>width</em>-by-<em>height</em> pixels.
     * This also erases the current drawing and resets the coordinate system,
     * pen radius, pen color, and font back to their default values.
     * Ordinarly, this method is called once, at the very beginning
     * of a program.
     *
     * @param  canvasWidth the width as a number of pixels
     * @param  canvasHeight the height as a number of pixels
     * @throws IllegalArgumentException unless both {@code width} and
     *         {@code height} are positive
     */
    public static void setCanvasSize(int canvasWidth, int canvasHeight) {
        if (canvasWidth <= 0 || canvasHeight <= 0)
            throw new IllegalArgumentException("width and height must be positive");
        width = canvasWidth;
        height = canvasHeight;
        init();
    }

    /**
     * Clears the screen to the default color (white).
     */
    public static void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }
    
    /**
     * Clears the screen to the specified color.
     *
     * @param color the color to make the background
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }
    
    /**
     * Display on-screen and turn off animation mode:
     * subsequent calls to
     * drawing methods such as {@code line()}, {@code circle()},
     * and {@code square()} will be displayed on screen when called.
     * This is the default.
     */
    public static void show() {
        defer = false;
        nextDraw = -1;
        draw();
    }

    // draw onscreen if defer is false
    private static void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }
    /**
     * Sets the font to the default font (sans serif, 16 point).
     */
    public static void setFont() {
        setFont(DEFAULT_FONT);
    }

    /**
     * Sets the font to the specified value.
     *
     * @param font the font
     */
    public static void setFont(Font font) {
        if (font == null) throw new NullPointerException();
        MazeCreator.font = font;
    }
    
    
    /**
     * Sets the pen size to the default size (0.002).
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     */
    public static void setPenRadius() {
        setPenRadius(DEFAULT_PEN_RADIUS);
    }

    /**
     * Sets the radius of the pen to the specified size.
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     *
     * @param  radius the radius of the pen
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public static void setPenRadius(double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = radius;
        float scaledPenRadius = (float) (radius * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }
    
    /**
     * Set the pen color to the default color (black).
     */
    public static void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }

    /**
     * Sets the pen color to the specified color.
     * <p>
     * The predefined pen colors are
     * <tt>StdDraw.BLACK</tt>, <tt>StdDraw.BLUE</tt>, <tt>StdDraw.CYAN</tt>,
     * <tt>StdDraw.DARK_GRAY</tt>, <tt>StdDraw.GRAY</tt>, <tt>StdDraw.GREEN</tt>,
     * <tt>StdDraw.LIGHT_GRAY</tt>, <tt>StdDraw.MAGENTA</tt>, <tt>StdDraw.ORANGE</tt>,
     * <tt>StdDraw.PINK</tt>, <tt>StdDraw.RED</tt>, <tt>StdDraw.WHITE</tt>, and
     * <tt>StdDraw.YELLOW</tt>.
     *
     * @param color the color to make the pen
     */
    public static void setPenColor(Color color) {
        if (color == null) throw new NullPointerException();
        penColor = color;
        offscreen.setColor(penColor);
    }
    

    /***************************************************************************
     *  User and screen coordinate systems.
     ***************************************************************************/

     /**
      * Sets the <em>x</em>-scale to be the default (between 0.0 and 1.0).
      */
     public static void setXscale() {
         setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
     }

     /**
      * Sets the <em>y</em>-scale to be the default (between 0.0 and 1.0).
      */
     public static void setYscale() {
         setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
     }

     /**
      * Sets the <em>x</em>-scale and <em>y</em>-scale to be the default
      * (between 0.0 and 1.0).
      */
     public static void setScale() {
         setXscale();
         setYscale();
     }
     
     /**
      * Sets the <em>x</em>-scale to the specified range.
      *
      * @param  min the minimum value of the <em>x</em>-scale
      * @param  max the maximum value of the <em>x</em>-scale
      * @throws IllegalArgumentException if {@code (max == min)}
      */
     public static void setXscale(double min, double max) {
         double size = max - min;
         if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
         synchronized (mouseLock) {
             xmin = min - BORDER * size;
             xmax = max + BORDER * size;
         }
     }
     
     

     /**
      * Sets the <em>y</em>-scale to the specified range.
      *
      * @param  min the minimum value of the <em>y</em>-scale
      * @param  max the maximum value of the <em>y</em>-scale
      * @throws IllegalArgumentException if {@code (max == min)}
      */
     public static void setYscale(double min, double max) {
         double size = max - min;
         if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
         synchronized (mouseLock) {
             ymin = min - BORDER * size;
             ymax = max + BORDER * size;
         }
     }
    
    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.add(e.getKeyCode());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.remove(e.getKeyCode());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }
    
    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseClicked(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseEntered(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseExited(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = MazeCreator.userX(e.getX());
            mouseY = MazeCreator.userY(e.getY());
            mousePressed = true;
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        synchronized (mouseLock) {
            mousePressed = false;
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseDragged(MouseEvent e)  {
        synchronized (mouseLock) {
            mouseX = MazeCreator.userX(e.getX());
            mouseY = MazeCreator.userY(e.getY());
        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = MazeCreator.userX(e.getX());
            mouseY = MazeCreator.userY(e.getY());
        }
    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public static JFrame getFrame() {
		return frame;
	}

    /**
     * Returns the next key that was typed by the user (that your program has not already processed).
     * This method should be preceded by a call to {@link #hasNextKeyTyped()} to ensure
     * that there is a next key to process.
     * This method returns a Unicode character corresponding to the key
     * typed (such as {@code 'a'} or {@code 'A'}).
     * It cannot identify action keys (such as F1 and arrow keys)
     * or modifier keys (such as control).
     *
     * @return the next key typed by the user (that your program has not already processed).
     * @throws NoSuchElementException if there is no remaining key
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            if (keysTyped.isEmpty()) {
                throw new NoSuchElementException("your program has already processed all keystrokes");
            }
            return keysTyped.removeLast();
        }
    }

    /**
     * Returns true if the given key is being pressed.
     * <p>
     * This method takes the keycode (corresponding to a physical key)
    *  as an argument. It can handle action keys
     * (such as F1 and arrow keys) and modifier keys (such as shift and control).
     * See {@link KeyEvent} for a description of key codes.
     *
     * @param  keycode the key to check if it is being pressed
     * @return <tt>true</tt> if {@code keycode} is currently being pressed;
     *         <tt>false</tt> otherwise
     */
    public static boolean isKeyPressed(int keycode) {
        synchronized (keyLock) {
            return keysDown.contains(keycode);
        }
    }
    
    /**
     * Draws the specified image centered at (<em>x</em>, <em>y</em>),
     * rescaled to the specified bounding box.
     * The supported image formats are JPEG, PNG, and GIF.
     *
     * @param  x the center <em>x</em>-coordinate of the image
     * @param  y the center <em>y</em>-coordinate of the image
     * @param  filename the name of the image/picture, e.g., "ball.gif"
     * @param  scaledWidth the width of the scaled image in pixels
     * @param  scaledHeight the height of the scaled image in pixels
     * @throws IllegalArgumentException if either {@code scaledWidth}
     *         or {@code scaledHeight} is negative
     * @throws IllegalArgumentException if the image filename is invalid
     */
    public static void picture(double x, double y, String filename, double scaledWidth, double scaledHeight) {
        Image image = getImage(filename);
        if (scaledWidth < 0) throw new IllegalArgumentException("width is negative: " + scaledWidth);
        if (scaledHeight < 0) throw new IllegalArgumentException("height is negative: " + scaledHeight);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(scaledWidth);
        double hs = factorY(scaledHeight);
        if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }
        draw();
    }
    
    /**
     * Draws one pixel at (<em>x</em>, <em>y</em>).
     * This method is private because pixels depend on the display.
     * To achieve the same effect, set the pen radius to 0 and call {@code point()}.
     *
     * @param  x the <em>x</em>-coordinate of the pixel
     * @param  y the <em>y</em>-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }
    
    // get an image from the given filename
    public static Image getImage(String filename) {
        if (filename == null) throw new NullPointerException();

        // to read from file
        ImageIcon icon = new ImageIcon(filename);

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            }
            catch (Exception e) {
                /* not a url */
            }
        }

        // in case file is inside a .jar (classpath relative to StdDraw)
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = MazeCreator.class.getResource(filename);
            if (url != null)
                icon = new ImageIcon(url);
        }

        // in case file is inside a .jar (classpath relative to root of jar)
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = MazeCreator.class.getResource("/" + filename);
            if (url == null) throw new IllegalArgumentException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }
	

    /**
     * Display on screen, pause for t milliseconds, and turn on
     * <em>animation mode</em>: subsequent calls to
     * drawing methods such as {@code line()}, {@code circle()}, and {@code square()}
     * will not be displayed on screen until the next call to {@code show()}.
     * This is useful for producing animations (clear the screen, draw a bunch of shapes,
     * display on screen for a fixed amount of time, and repeat). It also speeds up
     * drawing a huge number of shapes (call {@code show(0)} to defer drawing
     * on screen, draw the shapes, and call {@code show(0)} to display them all
     * on screen at once).
     * @param t number of milliseconds
     */
    public static void show(int t) {
        // sleep until the next time we're allowed to draw
        long millis = System.currentTimeMillis();
        if (millis < nextDraw) {
            try {
                Thread.sleep(nextDraw - millis);
            }
            catch (InterruptedException e) {
                System.out.println("Error sleeping");
            }
            millis = nextDraw;
        }

        defer = false;
        draw();
        defer = true;

        // when are we allowed to draw again
        nextDraw = millis + t;
    }

    /**
     * Draws a line segment between (<em>x</em><sub>0</sub>, <em>y</em><sub>0</sub>) and
     * (<em>x</em><sub>1</sub>, <em>y</em><sub>1</sub>).
     *
     * @param  x0 the <em>x</em>-coordinate of one endpoint
     * @param  y0 the <em>y</em>-coordinate of one endpoint
     * @param  x1 the <em>x</em>-coordinate of the other endpoint
     * @param  y1 the <em>y</em>-coordinate of the other endpoint
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }
    
	private static double  scaleX(double x) { return width  * (x - xmin) / (xmax - xmin); }
    private static double  scaleY(double y) { return height * (ymax - y) / (ymax - ymin); }
	private static double   userX(double x) { return xmin + x * (xmax - xmin) / width;    }
    private static double   userY(double y) { return ymax - y * (ymax - ymin) / height;   }
    private static double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    private static double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
}
