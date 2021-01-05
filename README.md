# my-pocket-parser

A bundle of Clojure functions that authenticate you into your Pocket account and fetch all of your saved articles.

## Usage

- Clone the repo.
- Create a new application in Pocket in order to get a `consumer_key` in order to use the API: https://getpocket.com/developer/apps
- Open `resources/config.edn` and fill in the consumer key.
- Run `lein repl`. (This will install any dependencies you need.)
- In the repl, follow the steps to [authenticate](https://getpocket.com/developer/docs/authentication) yourself. These steps can be found in the docs at the botmy-pocket-parsertom of `core.clj`.