all: missing_paren empty_if_block minimal classes e e1

missing_paren: src/test/fail/missing_paren.j
	./gradlew run --args="src/test/pass/missing_paren.j"

empty_if_block: src/test/fail/empty_if_block.j
	./gradlew run --args="src/test/pass/empty_if_block.j"

minimal: src/test/pass/minimal.j
	./gradlew run --args="src/test/pass/minimal.j"

classes: src/test/pass/classes.j
	./gradlew run --args="src/test/pass/classes.j"

e: src/test/pass/e.j
	./gradlew run --args="src/test/pass/e.j"

e1: src/test/pass/e1.j
	./gradlew run --args="src/test/pass/e1.j"
