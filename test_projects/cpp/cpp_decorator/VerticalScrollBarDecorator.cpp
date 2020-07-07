//
// Created by ThePrecisionBro on 27/11/2019.
//

#include "VerticalScrollBarDecorator.h"

void VerticalScrollBarDecorator::draw() {
    drawVerticalScrollBar();
    m_decoratedWindow->draw();
}

std::string VerticalScrollBarDecorator::getDescription() {
    return m_decoratedWindow->getDescription() + "with vertical scrollbars\n";
}

void VerticalScrollBarDecorator::drawVerticalScrollBar() {
}

VerticalScrollBarDecorator::VerticalScrollBarDecorator(Window *pWindow) : WindowScrollBar(pWindow) {}
