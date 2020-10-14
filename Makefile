all: ir3

ir3: src/test/pass/ir3.j
	./gradlew run --args="src/test/pass/ir3.j"
