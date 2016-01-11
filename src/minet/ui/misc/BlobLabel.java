package minet.ui.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import common.HtmlEscape;

public class BlobLabel extends JLabel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3808214299050510320L;

	public BlobLabel() {
        super();
        setVerticalAlignment(SwingConstants.TOP);
        setBorder(new LineBorder(new Color(250, 211, 157), 2, true));
        this.setMaximumSize(new Dimension(300, 2000));
        setOpaque(true);
        setBackground(Color.WHITE);
        setBackground(new Color(250, 250, 250));
        setVisible(true);
    }
    
    public void applyText(String text) {
        StringBuilder builder = new StringBuilder("<html>"); 
        int totalHeight = 6;
        char[] chars = text.toCharArray();
        int widthLimit = 250;
        FontMetrics fontMetrics = getFontMetrics(getFont());
        int lineBeginIndex = 0;
        while (lineBeginIndex < chars.length) {
            int limit = 1;
            boolean wrapLine = true;
            while (lineBeginIndex + limit <= chars.length &&
                   fontMetrics.charsWidth(chars, lineBeginIndex, limit) < widthLimit) {
                ++limit;
                if (chars[lineBeginIndex + limit - 2] == '\n') {
                    wrapLine = false;
                    break;
                }
            }
            StringBuilder expSb = new StringBuilder();
            
            for (int i = lineBeginIndex; i < lineBeginIndex + limit - 1; ++i) {
                expSb.append("s");
            }
            String s = String.valueOf(chars, lineBeginIndex, limit - 1); 
            s = HtmlEscape.escape(s);
            builder.append(s);
            if (wrapLine) {
                builder.append("<br/>");
            }
            lineBeginIndex += limit - 1;
            totalHeight += fontMetrics.getHeight();
        }
        builder.append("</html>");
        setLayout(null);
        setText(builder.toString());
        setSize(widthLimit,Math.max(50, totalHeight));
        this.setIgnoreRepaint(true);
    }
}
