## tags
reitit routing routes

## description

generates a route definition

takes path and method as arguments

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
