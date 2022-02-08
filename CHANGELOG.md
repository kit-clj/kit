# Change Log

<details>
<summary>kit-core</summary>

### `io.github.kit-clj/kit-core {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-core {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-hato</summary>

### `io.github.kit-clj/kit-hato {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-metrics</summary>

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.1"}`

- Breaking fix: metric definitions API is broken

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-quartz</summary>

### `io.github.kit-clj/kit-quartz {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-redis</summary>

### `io.github.kit-clj/kit-redis {:mvn/version "1.0.1"}`

- Change: `key-for` uses CacheKey protocol to convert based on type, defaulting to hashing Objects instead of `pr-str`
- Change: `key-for` ignores prefixing keys when there is no default `key-prefix` in the component config

### `io.github.kit-clj/kit-redis {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-repl</summary>

### `io.github.kit-clj/kit-repl {:mvn/version "1.0.1"}`

- Fix: ensure org.clojure/tools.logging is included in deps in case used as standalone

### `io.github.kit-clj/kit-repl {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-nrepl</summary>

### `io.github.kit-clj/kit-nrepl {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-sql</summary>

### `io.github.kit-clj/kit-sql {:mvn/version "1.1.0"}`

- Change: Now just a bare bones wrapper that imports kit-sql-conman and kit-sql-migratus for compatibility purposes

### `io.github.kit-clj/kit-sql {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-sql-conman</summary>

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-sql-hikari</summary>

### `io.github.kit-clj/kit-sql-hikari {:mvn/version "1.0.1"}`

- Initial release

</details>



<details>
<summary>kit-sql-migratus</summary>

### `io.github.kit-clj/kit-sql-migratus {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-postgres</summary>

### `io.github.kit-clj/kit-postgres {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-mysql</summary>

### `io.github.kit-clj/kit-mysql {:mvn/version "1.0.0"}`

- Initial release [PR #18](https://github.com/kit-clj/kit/pull/18)
</details>



<details>
<summary>kit-undertow</summary>

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.1"}`

- Fix: ensure org.clojure/tools.logging is included in deps in case used as standalone

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-http-kit</summary>

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-xtdb</summary>

### `io.github.kit-clj/kit-xtdb {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-selmer</summary>

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>lein-template</summary>

### `io.github.kit-clj/lein-template {:mvn/version "0.1.11"}`

- Change default cookie secret to randomly generated secret at template generation
  
### `io.github.kit-clj/lein-template {:mvn/version "0.1.10"}`
  
- Fix missing require in test utils
- Add test stub in template

### `io.github.kit-clj/lein-template {:mvn/version "0.1.9"}`

- Add symlink to kit libs so no need to manually bump versions when releasing template
- Update Dockerfile
- Refactor sql profiles
- Add clj-kondo cache to default gitignore

### `io.github.kit-clj/lein-template {:mvn/version "0.1.8"}`

- Update kit-generator lib

### `io.github.kit-clj/lein-template {:mvn/version "0.1.7"}`

- Update log config to exclude jgit debug noise

### `io.github.kit-clj/lein-template {:mvn/version "0.1.6"}`

- Template updated for the sql variants

### `io.github.kit-clj/lein-template {:mvn/version "0.1.5"}`

- New: Add `+socket-repl` profile for socket REPL
- Change: Default socket REPL host changed to `"127.0.0.1"`
- Change: Bump dependencies
- Change: Remove `kit-repl` library from default template profile
- Change: Remove `+bare` profile
- Change: Make socket repl as default in `+full` profile

### `io.github.kit-clj/lein-template {:mvn/version "0.1.4"}`

- Change: Add `nrepl` and `cider` deps aliases in template as default

### `io.github.kit-clj/lein-template {:mvn/version "0.1.3"}`

- Change: Default socket REPL port to 7200
- Fix: Default local nREPL port to 127.0.0.1

### `io.github.kit-clj/lein-template {:mvn/version "0.1.2"}`

- Change: Add `+nrepl` profile
- Change: Bump `kit-redis` to 1.0.1
- Change: Bump `kit-undertow` to 1.0.1
- Change: Bump `kit-repl` to 1.0.1

### `io.github.kit-clj/lein-template {:mvn/version "0.1.1"}`

- Change: repository URL for public modules to use HTTPS instead of SSH URL. Prevents breakage when SSH key fails to load

### `io.github.kit-clj/lein-template {:mvn/version "0.1.0"}`

- Initial ALPHA release, API subject to change
</details>



<details>
<summary>kit-generator</summary>

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.3"}`

- Fix issue with [git clone](https://github.com/kit-clj/kit/pull/30)

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.2"}`

- New: Snippet generation
- Change: Bump dependencies

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.0"}`

- Initial ALPHA release, API subject to change
</details>


