JAVAC = javac
JAVA  = java

help:
	@echo 'make [JAVAC=javac] compile    -- compiles the code (using given compiler)'
	@echo 'make [JAVA=java  ] benchmark  -- executes the benchmark (using given JVM)'
	@echo 'make               clean      -- removes compiled files'

compile:
	javac com/mina86/unsafe/UnsafeString*.java

benchmark:
	java -cp . com.mina86.unsafe.UnsafeStringBenchmark

clean:
	rm -f -- com/mina86/unsafe/UnsafeString*.class
