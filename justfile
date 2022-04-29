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