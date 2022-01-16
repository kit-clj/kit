# <<name>>

Start a [REPL](#repls) in your editor or terminal of choice.

Start the server with:

```clojure
(go)
```

The default API is found at localhost:3000/api

System configuration is found under `resources/system.edn`

Reload changes:

```clojure
(reset)
```

## REPLs

### Cursive

Configure a [REPL following the Cursive documentation](https://cursive-ide.com/userguide/repl.html). Using the default "Run with IntelliJ project classpath" will let you select your profiles from the ["Clojure deps" aliases selection](https://cursive-ide.com/userguide/deps.html#refreshing-deps-dependencies)

### CIDER

Use the profile `cider` for CIDER nREPL support. See the [CIDER docs](https://docs.cider.mx/cider/basics/up_and_running.html) for more help.

### Command Line

Run `clj -M:dev:nrepl` or `make repl`