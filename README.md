# kit

Lightweight, modular framework for scalable production
systems.

**ALPHA WARNING**: `kit-generator` and Kit modules / code
generation is in **ALPHA** meaning the API for code
generation is subject to change. This should not affect
users of Kit, but will affect anyone developing modules or
extending them. The libs/libraries are stable.

## Goal

The goal of `kit` is to provide a template for a robust,
scalable Clojure web application. It hides common plumbing
that is standard across projects via its libs system, while
exposing code that tends to be customized in the clj-new
template.

Thanks to `integrant`, and `aero`, the libs are simple
skeletons with the bulk of the customization being done in
the system configuration EDN file.

## Quick Start

`clojure -X:new :template io.github.kit-clj :name yourname/app :args '[+selmer]'`

## Latest versions

| Library                         | Latest Version                                                                                                                                  |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| io.github.kit-clj/kit-core      | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-core.svg)](https://clojars.org/io.github.kit-clj/kit-core)           |
| io.github.kit-clj/kit-hato      | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-hato.svg)](https://clojars.org/io.github.kit-clj/kit-hato)           |
| io.github.kit-clj/kit-metrics   | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-metrics.svg)](https://clojars.org/io.github.kit-clj/kit-metrics)     |
| io.github.kit-clj/kit-quartz    | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-quartz.svg)](https://clojars.org/io.github.kit-clj/kit-quartz)       |
| io.github.kit-clj/kit-redis     | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-redis.svg)](https://clojars.org/io.github.kit-clj/kit-redis)         |
| io.github.kit-clj/kit-repl      | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-repl.svg)](https://clojars.org/io.github.kit-clj/kit-repl)           |
| io.github.kit-clj/kit-selmer    | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-selmer.svg)](https://clojars.org/io.github.kit-clj/kit-selmer)       |
| io.github.kit-clj/kit-sql       | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-sql.svg)](https://clojars.org/io.github.kit-clj/kit-sql)             |
| io.github.kit-clj/kit-postgres  | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-postgres.svg)](https://clojars.org/io.github.kit-clj/kit-postgres)   |
| io.github.kit-clj/kit-xtdb      | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-xtdb.svg)](https://clojars.org/io.github.kit-clj/kit-xtdb)           |
| io.github.kit-clj/kit-generator | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/kit-generator.svg)](https://clojars.org/io.github.kit-clj/kit-generator) |
| io.github.kit-clj/lein-template | [![Clojars Project](https://img.shields.io/clojars/v/io.github.kit-clj/lein-template.svg)](https://clojars.org/io.github.kit-clj/lein-template) |

### Profiles

Default libs included with no profile specified:

- `kit-core`
- `kit-undertow`
- `kit-repl`

Additional profiles:

- `+bare` - Only includes the `kit-core` and `kit-undertow`
  libraries as the foundation
- `+xtdb` - Adds the `kit-xtdb` lib
- `+hato` - Adds the `kit-hato` lib
- `+metrics` - Adds the `kit-metrics` lib
- `+quartz` - Adds the `kit-quartz` lib
- `+redis` - Adds the `kit-redis` lib
- `+selmer` - Adds the `kit-selmer` lib
- `+sql` - Adds the `kit-sql` and `kit-postgres` libs
- `+full` - Adds the libs `kit-xtdb`, `kit-hato`
  , `kit-metrics`, `kit-quartz`, `kit-redis`, `kit-selmer`,
  and `kit-sql`

## Libs

- `kit-core` - basic utility functions used by some other
  libs
- `kit-xtdb` - Simple binding to connect to
  a [XTDB](https://xtdb.com/) database node
- `kit-hato` - HTTP client
  using [hato](https://github.com/gnarroway/hato)
- `kit-metrics` - Configurable metrics
  using [iapetos](https://github.com/clj-commons/iapetos)
- `kit-quartz` - Scheduler
  using [cronut](https://github.com/troy-west/cronut) as an
  integrant binding
  for [quartz](http://www.quartz-scheduler.org/). Exposes
  the `cronut` API, simply some extensions for `aero` and
  utilities
- `kit-redis` - An extension
  of [core.cache](https://github.com/clojure/core.cache) for
  Redis
  via [carmine](https://github.com/ptaoussanis/carmine)
- `kit-repl` - Socket REPL integrant binding
- `kit-selmer` - Templating configuration
  with [selmer](https://github.com/yogthos/Selmer)
- `kit-sql` - Generic SQL integrant binding.
  Uses [conman](https://github.com/luminus-framework/conman)
  , [next.jdbc](https://github.com/seancorfield/next-jdbc)
  , [hugsql](https://www.hugsql.org/),
  and [migratus](https://github.com/yogthos/migratus)
  directly, or implicitly. By default,
  imports `kit-postgres` lib which supports Postgresql
- `kit-postgres` - lib with data bindings and utilities for
  working with Postgres
- `kit-undertow` - Server binding
  via [ring-undertow-adapter](https://github.com/luminus-framework/ring-undertow-adapter)

## Build Tool Support

Presently only Clojure deps is supported, however there are
plans to add Leiningen support.

## Documentation

[Documentation can be found here](https://kit-clj.github.io)

## Development setup

Minimum Clojure CLI version: 1.10.3.933

To install the libraries locally

`clojure -T:build install-libs`

To install a single library locally

`clojure -T:build install-lib :artifact-id :lein-template`

To push a single library to Clojars

`clojure -T:build install-lib :artifact-id :lein-template :publish true`

## Inspiration and thanks to

- [integrant](https://github.com/weavejester/integrant) as
  the basis of the project
- [aero](https://github.com/juxt/aero) for powerful
  configuration used throughout
- [re-frame template](https://github.com/day8/re-frame-template)
  for code used directly in `kit-template`
- [Luminus framework](https://luminusweb.com/) from which
  the initial project that `kit`'s predecessor was built
  upon

## License

Copyright © 2021

Released under the MIT license.
