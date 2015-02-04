(ns fi.ruuvitracker.database.user
  (:require [crypto.password.scrypt :as kdf]
            [clojure.tools.logging :refer [trace debug info warn] :as log]
            [slingshot.slingshot :refer [throw+ try+]]
            [clojure.java.jdbc :as jdbc]
            [fi.ruuvitracker.database.db-util
             :refer [insert! get-by-id get-row to-domain] :as db-util]
            ))

(defn- hash-password
  "Create password hash using scrypt algorithm"
  [password]
  (kdf/encrypt password))

(defn- password-matches?
  [input-password stored-password]
  (kdf/check input-password stored-password))

(defn- format-user [user]
  (-> user
      to-domain
      (dissoc :password_hash)))

(defn create-user!
  [conn user]
  (let [password (:password user)
        db-user (-> (select-keys user [:username :email :name])
                    (assoc :password_hash (hash-password password)))
        new-user (insert! conn :users db-user)]
    (format-user new-user)))

(defn get-user [conn id]
  (format-user (get-by-id conn :users id)))

(defn- get-user-full [conn username]
  (get-row conn :users ["username = ?" username]))

(defn get-user-by-username [conn username]
  (format-user get-user-full))

(defn- update-last-login! [conn user]
  (jdbc/execute! conn
                 ["update users set latest_login = current_timestamp, updated_at = current_timestamp where id = ?"
                  (:id user)]))

(defn login [conn username password]
  (let [user (get-user-full conn username)]
    (when-not user
      (throw+ {:login-failed :user-not-found}))
    (when-not (password-matches? password (:password_hash user))
      (throw+ {:login-failed :invalid-password}))
    (update-last-login! conn user)
    (format-user user)))
