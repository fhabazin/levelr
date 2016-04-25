/*
 * Name:     Water
 * Date:     December 2004
 * Author:   Neil Wallis
 * Purpose:  Simulate ripples on water.
 */
package hr.tvz.diplomski.gui;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;

public class water extends Applet implements
        Runnable, MouseListener, MouseMotionListener {

    String str;
    int width,height,hwidth,hheight;
    MemoryImageSource source;
    Image image, offImage;
    Graphics offGraphics;
    int i,a,b;
    int MouseX,MouseY;
    int fps,delay,size;

    short ripplemap[];
    int texture[];
    int ripple[];
    int oldind,newind,mapind;
    int riprad;
    Image im;

    Thread animatorThread;
    boolean frozen = false;

    public void init() {
        addMouseListener(this);
        addMouseMotionListener(this);

        //Retrieve the base image
        str = "levelBackground.png";
        if (str != null) {
            try {
                MediaTracker mt = new MediaTracker(this);
                im = ImageIO.read(new File("\\levelBackground.png"));
                mt.addImage(im,0);
                try {
                    mt.waitForID(0);
                } catch (InterruptedException e) {
                    return;
                }
            } catch(Exception e) {}
        }

        //How many milliseconds between frames?
        str = getParameter("fps");

                fps = 3;

        delay = (fps > 0) ? (1000 / fps) : 100;

        width = im.getWidth(this);
        height = im.getHeight(this);
        hwidth = width>>1;
        hheight = height>>1;
        riprad=3;

        size = width * (height+2) * 2;
        ripplemap = new short[size];
        ripple = new int[width*height];
        texture = new int[width*height];
        oldind = width;
        newind = width * (height+3);

        PixelGrabber pg = new PixelGrabber(im,0,0,width,height,texture,0,width);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {}


        source = new MemoryImageSource(width, height, ripple, 0, width);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);

        image = createImage(source);
        offImage = createImage(width, height);
        offGraphics = offImage.getGraphics();
    }

    public void start() {
        if (frozen) {
            //Do nothing.
        } else {
            //Start animation thread
            if (animatorThread == null) {
                animatorThread = new Thread(this);
            }
            animatorThread.start();
        }
    }

    public void stop() {
        animatorThread = null;
    }

    public void destroy() {
        removeMouseListener(this);
        removeMouseMotionListener(this);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        if (frozen) {
            frozen = false;
            start();
        } else {
            frozen = true;
            animatorThread = null;
        }
    }

    public void mouseMoved(MouseEvent e) {
        disturb(e.getX(),e.getY());
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        long startTime = System.currentTimeMillis();

        while (Thread.currentThread() == animatorThread) {
            newframe();
            source.newPixels();
            offGraphics.drawImage(image,0,0,width,height,null);
            repaint();

            try {
                startTime += delay;
                Thread.sleep(Math.max(0,startTime-System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g) {
        g.drawImage(offImage,0,0,this);
    }

    public void disturb(int dx, int dy) {
        for (int j=dy-riprad;j<dy+riprad;j++) {
            for (int k=dx-riprad;k<dx+riprad;k++) {
                if (j>=0 && j<height && k>=0 && k<width) {
                    ripplemap[oldind+(j*width)+k] += 512;
                }
            }
        }
    }

    public void newframe() {
        //Toggle maps each frame
        i=oldind;
        oldind=newind;
        newind=i;

        i=0;
        mapind=oldind;
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                short data = (short)((ripplemap[mapind-width]+ripplemap[mapind+width]+ripplemap[mapind-1]+ripplemap[mapind+1])>>1);
                data -= ripplemap[newind+i];
                data -= data >> 5;
                ripplemap[newind+i]=data;

                //where data=0 then still, where data>0 then wave
                data = (short)(1024-data);

                //offsets
                a=((x-hwidth)*data/1024)+hwidth;
                b=((y-hheight)*data/1024)+hheight;

                //bounds check
                if (a>=width) a=width-1;
                if (a<0) a=0;
                if (b>=height) b=height-1;
                if (b<0) b=0;

                ripple[i]=texture[a+(b*width)];
                mapind++;
                i++;
            }
        }
    }

}
