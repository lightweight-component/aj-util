package com.ajaxjs.image.painter;

import com.ajaxjs.image.embedd.EmbeddedElement;

import java.awt.*;

/**
 * A painter can draw another image to an image
 */
public interface Painter<T extends EmbeddedElement> {
    void draw(Graphics2D g, T element, int canvasWidth);

    void drawRepeat(Graphics2D g, T element, int canvasWidth, int canvasHeight);
}
