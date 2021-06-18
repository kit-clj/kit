# wake

Lightweight, modular backend framework for scalable production systems.

## Goal

The goal of `wake` is to provide a template for a robust, scalable backend Clojure server. It hides common plumbing that is standard across projects via its modules system, while exposing code that tends to be customized in the leiningen template.

Thanks to `integrant`, and `aero`, the modules are simple skeletons with the bulk of the customization being done in the system configuration EDN file.

## Quick Start

`lein new wake myproject`

### Profiles

Default modules included with no profile specified:

- `wake-core`
- `wake-undertow`
- `wake-metrics`
- `wake-repl`

Additional profiles:

- `+bare` - Only includes the `wake-core` and `wake-undertow` libraries as the foundation
- `+crux` - Adds the `wake-crux` module
- `+hato` - Adds the `wake-hato` module
- `+oauth` - Adds the `wake-hato` (dependency) and `wake-oauth` module
- `+quartz` - Adds the `wake-quartz` module
- `+redis` - Adds the `wake-redis` module
- `+selmer` - Adds the `wake-selmer` module
- `+full` - Adds the modules `wake-crux`, `wake-hato`, `wake-oauth`, `wake-quartz`, `wake-redis`, `wake-selmer`, and `wake-sql`

## Modules

- `wake-core` - basic utility functions used by some other modules
- `wake-crux` - Simple binding to connect to a [crux](https://opencrux.com/) database node
- `wake-hato` - HTTP client using [hato](https://github.com/gnarroway/hato)
- `wake-metrics` - Configurable metrics using [iapetos](https://github.com/clj-commons/iapetos)
- `wake-oauth` - OAuth 2.0 integrant binding. Uses [hato](https://github.com/gnarroway/hato) as the HTTP client of choice
- `wake-quartz` - Scheduler using [cronut](https://github.com/troy-west/cronut) as an integrant binding for [quartz](http://www.quartz-scheduler.org/). Exposes the `cronut` API, simply some extensions for `aero` and utilities
- `wake-redis` - An extension of [core.cache](https://github.com/clojure/core.cache) for Redis via [carmine](https://github.com/ptaoussanis/carmine)
- `wake-repl` - Socket REPL integrant binding
- `wake-selmer` - Templating configuration with [selmer](https://github.com/yogthos/Selmer)
- `wake-sql` - Generic SQL integrant binding. Uses [conman](https://github.com/luminus-framework/conman), [next.jdbc](https://github.com/seancorfield/next-jdbc), [hugsql](https://www.hugsql.org/), and [migratus](https://github.com/yogthos/migratus) directly, or implicitly
- `wake-undertow` - Server binding via [luminus-undertow](https://github.com/luminus-framework/luminus-undertow)

## Documentation

TODO: link

## Inspiration and thanks to

- [integrant](https://github.com/weavejester/integrant) as the basis of the project
- [aero](https://github.com/juxt/aero) for powerful configuration used throughout
- [reitit](https://github.com/metosin/reitit) for inspiration on the `wake` code structure via [lein-parent](https://github.com/achin/lein-parent)
- [re-frame template](https://github.com/day8/re-frame-template) for code used directly in `wake-template`
- [Luminus framework](https://luminusweb.com/) from which the initial project that `wake`'s predecessor was built upon

Other libraries used are credited in their individual modules. 

## License

Copyright © 2021 Nikola Peric

Released under the MIT license.