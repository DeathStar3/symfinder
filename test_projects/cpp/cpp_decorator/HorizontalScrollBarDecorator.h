//
// Created by ThePrecisionBro on 27/11/2019.
//

#ifndef CPP_DECORATOR_HORIZONTALSCROLLBARDECORATOR_H
#define CPP_DECORATOR_HORIZONTALSCROLLBARDECORATOR_H


#include <string>
#include "WindowScrollBar.h"

class HorizontalScrollBarDecorator : public WindowScrollBar {
public:
    HorizontalScrollBarDecorator(Window *pWindow);

    void draw() override;
    std::string getDescription() override;
private:
    void drawHorizontalScrollBar();
};

#endif //CPP_DECORATOR_HORIZONTALSCROLLBARDECORATOR_H
