.PHONY: clean run repl test test-watch uberjar

clean:
	rm -rf target

run:
	clj -M:dev

repl:
	clj -M:dev:nrepl

test:
	clj -M:test

test-watch:
	clj -M:test:test-watch

uberjar:
	clj -T:build all
