SRC			= $(shell find . -name \*.cpp)
OBJS		= $(SRC:.cpp=.o)
BIN			= main.binary
CXX			= g++
CPPFLAGS	= -Wall -std=c++14

all: $(OBJS)
	$(CXX) $(CPPFLAGS) $(OBJS) -o $(BIN)

clean:
	rm -rf $(OBJS) $(BIN)

.PHONY: all clean