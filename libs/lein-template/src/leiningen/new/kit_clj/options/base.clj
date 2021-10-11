(ns leiningen.new.kit-clj.options.base
  (:require
    [leiningen.new.kit-clj.options.helpers :as helpers]))

(defn files
  [data]
  [[".gitignore" (helpers/render "gitignore" data)]
   ["README.md" (helpers/render "README.md" data)]
   ["Dockerfile" (helpers/render "Dockerfile" data)]
   ["project.clj" (helpers/render "project.clj" data)]
   ["deps.edn" (helpers/render "deps.edn" data)]
   ["build.clj" (helpers/render "build.clj" data)]
   ["kit.edn" (helpers/render "kit.edn" data)]
   ["kit.git-config.edn" (helpers/render "kit.git-config.edn" data)]

   ["env/dev/clj/{{sanitized}}/dev_middleware.clj" (helpers/render "env/dev/clj/dev_middleware.clj" data)]
   ["env/dev/clj/{{sanitized}}/env.clj" (helpers/render "env/dev/clj/env.clj" data)]
   ["env/dev/clj/user.clj" (helpers/render "env/dev/clj/user.clj" data)]
   ["env/dev/resources/logback.xml" (helpers/render "env/dev/resources/logback.xml" data)]

   ["env/test/resources/logback.xml" (helpers/render "env/test/resources/logback.xml" data)]

   ["env/prod/clj/{{sanitized}}/env.clj" (helpers/render "env/prod/clj/env.clj" data)]
   ["env/prod/resources/logback.xml" (helpers/render "env/prod/resources/logback.xml" data)]

   ["resources/system.edn" (helpers/render "resources/system.edn" data)]

   ["src/clj/{{sanitized}}/config.clj" (helpers/render "src/clj/config.clj" data)]
   ["src/clj/{{sanitized}}/core.clj" (helpers/render "src/clj/core.clj" data)]
   ["src/clj/{{sanitized}}/web/handler.clj" (helpers/render "src/clj/web/handler.clj" data)]
   ["src/clj/{{sanitized}}/web/controllers/health.clj" (helpers/render "src/clj/web/controllers/health.clj" data)]
   ["src/clj/{{sanitized}}/web/middleware/core.clj" (helpers/render "src/clj/web/middleware/core.clj" data)]
   ["src/clj/{{sanitized}}/web/middleware/exception.clj" (helpers/render "src/clj/web/middleware/exception.clj" data)]
   ["src/clj/{{sanitized}}/web/middleware/formats.clj" (helpers/render "src/clj/web/middleware/formats.clj" data)]
   ["src/clj/{{sanitized}}/web/routes/api.clj" (helpers/render "src/clj/web/routes/api.clj" data)]
   ["src/clj/{{sanitized}}/web/routes/utils.clj" (helpers/render "src/clj/web/routes/utils.clj" data)]

   ["test/clj/{{sanitized}}/test_utils.clj" (helpers/render "test/clj/test_utils.clj" data)]])
