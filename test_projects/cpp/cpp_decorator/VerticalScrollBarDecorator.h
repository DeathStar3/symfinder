//
// Created by ThePrecisionBro on 27/11/2019.
//

#ifndef CPP_DECORATOR_VERTICALSCROLLBARDECORATOR_H
#define CPP_DECORATOR_VERTICALSCROLLBARDECORATOR_H


#include <string>
#include "WindowScrollBar.h"
#include "Window.h"

class VerticalScrollBarDecorator : public WindowScrollBar {
public:

    VerticalScrollBarDecorator(Window *pWindow);

    void draw() override;

    std::string getDescription() override;

private:
    void drawVerticalScrollBar();
};


#endif //CPP_DECORATOR_VERTICALSCROLLBARDECORATOR_H
