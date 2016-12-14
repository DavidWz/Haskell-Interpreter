len Nil = 0
len (Cons x xs) = (plus 1 (len xs))

genList 0 = Nil
genList x = (Cons x (genList (decrement x)))

push x xs = (Cons x xs)

append x Nil = (Cons x Nil)
append x (Cons y ys) = (Cons y (append x ys))

decrement x = (minus x 1)

fact 0 = 1
fact x = (times x (fact (decrement x)))