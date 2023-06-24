package com.ashbysoft.swingland;

import java.io.IOException;
import java.net.URL;
import com.ashbysoft.logger.Logger;
import com.ashbysoft.swingland.image.ImageIO;

public class ImageIcon implements Icon {
    private final Logger _log = new Logger("[ImageIcon@"+hashCode()+"]");
    private Image _image;
    public ImageIcon() {}
    public ImageIcon(byte[] data) {
        try {
            _image = ImageIO.read(data);
        } catch (IOException e) {
            _log.error("unable to read image from byte array: "+e.toString());
        }
    }
    public ImageIcon(String file) {
        try {
            _image = ImageIO.read(file);
        } catch (IOException e) {
            _log.error("unable to read image from file: "+e.toString());
        }
    }
    public ImageIcon(URL url) {
        try {
            _image = ImageIO.read(url);
        } catch (IOException e) {
            _log.error("unable to read image from URL: "+e.toString());
        }
    }
    public ImageIcon(Image image) {
        _image = image;
    }
    public int getIconWidth() { return _image != null ? _image.getWidth(null) : -1; }
    public int getIconHeight() { return _image != null ? _image.getHeight(null) : -1; }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (_image != null)
            g.drawImage(_image, x, y);
    }
}
