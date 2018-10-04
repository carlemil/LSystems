import javax.swing.*
import java.awt.*
import java.awt.geom.Line2D

class DrawLine : JComponent() {
    override fun paint(g: Graphics?) {
        // Draw a simple line using the Graphics2D draw() method.
        val g2 = g as Graphics2D?
        g2!!.stroke = BasicStroke(2f)
        g2.color = Color.RED
        g2.draw(Line2D.Double(50.0, 150.0, 250.0, 350.0))
        g2.color = Color.GREEN
        g2.draw(Line2D.Double(250.0, 350.0, 350.0, 250.0))
        g2.color = Color.BLUE
        g2.draw(Line2D.Double(350.0, 250.0, 150.0, 50.0))
        g2.color = Color.YELLOW
        g2.draw(Line2D.Double(150.0, 50.0, 50.0, 150.0))
        g2.color = Color.BLACK
        g2.draw(Line2D.Double(0.0, 0.0, 400.0, 400.0))
    }

    init {
        val frame = JFrame("Draw Line")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.add(DrawLine())
        frame.pack()
        frame.size = Dimension(440, 440)
        frame.isVisible = true
    }
}