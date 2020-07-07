//
// Created by ThePrecisionBro on 27/11/2019.
//

#ifndef CPP_DECORATOR_WINDOW_H
#define CPP_DECORATOR_WINDOW_H


#include <string>

class Window  {
public:
    virtual void draw();
    virtual std::string getDescription();
    virtual ~Window() {}
};

#endif //CPP_DECORATOR_WINDOW_H
