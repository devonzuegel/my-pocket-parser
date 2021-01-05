(ns my-pocket-parser.core
  (:gen-class)
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def config
  (when-let [data (slurp (io/reader (io/resource "config.edn")))]
    (edn/read-string data)))

(def redirect-url "http://google.com")

(defn fetch-code! []
  (-> "https://getpocket.com/v3/oauth/request"
      (http/post {:content-type :json
                  :body         (json/write-str {:consumer_key (:consumer-key config)
                                                 :redirect_uri redirect-url})})
      :body
      (str/replace "code=" "")))

(defn authorize! [code]
  (-> "https://getpocket.com/v3/oauth/authorize"
      (http/post {:content-type :json
                  :body         (json/write-str {:code code
                                                 :consumer_key (:consumer-key config)})})
      :body
      (str/split #"\&")
      first
      (str/replace "access_token=" "")))

(comment
  (format "https://getpocket.com/auth/authorize?request_token=%s&redirect_uri=%s"
          "2a63bfbc-ad4e-c764-4016-435fcb"
          "http://google.com"))

(defn get-pocket-data
  []
  (-> "https://getpocket.com/v3/get"
      (http/post {:body (json/write-str {:consumer_key (:consumer-key config)
                                         :access_token (:access-token config)
                                         :state "unread"
                                         ;; :count "2"
                                         :detailType "complete"})
                  :content-type :json})
      :body
      (json/read-str :key-fn keyword)))

;; Get a URL to authorize
(defn generate-url! []
  (let [code (fetch-code!)]
    {:code code
     :url (format "https://getpocket.com/auth/authorize?request_token=%s&redirect_uri=%s"
                  code
                  redirect-url)}))

(defn write-new-config! [access-token]
  (spit "resources/new-config.edn" (pr-str (assoc config :access-token access-token))))

;; ======================================================================
;; Docs for how to use these functions in the REPL:
;; ======================================================================

(comment
  (do
    ;; Generate the code (aka "request_token", as sometimes referenced in Pocket's docs).
    (def -out (generate-url!))
    (def -code (:code -out))
    (def -url (:url -out))
    ;; Go to the url and go through pocket's flow. (This step is manual.)
    (print "Now go to the url:" -url)
    -out)

  ;; After you do that, the following code block will authorize the code and
  ;; write it to `config.edn`.
  (let [access-token (authorize! -code)]
    (write-new-config! access-token)
    access-token)

  ;; Now, you can call the following to actually get your data.
  ;; Beware: If you have a lot of notes, printing the result can take ages!
  (def -your-pocket-data (get-pocket-data)))