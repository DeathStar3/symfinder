//
// Created by ThePrecisionBro on 27/11/2019.
//

#include "HorizontalScrollBarDecorator.h"

void HorizontalScrollBarDecorator::draw() {
    drawHorizontalScrollBar();
    m_decoratedWindow->draw();
}

std::string HorizontalScrollBarDecorator::getDescription() {
    return m_decoratedWindow->getDescription() + "with horizontal scrollbars\n";
}

void HorizontalScrollBarDecorator::drawHorizontalScrollBar() {
}

HorizontalScrollBarDecorator::HorizontalScrollBarDecorator(Window *pWindow) : WindowScrollBar(pWindow) {}
