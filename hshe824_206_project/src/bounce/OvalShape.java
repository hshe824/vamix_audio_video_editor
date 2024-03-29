package bounce;

/**
 * This shape class produces an oval shape.
 * 
 * @author Harry She
 */
public class OvalShape extends Shape {

	/**
	 * Default constructor that creates a RectangleShape instance whose instance
	 * variables are set to default values.
	 */
	public OvalShape() {
		super();
	}

	/**
	 * Creates a OvalShape instance with specified values for instance
	 * variables.
	 * 
	 * @param x
	 *            x position.
	 * @param y
	 *            y position.
	 * @param deltaX
	 *            speed and direction for horizontal axis.
	 * @param deltaY
	 *            speed and direction for vertical axis.
	 */
	public OvalShape(int x, int y, int deltaX, int deltaY) {
		super(x, y, deltaX, deltaY);
	}

	/**
	 * Creates a OvalShape instance with specified values for instance
	 * variables.
	 * 
	 * @param x
	 *            x position.
	 * @param y
	 *            y position.
	 * @param deltaX
	 *            speed (pixels per move call) and direction for horizontal
	 *            axis.
	 * @param deltaY
	 *            speed (pixels per move call) and direction for vertical axis.
	 * @param width
	 *            width in pixels.
	 * @param height
	 *            height in pixels.
	 */
	public OvalShape(int x, int y, int deltaX, int deltaY, int width, int height) {
		super(x, y, deltaX, deltaY, width, height);
	}

	public OvalShape(int x, int y, int deltaX, int deltaY, int width, int height, String text) {
		super(x, y, deltaX, deltaY, width, height, text);
		_text = text;
	}

	/**
	 * Paints this OvalShape object using the supplied Painter object.
	 */
	protected void paints(Painter painter) {
		painter.drawOval(_x, _y, _width, _height);
	}

}
