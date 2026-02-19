.PHONY: all compile run test clean

# Main class with the main method
MAIN_CLASS = P3

# Directory containing the test input files
#INPUT_DIR = test_inputs

# The list of test input files (relative to INPUT_DIR)
#TEST_INPUTS = Factorial.java
#TEST_INPUTS = BinarySearch.java BinaryTree.java BubbleSort.java Factorial.java LinearSearch.java LinkedList.java QuickSort.java TreeVisitor.java Test1.java Test2.java Test3.java Test4.java Test5.java
#TEST_INPUTS = Lambda1.javajava BubbleSort.java Factorial.java LinearSearch.java LinkedList.java QuickSort.java TreeVisitor.java Test1.java Test2.java Test3.java Test4.java Test5.java
#TEST_INPUTS = Lambda1.java
#TEST_INPUTS = TreeVisitor.java
#TEST_INPUTS = BinarySearch.java
#TEST_INPUTS = LambaTest.java
#TEST_INPUTS = Test6.java
# The default target, which compiles and then tests the program


INPUT_DIR = ./
TEST_INPUTS = Test.java 


#INPUT_DIR = test_lambda
#TEST_INPUTS = test1.java test2.java test3.java test4.java test5.java test6.java test7.java test8.java test9.java test10.java test11.java test12.java test13.java test14.java test15.java test16.java
#TEST_INPUTS = test16.java
all: compile test compare clean

# Compiles translator + visitors
compile:
	@echo "Compiling translator..."
	javac $(MAIN_CLASS).java

# Runs translator to generate .miniIR files
test:
	@echo "Translating MiniJava to MiniIR..."
	@for input in $(TEST_INPUTS); do \
		name=$$(basename $$input .java); \
		java $(MAIN_CLASS) < $(INPUT_DIR)/$$input > $(INPUT_DIR)/$$name.miniIR; \
		echo "--- Generated $$name.miniIR ---"; \
	done

# Compares MiniJava output with MiniIR execution via pgi.jar
compare:
	@echo "Comparing outputs for semantic equivalence..."
	@for input in $(TEST_INPUTS); do \
		name=$$(basename $$input .java); \
		echo " Checking: $$name.java vs $$name.miniIR"; \
		javac $(INPUT_DIR)/$$input && \
		java -cp $(INPUT_DIR) $$name > $(INPUT_DIR)/$$name.java.out && \
		java -jar pgi.jar < $(INPUT_DIR)/$$name.miniIR > $(INPUT_DIR)/$$name.ir.out; \
		if diff -wB $(INPUT_DIR)/$$name.java.out $(INPUT_DIR)/$$name.ir.out > /dev/null; then \
			echo ""; \
		else \
			echo " $$name: ---- NO MATCH -----!"; \
		fi; \
	done

# Remove class files
clean:
	@echo "Cleaning up generated files..."
	find . -name "*.class" -type f -delete
#	find $(INPUT_DIR) -name "*.miniIR" -type f -delete
#	find $(INPUT_DIR) -name "*.java.out" -type f -delete
#	find $(INPUT_DIR) -name "*.ir.out" -type f -delete
	@echo "Clean complete."