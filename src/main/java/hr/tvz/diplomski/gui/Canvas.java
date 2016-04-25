package hr.tvz.diplomski.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by Filip on 28.1.2016..
 */
public class Canvas extends JComponent implements SwingConstants {

    /**
     *
     */
    private static final long serialVersionUID = -7273834532638324936L;
    private Image image;
    private Image backupImage;
    private int verticalAlignment = CENTER;
    private int horizontalAlignment = CENTER;
    private int imgSizeX,imgSizeY;

    public int getImgSizeX() {
        return imgSizeX;
    }

    public void setImgSizeX(int imgSizeX) {
        this.imgSizeX = imgSizeX;
    }

    public int getImgSizeY() {
        return imgSizeY;
    }

    public void setImgSizeY(int imgSizeY) {
        this.imgSizeY = imgSizeY;
    }

    public Canvas() {}

    public Canvas(Image image) {
        setImage(image);
    }

    public Image getImage() {
        return image;
    }

    public void resetImage(){
        if (image != null)
            image = backupImage;
        repaint();
    }
    public void setImage(Image image) {
        this.image = image;
        this.backupImage = image;
        imgSizeX = image.getWidth(this);
        imgSizeY = image.getHeight(this);
        repaint();
    }

    public void setImage(String file) {
        setImage(new ImageIcon(file).getImage());
    }

    public void setImage(File file) {
        setImage(new ImageIcon(file.getAbsolutePath()).getImage());
    }

    public void setImage(byte[] imageData) {
        setImage(imageData==null ? null : new ImageIcon(imageData).getImage());
    }

    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     *        bound: true
     *         enum: TOP    SwingConstants.TOP
     *               CENTER SwingConstants.CENTER
     *               BOTTOM SwingConstants.BOTTOM
     *    attribute: visualUpdate true
     *  description: The alignment of the image along the Y axis.
     */
    public void setVerticalAlignment(int verticalAlignment) {
        if( (verticalAlignment==TOP) || (verticalAlignment==CENTER) || (verticalAlignment==BOTTOM) )
            this.verticalAlignment = verticalAlignment;
        else
            throw new IllegalArgumentException("Invalid Vertical Alignment: " + verticalAlignment);
    }

    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     *        bound: true
     *         enum: LEFT    SwingConstants.LEFT
     *               CENTER SwingConstants.CENTER
     *               RIGHT SwingConstants.RIGHT
     *    attribute: visualUpdate true
     *  description: The alignment of the image along the X axis.
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        if( (horizontalAlignment==LEFT) || (horizontalAlignment==CENTER) || (horizontalAlignment==RIGHT) )
            this.horizontalAlignment = horizontalAlignment;
        else
            throw new IllegalArgumentException("Invalid Horizontal Alignment: " + horizontalAlignment);
    }

    @Override
    public Dimension getPreferredSize() {
        if(image == null)
            return super.getPreferredSize();
        else
            return new Dimension(image.getWidth(this), image.getHeight(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(image==null)
            return;

        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;

        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;

        int src_w = image.getWidth(null);
        int src_h = image.getHeight(null);

        double scale_x = ((double)w)/src_w;
        double scale_y = ((double)h)/src_h;

        double scale = Math.min(scale_x, scale_y);

        int dst_w = (int)(scale * src_w);
        int dst_h = (int)(scale * src_h);

        int dx = x + (w-dst_w)/2;
        if(horizontalAlignment==LEFT)
            dx = x;
        else if(horizontalAlignment==RIGHT)
            dx = x + w - dst_w;

        int dy = y + (h-dst_h)/2;
        if(verticalAlignment==TOP)
            dy = y;
        else if(verticalAlignment==BOTTOM)
            dy = y + h - dst_h;

        g.drawImage(image, dx, dy, dx+dst_w, dy+dst_h, 0, 0, src_w, src_h, null);
    }


}