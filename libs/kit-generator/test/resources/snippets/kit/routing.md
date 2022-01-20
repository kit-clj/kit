## tags
reitit routing routes

## description

generates a route definition
takes path and method as arguments

arguments are path string and method keyword

generates a route, e.g:
["/foo" {:get ...}]

## code

```clojure
  ["<<path>>"
   {<<method>>
    {:summary "TODO"
     :parameters {}
     :responses {200 {:body map?}}
     :handler
     (fn [request]
       {:body "<<ns>>"})}}]
```
