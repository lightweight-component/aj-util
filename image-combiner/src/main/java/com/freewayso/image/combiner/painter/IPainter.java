package com.freewayso.image.combiner.painter;

import com.freewayso.image.combiner.element.CombineElement;

import java.awt.*;
import java.io.IOException;

public interface IPainter {
    void draw(Graphics2D g, CombineElement element, int canvasWidth);

    void drawRepeat(Graphics2D g, CombineElement element, int canvasWidth, int canvasHeight) ;
}