(ns sqlkorma.views.docs
  (:require [sqlkorma.views.common :as common]
            [noir.options :as options]
            [sqlkorma.models.examples :as example])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]
        [sqlkorma.views.common :only [code]]
        hiccup.page-helpers))

;;************************************************
;; section util
;;************************************************

(defpartial doc-bubble [{:keys [name id rows content]}]
  [:div#docBubble
    [:h2 {:id id} name]
    content])

(def _sections (atom []))

(defn add-section [id v]
  (swap! _sections conj [id v]))

(defn sections []
  (for [[_ s] @_sections]
    (s)))

(defn section-links []
  (for [[id] @_sections]
    [:li (link-to (str "#" id) id)]))

(defmacro section [id title & content]
  `(do
     (defn ~id []
       (doc-bubble {:id ~(name id)
                    :name ~title
                    :content (html ~@content)}))
     (add-section ~(name id) ~id)))

;;************************************************
;; sections
;;************************************************

(section start "Getting started"
  (code :gettingstarted :span))

(section db "db"
  (code :db-intro :full)
  [:p "To define a connection to a database you use the (" [:em "defdb"] ") macro, which takes a map
      of database parameters. Those familiar with clojure.java.jdbc will notice this is the
      same connection map you would use there. To speed things up a bit, Korma provides a set of 
      convenience functions for common database specs."]
  [:p "(" [:em "defdb"] ") creates a connection pool for your db spec (using the
      wonderful c3p0 library) to make sure resources are used efficiently. It also sets the last
      created pool as the default for all queries. As such, connection management is essentially 
      taken care of for you."])

(section entities "entities"
  (code :entities :full)
  [:p "Entities map one to one with tables and are the initial building block for all your
      queries. You can specify a number of properties associated with them,
      such as the table name, an alias for the table, the primary key and so on. You can
      also set functions to be run before a record goes to the database as the result of
      an insert/update, or functions to be mapped over the results of a select. This allows you
      to apply common mutations to your data without having to think about it at every step."]
  [:p "Lastly, entities let you specify the relationships to be used when you do select queries.
      With these relationships defined, you can then simply use the (" [:em "with"] ") function in
      your select query to join the entities and return the results."]
  [:p "Entities offer a great deal of flexibility. The final example shown here demonstrates how
      you can even use subselects as entities, enabling you to join on these seamlessly within your
      normal queries."])

(section select "select queries"
  [:div
    (code :selects :full)
    [:p "Select queries are the most interesting of the query types and have a number of tools
        to help make them simple. As discussed in the entities section, you can use the (" [:em "with"] 
        ") function to include a relation. If you do so, you'll also want to specify the exact fields
        to be returned in the query using the (" [:em "fields"] ") function, which takes a variable
        number of keywords representing the field names you want. Likewise, you can use the (" [:em "aggregate"]
        ") function to call one of SQL's aggregators by specifying the function, an alias, and optionally
        a field to group by. One thing to note in all of this is that fields are always keywords and
        any without a table prefix are assumed to be for the current entity unless they're aliased."]
   [:p "We'll go more in depth about where clauses below, but as you can see, you have access to all
       the other parts of a select that you'd expect: subselects, joins, grouping, ordering, limits, and offsets. 
       Subselects work just like a select clause does, but they can be embedded anywhere in your query. Joins allow
       you to manually control how related tables a brought together by taking a standard where predicate. Group 
       and order clauses will be evaluated in the order they're added to the query. The default ordering
       direction is ASC."]]
  [:div
    (code :where :full)
    [:p "Where clauses are sort of their own mini-DSL. Anywhere you would provide a clause,
        you can use a map where each key in the map represents a field and each value is its
        value. Just like with the fields function, keys specified without a table prefix will
        be prefixed for the current entity. Also, a field's value can be a vector specifying
        a different comparison function to be used. Each clause that results from a map will
        be AND'd together."]
   [:p "You can also call the where predicates like any normal function, allowing you to compose
       your predicate clauses as if they were standard Clojure code. Fields in Korma are always
       specified as keywords and will be prefixed appropriately."]]
  [:div
    (code :with :full)
   [:p "With clauses act almost like selects, in that they can actually be further refined
       using all the standard functions you would use in a select. This allows for a great
       deal of flexibility when describing your relations."
    ]])

(section update "update queries"
         (code :update :full)
  [:p "Update queries use the (" [:em "set-fields"] ") function to specifiy the fields to be
      updated. Multiple calls to set-fields will be merged together, allowing you to build
      the update over time. Updates also allow where clauses as you would expect."])

(section insert "insert queries"
         (code :insert :full)
  [:p "Insert queries use the function (" [:em "values"] ") to add records. It takes either
      a single map or a collection of maps and returns the id of the first inserted record."])

(section delete "delete queries"
         (code :delete :full)
  [:p "Delete queries only allow where clauses and not including one will cause all
      records for the given entity to be deleted. The result of a delete is the 
      id of the first record deleted."])

(section misc "misc"
         [:div
          (code :execmodes :full)
          [:p "By default, when exec is called, it simply generates the SQL string and params
              necessary for your query and sends those to your database. Sometimes, however, what
              you really want is to just generate the string, or even do a dry run where you see
              the SQL printed to the console. As such, korma includes a couple of other execution
              modes that allow you to do exactly that. These also apply to queries that are composed
              over time and then executed using the (" [:em "exec"] ") function."]]
         [:div
          (code :transactions :full)
          [:p "You can do transactions in Korma simply by using the (" [:em "transaction"] " ..) macro,
              which ensures that all queries executed within it are part of a single transaction. You
              can then use the (" [:em "rollback"] ") function to force the transaction to rollback if necessary."]]
         [:div
          (code :raw :full)
          [:p "You may find yourself in need of functionality that Korma doesn't have. Luckily, there
              are a couple of facilities that help you here: (" [:em "raw"] ") which injects a string into
              a query and (" [:em "exec-raw"] ") which allows you to write a parameterized query directly."]
          ])

;;************************************************
;; pages
;;************************************************

(defpage "/docs" []
  (when (options/dev-mode?)
    (example/init))
  (common/layout
    [:ul#docLinks
     (section-links)
     [:li#api (link-to "/api/0.3.0/index.html" "API")]]
    (sections)))


