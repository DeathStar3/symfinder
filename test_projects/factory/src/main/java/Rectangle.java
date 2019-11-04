/*
 * This file is part of symfinder.
 *
 * symfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * symfinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

import java.awt.Point;

public class Rectangle extends Shape {

    private double width, length;

    public Rectangle(double width, double length) {
        this.width = width;
        this.length = length;
    }

    public Rectangle() {
        this.width = 1;
        this.length = 1;
    }

    public double area() {
        return width * length;
    }

    public double perimeter() {
        return 2 * (width + length);
    }

    public void draw(int x, int y) {
        // rectangle at (x, y, width, length)
    }

    public void draw(Point p) {
        // rectangle at (p.x, p.y, width, length)
    }
}