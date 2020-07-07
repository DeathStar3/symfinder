//
// Created by ThePrecisionBro on 27/11/2019.
//

#ifndef CPP_DECORATOR_WINDOWSCROLLBAR_H
#define CPP_DECORATOR_WINDOWSCROLLBAR_H


#include "Window.h"

class WindowScrollBar : public Window  {
protected:
    Window *m_decoratedWindow;
public:
    explicit WindowScrollBar (Window *decoratedWindow);
};


#endif //CPP_DECORATOR_WINDOWSCROLLBAR_H
