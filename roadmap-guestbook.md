
1. clojure -X:new :template kit :name kit/guestbook
2. add db sqlite module. Repl command to add SQL w/ SQLite. `(install-module :sql :sqlite)`
3. Writing migration etc. from Luminus guestbook tutorial
4. Run application
5. Add HTML module. Repl command `(install-module :html)` 
6. Write the HTML code for home page, save message. Show off `sample-code`
7. Package and run. Docker. Etc.
8. Switch to cljs, add cljs module. `(install-module :cljs)`
9. Maybe show off `generate-code`

## TODO: new generator stuff
(generate-code :cljs my-page) => creates the files (if no conflicts) after showing preview
(sample-code :reitit route) => shows you in the console sample shit

## Questions:
Services: share middleware between services and pages or not?