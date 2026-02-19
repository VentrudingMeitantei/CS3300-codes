.PHONY: all compile run test clean

# Main class with the main method
MAIN_CLASS = P5
PARSE_CLASS = ../microIR_parser/P4_parse
PARSE_DIR = ../microIR_parser
# Directory containing the test input files
#INPUT_DIR = test_inputs

# The list of test input files (relative to INPUT_DIR)
#TEST_INPUTS = Factorial.java
#TEST_INPUTS = BinarySearch.java BinaryTree.java BubbleSort.java Factorial.java LinearSearch.java LinkedList.java QuickSort.java TreeVisitor.java Test1.java Test2.java Test3.java Test4.java Test5.java
#TEST_INPUTS = Lambda1.java
#TEST_INPUTS = TreeVisitor.java
#TEST_INPUTS = BinarySearch.java
#TEST_INPUTS = LambdaTest.java
#TEST_INPUTS = Test6.java
# The default target, which compiles and then tests the program


#INPUT_DIR = test_m
#TEST_INPUTS = test1.java test2.java test3.java test4.java test8.java test9.java test10.java test11.java test12.java
#TEST_INPUTS = test5.java test6.java test7.java

INPUT_DIR = output
#TEST_INPUTS = MoreThan4.microIR MustSpill.microIR
TEST_INPUTS = BinarySearch.microIR BinaryTree.microIR BubbleSort.microIR Factorial.microIR LinearSearch.microIR LinkedList.microIR QuickSort.microIR TreeVisitor.microIR
# TEST_INPUTS = Factorial.microIR
# TEST_INPUTS = $(notdir $(wildcard $(INPUT_DIR)/*.microIR))
# TEST_INPUTS = BasicLambdaTest.microIR
#INPUT_DIR = test_lambda
#TEST_INPUTS = test1.java test2.java test3.java test4.java test5.java test6.java test7.java test8.java test9.java test10.java test11.java test12.java test13.java test14.java test15.java test16.java
#TEST_INPUTS = test16.java
all: compile test compare clean

# Compiles translator + visitors
compile:
	@echo "Compiling translator..."
	javac $(MAIN_CLASS).java

# Runs translator to generate .microIR files
test:
	@echo "Translating MiniJava to microIR..."
	@for input in $(TEST_INPUTS); do \
		name=$$(basename $$input .microIR); \
		java $(MAIN_CLASS) < $(INPUT_DIR)/$$input > ../miniRA_output/$$name.RA; \
		echo "--- Generated $$name.RA ---"; \
	done

# Compares MiniJava output with microIR execution via pgi.jar
compare:
	@echo "Checking output for syntax equivalence..."
	@for input in $(TEST_INPUTS); do \
		name=$$(basename $$input .microIR); \
		echo " Checking: $$name.microIR vs $$name.RA"; \
		java -jar pgi.jar < $(INPUT_DIR)/$$name.microIR > ../miniRA_output/$$name.mir.out; \
		java -jar kgi.jar < ../miniRA_output/$$name.RA > ../miniRA_output/$$name.ra.out; \
		if diff -wB ../miniRA_output/$$name.mir.out ../miniRA_output/$$name.ra.out > /dev/null; then \
			echo "SAFE!"; \
		else \
			echo " $$name: ---- NO MATCH -----!"; \
		fi; \
	done

# Remove class files
clean:
	@echo "Cleaning up generated files..."
	find . -name "*.class" -type f -delete
#	find ../miniRA_output/ -name "*.mir.out" -type f -delete
#	find ../miniRA_output/ -name "*.ra.out" -type f -delete
#	find $(INPUT_DIR)/ -name "*.mir.out" -type f -delete
	@echo "Clean complete."