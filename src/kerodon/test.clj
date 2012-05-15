(ns kerodon.test
  (:require [net.cgrand.enlive-html :as enlive]
            [kerodon.impl :as impl]))

(defn has
  ([state form]
     (has state form nil))
  ([state form msg]
     (form state msg)))

(defmacro validate [comparator generator expected exp-msg]
  `(fn [state# msg#]
     (try (let [value# (~generator state#)]
            (clojure.test/do-report {:actual value#
                                     :type (if (~comparator value# ~expected)
                                             :pass
                                             :fail)
                                     :message msg#
                                     :expected (quote ~exp-msg)}))
          (catch java.lang.Throwable t#
            (clojure.test/do-report {:actual t#
                                     :type :error
                                     :message msg#
                                     :expected (quote ~exp-msg)})))
     state#))

(defmacro text? [expected]
  `(validate =
             #(apply str (enlive/texts (:enlive %)))
             ~expected
             (~'text? ~expected)))

(defmacro status? [expected]
  `(validate =
            (comp :status :response)
            ~expected
            (~'status? ~expected)))

(defmacro value? [selector expected]
  `(validate =
             #(impl/get-value % ~selector)
             ~expected
             (~'value? ~selector ~expected)))

(defn missing? [state selector]
  (let [value (enlive/select (:enlive state) selector)]
    (clojure.test/do-report {:actual (count value)
                             :type (if (empty? value)
                                     :pass
                                     :fail)
                             :message "Should have matched zero elements"
                             :expected 0})
    state))