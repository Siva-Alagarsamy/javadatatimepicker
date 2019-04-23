JC = javac -classpath ../../../..

JAVAFILES= $(shell ls *.java)

CLASSFILES = $(JAVAFILES:%.java=%.class)

all: $(CLASSFILES)

clean: 
	rm -f *.class;

%.class: %.java
	$(JC) $<
