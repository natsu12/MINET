package minet.ui.misc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
        setSize(widthLimit, totalHeight);
        this.setIgnoreRepaint(true);
    }
    
    public void applyImg(String path, boolean self) {

        int widthLimit = 250;
       
        
        JLabel jlblImageViewer = new JLabel();
        ImageIcon ico=new ImageIcon(path);
        
        int IconWidth = ico.getIconWidth();
        int IconHeight = ico.getIconHeight();
        
        while (IconWidth > widthLimit) {
        	IconHeight *= 0.9;
        	IconWidth *= 0.9;
        }
        Image temp=ico.getImage().getScaledInstance(IconWidth, IconHeight, ico.getImage().SCALE_DEFAULT);
        
        jlblImageViewer.setIcon(new ImageIcon(temp));
        
        if (self) {
        	jlblImageViewer.setBounds(10, 10, IconWidth, IconHeight);
        } else {
        	jlblImageViewer.setBounds(50, 10, IconWidth, IconHeight);
        }
        
        add(jlblImageViewer);
        setLayout(null);
        
        setPreferredSize(new Dimension(widthLimit, IconHeight + 20));
        
        this.setIgnoreRepaint(true);
    }
    
    public ImageIcon getImageIcon(String path, int width, int height) {
    	  if (width == 0 || height == 0) {
    	   return new ImageIcon(this.getClass().getResource(path));
    	  }
    	  ImageIcon icon = new ImageIcon(this.getClass().getResource(path));
    	  icon.setImage(icon.getImage().getScaledInstance(width, height,
    	    Image.SCALE_DEFAULT));
    	  return icon;
    }
}
