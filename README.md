# kit

Lightweight, modular framework for scalable production systems.

## Goal

The goal of `kit` is to provide a template for a robust, scalable Clojure web application. It hides common plumbing that is standard across projects via its modules system, while exposing code that tends to be customized in the clj-new template.

Thanks to `integrant`, and `aero`, the modules are simple skeletons with the bulk of the customization being done in the system configuration EDN file.

## Quick Start

`clojure -T:build install-libs`

`clojure -X:new :template kit-clj :name yourname/app :args '[+selmer]'`

or

`lein new kit app +selmer`

### Profiles

Default modules included with no profile specified:

- `kit-core`
- `kit-undertow`
- `kit-repl`

Additional profiles:

- `+bare` - Only includes the `kit-core` and `kit-undertow` libraries as the foundation
- `+crux` - Adds the `kit-crux` module
- `+hato` - Adds the `kit-hato` module
- `+metrics` - Adds the `kit-metrics` module
- `+quartz` - Adds the `kit-quartz` module
- `+redis` - Adds the `kit-redis` module
- `+selmer` - Adds the `kit-selmer` module
- `+sql` - Adds the `kit-sql` and `kit-postgres` modules
- `+full` - Adds the modules `kit-crux`, `kit-hato`, `kit-metrics`, `kit-quartz`, `kit-redis`, `kit-selmer`, and `kit-sql`

## Modules

- `kit-core` - basic utility functions used by some other modules
- `kit-crux` - Simple binding to connect to a [crux](https://opencrux.com/) database node
- `kit-hato` - HTTP client using [hato](https://github.com/gnarroway/hato)
- `kit-metrics` - Configurable metrics using [iapetos](https://github.com/clj-commons/iapetos)
- `kit-quartz` - Scheduler using [cronut](https://github.com/troy-west/cronut) as an integrant binding for [quartz](http://www.quartz-scheduler.org/). Exposes the `cronut` API, simply some extensions for `aero` and utilities
- `kit-redis` - An extension of [core.cache](https://github.com/clojure/core.cache) for Redis via [carmine](https://github.com/ptaoussanis/carmine)
- `kit-repl` - Socket REPL integrant binding
- `kit-selmer` - Templating configuration with [selmer](https://github.com/yogthos/Selmer)
- `kit-sql` - Generic SQL integrant binding. Uses [conman](https://github.com/luminus-framework/conman), [next.jdbc](https://github.com/seancorfield/next-jdbc), [hugsql](https://www.hugsql.org/), and [migratus](https://github.com/yogthos/migratus) directly, or implicitly. By default imports `kit-postgres` module which supports Postgresql
- `kit-undertow` - Server binding via [luminus-undertow](https://github.com/luminus-framework/luminus-undertow)

## Documentation

TODO: link

## Inspiration and thanks to

- [integrant](https://github.com/weavejester/integrant) as the basis of the project
- [aero](https://github.com/juxt/aero) for powerful configuration used throughout
- [re-frame template](https://github.com/day8/re-frame-template) for code used directly in `kit-template`
- [Luminus framework](https://luminusweb.com/) from which the initial project that `kit`'s predecessor was built upon

## License

Copyright © 2021

Released under the MIT license.
