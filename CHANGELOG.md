# Change Log

## 23.01.2022

### `io.github.kit-clj/lein-template {:mvn/version "0.1.5"}`

- New: Add `+socket-repl` profile for socket REPL
- Change: Default socket REPL host changed to `"127.0.0.1"`
- Change: Bump dependencies
- Change: Remove `kit-repl` library from default template profile
- Change: Remove `+bare` profile
- Change: Make socket repl as default in `+full` profile

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.2"}`

- New: Snippet generation
- Change: Bump dependencies

### `io.github.kit-clj/kit-core {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.1"}`

- Change: Bump dependencies

## 17.01.2022

### `io.github.kit-clj/lein-template {:mvn/version "0.1.4"}`

- Change: Add `nrepl` and `cider` deps aliases in template as default

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.1"}`

- Breaking fix: metric definitions API is broken 

## 12.01.2022

### `io.github.kit-clj/lein-template {:mvn/version "0.1.3"}`

- Change: Default socket REPL port to 7200
- Fix: Default local nREPL port to 127.0.0.1

## 11.01.2022

### `io.github.kit-clj/kit-nrepl {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-repl {:mvn/version 1.0.1}`

- Fix: ensure org.clojure/tools.logging is included in deps in case used as standalone

### `io.github.kit-clj/kit-undertow {:mvn/version 1.0.1}`

- Fix: ensure org.clojure/tools.logging is included in deps in case used as standalone

### `io.github.kit-clj/lein-template {:mvn/version 0.1.2}`

- Change: Add `+nrepl` profile
- Change: Bump `kit-redis` to 1.0.1
- Change: Bump `kit-undertow` to 1.0.1
- Change: Bump `kit-repl` to 1.0.1

## 10.01.2022

### `io.github.kit-clj/kit-redis {:mvn/version 1.0.1}`

- Change: `key-for` uses CacheKey protocol to convert based on type, defaulting to hashing Objects instead of `pr-str`
- Change: `key-for` ignores prefixing keys when there is no default `key-prefix` in the component config

## 09.01.2022

### `io.github.kit-clj/lein-template {:mvn/version 0.1.1}`

- Change: repository URL for public modules to use HTTPS instead of SSH URL. Prevents breakage when SSH key fails to load

## 08.01.2022

### `io.github.kit-clj/kit-core {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-hato {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-metrics {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-postgres {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-quartz {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-redis {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-repl {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-sql {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-undertow {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/kit-xtdb {:mvn/version 1.0.0}`

initial release

### `io.github.kit-clj/lein-template {:mvn/version 0.1.0}`

initial ALPHA release, API subject to change

### `io.github.kit-clj/kit-generator {:mvn/version 0.1.0}`

initial ALPHA release, API subject to change