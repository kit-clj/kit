# Change Log

## 2026-02-18

### New Features

- Add module removal report and `remove-module` command — auto-deletes unchanged files, prints manual steps for injections, checks reverse dependencies [PR #171](https://github.com/kit-clj/kit/pull/171). Thanks [@kovan](https://github.com/kovan)
- Add options for dependencies with more advanced feature flags [PR #167](https://github.com/kit-clj/kit/pull/167). Thanks [@bilus](https://github.com/bilus)
- Update `kit.git-config.edn` template to match clj-jgit 1.0 syntax [PR #164](https://github.com/kit-clj/kit/pull/164). Thanks [@michaelglass](https://github.com/michaelglass)

### Bug Fixes

- Fix NPE in EDN injection when target path doesn't exist — now returns data unchanged
- Fix error map in `read-files` where `:path` was set to the keyword `:path` instead of the actual path value
- Fix inconsistent exception type in `edn-safe-merge` — now throws `ExceptionInfo` instead of plain `Exception`
- Replace unsafe `read-string` with `edn/read-string` in `kit.generator.git`, `kit.generator.modules`, and lein-template
- Re-throw git sync failures as `ex-info` instead of silently printing and swallowing
- Add nil check in `kit.config/read-config` for missing resource files
- Remove duplicate `:xtdb?` condition (copy-paste bug) in lein-template
- Remove dead `check-conflicts` function in lein-template
- Add nil guards in `zloc-get-in` and `z-update-in` to prevent `AssertionError` on missing keys

### Improvements

- Replace automatic `watch-deps/start!` with manual `update-deps` function in dev `user.clj` template to prevent REPL hangs on namespace reload
- Build script gracefully handles publishing a version that already exists on Clojars

### Dependency Updates

<details>
<summary>template dependencies</summary>

* `org.clojure/clojure` 1.12.3 -> 1.12.4
* `metosin/reitit` 0.9.2 -> 0.10.0
* `ch.qos.logback/logback-classic` 1.5.20 -> 1.5.32
* `io.github.clojure/tools.build` 0.10.11 -> 0.10.12
* `org.clojure/tools.namespace` 1.5.0 -> 1.5.1
* `com.xtdb/xtdb-rocksdb` 1.21.0-beta3 -> 1.24.5
* `com.xtdb/xtdb-http-server` 1.21.0-beta3 -> 1.24.5
* `selmer/selmer` 1.12.67 -> 1.13.1
* `babashka/fs` 0.5.27 -> 0.5.31
* `io.github.seancorfield/deps-new` v0.10.1 -> v0.10.3
* `com.github.seancorfield/clj-new` 1.2.381 -> 1.3.415
</details>

<details>
<summary>lib dependencies (synced)</summary>

* `integrant/integrant` 1.0.0 -> 1.0.1
* `org.clojure/tools.logging` 1.2.4 -> 1.3.0
* `http-kit/http-kit` 2.7.0 -> 2.8.1
* `ring/ring-jetty-adapter` 1.12.2 -> 1.15.3
* `com.mysql/mysql-connector-j` 8.1.0 -> 9.5.0
* `org.postgresql/postgresql` 42.6.0 -> 42.7.8
* `cheshire/cheshire` 5.11.0 -> 6.1.0
* `nrepl/nrepl` 1.1.1 -> 1.5.1 (kit-nrepl)
* `com.github.seancorfield/next.jdbc` 1.3.883 -> 1.3.1070
* `hikari-cp/hikari-cp` 3.0.1 -> 3.3.0
* `migratus/migratus` 1.5.1 -> 1.6.4
* `com.taoensso/carmine` 3.2.0 -> 3.4.1
* `org.clojure/core.cache` 1.0.225 -> 1.1.234
* `clj-commons/iapetos` 0.1.13 -> 0.1.14
* `com.xtdb/xtdb-core` 1.23.0 -> 2.0.0
</details>

<details>
<summary>version bumps</summary>

* `io.github.kit-clj/kit-core` 1.0.9 -> 1.0.10
* `io.github.kit-clj/kit-generator` 0.2.8 -> 0.2.9
* `io.github.kit-clj/deps-template` 0.1.86 -> 0.1.87
* `io.github.kit-clj/lein-template` 0.1.86 -> 0.1.87
* `io.github.kit-clj/kit-hato` 1.0.4 -> 1.0.5
* `io.github.kit-clj/kit-http-kit` 1.0.5 -> 1.0.6
* `io.github.kit-clj/kit-jetty` 1.0.0 -> 1.0.1
* `io.github.kit-clj/kit-metrics` 1.0.3 -> 1.0.4
* `io.github.kit-clj/kit-mysql` 1.0.5 -> 1.0.6
* `io.github.kit-clj/kit-nrepl` 1.0.5 -> 1.0.6
* `io.github.kit-clj/kit-postgres` 1.0.7 -> 1.0.8
* `io.github.kit-clj/kit-quartz` 1.0.4 -> 1.0.5
* `io.github.kit-clj/kit-redis` 1.0.6 -> 1.0.7
* `io.github.kit-clj/kit-repl` 1.0.3 -> 1.0.4
* `io.github.kit-clj/kit-selmer` 1.0.4 -> 1.0.5
* `io.github.kit-clj/kit-sql-conman` 1.10.5 -> 1.10.6
* `io.github.kit-clj/kit-sql-hikari` 1.0.6 -> 1.0.7
* `io.github.kit-clj/kit-sql-migratus` 1.0.5 -> 1.0.6
* `io.github.kit-clj/kit-xtdb` 1.0.4 -> 1.0.5
</details>

---

### `io.github.kit-clj/deps-template {:mvn/version "1.0.4"}`
<details>
<summary>version bumps</summary>

* `*failjure/failjure` 2.2.0 -> 2.3.0
* `ch.qos.logback/logback-classic` 1.4.4 -> 1.4.11
* `cider/cider-nrepl` 0.28.3 -> 0.35.0
* `com.lambdaisland/classpath` 0.0.27 -> 0.4.44
* `integrant/repl` 0.3.2 -> 0.3.3
* `io.github.clojure/tools.build` v0.8.0 -> v0.9.5
* `io.github.cognitect-labs/test-runner` v0.5.0 -> v0.5.1
* `luminus-transit/luminus-transit` 0.1.5 -> 0.1.6
* `nrepl/nrepl` 0.9.0 -> 1.0.0
* `org.clojure/tools.namespace` 1.2.0 -> 1.4.4
* `ring/ring-defaults` 0.3.3 -> 0.3.4
* `ring/ring-devel` 1.9.5 -> 1.10.0

* `integrant/integrant` 0.8.0 -> 0.8.1

<details>
<summary>kit-core</summary>

### `io.github.kit-clj/kit-core {:mvn/version "1.0.3"}`

- Change: Bump dependencies 

### `io.github.kit-clj/kit-core {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-core {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-hato</summary>

### `io.github.kit-clj/kit-hato {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-hato {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-metrics</summary>

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.1"}`

- Breaking fix: metric definitions API is broken

### `io.github.kit-clj/kit-metrics {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-quartz</summary>

### `io.github.kit-clj/kit-quartz {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-quartz {:mvn/version "1.0.1"}`

- bump dependencies

### `io.github.kit-clj/kit-quartz {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-redis</summary>

### `io.github.kit-clj/kit-redis {:mvn/version "1.0.4"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-redis {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-redis {:mvn/version "1.0.2"}`

- bump dependencies

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

### `io.github.kit-clj/kit-sql {:mvn/version "1.1.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql {:mvn/version "1.1.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql {:mvn/version "1.1.1"}`

- bump dependencies

### `io.github.kit-clj/kit-sql {:mvn/version "1.1.0"}`

- Change: Now just a bare bones wrapper that imports kit-sql-conman and kit-sql-migratus for compatibility purposes

### `io.github.kit-clj/kit-sql {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-sql-conman</summary>

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.6"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.4"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.2"}`

- bump dependencies

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.1"}`

- Fix: Remove unused reference to conman in require

### `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-sql-hikari</summary>

### `io.github.kit-clj/kit-sql-hikari {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-hikari {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-hikari {:mvn/version "1.0.1"}`

- Initial release

</details>



<details>
<summary>kit-sql-migratus</summary>

### `io.github.kit-clj/kit-sql-migratus {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-migratus {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-migratus {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-sql-migratus {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-postgres</summary>

### `io.github.kit-clj/kit-postgres {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-postgres {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-postgres {:mvn/version "1.0.1"}`

- bump dependencies

### `io.github.kit-clj/kit-postgres {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-mysql</summary>

### `io.github.kit-clj/kit-mysql {:mvn/version "1.0.3"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-mysql {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-mysql {:mvn/version "1.0.1"}`

- bump dependencies

### `io.github.kit-clj/kit-mysql {:mvn/version "1.0.0"}`

- Initial release [PR #18](https://github.com/kit-clj/kit/pull/18)
</details>



<details>
<summary>kit-undertow</summary>

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.4"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.2"}`

- Bump version

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.1"}`

- Fix: ensure org.clojure/tools.logging is included in deps in case used as standalone

### `io.github.kit-clj/kit-undertow {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-http-kit</summary>

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-http-kit {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-xtdb</summary>

### `io.github.kit-clj/kit-xtdb {:mvn/version "1.0.4"}`

- Bump dependencies

### `io.github.kit-clj/kit-xtdb {:mvn/version "1.0.3"}`

- Bump dependencies

### `io.github.kit-clj/kit-xtdb {:mvn/version "1.0.1"}`

- Bump dependencies

### `io.github.kit-clj/kit-xtdb {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>kit-selmer</summary>

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.2"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.1"}`

- Change: Bump dependencies

### `io.github.kit-clj/kit-selmer {:mvn/version "1.0.0"}`

- Initial release
</details>



<details>
<summary>lein-template</summary>

### `io.github.kit-clj/lein-template {:mvn/version "0.1.38"}`

- support deps-new [PR 72](https://github.com/kit-clj/kit/pull/72). Thanks [@rads](https://github.com/rads)
- bump dependencies

### `io.github.kit-clj/lein-template {:mvn/version "0.1.38"}`

- fix unused templating element [PR 86](https://github.com/kit-clj/kit/pull/86). Thanks [@dspearson](https://github.com/dspearson)
- improve CIDER REPL support [PR 84](https://github.com/kit-clj/kit/pull/84) [PR 85](https://github.com/kit-clj/kit/pull/85). Thanks [@dspearson](https://github.com/dspearson)

### `io.github.kit-clj/lein-template {:mvn/version "0.1.37"}`

- bump various dependencies

### `io.github.kit-clj/lein-template {:mvn/version "0.1.23"}`

- bump kit-undertow

### `io.github.kit-clj/lein-template {:mvn/version "0.1.22"}`

- bump kit-generator

### `io.github.kit-clj/lein-template {:mvn/version "0.1.21"}`

- bump dependencies

### `io.github.kit-clj/lein-template {:mvn/version "0.1.20"}`

- bump kit-xtdb

### `io.github.kit-clj/lein-template {:mvn/version "0.1.19"}`

- add rocksdb and xtdb http server for dev profile (thanks [@green-coder](https://github.com/green-coder))

### `io.github.kit-clj/lein-template {:mvn/version "0.1.18"}`

- fix cookie name in incorrect place (thanks [@jaimesangcap](https://github.com/jaimesangcap))

### `io.github.kit-clj/lein-template {:mvn/version "0.1.17"}`

- fix issue with default cookie session store configuration 
- bump to `org.clojure/clojure {:mvn/version "1.11.1"}`

### `io.github.kit-clj/lein-template {:mvn/version "0.1.16"}`

- bump to `io.github.kit-clj/kit-sql-conman {:mvn/version "1.0.1"}`

### `io.github.kit-clj/lein-template {:mvn/version "0.1.15"}`

- bump to `org.clojure/clojure {:mvn/version "1.11.0"}`
- bump to `metosin/reitit {:mvn/version "0.5.17"}`
- include deps hot loading by default

### `io.github.kit-clj/lein-template {:mvn/version "0.1.12"}`
- added `clojure.tools.namespace.rep/refresh` in `user` namespace
- added `bb.edn` script with tasks for starting nREPL, testing, and building an uberjar  
  
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

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.8"}`

- Change: bump dependencies

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.7"}`

- Change: bump dependencies

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.5"}`

- New: `feature-requires` support for modules to chain deep merge features 

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.4"}`

- bump dependencies

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.3"}`

- Fix issue with [git clone](https://github.com/kit-clj/kit/pull/30)

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.2"}`

- New: Snippet generation
- Change: Bump dependencies

### `io.github.kit-clj/kit-generator {:mvn/version "0.1.0"}`

- Initial ALPHA release, API subject to change
</details>


