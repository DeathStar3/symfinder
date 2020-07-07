#include <iostream>
#include <string>
#include "Window.h"
#include "SimpleWindow.h"
#include "HorizontalScrollBarDecorator.h"
#include "VerticalScrollBarDecorator.h"

using namespace std;




int main()
{
    Window *simple = new Window();
    cout << simple -> getDescription() << endl;

    Window *horiz = new HorizontalScrollBarDecorator ( new SimpleWindow());
    cout << horiz -> getDescription() << endl;

    Window *vert = new VerticalScrollBarDecorator ( new SimpleWindow());
    cout << vert -> getDescription() << endl;

    Window *decoratedWindow = new HorizontalScrollBarDecorator (
            new VerticalScrollBarDecorator(new SimpleWindow()));
    cout << decoratedWindow -> getDescription() << endl;

    return 0;
}