# https://github.com/casey/just

# list all options when just is called with no arguments
default:
  @just --list

# builds and pushes to local maven repo
publish-local lib:
  clojure -T:build install-lib :artifact-id :{{lib}}

# builds and pushes a library to clojars
publish lib:
  clojure -T:build install-lib :artifact-id :{{lib}} :publish true

# builds all libs and pushes to local maven repo
publish-all-local:
  clojure -T:build install-libs :publish false

# runs antq to list outdated dependencies
antq:
  @clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core

# writes dependencies from bb.edn to each lib
sync-deps:
  bb build/kit/sync_lib_deps.clj
