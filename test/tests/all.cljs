(ns tests.all
  (:require
   [cljs.test :as t :include-macros true]
   [cuerdas.core :as str]
   [bide.core :as b]))


(defn window-query-params
  []
  (let [search (aget js/window "location" "search")]
    (cond
      (empty? search)
      nil
      :else
      (-> search
          (str/trim "?")
          (str/split #"&")
          (as-> $ (map #(str/split % #"=") $)
            (map #(update-in % [0] str/keyword) $)
            (into {} $))))))


(t/deftest url-navigate-html5-query
  (let [test-routes [["/a" :route/a]
                     ["/b" :route/b]
                     ["/c" :route/c]
                     ["/d" :route/d]
                     ["/done" :route/done]]
        test-router (b/router test-routes)]
    (t/async done
             (b/start!
              test-router
              {:default :route/a
               :html5? true
               :on-navigate
               (fn [name params query]
                 (case name
                   
                   :route/a
                   (t/is (= query nil))
                   
                   :route/b
                   (do
                     (t/is (= query {:foo "bar"}))
                     (t/is (= query (window-query-params))))

                   :route/c
                   (do
                     (t/is (= query {:foo2 "bar2"}))
                     (t/is (= query (window-query-params))))
                   
                   :route/d
                   (do
                     (t/is (= query nil))
                     (t/is (= query (window-query-params))))

                   :route/done
                   (done)))})

             ;;
             ;; Trigger on-navigate calls
             ;;

             ;; Test
             (b/navigate! test-router :route/b nil {:foo "bar"})
             (b/navigate! test-router :route/c nil {:foo2 "bar2"})
             (b/navigate! test-router :route/d)

             ;; Finished
             (b/navigate! test-router :route/done))))
