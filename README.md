# wake

Lightweight, modular framework for scalable production systems.

## Goal

The goal of `wake` is to provide a template for a robust, scalable Clojure web application. It hides common plumbing that is standard across projects via its modules system, while exposing code that tends to be customized in the clj-new template.

Thanks to `integrant`, and `aero`, the modules are simple skeletons with the bulk of the customization being done in the system configuration EDN file.

## Quick Start

`clojure -T:build install-libs`

`clojure -X:new :template wake :name yourname/app :args '[+selmer]'`

or

`lein new wake app +selmer`

### Profiles

Default modules included with no profile specified:

- `wake-core`
- `wake-undertow`
- `wake-repl`

Additional profiles:

- `+bare` - Only includes the `wake-core` and `wake-undertow` libraries as the foundation
- `+crux` - Adds the `wake-crux` module
- `+hato` - Adds the `wake-hato` module
- `+metrics` - Adds the `wake-metrics` module
- `+quartz` - Adds the `wake-quartz` module
- `+redis` - Adds the `wake-redis` module
- `+selmer` - Adds the `wake-selmer` module
- `+sql` - Adds the `wake-sql` and `wake-postgres` modules
- `+full` - Adds the modules `wake-crux`, `wake-hato`, `wake-metrics`, `wake-quartz`, `wake-redis`, `wake-selmer`, and `wake-sql`

## Modules

- `wake-core` - basic utility functions used by some other modules
- `wake-crux` - Simple binding to connect to a [crux](https://opencrux.com/) database node
- `wake-hato` - HTTP client using [hato](https://github.com/gnarroway/hato)
- `wake-metrics` - Configurable metrics using [iapetos](https://github.com/clj-commons/iapetos)
- `wake-quartz` - Scheduler using [cronut](https://github.com/troy-west/cronut) as an integrant binding for [quartz](http://www.quartz-scheduler.org/). Exposes the `cronut` API, simply some extensions for `aero` and utilities
- `wake-redis` - An extension of [core.cache](https://github.com/clojure/core.cache) for Redis via [carmine](https://github.com/ptaoussanis/carmine)
- `wake-repl` - Socket REPL integrant binding
- `wake-selmer` - Templating configuration with [selmer](https://github.com/yogthos/Selmer)
- `wake-sql` - Generic SQL integrant binding. Uses [conman](https://github.com/luminus-framework/conman), [next.jdbc](https://github.com/seancorfield/next-jdbc), [hugsql](https://www.hugsql.org/), and [migratus](https://github.com/yogthos/migratus) directly, or implicitly. By default imports `wake-postgres` module which supports Postgresql
- `wake-undertow` - Server binding via [luminus-undertow](https://github.com/luminus-framework/luminus-undertow)

## Documentation

TODO: link

## Inspiration and thanks to

- [integrant](https://github.com/weavejester/integrant) as the basis of the project
- [aero](https://github.com/juxt/aero) for powerful configuration used throughout
- [re-frame template](https://github.com/day8/re-frame-template) for code used directly in `wake-template`
- [Luminus framework](https://luminusweb.com/) from which the initial project that `wake`'s predecessor was built upon

## License

Copyright © 2021

Released under the MIT license.
